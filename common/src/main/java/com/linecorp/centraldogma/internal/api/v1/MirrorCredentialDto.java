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

package com.linecorp.centraldogma.internal.api.v1;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class MirrorCredentialDto {

    public static MirrorCredentialDto ofPassword(int index, @Nullable String id, Set<String> hostnamePatterns,
                                                 String username,
                                                 String password) {
        return new MirrorCredentialDto(index, id, "password", hostnamePatterns, username, password, null, null, null,
                                       null);
    }

    public static MirrorCredentialDto ofAccessToken(int index, @Nullable String id, Set<String> hostnamePatterns,
                                                    String accessToken) {
        return new MirrorCredentialDto(index, id, "access_token", hostnamePatterns, null, null, accessToken, null,
                                       null,
                                       null);
    }

    public static MirrorCredentialDto ofPublicKey(int index, @Nullable String id, Set<String> hostnamePatterns,
                                                  String username, String publicKey, String privateKey,
                                                  @Nullable String passphrase) {
        return new MirrorCredentialDto(index, id, "public_key", hostnamePatterns, username, null, null,
                                       publicKey, privateKey, passphrase);
    }

    public static MirrorCredentialDto ofNone(int index, @Nullable String id, Set<String> hostnamePatterns) {
        return new MirrorCredentialDto(index, id, "none", hostnamePatterns, null, null, null, null, null, null);
    }

    /**
     * The index of this credential in the array at {@code credential.json}.
     */
    private final int index;

    /**
     * Common fields.
     */
    @Nullable
    private final String id;
    private final String type;
    private final Set<String> hostnamePatterns;

    /**
     * Password-based authentication.
     */
    @Nullable
    private final String username;
    @Nullable
    private final String password;

    /**
     * Access token-based authentication.
     */
    @Nullable
    private final String accessToken;

    /**
     * SSH key authentication.
     */
    @Nullable
    private final String publicKey;
    @Nullable
    private final String privateKey;
    @Nullable
    private final String passphrase;

    private MirrorCredentialDto(int index, @Nullable String id, String type, Set<String> hostnamePatterns,
                                @Nullable String username, @Nullable String password,
                                @Nullable String accessToken, @Nullable String publicKey,
                                @Nullable String privateKey, @Nullable String passphrase) {
        this.index = index;
        this.id = id;
        this.type = type;
        this.hostnamePatterns = hostnamePatterns;
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
    }

    @JsonProperty("index")
    public int index() {
        return index;
    }

    @JsonProperty("id")
    @Nullable
    public String id() {
        return id;
    }

    @JsonProperty("type")
    public String type() {
        return type;
    }

    @JsonProperty("hostnamePatterns")
    public Set<String> hostnamePatterns() {
        return hostnamePatterns;
    }

    @JsonProperty("username")
    @Nullable
    public String username() {
        return username;
    }

    @JsonProperty("password")
    @Nullable
    public String password() {
        return password;
    }

    @JsonProperty("accessToken")
    @Nullable
    public String accessToken() {
        return accessToken;
    }

    @JsonProperty("publicKey")
    @Nullable
    public String publicKey() {
        return publicKey;
    }

    @JsonProperty("privateKey")
    @Nullable
    public String privateKey() {
        return privateKey;
    }

    @JsonProperty("passphrase")
    @Nullable
    public String passphrase() {
        return passphrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MirrorCredentialDto)) {
            return false;
        }

        final MirrorCredentialDto that = (MirrorCredentialDto) o;
        return index == that.index &&
               Objects.equals(id, that.id) &&
               type.equals(that.type) &&
               hostnamePatterns.equals(that.hostnamePatterns) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password) &&
               Objects.equals(accessToken, that.accessToken) &&
               Objects.equals(publicKey, that.publicKey) &&
               Objects.equals(privateKey, that.privateKey) &&
               Objects.equals(passphrase, that.passphrase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, id, type, hostnamePatterns, username, password, accessToken,
                            publicKey, privateKey, passphrase);
    }

    @Override
    public String toString() {
        final ToStringHelper builder = MoreObjects.toStringHelper(this)
                                                  .omitNullValues()
                                                  .add("index", index)
                                                  .add("id", id)
                                                  .add("type", type)
                                                  .add("hostnamePatterns", hostnamePatterns)
                                                  .add("username", username);
        if (password != null) {
            builder.add("password", "...");
        }
        if (accessToken != null) {
            builder.add("accessToken", "...");
        }
        if (publicKey != null) {
            builder.add("publicKey", publicKey);
        }
        if (privateKey != null) {
            builder.add("privateKey", "...");
        }
        if (passphrase != null) {
            builder.add("passphrase", "...");
        }
        return builder.toString();
    }
}
