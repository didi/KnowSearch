import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import { Form, Select, Table } from "antd";
import { getPhyClusterRegionList, logicClusterNodesList } from "api/op-cluster-region-api";
import { filterOption } from "lib/utils";
import "./index.less";

const mapStateToProps = (state: any) => ({
  clusterList: state.region.phyClusterList,
  type: state.region.type,
});

// 永不重名的key
let count = 0;

export const RelevanceRegion: React.FC<any> = connect(mapStateToProps)(
  (props: { dataInfo: any; onChange?: (result: any) => any; type: string; clusterList: { value: string; label: string }[] }) => {
    const { dataInfo } = props;
    const [tableData, setTableData] = useState([]);
    const [regionList, setRegionList] = useState([]);
    const [regionId, setRegionId] = useState(null);
    const [nodeList, setNodeList] = useState([]);

    const [form] = Form.useForm();

    const onFormChange = (changedValues, allValues) => {
      if (changedValues.name) {
        //name变化时候清空regionlist，清空region值
        setRegionList([]);
        setRegionId(null);
        form.setFieldsValue({ region: undefined });
        //name表单变化时候调用接口
        getPhyClusterRegionList(changedValues.name, props.type, { hostNum: dataInfo.dataNodeNu, machineSpec: dataInfo?.dataNodeSpec }).then(
          (res) => {
            let resArray = [];
            res.forEach((item) => {
              let cold = false;
              if (item?.config) {
                let config = JSON.parse(item?.config);
                cold = config?.cold;
              }
              if (!cold) {
                resArray.push({
                  label: `${item.name}`,
                  value: JSON.stringify({
                    config: item.config,
                    logicClusterIds: item.logicClusterIds,
                    phyClusterName: item.clusterName,
                    id: item.id,
                    name: item.name,
                  }),
                });
              }
            });
            setRegionList([...resArray]);
          }
        );
      }

      //处理表单数据并通过onchange将数据透传给父组件表单
      if (!allValues.name || !allValues.region) return;
      if (Array.isArray(allValues.region)) {
        allValues.regionObject = allValues.region?.map((item) => JSON.parse(item));
      } else if (allValues.region.constructor === String) {
        const transObj = JSON.parse(allValues.region);
        allValues.regionObject = [{ ...transObj }];
      }
      let dataSource = [...tableData];
      allValues.regionObject.forEach((element) => {
        allValues.racks = element.racks;
        allValues.regionId = element.id;
        element.phyClusterName = allValues.name;
        dataSource.push({ key: count++, ...{ ...allValues, region: [JSON.stringify(element)] } });
      });
      const { onChange } = props;
      onChange && onChange(dataSource);
    };

    return (
      <>
        <Form className="relevance-region" layout="inline" form={form} name="control-hooks1" onValuesChange={onFormChange}>
          <Form.Item name="name" rules={[{ required: true, message: "请选择物理集群" }]}>
            <Select
              showSearch
              className="select-cluster"
              placeholder="请选择物理集群"
              options={props.clusterList}
              disabled={tableData && tableData.length ? true : false}
              filterOption={filterOption}
            />
          </Form.Item>
          <Form.Item name="region" rules={[{ required: true, message: "请选择region" }]}>
            <Select
              showSearch
              className="select-region"
              filterOption={filterOption}
              placeholder="请选择region"
              options={regionList}
              onSelect={async (val: string) => {
                let id = JSON.parse(val)?.id;
                let data = await logicClusterNodesList(id);
                setRegionId(id);
                setNodeList(data);
              }}
            ></Select>
          </Form.Item>
          {regionId && (
            <Form.Item className="nodelist">
              <Table
                rowKey="id"
                columns={[
                  {
                    title: "主机IP",
                    dataIndex: "ip",
                  },
                  {
                    title: "节点规格",
                    dataIndex: "machineSpec",
                  },
                ]}
                dataSource={nodeList}
                pagination={false}
              ></Table>
            </Form.Item>
          )}
        </Form>
      </>
    );
  }
);
