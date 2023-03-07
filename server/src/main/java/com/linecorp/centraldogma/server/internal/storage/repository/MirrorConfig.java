package com.linecorp.centraldogma.server.internal.storage.repository;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.mirror.MirrorCredential;
import com.linecorp.centraldogma.server.storage.project.Project;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @Type(value = SingleMirrorConfig.class, name = "single"),
        @Type(value = MultipleMirrorConfig.class, name = "multiple")
})
public abstract class MirrorConfig {

    static final String DEFAULT_SCHEDULE = "0 * * * * ?"; // Every minute

    static final CronParser cronParser = new CronParser(
            CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    abstract List<Mirror> toMirrors(Project parent, Iterable<MirrorCredential> credentials);

    static MirrorCredential findCredential(Iterable<MirrorCredential> credentials, URI remoteUri,
                                           @Nullable String credentialId) {
        if (credentialId != null) {
            // Find by credential ID.
            for (MirrorCredential c : credentials) {
                final Optional<String> id = c.id();
                if (id.isPresent() && credentialId.equals(id.get())) {
                    return c;
                }
            }
        } else {
            // Find by host name.
            for (MirrorCredential c : credentials) {
                if (c.matches(remoteUri)) {
                    return c;
                }
            }
        }

        return MirrorCredential.FALLBACK;
    }

    final boolean enabled;

    MirrorConfig(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("enabled")
    public boolean enabled() {
        return enabled;
    }
}
