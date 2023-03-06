package com.linecorp.centraldogma.internal.api.v1;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonInclude(Include.NON_NULL)
public final class MirrorDto {

    @Nullable
    private final String name;
    private final String schedule;
    private final String direction;
    private final String localRepo;
    private final String localPath;
    private final String remoteScheme;
    private final String remoteUrl;
    private final String remotePath;
    private final String remoteBranch;
    private final List<String> gitignore;
    @Nullable
    private final String credentialId;
    private final boolean enabled;

    public MirrorDto(@Nullable String name, String schedule, String direction, String localRepo,
                     String localPath, final String remoteScheme, String remoteUrl, String remotePath, String remoteBranch,
                     List<String> gitignore, @Nullable String credentialId, boolean enabled) {
        this.name = name;
        this.schedule = requireNonNull(schedule, "schedule");
        this.direction = requireNonNull(direction, "direction");
        this.localRepo = requireNonNull(localRepo, "localRepo");
        this.localPath = requireNonNull(localPath, "localPath");
        this.remoteScheme = requireNonNull(remoteScheme, "remoteScheme");
        this.remoteUrl = requireNonNull(remoteUrl, "remoteUrl");
        this.remotePath = remotePath;
        this.remoteBranch = remoteBranch;
        this.gitignore = gitignore;
        this.credentialId = credentialId;
        this.enabled = enabled;
    }

    @Nullable
    @JsonProperty("name")
    public String name() {
        return name;
    }

    @JsonProperty("schedule")
    public String schedule() {
        return schedule;
    }

    @JsonProperty("direction")
    public String direction() {
        return direction;
    }

    @JsonProperty("localRepo")
    public String localRepo() {
        return localRepo;
    }

    @JsonProperty("localPath")
    public String localPath() {
        return localPath;
    }

    @JsonProperty("remoteScheme")
    public String remoteScheme() {
        return remoteScheme;
    }

    @JsonProperty("remoteUrl")
    public String remoteUrl() {
        return remoteUrl;
    }

    @JsonProperty("remotePath")
    public String remotePath() {
        return remotePath;
    }

    @JsonProperty("remoteBranch")
    public String remoteBranch() {
        return remoteBranch;
    }

    @JsonProperty("gitignore")
    public List<String> gitignore() {
        return gitignore;
    }

    @Nullable
    @JsonProperty("credentialId")
    public String credentialId() {
        return credentialId;
    }

    @JsonProperty("enabled")
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .omitNullValues()
                          .add("name", name)
                          .add("schedule", schedule)
                          .add("direction", direction)
                          .add("localRepo", localRepo)
                          .add("localPath", localPath)
                          .add("remoteUrl", remoteUrl)
                          .add("gitignore", gitignore)
                          .add("credentialId", credentialId)
                          .add("enabled", enabled)
                          .toString();
    }
}
