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
import com.google.common.base.MoreObjects.ToStringHelper;

import com.linecorp.centraldogma.server.credential.Credential;
import com.linecorp.centraldogma.server.credential.CredentialType;

public final class StringCredential extends AbstractCredential {

    private final String value;

    @JsonCreator
    public StringCredential(@JsonProperty("name") String name,
                            @JsonProperty("value") String value) {
        super(name, CredentialType.STRING);
        this.value = value;
    }

    @JsonProperty("value")
    public String value() {
        // XXX(ikhoon): Do we need to decode the value with `CentralDogmaConfig.convertValue()`?
        return value;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + value.hashCode();
    }

    @Override
    void addProperties(ToStringHelper helper) {
        // Value is a secret, so do not print it.
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StringCredential)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final StringCredential that = (StringCredential) obj;
        return value.equals(that.value);
    }

    @Override
    public Credential withoutSecret() {
        return new StringCredential(name(), "****");
    }

    @Override
    public Credential withName(String credentialName) {
        return new StringCredential(credentialName, value);
    }
}
