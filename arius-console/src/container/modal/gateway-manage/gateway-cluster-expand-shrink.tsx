import React, { useEffect, useRef, useState } from "react";
import { Drawer, Button } from "antd";
import { FormItemType, XForm as XFormComponent } from "component/x-form";
import { ExpandShrinkTable } from "container/custom-form/expand-shrink-table";
import { CLUSTER_INDECREASE_TYPE } from "constants/status-map";
import { getNodeSpecification } from "api/cluster-api";
import { getGatewayConfigDetail, expandGateway, shrinkGateway } from "api/gateway-manage";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { XNotification } from "component/x-notification";
import { RenderText } from "container/custom-form";
import { Dispatch } from "redux";
import { connect } from "react-redux";
import * as actions from "actions";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (drawerId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(drawerId, params, cb)),
});

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ExpandShrinkCluster = (props) => {
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [options, setOptions] = useState([]);
  const [isExpand, setIsExpand] = useState(false);
  const [originData, setOriginData] = useState([]);
  const [configData, setConfigData] = useState([]);

  const formRef = useRef(null);

  useEffect(() => {
    getData();
  }, []);

  const getData = async () => {
    let res = await getGatewayConfigDetail(props.params.id);
    let data = (res || []).map((item) => {
      let id = item.id;
      let groupName = item.groupName;
      let list = (item.componentHosts || []).map((ele) => ({
        id: ele.host,
        host: ele.host,
        machineSpec: ele.machineSpec,
        processNum: ele.processNum,
        install: JSON.parse(item.installDirectoryConfig)[ele.host],
      }));
      return { ...item, id, groupName, list };
    });
    setConfigData(data);
    setOriginData(data);
    _getNodeSpecification();
  };

  const _getNodeSpecification = async () => {
    let list = await getNodeSpecification();
    let options = (list || []).map((item) => ({ label: item, value: item }));
    setOptions(options);
  };

  const getShrinkData = () => {
    let groupConfigList = [];
    originData.forEach((item, index) => {
      let originList = (item?.list || []).map((ele) => ele.host);
      let newList = (configData[index]?.list || []).map((ele) => ele.host);
      let hosts = [];
      originList.forEach((ele) => {
        if (!newList.includes(ele)) {
          hosts.push(ele);
        }
      });
      if (hosts.length) {
        let list = {
          id: item?.id,
          dependConfigComponentId: props?.params?.componentId,
          groupName: item?.groupName,
          systemConfig: item?.systemConfig,
          runningConfig: item?.runningConfig,
          fileConfig: item?.fileConfig,
          installDirectoryConfig: item?.installDirectoryConfig,
          processNumConfig: item?.processNumConfig,
          hosts: hosts?.join(","),
          version: item?.version,
          machineSpec: item?.machineSpec,
        };
        if (!groupConfigList.length) {
          groupConfigList.push(list);
        } else {
          let hasNode = false;
          groupConfigList.forEach((ele) => {
            if (ele?.groupName === item?.groupName) {
              let installDirectoryConfig = JSON.parse(ele?.installDirectoryConfig);
              installDirectoryConfig[ele?.host] = ele?.install;
              ele.installDirectoryConfig = JSON.stringify(installDirectoryConfig);
              let processNumConfig = JSON.parse(ele?.processNumConfig);
              processNumConfig[ele?.host] = Number(ele?.processNum);
              ele.processNumConfig = JSON.stringify(processNumConfig);
              let machineSpec = JSON.parse(ele?.machineSpec);
              machineSpec[ele?.host] = ele?.machineSpec;
              ele.machineSpec = JSON.stringify(machineSpec);
              ele.hosts = `${ele.hosts},${ele?.host}`;
              hasNode = true;
            }
          });
          if (!hasNode) {
            groupConfigList.push(list);
          }
        }
      }
    });
    return { componentId: props.params.componentId, groupConfigList };
  };

  const getExpandData = (expandData) => {
    let groupConfigList = [];
    expandData.forEach((ele) => {
      let config = ele?.config;
      let groupConfig = {
        id: config?.id,
        dependConfigComponentId: props?.params?.componentId,
        groupName: config?.groupName,
        systemConfig: config?.systemConfig,
        runningConfig: config?.runningConfig,
        version: config?.version,
        fileConfig: config?.fileConfig,
        installDirectoryConfig: JSON.stringify({ [ele?.host]: ele?.install }),
        processNumConfig: JSON.stringify({ [ele?.host]: 1 }),
        machineSpec: JSON.stringify({ [ele?.host]: ele?.machineSpec }),
        hosts: ele?.host,
      };
      if (!groupConfigList.length) {
        groupConfigList.push(groupConfig);
      } else {
        let hasNode = false;
        groupConfigList.forEach((item) => {
          if (item?.groupName === config?.groupName) {
            let installDirectoryConfig = JSON.parse(item?.installDirectoryConfig);
            installDirectoryConfig[ele?.host] = ele?.install;
            item.installDirectoryConfig = JSON.stringify(installDirectoryConfig);
            let processNumConfig = JSON.parse(item?.processNumConfig);
            processNumConfig[ele?.host] = 1;
            item.processNumConfig = JSON.stringify(processNumConfig);
            let machineSpec = JSON.parse(item?.machineSpec);
            machineSpec[ele?.host] = ele?.machineSpec;
            item.machineSpec = JSON.stringify(machineSpec);
            item.hosts = `${item.hosts},${ele?.host}`;
            hasNode = true;
          }
        });
        if (!hasNode) {
          groupConfigList.push(groupConfig);
        }
      }
    });
    return {
      componentId: props.params.componentId,
      groupConfigList,
    };
  };

  const handleOk = async () => {
    setConfirmLoading(true);
    if (!isExpand) {
      // 缩容
      let changeData = false;
      for (let i = 0; i < originData.length; i++) {
        if (originData[i]?.list?.length !== configData[i]?.list?.length) {
          changeData = true;
          break;
        }
      }
      if (!changeData) {
        XNotification({ type: "error", message: "请选择缩容节点" });
        setConfirmLoading(false);
        return;
      }
      try {
        let params = getShrinkData();
        let res = await shrinkGateway({ expandData: JSON.stringify(params) });
        showSubmitTaskSuccessModal(res, props.params?.history);
        props.setDrawerId("");
      } finally {
        setConfirmLoading(false);
      }
    } else {
      // 扩容
      let expandData = [];
      (configData || []).forEach((element) => {
        (element.list || []).forEach((item: any) => {
          if (item?.type === "expand" && item.host && item.machineSpec && item.install) {
            expandData.push({ ...item, config: element });
          }
        });
      });
      if (!expandData.length) {
        XNotification({ type: "error", message: "请添加扩容节点" });
        setConfirmLoading(false);
        return;
      }
      let params = getExpandData(expandData);
      try {
        let res = await expandGateway({ expandData: JSON.stringify(params) });
        showSubmitTaskSuccessModal(res, props.params?.history);
        props.setDrawerId("");
      } finally {
        setConfirmLoading(false);
      }
    }
  };

  const commonFormMap = () => {
    let formMap = [
      [
        {
          key: "clusterName",
          label: "集群名称",
          type: FormItemType.text,
          className: "cluster-name",
          customFormItem: <RenderText text={props.params.clusterName} />,
        },
        {
          key: "version",
          label: "集群版本",
          type: FormItemType.text,
          className: "cluster-version",
          customFormItem: <RenderText text={props.params.version} />,
        },
      ],
      {
        key: "type",
        label: "扩缩容",
        type: FormItemType.select,
        className: "expand-shrink-type",
        defaultValue: isExpand ? 2 : 3,
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              setConfigData(originData);
              setIsExpand(value === 2 ? true : false);
              return Promise.resolve();
            },
          },
        ],
        options: CLUSTER_INDECREASE_TYPE,
      },
      {
        key: "list",
        type: FormItemType.custom,
        customFormItem: configData.map((item, index) => {
          return (
            <ExpandShrinkTable
              key={item.id}
              index={index}
              data={item}
              isExpand={isExpand}
              options={options}
              onChange={(data, index) => {
                let config = JSON.parse(JSON.stringify(configData));
                config[index].list = data;
                setConfigData(config);
              }}
            />
          );
        }),
      },
    ] as any;
    return formMap;
  };

  const renderBtn = () => {
    return [
      <Button key="cancel" onClick={() => props.setDrawerId("")}>
        取消
      </Button>,
      <Button key="submit" className="submit" type="primary" loading={confirmLoading} onClick={handleOk}>
        确定
      </Button>,
      <span className="divide" key="divide"></span>,
    ];
  };

  return (
    <>
      <Drawer
        closable={true}
        className="expand-shrink"
        visible={true}
        title="集群扩缩容"
        width={800}
        destroyOnClose={true}
        onClose={() => props.setDrawerId("")}
        maskClosable={false}
      >
        <div className="expand-shrink-container">
          <div className="steps-content">
            <XFormComponent formData={{}} formMap={commonFormMap()} layout="vertical" wrappedComponentRef={formRef} />
          </div>
          <div className="footer-content">{renderBtn()}</div>
        </div>
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(ExpandShrinkCluster);
