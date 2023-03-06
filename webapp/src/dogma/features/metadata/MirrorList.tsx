import { ColumnDef, createColumnHelper } from "@tanstack/react-table";
import React, { useMemo } from "react";
import { MirrorDto } from "dogma/features/metadata/MirrorDto";
import { DataTableClientPagination } from "dogma/common/components/table/DataTableClientPagination";

export type MirrorListProps<Data extends object> = {
  data: Data[];
  projectName: string;
}

const MirrorList = <Data extends object>({data, projectName}: MirrorListProps<Data>) => {
  const columnHelper = createColumnHelper<MirrorDto>();
  const columns = useMemo(
    () => [
      columnHelper.accessor((row: MirrorDto) => `${row.localRepo}${row.localPath}`, {
        cell: (info) => info.getValue(),
        header: "Local"
      }),
      columnHelper.accessor((row: MirrorDto) => row.direction, {
        cell: (info) => {
          const direction = info.getValue();
          if (direction === "LOCAL_TO_REMOTE") {
            return " ➡️"
          } else {
            return " ⬅️️"
          }
        },
        header: "Direction"
      }),
      columnHelper.accessor((row: MirrorDto) => row.remoteUrl, {
        cell: (info) => info.getValue(),
        header: "Remote"
      }),
      columnHelper.accessor((row: MirrorDto) => row.enabled, {
        cell: (info) => {
          if (info.getValue()) {
            return "Active";
          } else {
            return "Inactive";
          }
        },
        header: "Status"
      }),
    ], [columnHelper, projectName]
  );
  return <DataTableClientPagination columns={columns as ColumnDef<Data>[]} data={data} />
}

export default MirrorList;
