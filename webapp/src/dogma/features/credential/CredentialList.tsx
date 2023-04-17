import { ColumnDef, createColumnHelper } from '@tanstack/react-table';
import React, { useMemo } from 'react';
import { MirrorDto } from 'dogma/features/mirror/MirrorDto';
import { DataTableClientPagination } from 'dogma/common/components/table/DataTableClientPagination';
import { useGetCredentialsQuery } from 'dogma/features/api/apiSlice';
import {CredentialDto} from "dogma/features/credential/CredentialDto";

export type CredentialListProps<Data extends object> = {
  projectName: string;
};

const CredentialList = <Data extends object>({ projectName }: CredentialListProps<Data>) => {
  const { data, error } = useGetCredentialsQuery(projectName);
  const columnHelper = createColumnHelper<CredentialDto>();
  const columns = useMemo(
    () => [
      columnHelper.accessor((row: CredentialDto) => row.type, {
        cell: (info) => info.getValue(),
        header: 'Type',
      }),
      columnHelper.accessor((row: CredentialDto) => row.id, {
        cell: (info) => info.getValue() || '-',
        header: 'ID',
      }),
      columnHelper.accessor((row: CredentialDto) => row.hostnamePatterns, {
        cell: (info) => info.getValue().join(', '),
        header: 'Hostnames',
      }),
      columnHelper.accessor((row: CredentialDto) => row.enabled, {
        cell: (info) => {
          if (info.getValue()) {
            return 'Active';
          } else {
            return 'Inactive';
          }
        },
        header: 'Status',
      }),
    ],
    [columnHelper, projectName],
  );
  return <DataTableClientPagination columns={columns as ColumnDef<MirrorDto>[]} data={data || []} />;
};

export default CredentialList;
