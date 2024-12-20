/*
 * Copyright 2023 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
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

package com.linecorp.centraldogma.server.internal.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.linecorp.armeria.server.annotation.ConsumesJson;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import com.linecorp.armeria.server.annotation.StatusCode;
import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.internal.api.v1.PushResultDto;
import com.linecorp.centraldogma.server.command.CommandExecutor;
import com.linecorp.centraldogma.server.credential.Credential;
import com.linecorp.centraldogma.server.internal.api.auth.RequiresReadPermission;
import com.linecorp.centraldogma.server.internal.api.auth.RequiresWritePermission;
import com.linecorp.centraldogma.server.internal.storage.project.ProjectApiManager;
import com.linecorp.centraldogma.server.metadata.User;
import com.linecorp.centraldogma.server.storage.project.Project;
import com.linecorp.centraldogma.server.storage.repository.MetaRepository;

/**
 * Annotated service object for managing credential service.
 */
@ProducesJson
public class CredentialServiceV1 extends AbstractService {

    private final ProjectApiManager projectApiManager;

    public CredentialServiceV1(ProjectApiManager projectApiManager, CommandExecutor executor) {
        super(executor);
        this.projectApiManager = projectApiManager;
    }

    /**
     * GET /projects/{projectName}/credentials
     *
     * <p>Returns the list of the credentials in the project.
     */
    @RequiresReadPermission(repository = Project.REPO_META)
    @Get("/projects/{projectName}/credentials")
    public CompletableFuture<List<Credential>> listCredentials(User loginUser,
                                                               @Param String projectName) {
        final CompletableFuture<List<Credential>> future = metaRepo(projectName, loginUser).credentials();
        if (loginUser.isAdmin()) {
            return future;
        }
        return future.thenApply(credentials -> {
            return credentials
                    .stream()
                    .map(Credential::withoutSecret)
                    .collect(toImmutableList());
        });
    }

    /**
     * GET /projects/{projectName}/credentials/{id}
     *
     * <p>Returns the credential for the ID in the project.
     */
    @RequiresReadPermission(repository = Project.REPO_META)
    @Get("/projects/{projectName}/credentials/{id}")
    public CompletableFuture<Credential> getCredentialById(User loginUser,
                                                           @Param String projectName, @Param String id) {
        final CompletableFuture<Credential> future = metaRepo(projectName, loginUser).credential(id);
        if (loginUser.isAdmin()) {
            return future;
        }
        return future.thenApply(Credential::withoutSecret);
    }

    /**
     * POST /projects/{projectName}/credentials
     *
     * <p>Creates a new credential.
     */
    @RequiresWritePermission(repository = Project.REPO_META)
    @Post("/projects/{projectName}/credentials")
    @ConsumesJson
    @StatusCode(201)
    public CompletableFuture<PushResultDto> createCredential(@Param String projectName,
                                                             Credential credential, Author author, User user) {
        return createOrUpdate(projectName, credential, author, user, false);
    }

    /**
     * PUT /projects/{projectName}/credentials/{id}
     *
     * <p>Update the existing credential.
     */
    @RequiresWritePermission(repository = Project.REPO_META)
    @Put("/projects/{projectName}/credentials/{id}")
    @ConsumesJson
    public CompletableFuture<PushResultDto> updateCredential(@Param String projectName, @Param String id,
                                                             Credential credential, Author author, User user) {
        checkArgument(id.equals(credential.id()), "The credential ID (%s) can't be updated", id);
        return createOrUpdate(projectName, credential, author, user, true);
    }

    private CompletableFuture<PushResultDto> createOrUpdate(String projectName, Credential credential,
                                                            Author author, User user, boolean update) {
        return metaRepo(projectName, user).createPushCommand(credential, author, update).thenCompose(
                command -> {
                    return executor().execute(command).thenApply(result -> {
                        return new PushResultDto(result.revision(), command.timestamp());
                    });
                });
    }

    private MetaRepository metaRepo(String projectName, User user) {
        return projectApiManager.getProject(projectName, user).metaRepo();
    }
}
