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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.linecorp.armeria.client.BlockingWebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ResponseEntity;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.CentralDogmaRepository;
import com.linecorp.centraldogma.common.Entry;
import com.linecorp.centraldogma.common.Revision;
import com.linecorp.centraldogma.testing.junit.CentralDogmaExtension;

class MirroringServiceV1Test {

    private static final String FOO_PROJ = "foo-proj";
    private static final String BAR_REPO = "bar-repo";

    @RegisterExtension
    static final CentralDogmaExtension dogma = new CentralDogmaExtension() {

        @Override
        protected void scaffold(CentralDogma client) {
            client.createProject(FOO_PROJ).join();
            client.createRepository(FOO_PROJ, BAR_REPO).join();
        }
    };

    @Test
    void createNewMirrorConfiguration() throws JsonParseException {
        final BlockingWebClient client = dogma.blockingHttpClient();
        final ResponseEntity<Revision> revision =
                client.prepare()
                      .post("/api/v1/mirrors/projects/{proj}")
                      .pathParam("proj", FOO_PROJ)
                      .header(HttpHeaderNames.AUTHORIZATION, "Bearer anonymous")
                      .content(MediaType.JSON, '{' +
                                               "  \"name\" : \"Git mirror configation for the cart service\", " +
                                               "  \"schedule\": \"5 * * * * ?\"," +
                                               "  \"direction\": \"REMOTE_TO_LOCAL\"," +
                                               "  \"localRepo\": \"" + BAR_REPO + "\"," +
                                               "  \"localPath\": \"/abc\"," +
                                               "  \"remoteScheme\": \"git+https\"," +
                                               "  \"remoteUrl\": \"github.com/line/centraldogma-authtest.git\"," +
                                               "  \"remotePath\": \"/\"," +
                                               "  \"remoteBranch\": \"master\"," +
                                               "  \"gitignore\": []," +
                                               // TODO(ikhoon): Test with credentialId
//                                               "  \"credentialId\": \"my-credential\"," +
                                               "  \"enabled\": true" +
                                               '}')
                      .asJson(Revision.class)
                      .execute();
        assertThat(revision.content().major()).isEqualTo(2);
        final CentralDogmaRepository repo = dogma.client().forRepo(FOO_PROJ, "meta");
        final Entry<?> entry = repo.file("/mirrors.json").get().join();
        assertThatJson(entry.contentAsJson())
                .isEqualTo("[{" +
                           "  \"type\": \"single\"," +
                           "  \"enabled\": true," +
                           "  \"schedule\": \"5 * * * * ?\"," +
                           "  \"direction\": \"REMOTE_TO_LOCAL\"," +
                           "  \"localRepo\": \"" + BAR_REPO + "\"," +
                           "  \"localPath\": \"/abc\"," +
                           "  \"remoteUri\": \"git+https://github.com/line/centraldogma-authtest.git/#master\"" +
//                           "  \"credentialId\": \"my-credential\"" +
                           "}]");

        final ArrayNode response = client.prepare()
                                        .get("/api/v1/mirrors/projects/{proj}")
                                        .pathParam("proj", FOO_PROJ)
                                        .header(HttpHeaderNames.AUTHORIZATION, "Bearer anonymous")
                                        .asJson(ArrayNode.class)
                                        .execute()
                                        .content();
        assertThat(response.size()).isOne();
        assertThatJson(response)
                .isEqualTo("[{" +
                           "  \"schedule\": \"5 * * * * ?\"," +
                           "  \"direction\": \"REMOTE_TO_LOCAL\"," +
                           "  \"localRepo\": \"" + BAR_REPO + "\"," +
                           "  \"localPath\": \"/abc/\"," +
                           "  \"remoteScheme\": \"git+https\"," +
                           "  \"remoteUrl\": \"github.com/line/centraldogma-authtest.git\"," +
                           "  \"remotePath\": \"/\"," +
                           "  \"remoteBranch\": \"master\"," +
                           "  \"gitignore\": []," +
//                           "  \"credentialId\": \"my-credential\"," +
                           "  \"enabled\": true" +
                           "}]");
    }
}
