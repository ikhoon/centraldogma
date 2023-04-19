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

package com.linecorp.centraldogma.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.linecorp.centraldogma.internal.Jackson;
import com.linecorp.centraldogma.server.internal.mirror.credential.PasswordMirrorCredential;
import org.junit.jupiter.api.Test;

class CredentialSerialTest {
    @Test
    void test() {
        final PasswordMirrorCredential credential =
                new PasswordMirrorCredential("a", ImmutableList.of(), "foo", "pass", true);
        final JsonNode jsonNode = Jackson.valueToTree(credential);
        System.out.println(jsonNode);
    }

    @Test
    void testWithCollection() {
        final PasswordMirrorCredential credential =
                new PasswordMirrorCredential("a", ImmutableList.of(), "foo", "pass", true);
        final JsonNode jsonNode = Jackson.valueToTree(ImmutableList.of(credential));
        System.out.println(jsonNode);
    }
}
