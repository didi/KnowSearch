import { getClusterLogicNames } from "api/cluster-api";
import { Button, Form, Popconfirm, Select, Spin, Table } from 'antd';
import { RESOURCE_TYPE_LIST } from "constants/common";
import React from "react";
import "./index.less";

export const LogicCluserSelect: React.FC<any> = (props: {
  value: any;
  isModifyPage: boolean;
  onChange?: (result: any) => any;
}) => {
  const { value } = props;
  const [logicClusterList, setLogicClusterList] = React.useState([]);
  const [clusterType, setType] = React.useState(value?.clusterType || null);
  const [cluster, setCluster] = React.useState(value?.cluster || null);
  const [fetching, setFetching] = React.useState(false);

  React.useEffect(() => {
    setFetching(true);
    if (clusterType) {
      getClusterLogicNames(clusterType).then((res) => {
        if (res) {
          res = res.map((item) => {
            return {
              // 删掉括号和id
              // label: `${item.name}(${item.id})`,
              label: `${item.name}`,
              value: item.id,
              type: item.type,
              level: item.level,
            };
          });
          setLogicClusterList(res);
        }
      }).finally(() => {
        setFetching(false);
      });
    }
  }, [clusterType]);

  // const getLogicClusterList = () => {
  //   if (clusterType) {
  //     return logicClusterList.filter((row) => row.type === clusterType);
  //   }
  //   return [];
  // };

  const onSubmit = (valus: any, type: string) => {
    const obj = {
      cluster,
      clusterType,
      clusterName: '',
      level: null,
    };
    if (type === "type") {
      obj.clusterType = valus;
      setType(valus);
      if (cluster) {
        setCluster(null);
        obj.cluster = null;
      }
    }
    if (type === "name") {
      obj.cluster = valus;
      obj.clusterName = logicClusterList.filter((item) => item.value === valus)[0]?.label;
      obj.level = logicClusterList.filter((item) => item.value === valus)[0]?.level;
      setCluster(valus);
    }
    const { onChange } = props;
    onChange && onChange(obj);
  };

  return (
    <div style={{ display: "flex" }}>
      <Form.Item
        name="type"
        initialValue={clusterType}
        rules={[{ required: false }]}
        style={{ paddingRight: 20, marginBottom: 0 }}
      >
        <Select
          style={{ width: 200 }}
          disabled={props.isModifyPage}
          placeholder="请选择集群类型"
          options={RESOURCE_TYPE_LIST}
          onChange={(e) => onSubmit(e, "type")}
        />
      </Form.Item>
      <Form.Item
        name="clusterName"
        initialValue={cluster}
        rules={[{ required: false }]}
        style={{ marginBottom: 0 }}
      >
        <Select
          showSearch
          placeholder="请选择集群"
          style={{ width: 300 }}
          disabled={props.isModifyPage}
          options={logicClusterList}
          onChange={(e) => onSubmit(e, "name")}
          notFoundContent={fetching ? <Spin size="small" /> : null}
          filterOption={(input, option) =>
            JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >=
            0
          }
        />
      </Form.Item>
    </div>
  );
};
