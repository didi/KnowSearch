import React, { useEffect, useRef, useState } from "react";
import { Drawer, Button } from "antd";
import { connect } from "react-redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { ExpandShrinkList } from "container/custom-form";
import "./index.less";
import * as actions from "actions";
import { CLUSTER_INDECREASE_TYPE } from "constants/status-map";
import { clusterExpand, clusterShrink, getNodeSpecification } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { Dispatch } from "redux";
import { XNotification } from "component/x-notification";

const labelList = [
  {
    label: "集群名称：",
    key: "cluster",
  },
  {
    label: "现有ES版本：",
    key: "esVersion",
  },
];

interface IExpandShrinkParams {
  title: string;
  id: number;
  businessKey: string;
  taskType: number;
  status: string;
  expandData: string;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ExpandShrinkCluster = (props) => {
  const [clientNodes, setClientNodes] = useState([]);
  const [dataNodes, setDataNodes] = useState([]);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [options, setOptions] = useState([]);
  const [formMap, setFormMap] = useState([] as IFormItem[]);
  const [isExpand, setIsExpand] = useState(false);

  const formRef = useRef(null);

  useEffect(() => {
    _getNodeSpecification();
  }, []);

  useEffect(() => {
    getFormMap();
  }, [isExpand]);

  const _getNodeSpecification = async () => {
    let list = await getNodeSpecification();
    let options = (list || []).map((item) => ({ label: item, value: item }));
    setOptions(options);
  };

  const filterExpandData = (result) => {
    let res = [];
    let keys = Object.keys(result);
    let filterKeys = keys.filter((item) => result[item] && (item.startsWith("ip") || item.startsWith("machineSpec")));
    if (!filterKeys.length) {
      XNotification({ type: "error", message: "请对数据进行更改后再提交" });
      setConfirmLoading(false);
      return;
    }
    filterKeys.forEach((item) => {
      let arr = item.split("&");
      let data = {
        id: arr[2],
        role: arr[1],
      };
      arr[0] === "ip" ? (data["hostname"] = result[item]) : (data["machineSpec"] = result[item]);
      let index = -1;
      for (let i = 0; i < res.length; i++) {
        if (res[i].id === data.id) {
          res[i] = { ...res[i], ...data };
          index = i;
          break;
        }
      }
      index === -1 && res.push(data);
    });
    for (let i = 0; i < res.length; i++) {
      if (!res[i].hostname || !res[i].machineSpec) {
        XNotification({ type: "error", message: "请填写完整" });
        setConfirmLoading(false);
        return;
      }
      for (let j = i + 1; j < res.length; j++) {
        if (res[i].hostname === res[j].hostname && res[i].role === res[j].role) {
          XNotification({ type: "error", message: "存在重复IP，请修改后再提交" });
          setConfirmLoading(false);
          return;
        }
      }
    }
    return res;
  };

  const handleOk = () => {
    setConfirmLoading(true);
    formRef.current!.validateFields().then(async (result) => {
      const { id, cluster, esClusterRoleVOS } = props.params;
      let clusterRoleHosts = [];
      if (result?.type === 2) {
        // 扩容
        clusterRoleHosts = filterExpandData(result);
        if (!clusterRoleHosts) {
          setConfirmLoading(false);
          return;
        }
        clusterRoleHosts.forEach((item) => {
          delete item.id;
        });
      } else {
        // 缩容
        if (!clientNodes.length && !dataNodes.length) {
          XNotification({ type: "error", message: "请对数据进行更改后再提交" });
          setConfirmLoading(false);
          return;
        }
        dataNodes.forEach((item) => {
          clusterRoleHosts.push({
            role: "datanode",
            hostname: item.hostname,
            machineSpec: item.machineSpec,
          });
        });
        clientNodes.forEach((item) => {
          clusterRoleHosts.push({
            role: "clientnode",
            hostname: item.hostname,
            machineSpec: item.machineSpec,
          });
        });
      }

      let filterNode = (esClusterRoleVOS || []).filter((item) => item.role !== "masternode");
      let node = [];
      filterNode.forEach((item) => {
        let list = item.esClusterRoleHostVO.map((ele) => ({ ...ele, role: item.role }));
        node = [...node, ...list];
      });
      let originClusterRoleHosts = node.map((item) => {
        return { role: item.role, hostname: item.hostname, machineSpec: item.machineSpec };
      });

      const contentObj = {
        type: 4,
        operationType: result?.type,
        phyClusterId: id,
        phyClusterName: cluster,
        clusterRoleHosts,
        originClusterRoleHosts,
      };
      let expandData = JSON.stringify(contentObj);
      let ret = {} as IExpandShrinkParams;
      try {
        if (result?.type === 2) {
          ret = await clusterExpand({ expandData });
        } else {
          ret = await clusterShrink({ expandData });
        }
        props.setModalId("");
        showSubmitTaskSuccessModal(ret, props.params?.history);
      } finally {
        setConfirmLoading(false);
      }
    });
  };

  const handleCancel = () => {
    props.setModalId("");
  };

  const getFormMap = () => {
    const { esClusterRoleVOS = [] } = props.params;
    const expandShrinkArr = [
      {
        key: "type",
        label: "扩缩容",
        type: FormItemType.select,
        defaultValue: 3,
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              if (value === 2) {
                setIsExpand(true);
              } else {
                setIsExpand(false);
              }
              setClientNodes([]);
              setDataNodes([]);
              return Promise.resolve();
            },
          },
        ],
        options: CLUSTER_INDECREASE_TYPE,
        attrs: {
          style: { width: "50%" },
        },
      },
      {
        key: "masternode",
        label: "master列表",
        type: FormItemType.custom,
        customFormItem: <ExpandShrinkList type="masternode" data={esClusterRoleVOS} isExpand={isExpand} />,
      },
      {
        key: "clientnode",
        label: "client列表",
        type: FormItemType.custom,
        customFormItem: (
          <ExpandShrinkList
            type="clientnode"
            form={formRef?.current}
            data={esClusterRoleVOS}
            isExpand={isExpand}
            options={options}
            clientNodes={clientNodes}
            onShrink={(val) => {
              setClientNodes(val);
            }}
          />
        ),
      },
      {
        key: "datanode",
        label: "data列表",
        type: FormItemType.custom,
        customFormItem: (
          <ExpandShrinkList
            type="datanode"
            form={formRef?.current}
            data={esClusterRoleVOS}
            isExpand={isExpand}
            options={options}
            dataNodes={dataNodes}
            onShrink={(val) => {
              setDataNodes(val);
            }}
          />
        ),
      },
    ] as IFormItem[];
    setFormMap(expandShrinkArr);
  };

  return (
    <>
      <Drawer
        closable={true}
        className="expand-shrink"
        visible={true}
        title="集群扩缩容"
        width={660}
        destroyOnClose={true}
        onClose={handleCancel}
        maskClosable={false}
        footer={
          <div className="footer-btn">
            <Button style={{ marginRight: 10 }} loading={confirmLoading} type="primary" onClick={handleOk}>
              确定
            </Button>
            <Button onClick={handleCancel}>取消</Button>
          </div>
        }
      >
        <div className="upgrade-cluster-box">
          {labelList.map((item, index) => (
            <div key={item.label + index} className="upgrade-cluster-box-item">
              <span className="label">{item.label}</span>
              <span>{props.params[item.key]}</span>
            </div>
          ))}
        </div>
        <div>
          <XFormComponent formData={{}} formMap={formMap} wrappedComponentRef={formRef} layout={"vertical"} />
        </div>
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(ExpandShrinkCluster);
