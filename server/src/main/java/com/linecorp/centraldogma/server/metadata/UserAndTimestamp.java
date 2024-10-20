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

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import com.linecorp.centraldogma.common.Author;

/**
 * Specifies when an object is accessed by whom.
 */
public final class UserAndTimestamp {

    /**
     * Creates a new instance using the specified {@link Author}'s e-mail address as a user name and
     * the current time as a timestamp.
     */
    public static UserAndTimestamp of(Author author) {
        return new UserAndTimestamp(requireNonNull(author, "author").email());
    }

    private final String user;
    private final Instant timestamp;
    @Nullable
    private String timestampAsText;

    /**
     * Creates a new instance using the current time as a timestamp.
     */
    public UserAndTimestamp(String user) {
        this(user, Instant.now());
    }

    /**
     * Creates a new instance.
     */
    public UserAndTimestamp(String user, Instant timestamp) {
        this.user = requireNonNull(user, "user");
        this.timestamp = requireNonNull(timestamp, "timestamp");
    }

    @JsonCreator
    UserAndTimestamp(@JsonProperty("user") String user,
                     @JsonProperty("timestamp") String timestampAsText) {
        this.user = requireNonNull(user, "user");
        this.timestampAsText = requireNonNull(timestampAsText, "timestampAsText");
        timestamp = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestampAsText));
    }

    /**
     * Returns a {@code login} name who took any action on this object.
     */
    @JsonProperty
    public String user() {
        return user;
    }

    /**
     * Returns a date and time string which is formatted as ISO-8601.
     */
    @JsonProperty
    public String timestamp() {
        if (timestampAsText == null) {
            timestampAsText = DateTimeFormatter.ISO_INSTANT.format(timestamp);
        }
        return timestampAsText;
    }

    /**
     * Returns the epoch milliseconds of the {@link #timestamp()}.
     */
    public long timestampMillis() {
        return timestamp.toEpochMilli();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserAndTimestamp that = (UserAndTimestamp) o;
        return user.equals(that.user) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return user.hashCode() * 31 + timestamp.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("user", user())
                          .add("timestamp", timestamp())
                          .toString();
    }
}
