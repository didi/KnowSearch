import { getClusterLogicNames } from "api/cluster-api";
import { Form, Select, Spin } from "antd";
import { RESOURCE_TYPE_LIST } from "constants/common";
import { filterOption } from "lib/utils";
import React from "react";
import "./index.less";

export const LogicCluserSelect: React.FC<any> = (props: {
  value: any;
  isModifyPage: boolean;
  $form: any;
  onChange?: (result: any) => any;
}) => {
  const { value } = props;
  const [logicClusterList, setLogicClusterList] = React.useState([]);
  const [clusterType, setType] = React.useState(value?.clusterType || null);
  const [cluster, setCluster] = React.useState(value?.cluster || null);
  const [fetching, setFetching] = React.useState(false);

  React.useEffect(() => {
    if (clusterType) {
      setFetching(true);
      getClusterLogicNames(clusterType)
        .then((res) => {
          if (res) {
            res = res.map((item) => {
              return {
                label: item.name,
                value: item.id,
                type: item.type,
                level: item.level,
                dataCenter: item.dataCenter,
              };
            });
            setLogicClusterList(res);
          }
        })
        .finally(() => {
          setFetching(false);
        });
    }
  }, [clusterType]);

  const onSubmit = (valus: any, type: string) => {
    const obj = {
      cluster,
      clusterType,
      clusterName: "",
      level: null,
      dataCenter: null,
    };
    if (type === "type") {
      obj.clusterType = valus;
      setType(valus);
      if (cluster) {
        setCluster(null);
        obj.cluster = null;
        props.$form?.current.setFieldsValue({ clusterName: null });
      }
    }
    if (type === "name") {
      const clusterInfo = logicClusterList.filter((item) => item.value === valus);
      obj.cluster = valus;
      obj.clusterName = clusterInfo[0]?.label;
      obj.level = clusterInfo[0]?.level;
      obj.dataCenter = clusterInfo[0]?.dataCenter;
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
        className="no-margin-bottom"
        style={{ paddingRight: 20 }}
      >
        <Select
          showSearch
          disabled={props.isModifyPage}
          placeholder="请选择集群类型"
          options={RESOURCE_TYPE_LIST}
          onChange={(e) => onSubmit(e, "type")}
          filterOption={filterOption}
        />
      </Form.Item>
      <Form.Item name="clusterName" initialValue={cluster} rules={[{ required: false }]} className="no-margin-bottom">
        <Select
          showSearch
          placeholder="请选择集群"
          disabled={props.isModifyPage}
          options={logicClusterList.map((item) => ({ label: item.label, value: item.value }))}
          onChange={(e) => onSubmit(e, "name")}
          notFoundContent={fetching ? <Spin size="small" /> : null}
          filterOption={filterOption}
        />
      </Form.Item>
    </div>
  );
};
