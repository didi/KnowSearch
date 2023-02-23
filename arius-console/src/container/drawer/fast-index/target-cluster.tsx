import React, { useState, useEffect } from "react";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { SOURCE_CLUSTER_TYPE, TEMPLATE_RELATION_TYPE, INDEX_RELATION_TYPE } from "constants/common";
import { Tooltip, Select, Form } from "antd";
import TargetTable from "./target-table";
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

const TargetCluster = (props) => {
  const { dataType, targetCluster, targetClusterRef, clusterList, targetTaskList } = props;

  const [relationType, setRelationType] = useState(2);
  const [logicList, setLogicList] = useState([]);
  const [projectList, setProjectList] = useState([]);
  const [cluster, setCluster] = useState({} as any);
  const [logic, setLogic] = useState({} as any);

  useEffect(() => {
    if (targetCluster?.targetTaskList) {
      setCluster(targetCluster?.targetCluster);
      setLogic(targetCluster?.targetLogic);
      setRelationType(targetCluster?.relationType);
      targetClusterRef?.current?.setFieldsValue({ ...targetCluster });
    }
    return () => {
      setCluster({});
      setLogic({});
      targetClusterRef?.current?.setFieldsValue({ targetCluster: undefined, targetTaskList: undefined });
    };
  }, [targetCluster]);

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

  const targetClusterFormMap = () => {
    let formMap = [
      {
        key: "targetClusterType",
        label: "目标集群类型",
        type: FormItemType.select,
        options: SOURCE_CLUSTER_TYPE,
        attrs: { placeholder: "请选择目标集群类型" },
        rules: [{ required: true }],
      },
      {
        key: "targetCluster",
        label: "目标集群",
        type: FormItemType.custom,
        customFormItem:
          dataType === 1 ? (
            <div className="template-source-cluster">
              <Form.Item name="targetCluster" rules={[{ required: true, message: "请选择物理集群" }]}>
                <Select placeholder="请选择物理集群" options={clusterList} labelInValue></Select>
              </Form.Item>
              <Form.Item
                name="targetLogic"
                className="select-logic"
                rules={[{ required: true, message: "请选择逻辑集群" }]}
                initialValue={targetCluster?.sourceLogic}
              >
                <Select
                  placeholder="请选择逻辑集群"
                  options={logicList}
                  labelInValue
                  onChange={(logic: any) => {
                    let logicItem = logicList.filter((item) => item.value === logic.value)?.[0];
                    logicItem.label = `逻辑集群：${logic?.label}`;
                    targetClusterRef.current.setFieldsValue({ targetLogic: logicItem });
                    setLogic(logic);
                  }}
                ></Select>
              </Form.Item>
              <Form.Item
                name="targetProject"
                rules={[{ required: true, message: "请选择应用" }]}
                initialValue={targetCluster?.sourceProject}
              >
                <Select
                  placeholder="请选择应用"
                  options={projectList}
                  labelInValue
                  onChange={(project: any) => {
                    let projectItem = { label: project?.value, value: project?.value };
                    projectItem.label = `应用：${project?.label}`;
                    targetClusterRef.current.setFieldsValue({ targetProject: projectItem });
                  }}
                ></Select>
              </Form.Item>
            </div>
          ) : (
            <Select placeholder="请选择目标集群" options={clusterList} labelInValue></Select>
          ),
        rules: [
          {
            required: true,
            validator: async (rule, cluster) => {
              if (!cluster?.value) {
                return dataType === 1 ? Promise.reject("") : Promise.reject("请选择目标集群");
              }
              let clusterItem = JSON.parse(JSON.stringify(clusterList.filter((item) => item.value === cluster.value)?.[0]));
              if (dataType === 1) {
                clusterItem.label = `物理集群：${cluster?.value}`;
              }
              targetClusterRef.current.setFieldsValue({ targetCluster: clusterItem });
              setCluster(cluster);
              return Promise.resolve("");
            },
          },
        ],
      },
      {
        key: "relationType",
        label: (
          <div className="relationType">
            <span className="title">对应关系</span>
            <Tooltip
              title={
                <div>
                  {dataType === 2 && <div>对应关系分为以下两种：</div>}
                  {dataType === 2 && (
                    <div>all to one：只需选择一份目标集群资源，如：选择一条索引，源集群数据会全部迁移到这一条索引中。</div>
                  )}
                  <div>one to one：目标集群资源与源集群资源一一对应，支持自定义修改。</div>
                </div>
              }
            >
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: dataType === 1 ? TEMPLATE_RELATION_TYPE : INDEX_RELATION_TYPE,
        attrs: {
          placeholder: "请选择对应关系",
          onChange: (val) => {
            setRelationType(val);
          },
        },
        rules: [{ required: true }],
      },
      {
        key: "targetTaskList",
        label: "资源列表",
        invisible: dataType === 1 ? !logic?.value : false,
        type: FormItemType.custom,
        className: "source-table-container",
        customFormItem: <TargetTable dataType={dataType} targetTaskList={targetTaskList} relationType={relationType} logic={logic} />,
        rules: [{ required: true }],
      },
    ] as IFormItem[];
    return formMap;
  };

  return (
    <XFormComponent
      formData={{ relationType: 2, ...targetCluster }}
      formMap={targetClusterFormMap()}
      wrappedComponentRef={targetClusterRef}
      layout="vertical"
    />
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(TargetCluster);
