import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { RenderText } from "container/custom-form";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { AppState, UserState } from "store/type";
import { clusterRestart, getPhyConfigFile } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { Tooltip, Checkbox } from "antd";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
  app: state.app,
});

const RestartClusterModal = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: IOpPhysicsClusterDetail }) => {
  const [nodeList, setNodeList] = useState([]);
  const [checkedNodes, setCheckedNodes] = useState({});

  useEffect(() => {
    getNodeList();
  }, []);

  const getNodeList = async () => {
    let res = await getPhyConfigFile(props.params.id);
    let list = (res || []).map((item) => {
      return {
        id: item?.id,
        name: item?.groupName,
        nodes: item?.componentHosts,
      };
    });
    setNodeList(list);
  };

  const onCheckboxChange = (checkedValues, item) => {
    const newCheckedNodes = { ...checkedNodes };
    newCheckedNodes[item] = checkedValues;
    setCheckedNodes(newCheckedNodes);
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "集群名称",
        type: FormItemType.text,
        isCustomStyle: true,
        CustomStyle: { marginTop: 24 },
        customFormItem: <RenderText text={props.params.cluster} />,
      },
      {
        key: "node",
        label: "重启节点选择",
        type: FormItemType.custom,
        customFormItem: (
          <>
            {nodeList.map((item, index) => (
              <div key={index} className="restart-cluster-node">
                <div className="name">配置名称: {item?.name}</div>
                <Checkbox.Group onChange={(checkedValues) => onCheckboxChange(checkedValues, item.name)}>
                  {(item?.nodes || []).map((node) => (
                    <div>
                      <Checkbox value={node.host}>
                        <span className={`node ${node.status === 1 ? "fail" : ""}`}>{node?.host}</span>
                      </Checkbox>
                      {node.status === 1 && (
                        <Tooltip title="宕机节点">
                          <span className="icon iconfont iconinfo"></span>
                        </Tooltip>
                      )}
                    </div>
                  ))}
                </Checkbox.Group>
              </div>
            ))}
          </>
        ),
      },
    ] as IFormItem[],
    visible: true,
    title: "集群重启",
    formData: props.params || {},
    isWaitting: true,
    type: "drawer",
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: async (result: any) => {
      let keys = Object.keys(checkedNodes);
      let groupConfigList = [];
      keys.forEach((item) => {
        if (checkedNodes[item].length) {
          let hosts = checkedNodes[item].join(",");
          groupConfigList.push({ groupName: item, hosts });
        }
      });
      let params = {
        componentId: props.params.componentId,
        groupConfigList,
      };
      let expandData = JSON.stringify(params);
      let ret = await clusterRestart({ expandData });
      props.dispatch(actions.setDrawerId(""));
      showSubmitTaskSuccessModal(ret, props.params?.history);
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(RestartClusterModal);
