import React, { useEffect, useState } from "react";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { Select, Form } from "antd";
import { SOURCE_CLUSTER_TYPE } from "constants/common";
import SourceTable from "./source-table";
import { getLogicNameList } from "api/cluster-api";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});

const SourceCluster = (props) => {
  const { dataType, sourceCluster, sourceClusterRef, clusterList } = props;

  const [cluster, setCluster] = useState(sourceCluster?.sourceCluster || ({} as any));
  const [logic, setLogic] = useState(sourceCluster?.sourceLogic || ({} as any));
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [logicList, setLogicList] = useState([]);
  const [projectList, setProjectList] = useState([]);

  useEffect(() => {
    if (sourceCluster?.sourceTaskList) {
      let rows = sourceCluster?.sourceTaskList;
      let keys = rows.map((item) => (dataType === 1 ? item.id : item.key));
      setSelectedRowKeys(keys);
      setSelectedRows(rows);
    }
    return () => {
      setSelectedRowKeys([]);
      setSelectedRows([]);
      setCluster({});
      setLogic({});
      sourceClusterRef?.current?.setFieldsValue({ sourceCluster: undefined, sourceTaskList: undefined });
    };
  }, [sourceCluster]);

  useEffect(() => {
    _getLogicNameList();
  }, [cluster]);

  useEffect(() => {
    getProjectList();
  }, [logic]);

  const _getLogicNameList = async () => {
    if (dataType === 1 && cluster?.value) {
      let list = await getLogicNameList(cluster?.value);
      let ids = [];
      let logicList = [];
      (list || []).forEach((item) => {
        if (!ids.includes(item?.id)) {
          ids.push(item.id);
          logicList.push({ label: item.name, value: item.id, projectid: [item.projectId] });
        } else {
          logicList.forEach((logic) => {
            if (logic?.value === item?.id) {
              !logic?.projectid?.includes(item.projectId) && logic?.projectid?.push(item?.projectId);
            }
          });
        }
      });
      setLogicList(logicList);
    }
  };

  const getProjectList = () => {
    if (logic?.value) {
      let list = props.app.projectList;
      let currentLogic: any = logicList.filter((item) => item.value === logic.value);
      let projectList = [];
      list.forEach((item) => {
        if (currentLogic?.[0]?.projectid?.includes(item.id)) {
          let project = { label: item?.projectName, value: item?.id };
          projectList.push(project);
        }
      });
      setProjectList(projectList);
    }
  };

  const sourceClusterFormMap = () => {
    let formMap = [
      {
        key: "sourceClusterType",
        label: "源集群类型",
        type: FormItemType.select,
        options: SOURCE_CLUSTER_TYPE,
        attrs: { placeholder: "请选择源集群类型" },
        rules: [{ required: true }],
      },
      {
        key: "sourceCluster",
        label: "源集群",
        type: FormItemType.custom,
        className: "select-source-cluster",
        customFormItem:
          dataType === 1 ? (
            <div className="template-source-cluster">
              <Form.Item name="sourceCluster" rules={[{ required: true, message: "请选择物理集群" }]}>
                <Select placeholder="请选择物理集群" options={clusterList} labelInValue></Select>
              </Form.Item>
              <Form.Item
                name="sourceLogic"
                className="select-logic"
                rules={[{ required: true, message: "请选择逻辑集群" }]}
                initialValue={sourceCluster?.sourceLogic}
              >
                <Select
                  placeholder="请选择逻辑集群"
                  options={logicList}
                  labelInValue
                  onChange={(logic: any) => {
                    let logicItem = { label: logic?.label, value: logic?.value };
                    logicItem.label = `${logicItem?.label?.startsWith("逻辑集群：") ? "" : "逻辑集群："}${logic?.label}`;
                    sourceClusterRef.current.setFieldsValue({ sourceLogic: logicItem });
                    setLogic(logic);
                    if (selectedRowKeys) {
                      setSelectedRowKeys([]);
                      setSelectedRows([]);
                    }
                  }}
                ></Select>
              </Form.Item>
              <Form.Item
                name="sourceProject"
                rules={[{ required: true, message: "请选择应用" }]}
                initialValue={sourceCluster?.sourceProject}
              >
                <Select
                  placeholder="请选择应用"
                  options={projectList}
                  labelInValue
                  onChange={(project: any) => {
                    let projectItem = { label: project?.label, value: project?.value };
                    projectItem.label = `${projectItem?.label?.startsWith("应用：") ? "" : "应用："}${project?.label}`;
                    sourceClusterRef.current.setFieldsValue({ sourceProject: projectItem });
                  }}
                ></Select>
              </Form.Item>
            </div>
          ) : (
            <Select placeholder="请选择源集群" options={clusterList} labelInValue></Select>
          ),
        rules: [
          {
            required: true,
            validator: async (rule, cluster) => {
              if (!cluster?.value) {
                return dataType === 1 ? Promise.reject("") : Promise.reject("请选择源集群");
              }
              let clusterItem = { label: cluster?.value, value: cluster?.value };
              if (dataType === 1) {
                clusterItem.label = `${cluster?.label?.startsWith("物理集群：") ? "" : "物理集群："}${cluster?.label}`;
                sourceClusterRef.current.setFieldsValue({ sourceCluster: clusterItem });
              }
              setCluster(cluster);
              return Promise.resolve("");
            },
          },
        ],
      },
      {
        key: "sourceTaskList",
        label: "资源列表",
        invisible: dataType === 1 ? !logic?.value : !cluster?.value,
        type: FormItemType.custom,
        className: "source-table-container",
        customFormItem: (
          <SourceTable
            dataType={dataType}
            cluster={cluster?.value}
            logic={logic?.value}
            selectedRowKeys={selectedRowKeys}
            setSelectedRowKeys={setSelectedRowKeys}
            selectedRows={selectedRows}
            setSelectedRows={setSelectedRows}
            sourceCluster={sourceCluster}
          />
        ),
        rules: [{ required: true, message: "请选择资源列表" }],
      },
    ] as IFormItem[];
    return formMap;
  };

  return (
    <XFormComponent formData={sourceCluster} formMap={sourceClusterFormMap()} wrappedComponentRef={sourceClusterRef} layout="vertical" />
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(SourceCluster);
