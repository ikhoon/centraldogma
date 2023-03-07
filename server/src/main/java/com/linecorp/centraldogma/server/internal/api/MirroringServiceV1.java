package com.linecorp.centraldogma.server.internal.api;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.linecorp.centraldogma.server.internal.storage.repository.SingleMirrorConfig.gitignoreSplitter;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.annotation.ConsumesJson;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.common.Revision;
import com.linecorp.centraldogma.internal.api.v1.MirrorDto;
import com.linecorp.centraldogma.server.command.CommandExecutor;
import com.linecorp.centraldogma.server.internal.api.auth.RequiresRole;
import com.linecorp.centraldogma.server.metadata.MetadataService;
import com.linecorp.centraldogma.server.metadata.ProjectRole;
import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.storage.project.ProjectManager;

/**
 * Annotated service object for managing mirroring service.
 */
@ProducesJson
@RequiresRole(roles = ProjectRole.OWNER)
@ExceptionHandler(HttpApiExceptionHandler.class)
public class MirroringServiceV1 extends AbstractService {

    private final MetadataService mds;

    public MirroringServiceV1(ProjectManager projectManager, CommandExecutor executor, MetadataService mds) {
        super(projectManager, executor);
        this.mds = mds;
    }

    @Get("/mirrors/projects/{projectName}")
    public CompletableFuture<List<MirrorDto>> listMirrors(@Param String projectName) {
        return projectManager().get(projectName).metaRepo().mirrors(true).thenApply(mirrors -> {
            return mirrors.stream()
                          .map(mirror -> convertToMirrorDto(projectName, mirror))
                          .collect(toImmutableList());
        });
    }

    @Post("/mirrors/projects/{projectName}")
    @ConsumesJson
    public CompletableFuture<Revision> createMirror(@Param String projectName, MirrorDto newMirror,
                                                    Author author) {
        return mds.createMirror(projectName, newMirror, author);
    }

    private static MirrorDto convertToMirrorDto(String projectName, Mirror mirror) {
        final String gitignore = mirror.gitignore();
        final List<String> gitignoreList;
        if (gitignore == null) {
            gitignoreList = ImmutableList.of();
        } else {
            gitignoreList = gitignoreSplitter.splitToList(gitignore);
        }

        final URI remoteRepoUri = mirror.remoteRepoUri();
        return new MirrorDto(null, // TODO(ikhoon): Get the mirror name from mirror form UI.
                             projectName,
                             mirror.schedule().asString(),
                             mirror.direction().name(),
                             mirror.localRepo().name(),
                             mirror.localPath(),
                             remoteRepoUri.getScheme(),
                             remoteRepoUri.getHost() + remoteRepoUri.getPath(),
                             mirror.remotePath(),
                             firstNonNull(mirror.remoteBranch(), "master"),
                             gitignoreList,
                             mirror.credential().id().orElse(null),
                             mirror.enabled());
    }
}
