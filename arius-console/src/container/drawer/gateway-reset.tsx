import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem } from "component/x-form";
import { XFormWrapper } from "component/x-form-wrapper";
import { getGatewayConfigDetail, resetGateway } from "api/gateway-manage";
import { Tooltip, Checkbox } from "antd";
import { RenderText } from "container/custom-form";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (drawerId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(drawerId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  params: state.modal.params,
  cb: state.modal.cb,
});

const GatewayReset = ({ app, cb, params, setModalId, setDrawerId }) => {
  const [nodeList, setNodeList] = useState([]);
  const [checkedNodes, setCheckedNodes] = useState({});

  useEffect(() => {
    getNodeList();
  }, []);

  const getNodeList = async () => {
    let res = await getGatewayConfigDetail(params.id);
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

  const xFormModalConfig = () => {
    let formMap = [
      {
        key: "gatewayClusterName",
        label: "Gateway集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.clusterName} />,
        isCustomStyle: true,
        CustomStyle: { marginTop: 24 },
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
    ] as IFormItem[];
    return {
      formMap,
      type: "drawer",
      title: "Gateway集群重启",
      width: 480,
      visible: true,
      needBtnLoading: true,
      onCancel: () => setDrawerId(""),
      onSubmit: async (result) => {
        let keys = Object.keys(checkedNodes);
        let groupConfigList = [];
        keys.forEach((item) => {
          if (checkedNodes[item].length) {
            let hosts = checkedNodes[item].join(",");
            groupConfigList.push({ groupName: item, hosts });
          }
        });
        let data = {
          componentId: params.componentId,
          groupConfigList,
        };
        let expandData = JSON.stringify(data);
        let ret = await resetGateway({ expandData });
        setDrawerId("");
        showSubmitTaskSuccessModal(ret, params?.history);
        cb();
      },
    };
  };

  return <XFormWrapper {...xFormModalConfig()} />;
};

export default connect(mapStateToProps, mapDispatchToProps)(GatewayReset);
