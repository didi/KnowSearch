import React from 'react';
import '@ant-design/compatible/assets/index.css';
import {
  Input,
  Button,
  Table,
  Tooltip
} from 'antd';
import './index.less';
import { pagination } from 'constants/table';
import { ReloadOutlined } from '@ant-design/icons';
import { openSourceTip } from 'constants/status-map';


export interface ITableBtn {
  clickFunc?: (...args: any[]) => void;
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
  isOpenUp?: boolean;
  history?: {[key: string]: any} 
}
interface ISearchInput {
  placeholder?: string;
  submit: (params?: any) => any;
  text?: string;
}

interface IDTableProps {
  paginationProps?: object;
  rowKey: string;
  columns: object[];
  dataSource: object[];
  loading?: boolean;
  reloadData: (params?: object) => any;
  getOpBtns?: (params?: object) => ITableBtn[];
  renderInnerOperation?: (params?: object) => JSX.Element;
  tableHeaderSearchInput?: ISearchInput;
  attrs?: object;
}

export const DTable = (props: IDTableProps) => {

  const renderSearch = () => {
    if (!props?.tableHeaderSearchInput) return;
    const Search = Input.Search;
    const { placeholder = null, submit, text } = props?.tableHeaderSearchInput;
    return (
      <div>
        <span>{text}</span>
        <Search
          placeholder={placeholder || '请输入关键字'}
          onSearch={submit}
          style={{ width: 200 }}
        />
      </div>
    );
  }

  const renderTableInnerOp = (reloadFunc: any, btns?: ITableBtn[], element?: JSX.Element) => {
    return (
      <div className="table-op-btn">
        <ReloadOutlined className="reload" onClick={reloadFunc} />
        {btns?.map((item, index) => {
          if (item.isOpenUp) {
            return (<Tooltip key={index} title={openSourceTip}>
              <Button disabled={true}> {item.label} </Button>
          </Tooltip>)
          }
          return item.noRefresh ?
          <Button className={item.className} key={index}>{item.label}</Button> :
          <Button disabled={item.disabled} loading={item.loading} key={index} className={item.className} onClick={item.clickFunc}> {item.label} </Button>
        })}
        {
          element
        }
      </div>
    );
  }

  const {
    rowKey,
    loading,
    dataSource,
    columns,
    paginationProps = pagination,
    reloadData,
    getOpBtns = () => null,
    renderInnerOperation = () => null,
    attrs
  } = props;

  return (
    <>
      <div className='d-table'>
        <div className='d-table-box'>
          <div className='d-table-box-header'>
            {renderTableInnerOp(reloadData, getOpBtns(), renderInnerOperation())}
            {renderSearch()}
          </div>
          <Table
            loading={loading}
            rowKey={rowKey}
            dataSource={dataSource}
            columns={columns}
            pagination={paginationProps}
            {...attrs}
          />
        </div>
      </div>
    </>
  );
}