import React from "react";
import { DTable } from "../DTable";
import { QueryForm } from "../../ProForm";
import { IDTableProps } from "../DTable";
import { IQueryFormProps } from "../../ProForm/QueryForm";
import "./index.less";

export default function XTable<T>(props: {
  showQueryForm?: boolean;
  queryFormProps?: IQueryFormProps;
  tableProps: IDTableProps;
}) {
  const { showQueryForm = false, queryFormProps, tableProps } = props;
  return (
    <div className="pro-table-container">
      {showQueryForm && (
        <div className="container-query">
          <QueryForm {...queryFormProps} />
        </div>
      )}
      <div className="container-table">
        <DTable
          {...tableProps}
        />
      </div>
    </div>
  );
}
