/*
 * Copyright 2017 LINE Corporation
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

package com.linecorp.centraldogma.server.internal.storage.repository;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.common.CentralDogmaException;
import com.linecorp.centraldogma.common.RepositoryNotFoundException;
import com.linecorp.centraldogma.internal.Util;
import com.linecorp.centraldogma.server.storage.project.Project;
import com.linecorp.centraldogma.server.storage.repository.Repository;
import com.linecorp.centraldogma.server.storage.repository.RepositoryManager;

public class RepositoryManagerWrapper implements RepositoryManager {
    private final RepositoryManager delegate;
    private final Function<Repository, Repository> repoWrapper;
    private final ConcurrentMap<String, Repository> repos = new ConcurrentHashMap<>();

    public RepositoryManagerWrapper(RepositoryManager repoManager,
                                    Function<Repository, Repository> repoWrapper) {

        delegate = requireNonNull(repoManager, "repoManager");
        this.repoWrapper = requireNonNull(repoWrapper, "repoWrapper");
        for (Map.Entry<String, Repository> entry : delegate.list().entrySet()) {
            repos.computeIfAbsent(entry.getKey(), n -> repoWrapper.apply(entry.getValue()));
        }
    }

    @Override
    public Project parent() {
        return delegate.parent();
    }

    @Override
    public void close(Supplier<CentralDogmaException> failureCauseSupplier) {
        delegate.close(failureCauseSupplier);
    }

    @Override
    public boolean exists(String name) {
        return delegate.exists(name);
    }

    @Override
    public Repository get(String name) {
        ensureOpen();
        final Repository r = repos.get(name);
        if (r == null) {
            throw new RepositoryNotFoundException(name);
        }
        return r;
    }

    @Override
    public Repository create(String name, long creationTimeMillis, Author author, boolean encrypt) {
        return repos.compute(
                name, (n, v) -> repoWrapper.apply(delegate.create(name, creationTimeMillis, author, encrypt)));
    }

    @Override
    public Map<String, Repository> list() {
        ensureOpen();
        final int estimatedSize = repos.size();
        final String[] names = repos.keySet().toArray(new String[estimatedSize]);
        Arrays.sort(names);

        final Map<String, Repository> ret = new LinkedHashMap<>(names.length);
        for (String name : names) {
            final Repository repo = repos.get(name);
            if (repo != null) {
                ret.put(name, repo);
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public Map<String, Instant> listRemoved() {
        return delegate.listRemoved();
    }

    @Override
    public void remove(String name) {
        repos.compute(name, (n, v) -> {
            delegate.remove(n);
            return null;
        });
    }

    @Override
    public Repository unremove(String name) {
        ensureOpen();
        return repos.computeIfAbsent(name, n -> repoWrapper.apply(delegate.unremove(n)));
    }

    @Override
    public void purgeMarked() {
        delegate.purgeMarked();
    }

    @Override
    public void markForPurge(String name) {
        repos.compute(name, (n, v) -> {
            delegate.markForPurge(name);
            return null;
        });
    }

    @Override
    public void ensureOpen() {
        delegate.ensureOpen();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(Util.simpleTypeName(getClass()) + '{');
        sb.append("repositoryManager=");
        sb.append(delegate);
        sb.append(", repos={");
        final Iterator<Map.Entry<String, Repository>> iterator = repos.entrySet().iterator();
        Map.Entry<String, Repository> entry;
        if (iterator.hasNext()) {
            for (;;) {
                entry = iterator.next();
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(Util.simpleTypeName(entry.getValue()));
                if (!iterator.hasNext()) {
                    break;
                }
                sb.append(", ");
            }
        }
        sb.append("}}");
        return sb.toString();
    }
}
