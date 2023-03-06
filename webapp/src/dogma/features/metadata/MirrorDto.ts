
export interface MirrorDto {
  name: string;
  schedule: string;
  direction: 'REMOTE_TO_LOCAL' | 'LOCAL_TO_REMOTE';
  localRepo: string;
  localPath: string;
  remoteUrl: string;
  gitignore?: string;
  credentialId?: string;
  enabled: boolean;
}
