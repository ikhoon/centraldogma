/*
 * Copyright 2023 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import { useRouter } from 'next/router';
import {
  Badge,
  Button,
  Center,
  Code,
  Icon,
  Link,
  Spacer,
  Table,
  TableContainer,
  Tbody,
  Td,
  Text,
  Tr,
} from '@chakra-ui/react';
import { useGetCredentialsQuery, useGetMirrorQuery } from 'dogma/features/api/apiSlice';
import { Deferred } from 'dogma/common/components/Deferred';
import { GoKey, GoMirror, GoRepo, GoRepoClone } from 'react-icons/go';
import React, { ReactNode } from 'react';
import { EditIcon } from '@chakra-ui/icons';
import { GiMirrorMirror, GiPowerButton } from 'react-icons/gi';
import { BiTimer } from 'react-icons/bi';
import { IoBanSharp } from 'react-icons/io5';
import { Breadcrumbs } from 'dogma/common/components/Breadcrumbs';
import { IconType } from 'react-icons';

const HeadRow = ({ children }: { children: ReactNode }) => (
  <Td width="250px" fontWeight="semibold">
    {children}
  </Td>
);

const AlignedIcon = ({ as }: { as: IconType }) => <Icon as={as} marginBottom="-4px" marginRight={2} />;

const MirrorViewPage = () => {
  const router = useRouter();
  const projectName = router.query.projectName as string;
  const index = parseInt(router.query.index as string, 10);
  const { data: mirror, isLoading, error } = useGetMirrorQuery({ projectName, index });
  const { data: credentials } = useGetCredentialsQuery(projectName);
  const credential = (credentials || []).find((credential) => {
    return credential.id === mirror?.credentialId;
  });

  return (
    <Deferred isLoading={isLoading} error={error}>
      {() => {
        const breadcrumbPath = router.asPath.replace(`/mirrors/${index}`, `/mirrors/${mirror.id}`);

        return (
          <>
            <Breadcrumbs path={breadcrumbPath} omitIndexList={[0]} />
            <Spacer />

            <TableContainer mt="7">
              <Table fontSize={'lg'} variant="unstyled">
                <Tbody>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GiMirrorMirror} /> Mirror ID
                    </HeadRow>
                    <Td fontWeight="semibold">{mirror.id}</Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={BiTimer} /> Schedule
                    </HeadRow>
                    <Td>
                      <Code variant="outline" p={1}>
                        {mirror.schedule}
                      </Code>
                    </Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GoMirror} /> Direction
                    </HeadRow>
                    <Td>
                      <Badge colorScheme={'blue'}>{mirror.direction}</Badge>
                    </Td>
                  </Tr>

                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GoRepo} /> Local path
                    </HeadRow>
                    <Td>
                      <Link
                        href={`/app/projects/${projectName}/repos/${mirror.localRepo}/list/head${mirror.localPath}`}
                      >
                        <Code fontSize="md" padding="2px 10px 2px 10px">
                          dogma://{projectName}/{mirror.localRepo}
                          {mirror.localPath}
                        </Code>
                      </Link>
                    </Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GoRepoClone} /> Remote path
                    </HeadRow>
                    <Td>
                      <Code fontSize="md" padding="2px 10px 2px 10px">
                        {mirror.remoteScheme}://{mirror.remoteUrl}
                        {mirror.remotePath}#{mirror.remoteBranch}
                      </Code>
                    </Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GoKey} /> Credential
                    </HeadRow>
                    <Td>
                      {credential && (
                        <Link href={`/app/projects/${projectName}/credentials/${credential.index}`}>
                          {mirror.credentialId}
                        </Link>
                      )}
                    </Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={IoBanSharp} /> gitignore
                    </HeadRow>
                    <Td>
                      <Text>{mirror.gitignore}</Text>
                    </Td>
                  </Tr>
                  <Tr>
                    <HeadRow>
                      <AlignedIcon as={GiPowerButton} /> Status
                    </HeadRow>
                    <Td>
                      {mirror.enabled ? (
                        <Badge colorScheme={'green'}>Enabled</Badge>
                      ) : (
                        <Badge colorScheme={'red'}>Disabled</Badge>
                      )}
                    </Td>
                  </Tr>
                </Tbody>
              </Table>
            </TableContainer>

            <Center mt={10}>
              <Link href={`/app/projects/${projectName}/mirrors/${index}/edit`}>
                <Button colorScheme="teal">
                  <EditIcon mr={2} />
                  Edit mirror
                </Button>
              </Link>
            </Center>
          </>
        );
      }}
    </Deferred>
  );
};

export default MirrorViewPage;
