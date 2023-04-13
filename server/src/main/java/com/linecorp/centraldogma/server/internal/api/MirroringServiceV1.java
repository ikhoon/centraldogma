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

import com.linecorp.armeria.server.annotation.ConsumesJson;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
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
     * GET /projects/{projectName}/mirrors?id={id} or GET /projects/{projectName}/mirrors?index={index}
     *
     * <p>Returns the mirror whose ID or index matches in the project.
     */
    @Get("/projects/{projectName}/mirrors")
    public CompletableFuture<MirrorDto> getMirror(@Param String projectName, @Param @Nullable Integer index,
                                                  @Param @Nullable String id) {
        if (index == null && id == null) {
            throw new IllegalArgumentException("Either index or id must be specified");
        }
        if (index != null && id != null) {
            throw new IllegalArgumentException("Either index or id must be specified, not both");
        }
        if (index != null) {
            checkArgument(index >= 0, "index: %s (expected: >= 0)", index);
        }

        return projectManager().get(projectName).metaRepo().mirrors(true).thenApply(mirrors -> {
            Mirror mirror;
            if (index != null) {
                if (index >= mirrors.size()) {
                    throw new EntryNotFoundException(
                            "No such mirror at the index " + index + " in " + projectName);
                }
                mirror = mirrors.get(index);
            } else {
                mirror = mirrors.stream().filter(m -> id.equals(m.id()))
                                .findFirst()
                                .orElseThrow(() -> new EntryNotFoundException(
                                        "No such mirror with the ID " + id + " in " + projectName));
            }

            return convertToMirrorDto(projectName, mirror);
        });
    }

    /**
     * POST /projects/{projectName}/mirrors
     *
     * <p>Creates a new mirror.
     */
    @Post("/projects/{projectName}/mirrors")
    @ConsumesJson
    public CompletableFuture<Revision> createMirror(@Param String projectName, MirrorDto newMirror,
                                                    Author author) {
        return mds.createMirror(projectName, newMirror, author);
    }

    @Put("/projects/{projectName}/mirrors/{index}")
    @ConsumesJson
    public CompletableFuture<Revision> updateMirror(@Param String projectName, @Param int index,
                                                    MirrorDto newMirror, Author author) {
        return mds.updateMirror(projectName, index, newMirror, author);
    }

    @Get("/projects/{projectName}/credentials")
    public CompletableFuture<List<MirrorCredentialDto>> listCredentials(@Param String projectName) {
        return projectManager()
                .get(projectName)
                .metaRepo()
                .credentials()
                .thenApply(credentials -> credentials.stream()
                                                     .map(MirroringServiceV1::convertToMirrorCredentialDto)
                                                     .collect(toImmutableList()));
    }

    @Post("/projects/{projectName}/credentials")
    @ConsumesJson
    public CompletableFuture<Revision> createCredential(@Param String projectName,
                                                        MirrorCredential credential, Author author) {
        return mds.createCredential(projectName, credential, author);
    }

    @Put("/projects/{projectName}/credentials/{index}")
    @ConsumesJson
    public CompletableFuture<Revision> updateCredential(@Param String projectName, @Param int index,
                                                        MirrorCredential credential, Author author) {
        return mds.updateCredential(projectName, index, credential, author);
    }

    private static MirrorDto convertToMirrorDto(String projectName, Mirror mirror) {
        final URI remoteRepoUri = mirror.remoteRepoUri();
        return new MirrorDto(mirror.index(),
                             null, // TODO(ikhoon): Take the mirror ID from mirror form UI.
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

    private static MirrorCredentialDto convertToMirrorCredentialDto(MirrorCredential credential) {
        final int index = credential.index();
        final String id = credential.id().orElse(null);
        final Set<String> hostnamePatterns = credential.hostnamePatterns().stream()
                                                       .map(Pattern::pattern)
                                                       .collect(toImmutableSet());

        if (credential instanceof PasswordMirrorCredential) {
            final PasswordMirrorCredential credential0 = (PasswordMirrorCredential) credential;
            return MirrorCredentialDto.ofPassword(index, id, hostnamePatterns,
                                                  credential0.username(), credential0.password());
        } else if (credential instanceof AccessTokenMirrorCredential) {
            final AccessTokenMirrorCredential credential0 = (AccessTokenMirrorCredential) credential;
            return MirrorCredentialDto.ofAccessToken(index, id, hostnamePatterns, credential0.accessToken());
        } else if (credential instanceof PublicKeyMirrorCredential) {
            final PublicKeyMirrorCredential credential0 = (PublicKeyMirrorCredential) credential;
            final byte[] passphrase = credential0.passphrase();
            final String passphraseString = passphrase != null ?
                                            new String(passphrase, StandardCharsets.UTF_8) : null;
            return MirrorCredentialDto.ofPublicKey(index, id, hostnamePatterns,
                                                   new String(credential0.publicKey(), StandardCharsets.UTF_8),
                                                   new String(credential0.privateKey(), StandardCharsets.UTF_8),
                                                   passphraseString);
        } else if (credential instanceof NoneMirrorCredential) {
            return MirrorCredentialDto.ofNone(index, id, hostnamePatterns);
        } else {
            throw new Error("unknown credential type: " + credential.getClass().getName());
        }
    }
}
