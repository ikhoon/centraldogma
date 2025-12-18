/*
 * Copyright 2025 LY Corporation
 *
 * LY Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.centraldogma.server.internal.api.variable;

import static com.linecorp.centraldogma.server.internal.api.variable.VariableServiceV1.crudContext;
import static org.apache.curator.shaded.com.google.common.collect.ImmutableList.toImmutableList;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.spotify.futures.CompletableFutures;

import com.linecorp.centraldogma.common.Entry;
import com.linecorp.centraldogma.common.TemplateProcessingException;
import com.linecorp.centraldogma.internal.Jackson;
import com.linecorp.centraldogma.server.command.CommandExecutor;
import com.linecorp.centraldogma.server.internal.storage.repository.HasRevision;
import com.linecorp.centraldogma.server.internal.storage.repository.git.CrudOperation;
import com.linecorp.centraldogma.server.internal.storage.repository.git.GitCrudOperation;
import com.linecorp.centraldogma.server.storage.project.ProjectManager;
import com.linecorp.centraldogma.server.storage.repository.Repository;

import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;

public final class Templater {

    private final CrudOperation<Variable> crudRepo;
    private final LoadingCache<Entry<?>, Template> cache;

    public Templater(CommandExecutor executor, ProjectManager pm) {
        crudRepo = new GitCrudOperation<>(Variable.class, executor, pm);
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setClassicCompatible(false);

        cfg.setAPIBuiltinEnabled(false);
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);

        cache = Caffeine.newBuilder()
                        .expireAfterAccess(Duration.ofHours(1))
                        .maximumSize(4096)
                        .build(key -> {
                            assert key.rawContent() != null;
                            return new Template(key.path(), key.rawContent(), cfg);
                        });
    }

    public CompletableFuture<Collection<Entry<?>>> render(Repository repo, Collection<Entry<?>> entries) {
        //noinspection unchecked
        return (CompletableFuture<Collection<Entry<?>>>) render0(repo, entries);
    }

    public CompletableFuture<? extends Collection<Entry<?>>> render0(Repository repo,
                                                                     Collection<Entry<?>> entries) {
        final List<CompletableFuture<Entry<?>>> futures =
                entries.stream().map(entry -> render(repo, entry))
                       .collect(toImmutableList());
        return CompletableFutures.allAsList(futures);
    }

    public CompletableFuture<Entry<?>> render(Repository repo, Entry<?> entry) {
        if (!entry.hasContent()) {
            return CompletableFuture.completedFuture(entry);
        }

        final String projectName = repo.parent().name();
        // TODO(ikhoon): Optimize by caching the rendering result for the same set of variables and template.
        return collectVariables(crudRepo.findAll(crudContext(projectName)),
                                crudRepo.findAll(crudContext(projectName, repo.name())))
                .thenApply(variables -> process(entry, variables));
    }

    private Entry<?> process(Entry<?> entry, Map<String, Object> variables) {
        final StringWriter out = new StringWriter();
        final Template template = cache.get(entry);
        try {
            template.process(variables, out);
            return newEntryWithContent(entry, out.toString());
        } catch (Exception e) {
            throw new TemplateProcessingException("Failed to process the template: " + entry.path(), e);
        }
    }

    private static Entry<?> newEntryWithContent(Entry<?> entry, String content) {
        switch (entry.type()) {
            case TEXT:
                return Entry.ofText(entry.revision(), entry.path(), content);
            case JSON:
                try {
                    return Entry.ofJson(entry.revision(), entry.path(), content);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
                // TODO(ikhoon): Support YAML type.
            case DIRECTORY:
            default:
                // Should not reach here.
                throw new Error();
        }
    }

    private static CompletableFuture<Map<String, Object>> collectVariables(
            CompletableFuture<List<HasRevision<Variable>>> projFuture,
            CompletableFuture<List<HasRevision<Variable>>> repoFuture) {
        return projFuture.thenCombine(repoFuture, (projVars, repoVars) -> {
            final ImmutableMap.Builder<String, Object> builder =
                    ImmutableMap.builderWithExpectedSize(projVars.size() + repoVars.size());
            for (HasRevision<Variable> it : projVars) {
                final Variable variable = it.object();
                assert variable.name() != null;
                builder.put(variable.name(), parseValue(variable));
            }
            for (HasRevision<Variable> it : repoVars) {
                final Variable variable = it.object();
                assert variable.name() != null;
                // Repo-level variables override project-level ones.
                builder.put(variable.name(), parseValue(variable));
            }
            final Map<String, Object> variables = builder.buildKeepingLast();
            // Prefix variables map with "vars" key.
            // This allows using "vars.varName" in the template.
            // TODO(ikhoon): Support secret variables that will be prefixed with "secrets" key.
            return ImmutableMap.of("vars", variables);
        });
    }

    private static Object parseValue(Variable variable) {
        switch (variable.type()) {
            case JSON:
                try {
                    return Jackson.readValue(variable.value(), Object.class);
                } catch (JsonProcessingException e) {
                    // Should not reach here as the value has been validated before storing.
                    throw new IllegalStateException(e);
                }
            case STRING:
                return variable.value();
            default:
                // Should not reach here.
                throw new Error();
        }
    }
}
