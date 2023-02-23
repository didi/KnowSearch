import React, { useEffect, useState } from "react";
import { Table, Input, Pagination, Checkbox } from "antd";
import { getAllIndexList } from "api/cluster-index-api";
import { getIndexAdminData } from "api/index-admin";
import _ from "lodash";
import "./index.less";

export default function SourceTable(props) {
  const [data, setData] = useState([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [name, setName] = useState("");

  const { dataType, cluster, logic, selectedRowKeys, setSelectedRowKeys, selectedRows, setSelectedRows, onChange, sourceCluster } = props;

  useEffect(() => {
    reloadData();
  }, [cluster, logic, page]);

  useEffect(() => {
    let taskList = (selectedRows || []).map((item) => ({ ...item, targetName: dataType === 1 ? item.name : item.index }));
    selectedRows?.length && onChange(taskList);
  }, [selectedRows]);

  const reloadData = async () => {
    setLoading(true);
    if (dataType === 1) {
      let params = {
        page,
        size: 10,
        resourceId: logic,
        name: name ? name : undefined,
      };
      let res = await getAllIndexList(params);
      setData(res?.bizData);
      setTotal(res?.pagination?.total);
    } else {
      let params = {
        page,
        size: 10,
        index: name ? name : undefined,
        cluster,
      };
      let res = await getIndexAdminData(params);
      let data = (res?.bizData || []).map((item) => {
        let indexType = [];
        if (sourceCluster?.sourceTaskList) {
          sourceCluster?.sourceTaskList.forEach((task) => {
            if (task.key === item.key) {
              indexType = task.indexType;
            }
          });
        }
        return {
          ...item,
          indexType,
        };
      });
      setData(data);
      setTotal(res?.pagination?.total);
    }
    setLoading(false);
  };

  const getColumns = () => {
    let columns = [
      {
        title: dataType === 1 ? "源索引模板" : "源索引",
        dataIndex: dataType === 1 ? "name" : "index",
        width: 140,
      },
    ];
    if (dataType === 1) {
      columns.push({
        title: "源索引模板ID",
        dataIndex: "id",
        width: 140,
      });
    } else {
      columns.push({
        title: "Type",
        dataIndex: "indexTypeList",
        width: 140,
        render: (list, record) => {
          if (!list?.length) return "-";
          let options = list.map((item) => ({ value: item, label: item }));
          const onChange = (checkedValues) => {
            data.forEach((item) => {
              let indexType = _.cloneDeep(item.indexType);
              if (item.key === record.key) {
                let value = checkedValues.filter((ele) => {
                  return !indexType?.includes(ele);
                });
                item.indexType = value;
              }
            });
            let newData = _.cloneDeep(data);
            setData(newData);
          };
          return <Checkbox.Group className="check-group" options={options} value={record.indexType} onChange={onChange} />;
        },
      } as any);
    }
    return columns;
  };

  return (
    <>
      <Input
        allowClear
        className="source-search"
        placeholder={dataType === 1 ? "请输入索引模板名称" : "请输入索引名称"}
        onChange={(e) => {
          let val = e.target.value;
          setName(val);
        }}
        onPressEnter={async () => {
          await reloadData();
          if (name && selectedRowKeys) {
            let rows = selectedRows.filter((item) => item?.name?.includes(name));
            let keys = rows.map((item) => (dataType === 1 ? item.id : item.key));
            setSelectedRowKeys(keys);
            setSelectedRows(rows);
          }
        }}
        suffix={
          <span
            className="icon iconfont icontubiao-sousuo"
            onClick={async () => {
              await reloadData();
              if (name && selectedRowKeys) {
                let rows = selectedRows.filter((item) => item?.name.includes(name));
                let keys = rows.map((item) => (dataType === 1 ? item.id : item.key));
                setSelectedRowKeys(keys);
                setSelectedRows(rows);
              }
            }}
          ></span>
        }
      />
      <Table
        loading={loading}
        rowKey={dataType === 1 ? "id" : "key"}
        columns={getColumns()}
        dataSource={data}
        pagination={false}
        rowSelection={{
          type: "checkbox",
          selectedRowKeys,
          onChange: (keys, rows) => {
            setSelectedRowKeys(keys);
            setSelectedRows(rows);
            let taskList = (rows || []).map((item) => ({ ...item, targetName: dataType === 1 ? item.name : item.index }));
            onChange(taskList);
          },
        }}
      ></Table>
      <Pagination className="source-pagination" simple current={page} total={total} onChange={(page) => setPage(page)} />
    </>
  );
}
