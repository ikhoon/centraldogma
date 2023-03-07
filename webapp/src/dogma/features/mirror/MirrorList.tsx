import { ColumnDef, createColumnHelper } from '@tanstack/react-table';
import React, { useMemo } from 'react';
import { MirrorDto } from 'dogma/features/mirror/MirrorDto';
import { DataTableClientPagination } from 'dogma/common/components/table/DataTableClientPagination';
import { useGetMirrorsQuery } from 'dogma/features/api/apiSlice';

export type MirrorListProps<Data extends object> = {
  projectName: string;
};

const MirrorList = <Data extends object>({ projectName }: MirrorListProps<Data>) => {
  const { data, error } = useGetMirrorsQuery(projectName);
  const columnHelper = createColumnHelper<MirrorDto>();
  const columns = useMemo(
    () => [
      columnHelper.accessor((row: MirrorDto) => `${row.localRepo}${row.localPath}`, {
        cell: (info) => info.getValue(),
        header: 'Local',
      }),
      columnHelper.accessor((row: MirrorDto) => row.direction, {
        cell: (info) => {
          const direction = info.getValue();
          if (direction === 'LOCAL_TO_REMOTE') {
            return ' ➡️';
          } else {
            return ' ⬅️️';
          }
        },
        header: 'Direction',
      }),
      columnHelper.accessor((row: MirrorDto) => row.remoteUrl, {
        cell: (info) => info.getValue(),
        header: 'Remote',
      }),
      columnHelper.accessor((row: MirrorDto) => row.schedule, {
        cell: (info) => info.getValue(),
        header: 'Schedule',
      }),
      columnHelper.accessor((row: MirrorDto) => row.enabled, {
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
  return <DataTableClientPagination columns={columns as ColumnDef<Data>[]} data={data || []} />;
};

export default MirrorList;
