/*
 * Copyright 2019 LINE Corporation
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

package com.linecorp.centraldogma.server.metadata;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.common.ProjectRole;
import com.linecorp.centraldogma.internal.Util;
import com.linecorp.centraldogma.server.storage.project.Project;
import com.linecorp.centraldogma.server.storage.repository.HasWeight;

/**
 * Specifies details of a member who belongs to the {@link Project}.
 */
public class Member implements Identifiable, HasWeight {

    /**
     * A login name of a member.
     */
    private final String login;

    /**
     * A role of a member in a project.
     */
    private final ProjectRole role;

    /**
     * Specifies when this member is added to the project by whom.
     */
    private final UserAndTimestamp creation;

    /**
     * Creates a new instance.
     */
    public Member(User user, ProjectRole role, UserAndTimestamp creation) {
        this(requireNonNull(user, "user").id(), role, creation);
    }

    /**
     * Creates a new instance.
     */
    public Member(Author author, ProjectRole role, UserAndTimestamp creation) {
        this(requireNonNull(author, "author").email(), role, creation);
    }

    /**
     * Creates a new instance.
     */
    @JsonCreator
    public Member(@JsonProperty("login") String login,
                  @JsonProperty("role") ProjectRole role,
                  @JsonProperty("creation") UserAndTimestamp creation) {
        this.login = Util.toEmailAddress(login, "login");
        this.role = requireNonNull(role, "role");
        this.creation = requireNonNull(creation, "creation");
    }

    @Override
    public String id() {
        return login;
    }

    /**
     * Returns the login name.
     */
    @JsonProperty
    public String login() {
        return login;
    }

    /**
     * Returns the role in a certain project.
     */
    @JsonProperty
    public ProjectRole role() {
        return role;
    }

    @Override
    public int weight() {
        return login.length() + role.name().length();
    }

    /**
     * Returns who added this member to a project when.
     */
    @JsonProperty
    public UserAndTimestamp creation() {
        return creation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("login", login())
                          .add("role", role())
                          .add("creation", creation())
                          .toString();
    }
}
