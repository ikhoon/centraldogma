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

import { NextApiRequest, NextApiResponse } from 'next';
import { MirrorDto } from 'dogma/features/metadata/MirrorDto';

let mirrorConfigs: MirrorDto[] = [];
for (let i = 0; i < 10; i++) {
  mirrorConfigs.push({
    name: `mirror-${i}`,
    credentialId: `credential-${i}`,
    direction: 'LOCAL_TO_REMOTE',
    enabled: true,
    gitignore: [`ignore${i}`],
    localPath: `/local/path/${i}`,
    localRepo: `local-repo-${i}`,
    remotePath: `/remote/path/${i}`,
    remoteScheme: 'git+https',
    remoteUrl: 'github.com:line/centraldogma',
    schedule: `${i} * * * * ?`,
  });
}

let revision = mirrorConfigs.length + 1;
export default function handler(req: NextApiRequest, res: NextApiResponse) {
  switch (req.method) {
    case 'GET':
      res.status(200).json(mirrorConfigs);
      break;
    case 'POST':
      mirrorConfigs.push(req.body);
      res.status(201).json(`${++revision}`);
      break;
    default:
      res.setHeader('Allow', ['GET', 'POST']);
      res.status(405).end(`Method ${req.method} Not Allowed`);
      break;
  }
}
