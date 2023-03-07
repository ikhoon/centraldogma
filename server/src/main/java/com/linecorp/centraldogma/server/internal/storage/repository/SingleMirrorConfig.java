package com.linecorp.centraldogma.server.internal.storage.repository;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.mirror.MirrorCredential;
import com.linecorp.centraldogma.server.mirror.MirrorDirection;
import com.linecorp.centraldogma.server.storage.project.Project;

@JsonInclude(Include.NON_NULL)
public final class SingleMirrorConfig extends MirrorConfig {

    public static Splitter gitignoreSplitter = Splitter.on('\n');

    final MirrorDirection direction;
    @Nullable
    final String localRepo;
    @Nullable
    final String localPath;
    final URI remoteUri;
    @Nullable
    final String gitignore;
    @Nullable
    final String credentialId;
    final Cron schedule;

    @JsonCreator
    public SingleMirrorConfig(@JsonProperty("enabled") @Nullable Boolean enabled,
                              @JsonProperty("schedule") @Nullable String schedule,
                              @JsonProperty(value = "direction", required = true) MirrorDirection direction,
                              @JsonProperty(value = "localRepo", required = true) String localRepo,
                              @JsonProperty("localPath") @Nullable String localPath,
                              @JsonProperty(value = "remoteUri", required = true) URI remoteUri,
                              @JsonProperty("gitignore") @Nullable Object gitignore,
                              @JsonProperty("credentialId") @Nullable String credentialId) {

        super(firstNonNull(enabled, true));
        this.schedule = cronParser.parse(firstNonNull(schedule, DEFAULT_SCHEDULE));
        this.direction = requireNonNull(direction, "direction");
        this.localRepo = requireNonNull(localRepo, "localRepo");
        this.localPath = firstNonNull(localPath, "/");
        this.remoteUri = requireNonNull(remoteUri, "remoteUri");
        if (gitignore != null) {
            if (gitignore instanceof Iterable &&
                Streams.stream((Iterable<?>) gitignore).allMatch(String.class::isInstance)) {
                this.gitignore = String.join("\n", (Iterable<String>) gitignore);
            } else if (gitignore instanceof String) {
                this.gitignore = (String) gitignore;
            } else {
                throw new IllegalArgumentException(
                        "gitignore: " + gitignore + " (expected: either a string or an array of strings)");
            }
        } else {
            this.gitignore = null;
        }
        this.credentialId = credentialId;
    }

    @Override
    List<Mirror> toMirrors(Project parent, Iterable<MirrorCredential> credentials,
                           boolean includeDisabled) {
        if (localRepo == null || !parent.repos().exists(localRepo)) {
            return ImmutableList.of();
        }
        if (!includeDisabled && !enabled) {
            // Should return an active mirror.
            return ImmutableList.of();
        }

        return ImmutableList.of(Mirror.of(
                schedule, direction, findCredential(credentials, remoteUri, credentialId),
                parent.repos().get(localRepo), localPath, remoteUri, gitignore, enabled));
    }

    @JsonProperty("direction")
    public MirrorDirection direction() {
        return direction;
    }

    @Nullable
    @JsonProperty("localRepo")
    public String localRepo() {
        return localRepo;
    }

    @Nullable
    @JsonProperty("localPath")
    public String localPath() {
        return localPath;
    }

    @JsonProperty("remoteUri")
    public String remoteUri() {
        return remoteUri.toString();
    }

    @Nullable
    public List<String> gitignore() {
        if (gitignore == null) {
            return null;
        }
        return gitignoreSplitter.splitToList(gitignore);
    }

    @Nullable
    @JsonProperty("credentialId")
    public String credentialId() {
        return credentialId;
    }

    @JsonProperty("schedule")
    public String schedule() {
        return schedule.asString();
    }
}
