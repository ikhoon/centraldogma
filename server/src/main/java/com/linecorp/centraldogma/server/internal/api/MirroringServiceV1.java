package com.linecorp.centraldogma.server.internal.api;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.annotation.ConsumesJson;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.centraldogma.internal.api.v1.MirrorDto;
import com.linecorp.centraldogma.server.command.CommandExecutor;
import com.linecorp.centraldogma.server.internal.api.auth.RequiresRole;
import com.linecorp.centraldogma.server.internal.storage.repository.SingleMirrorConfig;
import com.linecorp.centraldogma.server.metadata.ProjectRole;
import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.mirror.MirrorDirection;
import com.linecorp.centraldogma.server.mirror.MirrorUtil;
import com.linecorp.centraldogma.server.storage.project.ProjectManager;

/**
 * Annotated service object for managing mirroring service.
 */
@ProducesJson
@RequiresRole(roles = ProjectRole.OWNER)
@ExceptionHandler(HttpApiExceptionHandler.class)
public class MirroringServiceV1 extends AbstractService {

    static Splitter gitignoreSplitter = Splitter.on('\n');

    public MirroringServiceV1(ProjectManager projectManager, CommandExecutor executor) {
        super(projectManager, executor);
    }

    @Get("/mirrors/projects/{projectName}")
    public CompletableFuture<List<MirrorDto>> listMirrors(@Param String projectName) {
        return projectManager().get(projectName).metaRepo().mirrors(true)
                               .thenApply(mirrors -> mirrors.stream()
                                                            .map(MirroringServiceV1::convertToMirrorDto)
                                                            .collect(toImmutableList()));
    }

    @Post("/mirrors/projects/{projectName}")
    @ConsumesJson
    public CompletableFuture<MirrorDto> createMirror(MirrorDto newMirror) {
        return null;
    }

    private static SingleMirrorConfig converterToMirrorConfig(MirrorDto mirrorDto) {
        final String remoteUri =
                mirrorDto.remoteScheme() + "://" + mirrorDto.remoteUrl() +
                MirrorUtil.normalizePath(mirrorDto.localPath()) + '#' + mirrorDto.remoteBranch();

        return new SingleMirrorConfig(
                mirrorDto.enabled(),
                mirrorDto.schedule(),
                MirrorDirection.valueOf(mirrorDto.direction()),
                mirrorDto.localRepo(),
                mirrorDto.localPath(),
                URI.create(remoteUri),
                mirrorDto.gitignore(),
                mirrorDto.credentialId());
    }

    private static MirrorDto convertToMirrorDto(Mirror mirror) {
        final String gitignore = mirror.gitignore();
        final List<String> gitignoreList;
        if (gitignore == null) {
            gitignoreList = ImmutableList.of();
        } else {
            gitignoreList = gitignoreSplitter.splitToList(gitignore);
        }

        return new MirrorDto(null, // TODO(ikhoon): Get the mirror name from mirror form UI.
                             mirror.schedule().asString(),
                             mirror.direction().name(),
                             mirror.localRepo().name(),
                             mirror.localPath(),
                             mirror.remoteRepoUri().getScheme(),
                             // TODO(ikhoon): Remove scheme part from URI.
                             mirror.remoteRepoUri().toString(),
                             mirror.remotePath(),
                             firstNonNull(mirror.remoteBranch(), "master"),
                             gitignoreList,
                             mirror.credential().id().orElse(null),
                             mirror.enabled());
    }
}
