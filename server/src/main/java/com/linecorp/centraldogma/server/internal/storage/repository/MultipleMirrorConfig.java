package com.linecorp.centraldogma.server.internal.storage.repository;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.linecorp.centraldogma.internal.Util.requireNonNullElements;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.mirror.MirrorCredential;
import com.linecorp.centraldogma.server.mirror.MirrorDirection;
import com.linecorp.centraldogma.server.storage.project.Project;

final class MultipleMirrorConfig extends MirrorConfig {

    final MirrorDirection defaultDirection;
    final String defaultLocalPath;
    final Cron defaultSchedule;
    @Nullable
    final String defaultCredentialId;
    final List<MirrorInclude> includes;
    final List<Pattern> excludes;

    @JsonCreator
    MultipleMirrorConfig(
            @JsonProperty("enabled") @Nullable Boolean enabled,
            @JsonProperty("defaultSchedule") @Nullable String defaultSchedule,
            @JsonProperty(value = "defaultDirection", required = true) MirrorDirection defaultDirection,
            @JsonProperty("defaultLocalPath") @Nullable String defaultLocalPath,
            @JsonProperty("defaultCredentialId") @Nullable String defaultCredentialId,
            @JsonProperty(value = "includes", required = true)
            @JsonDeserialize(contentAs = MirrorInclude.class)
            Iterable<MirrorInclude> includes,
            @JsonProperty("excludes") @Nullable
            @JsonDeserialize(contentAs = Pattern.class)
            Iterable<Pattern> excludes) {

        super(firstNonNull(enabled, true));
        this.defaultSchedule = cronParser.parse(firstNonNull(defaultSchedule, DEFAULT_SCHEDULE));
        this.defaultDirection = requireNonNull(defaultDirection, "defaultDirection");
        this.defaultLocalPath = firstNonNull(defaultLocalPath, "/");
        this.defaultCredentialId = defaultCredentialId;
        this.includes = ImmutableList.copyOf(requireNonNullElements(includes, "includes"));
        if (excludes != null) {
            this.excludes = ImmutableList.copyOf(requireNonNullElements(excludes, "excludes"));
        } else {
            this.excludes = Collections.emptyList();
        }
    }

    @Override
    List<Mirror> toMirrors(Project parent, Iterable<MirrorCredential> credentials) {
        final ImmutableList.Builder<Mirror> builder = ImmutableList.builder();
        parent.repos().list().forEach((repoName, repo) -> {
            if (repoName == null || excludes.stream().anyMatch(p -> p.matcher(repoName).find())) {
                return;
            }

            for (MirrorInclude i : includes) {
                final Matcher m = i.pattern.matcher(repoName);
                if (!m.matches()) {
                    continue;
                }

                final URI remoteUri = URI.create(m.replaceFirst(i.replacement));
                builder.add(Mirror.of(firstNonNull(i.schedule, defaultSchedule),
                                      firstNonNull(i.direction, defaultDirection),
                                      findCredential(credentials, remoteUri,
                                                     i.credentialId != null ? i.credentialId
                                                                            : defaultCredentialId),
                                      repo,
                                      firstNonNull(i.localPath, defaultLocalPath),
                                      remoteUri,
                                      null, enabled));
            }
        });

        return builder.build();
    }
}
