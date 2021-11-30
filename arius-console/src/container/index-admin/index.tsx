import React, { memo, useCallback, useEffect, useState } from 'react'
import { useSelector, shallowEqual, useDispatch } from 'react-redux';
import { withRouter } from 'react-router-dom';
import { Tooltip, Button } from 'antd';
import { DTable } from 'component/dantd/dtable';
import { RenderTitle } from 'component/render-title';
import { getColumns, formColumns, queryFormText, cherryList } from './config';
import QueryForm from 'component/dantd/query-form';
import * as actions from "actions";
import './index.less';
import { getIndexAdminData } from 'api/index-admin';
import { getClusterNameList } from 'api/cluster-kanban';
import { getCheckedList, setCheckedList } from 'api/search-query';
import FilterColumns from 'component/filterColumns';

export const IndexAdmin = withRouter((props: { history: any }) => {
  const department: string = localStorage.getItem("current-project");

  const dispatch = useDispatch();
  const setModalId = (modalId: string, params?: any, cb?: Function) => {
    dispatch(actions.setModalId(modalId, params, cb))
  }
  const setDrawerId = (drawerId: string, params?: any, cb?: Function) => {
    dispatch(actions.setDrawerId(drawerId, params, cb))
  }

  const [data, setData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  // 控制分页的总数
  const [total, setTotal] = useState(0);
  // 后端数据的真实数量
  const [realTotal, setRealTotal] = useState(0);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [delList, setDelList] = useState([]);
  const [queryFromColumn, setQueryFormColumns] = useState(formColumns);
  const [current, setCurrent] = useState(1);
  const [queryData, setQueryData] = useState({
    clusterPhyName: "",
    index: "",
    health: "",
    from: 0,
    size: 10,
    orderByDesc: false,
    sortTerm: "",
  });
  const [columns, setColumns] = useState([])

  const getAsyncData = async (num = 1) => {
    if(num >= 3) {
      setTotal(0);
      setData([]);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    try {
      const res = await getIndexAdminData(queryData);
      const { bizData, pagination: { total } } = res;
      if(!bizData) {
        // 递归请求三次，后端获取数据可能不成功
        getAsyncData(num + 1);
        return;
      }
      // 控制分页，后端超过一万条后，不支持查询
      const tenThousand = 10 * 1000;
      setTotal(total > tenThousand ? tenThousand : total);
      // 存储后端的数据真实数量
      setRealTotal(total);
      setData(bizData);
      setIsLoading(false);
    } catch (error) {
      console.log(error);
      setTotal(0);
      setData([]);
      setIsLoading(false);
    } 
  }

  const getAsyncClusterName = async () => {
    const clusterNameList = await getClusterNameList();
    if (clusterNameList && clusterNameList.length > 0) {
      formColumns[1].options = clusterNameList.map(item => ({
        title: item,
        value: item,
      }))
      setQueryFormColumns(formColumns);
    }
  };

  const handleSubmit = (result) => {
    for(let key in result) {
      result[key] = result[key] || "";
    }
    // 查询可能不足当前页码，页码重置为第一页，
    setCurrent(1);
    setQueryData({
      ...queryData,
      from: 0,
      size: 10,
      ...result
    });
  };

  const pageChange = (pagination, _filters, sorter) => {
    const { current, pageSize } = pagination;
    const { order, field } = sorter;
    setCurrent(current);
    const page = {
      from: (current - 1) * pageSize,
      size: pageSize,
      orderByDesc: order !== 'ascend',
      sortTerm: field
    };
    setQueryData({
      ...queryData,
      ...page,
    })
  };

  const modalReloadData = (del?: boolean) => {
    // 删除操作，
    if(del) {
      setQueryData({
        ...queryData,
        from: 0,
        size: 10,
      });
      setDelList([]);
      setSelectedRowKeys([]);
      setCurrent(1)
      return;
    }
    getAsyncData();
  }

  const onSelectChange = (selectedRowKeys, records) => {
    setSelectedRowKeys(selectedRowKeys);
    setDelList(records.map(item => ({
      "clusterPhyName": item.cluster,
      "index": item.index
    })))
  };

  useEffect(() => {
    getAsyncClusterName();
  }, [department]);

  useEffect(() => {
     getAsyncData();
  }, [queryData, department]);

  const renderTitleContent = () => {
    return {
      title: "索引管理",
      content: null,
    };
  };

  const getOpBtns = useCallback(() => {
    return (
      <>
        <FilterColumns 
          columns={getColumns(setModalId, setDrawerId, modalReloadData)}
          setColumns={setColumns}
          checkArr={cherryList}
          getCheckFn={getCheckList}
          saveCheckFn={saveCheckFn}
        />
        <Tooltip title={(selectedRowKeys && selectedRowKeys.length) ? '' : '需要选中后批量删除'}>
          <Button onClick={() => {
            setModalId("deleteIndex", {
              delList: delList,
              title: `确定批量删除所选索引吗?`
            }, modalReloadData);
          }} style={{ marginRight: 0 }} type={"primary"} disabled={(selectedRowKeys && selectedRowKeys.length) ? false : true}>
            批量删除
          </Button>
        </Tooltip>
      </>
    )
  }, [selectedRowKeys]);

  const getCheckList = async () => {
    const checkList: string[] = await getCheckedList('indexSearch');
    return checkList;
  }

  const saveCheckFn = (list: string[]) => {
    setCheckedList('indexSearch', list)
  }

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <QueryForm
          onReset={handleSubmit}
          onSearch={handleSubmit}
          onChange={() => { }}
          columns={queryFromColumn}
          initialValues={{}}
          isResetClearAll
          {...queryFormText}
          defaultCollapse
        />
      </div>
      <div className="table-content">
        <DTable
          loading={isLoading}
          rowKey="key"
          dataSource={data}
          attrs={{
            onChange: pageChange,
            rowSelection: {
              selectedRowKeys,
              onChange: onSelectChange,
            },
            border: true,
            scroll: {
              x: 1550 - ((12 - columns.length) * 120),
            }
          }}
          paginationProps={{
            position: "bottomRight",
            showQuickJumper: true,
            total: total,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (t) => `共 ${realTotal} 条`,
            current: current
          }}
          reloadData={() => {getAsyncData()}}
          columns={columns}
          renderInnerOperation={getOpBtns}
        />
      </div>
    </>
  )
});
