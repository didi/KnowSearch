import { Drawer } from "antd";
import React, { memo, useEffect, useState } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import { formColumns, getColumns, queryFormText } from "./config";
import QueryForm from "component/dantd/query-form";
import { DTable } from "component/dantd/dtable";
import * as actions from "actions";
import { getShardDetail } from "api/index-admin";
import "./index.less";

export const ShardList = memo((props) => {
  const dispatch = useDispatch();
  const { modal } = useSelector((state) => ({ modal: (state as any).modal }), shallowEqual);
  const { params = {} } = modal;
  const clusterName = params.cluster || "";
  const indexName = params.index || "";

  const [isLoading, setIsLoading] = useState(false);
  const [data, setData] = useState([]);
  const [queryFormObject, setQueryFormObject] = useState({});
  const [pageSize, setPageSize] = useState(10);

  const handleSubmit = (result) => {
    for (let key in result) {
      result[key] = result[key] || "";
    }
    setQueryFormObject(result);
  };

  const mergeCell = (data) => {
    let start = 0;
    let end = 0;
    // 用来判断元素是否在一页数据中重复出现
    let obj = {};
    const newData = data.map((item, index) => {
      // 根据 pageSize 大小判断合并单元格数，避免出现，分页多合并单元格的情况
      // 每达到一页数据，更新初始值
      if (index % pageSize === 0) {
        start = end;
        end += pageSize;
        obj = {};
      }
      // 截取这一页的数据
      const pageData = data.slice(start, end);

      const newItem = {
        // 判断是否是当前页第一次出现，合并相同单元格，否则不展示
        rowSpan: !obj[item.node] ? pageData?.filter((i) => item.node === i.node).length : 0,
        ...item,
      };
      // 记录已经出现过的单元格
      obj[item.node] = true;

      return newItem;
    });

    return newData;
  };

  const getData = () => {
    // 查询项的key 要与 数据源的key  对应
    const keys = Object.keys(queryFormObject);

    if (!queryFormObject || !keys.length) return mergeCell(data);

    const filterData = data.filter((d) => {
      let flag = true;
      keys.forEach((item) => {
        if (queryFormObject[item] && d[item] && !d[item].includes(queryFormObject[item])) {
          flag = false;
        }
      });
      return flag;
    });
    return mergeCell(filterData);
  };

  const getAsyncData = async () => {
    setIsLoading(true);
    try {
      const res = await getShardDetail(clusterName, indexName);
      if (!res) {
        return;
      }
      // 对数组中相同 ip 的元素进行聚合，相加
      for (let i = 0; i < res.length; i++) {
        let temp = res[i];
        temp.totalDocs = temp.docs || 0;
        temp.totalStore = temp.storeInByte || 0;
        temp.shardCount = 1;
        temp.shardCells = [
          {
            node: temp.node,
            shard: temp.shard,
            docs: temp.docs,
            storeInByte: temp.storeInByte,
          },
        ];
        for (let j = i + 1; j < res.length; j++) {
          if (temp.ip === res[j].ip) {
            temp.totalDocs += res[j].docs || 0;
            temp.totalStore += res[j].storeInByte || 0;
            temp.shardCount++;
            temp.shardCells.push({
              node: res[j].node,
              shard: res[j].shard,
              docs: res[j].docs,
              storeInByte: res[j].storeInByte,
            });
            res.splice(j, 1);
            j--;
          }
        }
      }
      const data = [];
      let i = 0;
      res.forEach((item) => {
        data.push(...item.shardCells.map((cell) => ({ ...item, ...cell, key: i++ })));
      });
      setData(data);
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  const reloadData = () => {
    getAsyncData();
  };

  const pageChange = (pagination) => {
    if (pagination.pageSize === pageSize) {
      return;
    }
    setPageSize(pagination.pageSize);
  };

  useEffect(() => {
    getAsyncData();
  }, []);

  return (
    <Drawer
      title="Shard 列表"
      placement="right"
      className="index-container"
      width={`calc(100% - 190px)`}
      visible={true}
      onClose={() => {
        dispatch(actions.setDrawerId(""));
      }}
    >
      <div className="shard-list-header">
        <QueryForm
          onReset={handleSubmit}
          onSearch={handleSubmit}
          onChange={() => {}}
          columns={formColumns}
          initialValues={{}}
          isResetClearAll
          {...queryFormText}
          defaultCollapse
          showCollapseButton={false}
        />
      </div>
      <div className="table-content">
        <DTable
          loading={isLoading}
          rowKey="key"
          dataSource={getData()}
          attrs={{
            bordered: true,
            onChange: pageChange,
          }}
          paginationProps={{
            position: "bottomRight",
            showQuickJumper: true,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total) => `共 ${total} 条`,
          }}
          reloadData={reloadData}
          columns={getColumns()}
        />
      </div>
    </Drawer>
  );
});
