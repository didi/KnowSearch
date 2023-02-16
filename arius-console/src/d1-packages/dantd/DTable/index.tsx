import React from "react";
import { Input, Button, Table } from "antd";
import { ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import "./index.less";

export const DTablerefix = "d-table";

export const pagination = {
  // position: 'bottomRight',
  showQuickJumper: true,
  showSizeChanger: true,
  pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
  showTotal: (total: number) => `共 ${total} 条`,
  // hideOnSinglePage: true,
};

export interface ITableBtn {
  clickFunc?: () => void;
  type?: string;
  customFormItem?: string | JSX.Element;
  isRouterNav?: boolean;
  label: string | JSX.Element;
  className?: string;
  needConfirm?: boolean;
  aHref?: string;
  confirmText?: string;
  noRefresh?: boolean;
  loading?: boolean;
  disabled?: boolean;
  invisible?: boolean; // 不可见
}

export interface ISearchInput {
  placeholder?: string;
  submit: (params?: any) => any;
  width?: string;
}

export interface IDTableProps {
  paginationProps?: object;
  noPagination?: boolean;
  rowKey: string;
  columns: object[];
  dataSource: object[];
  loading?: boolean;
  reloadData?: (params?: object) => any;
  getOpBtns?: (params?: object) => ITableBtn[];
  getJsxElement?: (params?: object) => JSX.Element;
  tableHeaderSearchInput?: ISearchInput;
  attrs?: object;
  searchInputRightBtns?: ITableBtn[];
}

export const DTable = (props: IDTableProps) => {
  const renderSearch = () => {
    if (!props?.tableHeaderSearchInput) return;
    const { searchInputRightBtns = [] } = props;
    const { placeholder = null, submit, width } = props?.tableHeaderSearchInput;
    return (
      <div className={`${DTablerefix}-box-header-search`}>
        <div className={`${DTablerefix}-box-header-search-custom`}>
          {searchInputRightBtns.map((item, index) => {
            if (item?.type === "custom") {
              return (
                <span style={{ marginLeft: 10 }} className={item.className} key={index}>
                  {item?.customFormItem}
                </span>
              );
            }
            return item.noRefresh ? (
              <Button className={item.className} key={index}>
                {item.label}
              </Button>
            ) : (
              <Button disabled={item.disabled} loading={item.loading} key={index} className={item.className} onClick={item.clickFunc}>
                {" "}
                {item.label}{" "}
              </Button>
            );
          })}
        </div>
        <div>
          <Input
            allowClear
            placeholder={placeholder || "请输入关键字"}
            style={{ width: width || 200 }}
            onChange={(e) => {
              console.log(e, 111);
              submit(e.target.value);
            }}
            suffix={<SearchOutlined style={{ color: "#ccc" }} />}
          />
        </div>
      </div>
    );
  };

  const renderTableInnerOp = (reloadFunc: any, btns?: ITableBtn[], element?: JSX.Element) => {
    return (
      <div className={`${DTablerefix}-box-header-btn`}>
        {reloadFunc && <ReloadOutlined className="reload" onClick={reloadFunc} />}
        {btns?.map((item, index) => {
          return item.noRefresh ? (
            <Button className={item.className} key={index}>
              {item.label}
            </Button>
          ) : (
            <Button disabled={item.disabled} loading={item.loading} key={index} className={item.className} onClick={item.clickFunc}>
              {" "}
              {item.label}{" "}
            </Button>
          );
        })}
        {element}
      </div>
    );
  };

  const {
    rowKey,
    loading,
    dataSource,
    columns,
    paginationProps = pagination,
    noPagination,
    reloadData,
    getOpBtns = () => [],
    getJsxElement = () => <></>,
    attrs,
  } = props;

  return (
    <>
      <div className={`${DTablerefix}`}>
        <div className={`${DTablerefix}-box`}>
          <div className={`${DTablerefix}-box-header`}>
            {renderTableInnerOp(reloadData, getOpBtns(), getJsxElement())}
            {renderSearch()}
          </div>
          <Table
            loading={loading}
            rowKey={rowKey}
            dataSource={dataSource}
            columns={columns}
            pagination={!noPagination ? { ...pagination, ...paginationProps } : false}
            {...attrs}
          />
        </div>
      </div>
    </>
  );
};
