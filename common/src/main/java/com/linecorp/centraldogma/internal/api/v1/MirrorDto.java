package com.linecorp.centraldogma.internal.api.v1;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonInclude(Include.NON_NULL)
public final class MirrorDto {

    /**
     * The index of this credential in the array at {@code path.json}.
     * {@code null} if newly created.
     */
    @Nullable
    private final Integer index;

    @Nullable
    private final String id;
    private final String projectName;
    private final String schedule;
    private final String direction;
    private final String localRepo;
    private final String localPath;
    private final String remoteScheme;
    private final String remoteUrl;
    private final String remotePath;
    private final String remoteBranch;
    @Nullable
    private final String gitignore;
    @Nullable
    private final String credentialId;
    private final boolean enabled;

    @JsonCreator
    public MirrorDto(@JsonProperty("index") @Nullable Integer index,
                     @JsonProperty("id") @Nullable String id,
                     @JsonProperty("projectName") String projectName,
                     @JsonProperty("schedule") String schedule,
                     @JsonProperty("direction") String direction,
                     @JsonProperty("localRepo") String localRepo,
                     @JsonProperty("localPath") String localPath,
                     @JsonProperty("remoteScheme") String remoteScheme,
                     @JsonProperty("remoteUrl") String remoteUrl,
                     @JsonProperty("remotePath") String remotePath,
                     @JsonProperty("remoteBranch") String remoteBranch,
                     @JsonProperty("gitignore") @Nullable String gitignore,
                     @JsonProperty("credentialId") @Nullable String credentialId,
                     @JsonProperty("enabled") boolean enabled) {
        this.index = index;
        this.id = id;
        this.projectName = requireNonNull(projectName, "projectName");
        this.schedule = requireNonNull(schedule, "schedule");
        this.direction = requireNonNull(direction, "direction");
        this.localRepo = requireNonNull(localRepo, "localRepo");
        this.localPath = requireNonNull(localPath, "localPath");
        this.remoteScheme = requireNonNull(remoteScheme, "remoteScheme");
        this.remoteUrl = requireNonNull(remoteUrl, "remoteUrl");
        this.remotePath = requireNonNull(remotePath, "remotePath");
        this.remoteBranch = requireNonNull(remoteBranch, "remoteBranch");
        this.gitignore = gitignore;
        this.credentialId = credentialId;
        this.enabled = enabled;
    }

    @JsonProperty("index")
    @Nullable
    public Integer index() {
        return index;
    }

    @JsonProperty("id")
    @Nullable
    public String id() {
        return id;
    }

    @JsonProperty("projectName")
    public String projectName() {
        return projectName;
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
    public String gitignore() {
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
                          .add("id", id)
                          .add("projectName", projectName)
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
