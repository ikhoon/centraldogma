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

package com.linecorp.centraldogma.server.internal.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects.ToStringHelper;

import com.linecorp.centraldogma.internal.Jackson;
import com.linecorp.centraldogma.server.credential.Credential;
import com.linecorp.centraldogma.server.credential.CredentialType;

public final class JsonCredential extends AbstractCredential {

    private final JsonNode json;

    @JsonCreator
    public JsonCredential(@JsonProperty("name") String name, @JsonProperty("json") JsonNode json) {
        super(name, CredentialType.JSON);
        this.json = json;
    }

    @JsonProperty("json")
    public JsonNode json() {
        return json;
    }

    @Override
    void addProperties(ToStringHelper helper) {
        // Json is a secret, so do not print it.
    }

    @Override
    public Credential withoutSecret() {
        return new JsonCredential(name(), Jackson.nullNode);
    }

    @Override
    public Credential withName(String credentialName) {
        return new JsonCredential(credentialName, json);
    }
}
