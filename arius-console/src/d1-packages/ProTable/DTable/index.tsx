import React from 'react';
import { Input, Button, Table, ConfigProvider, Tooltip } from 'antd';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import './index.less';
// 表格国际化无效问题手动加
import antdZhCN from 'antd/es/locale/zh_CN';

export const DTablerefix = 'd-table';

export const pagination = {
  // position: 'bottomRight',
  showQuickJumper: true,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
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
  searchTrigger?: string;
}

export interface IDTableProps {
  showHeader?: boolean;
  paginationProps?: any;
  noPagination?: boolean;
  rowKey: string;
  columns: any[];
  dataSource: any[];
  loading?: boolean;
  reloadData?: (params?: any) => any;
  getOpBtns?: (params?: any) => ITableBtn[];
  getJsxElement?: (params?: any) => JSX.Element;
  tableHeaderSearchInput?: ISearchInput;
  attrs?: any;
  searchInputRightBtns?: ITableBtn[];
}

export const DTable = (props: IDTableProps) => {
  const renderSearch = () => {
    if (!props?.tableHeaderSearchInput) return;
    const { searchInputRightBtns = [] } = props;
    const { placeholder = null, submit, width, searchTrigger } = props?.tableHeaderSearchInput;
    return (
      <div className={`${DTablerefix}-box-header-search`}>
        <div className={`${DTablerefix}-box-header-search-custom`}>
          {searchInputRightBtns.map((item, index) => {
            if (item?.type === 'custom') {
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
                {' '}
                {item.label}{' '}
              </Button>
            );
          })}
        </div>
        <div>
          <Input
            placeholder={placeholder || '请输入关键字'}
            style={{ width: width || 200 }}
            onChange={(e) => searchTrigger === 'change' && submit(e.target.value)}
            onPressEnter={(e: any) => searchTrigger === 'enter' && submit(e.target.value)}
            onBlur={(e: any) => searchTrigger === 'blur' && submit(e.target.value)}
            suffix={<SearchOutlined style={{ color: '#ccc' }} />}
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
              {' '}
              {item.label}{' '}
            </Button>
          );
        })}
        {element}
      </div>
    );
  };

  const renderColumns = (columns: any[]) => {
    return columns.map((currentItem: any) => {
      return {
        ...currentItem,
        showSorterTooltip: false,
        onCell: () => {
          return {
            style: {
              maxWidth: currentItem.width,
              overflow: 'hidden',
              whiteSpace: 'nowrap',
              textOverflow: 'ellipsis',
              cursor: 'pointer',
            },
          };
        },
        render: (...args) => {
          const value = args[0];
          const renderData = currentItem.render
            ? currentItem.render(...args)
            : value === '' || value === null || value === undefined
            ? '-'
            : value;
          const notTooltip = currentItem.render || renderData === '-';
          return !notTooltip ? (
            <Tooltip placement="bottomLeft" title={renderData}>
              <span>{renderData}</span>
            </Tooltip>
          ) : (
            renderData
          );
        },
      };
    });
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
    showHeader = true,
  } = props;

  return (
    <>
      <ConfigProvider locale={antdZhCN}>
        <div className={`${DTablerefix}`}>
          <div className={`${DTablerefix}-box`}>
            {showHeader && (
              <div className={`${DTablerefix}-box-header`}>
                {renderTableInnerOp(reloadData, getOpBtns(), getJsxElement())}
                {renderSearch()}
              </div>
            )}
            <Table
              loading={loading}
              rowKey={rowKey}
              dataSource={dataSource}
              columns={renderColumns(columns)}
              pagination={!noPagination ? { ...pagination, ...paginationProps } : false}
              {...attrs}
            />
          </div>
        </div>
      </ConfigProvider>
    </>
  );
};
