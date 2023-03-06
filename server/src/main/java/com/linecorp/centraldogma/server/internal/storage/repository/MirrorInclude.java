package com.linecorp.centraldogma.server.internal.storage.repository;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.linecorp.centraldogma.server.mirror.MirrorDirection;

final class MirrorInclude {

    final Pattern pattern;
    final String replacement;
    @Nullable
    final MirrorDirection direction;
    @Nullable
    final String localPath;
    @Nullable
    final String credentialId;
    @Nullable
    final Cron schedule;

    @JsonCreator
    MirrorInclude(@JsonProperty("schedule") @Nullable String schedule,
                  @JsonProperty(value = "pattern", required = true) Pattern pattern,
                  @JsonProperty(value = "replacement", required = true) String replacement,
                  @JsonProperty("direction") @Nullable MirrorDirection direction,
                  @JsonProperty("localPath") @Nullable String localPath,
                  @JsonProperty("credentialId") @Nullable String credentialId) {

        this.schedule = schedule != null ? MirrorConfig.cronParser.parse(schedule) : null;
        this.pattern = requireNonNull(pattern, "pattern");
        this.replacement = requireNonNull(replacement, "replacement");
        this.direction = direction;
        this.localPath = localPath;
        this.credentialId = credentialId;
    }
}
