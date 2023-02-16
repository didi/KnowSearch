import React from "react";
import { IQueryFormProps } from "../../QueryForm";
import { IDTableProps, DTable } from "../DTable";
import { Select } from "knowdesign";
import { IconFont } from "@knowdesign/icons";

interface MiniSelectInterface extends React.FC<any> {
  Option: typeof Select.Option;
}

interface ITableExtendProps extends IDTableProps {
  isCustomPg?: boolean; // 是否展示自定义分页器样式 -- true
  pgSelectComponentText?: string; // 分页下拉左侧展示文案
  pgCustomSelectComponent?: () => any; // 展示自定义分页下拉框
  selectComponentIcon?: string; // 自定义分页下拉框Icon -- 'icon-xiala'
}

export default function ProTable<T>(props: { showQueryForm?: boolean; queryFormProps?: IQueryFormProps; tableProps: ITableExtendProps }) {
  const { showQueryForm = false, queryFormProps, tableProps } = props;
  const { pgSelectComponentText = "", pgCustomSelectComponent, selectComponentIcon = "icon-xiala", isCustomPg = true } = tableProps;
  const SelectComponent: MiniSelectInterface = (props) => {
    return (
      <>
        <span>{pgSelectComponentText || ""}</span>
        <Select bordered={false} suffixIcon={<IconFont type={selectComponentIcon} />} {...props} />
      </>
    );
  };

  SelectComponent.Option = Select.Option;

  const customPg = isCustomPg
    ? {
        locale: {
          items_per_page: "/页",
        },
        selectComponentClass: SelectComponent,
        className: "pro-table-pagination-custom",
      }
    : null;

  const pagination = {
    ...customPg,
    ...tableProps.paginationProps,
    // className: `${isCustomPg ? customPg.className : ""}${
    //   tableProps?.paginationProps?.className ? " " + tableProps?.paginationProps?.className : ""
    // }`,
  };

  return (
    <div className="pro-table-container">
      {/* {showQueryForm && (
        <div className="container-query">
          <QueryForm {...queryFormProps} />
        </div>
      )} */}
      <div className="container-table">
        <DTable {...{ ...tableProps, paginationProps: pagination }} showQueryForm={showQueryForm} queryFormProps={queryFormProps} />
      </div>
    </div>
  );
}
