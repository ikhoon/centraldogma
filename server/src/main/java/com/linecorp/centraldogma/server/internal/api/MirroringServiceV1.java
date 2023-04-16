package com.linecorp.centraldogma.server.internal.api;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.ConsumesJson;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import com.linecorp.armeria.server.annotation.StatusCode;
import com.linecorp.centraldogma.common.Author;
import com.linecorp.centraldogma.common.EntryNotFoundException;
import com.linecorp.centraldogma.common.Revision;
import com.linecorp.centraldogma.internal.api.v1.MirrorCredentialDto;
import com.linecorp.centraldogma.internal.api.v1.MirrorDto;
import com.linecorp.centraldogma.server.command.CommandExecutor;
import com.linecorp.centraldogma.server.internal.api.auth.RequiresRole;
import com.linecorp.centraldogma.server.internal.mirror.credential.AccessTokenMirrorCredential;
import com.linecorp.centraldogma.server.internal.mirror.credential.NoneMirrorCredential;
import com.linecorp.centraldogma.server.internal.mirror.credential.PasswordMirrorCredential;
import com.linecorp.centraldogma.server.internal.mirror.credential.PublicKeyMirrorCredential;
import com.linecorp.centraldogma.server.metadata.MetadataService;
import com.linecorp.centraldogma.server.metadata.ProjectRole;
import com.linecorp.centraldogma.server.mirror.Mirror;
import com.linecorp.centraldogma.server.mirror.MirrorCredential;
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

    /**
     * GET /projects/{projectName}/mirrors
     *
     * <p>Returns the list of the mirrors in the project.
     */
    @Get("/projects/{projectName}/mirrors")
    public CompletableFuture<List<MirrorDto>> listMirrors(@Param String projectName) {
        return projectManager().get(projectName).metaRepo().mirrors(true).thenApply(mirrors -> {
            return mirrors.stream()
                          .map(mirror -> convertToMirrorDto(projectName, mirror))
                          .collect(toImmutableList());
        });
    }

    /**
     * POST /projects/{projectName}/mirrors
     *
     * <p>Creates a new mirror.
     */
    @Post("/projects/{projectName}/mirrors")
    @ConsumesJson
    @StatusCode(201)
    public CompletableFuture<Revision> createMirror(@Param String projectName, MirrorDto newMirror, Author author) {
        return mds.createMirror(projectName, newMirror, author);
    }

    /**
     * GET /projects/{projectName}/mirrors/{index}
     *
     * <p>Returns the mirror at the specified index in the project mirror list.
     */
    @Get("/projects/{projectName}/mirrors/{index}")
    public CompletableFuture<MirrorDto> getMirror(@Param String projectName, @Param int index) {
        checkArgument(index >= 0, "index: %s (expected: >= 0)", index);

        return projectManager().get(projectName).metaRepo().mirrors(true).thenApply(mirrors -> {
            if (index >= mirrors.size()) {
                throw new EntryNotFoundException(
                        "No such mirror at the index " + index + " in " + projectName);
            }
            return convertToMirrorDto(projectName, mirrors.get(index));
        });
    }

    /**
     * PUT /projects/{projectName}/mirrors/{index}
     *
     * <p>Update the exising mirror.
     */
    @Put("/projects/{projectName}/mirrors/{index}")
    @ConsumesJson
    public CompletableFuture<Revision> updateMirror(@Param String projectName, @Param int index,
                                                    MirrorDto newMirror, Author author) {
        return mds.updateMirror(projectName, index, newMirror, author);
    }

    private static MirrorDto convertToMirrorDto(String projectName, Mirror mirror) {
        final URI remoteRepoUri = mirror.remoteRepoUri();
        return new MirrorDto(mirror.index(),
                             mirror.id(),
                             projectName,
                             mirror.schedule().asString(),
                             mirror.direction().name(),
                             mirror.localRepo().name(),
                             mirror.localPath(),
                             remoteRepoUri.getScheme(),
                             remoteRepoUri.getHost() + remoteRepoUri.getPath(),
                             mirror.remotePath(),
                             firstNonNull(mirror.remoteBranch(), "master"),
                             mirror.gitignore(),
                             mirror.credential().id().orElse(null),
                             mirror.enabled());
    }
}
