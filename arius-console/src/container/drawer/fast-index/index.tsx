import React, { useEffect, useState, useRef } from "react";
import { Button, Steps, Drawer, Tooltip } from "antd";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { DATA_TYPE_LIST } from "constants/common";
import { getSourceClusterList, getTargetClusterList, getAddress } from "api/cluster-api";
import { queryFastIndex } from "api/fastindex-api";
import SourceCluster from "./source-cluster";
import TargetCluster from "./target-cluster";
import OtherInfo from "./other-info";
import moment from "moment";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import "./index.less";

const { Step } = Steps;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
});

const FastIndex = (props) => {
  const [current, setCurrent] = useState(0);
  const [dataType, setDataType] = useState(1);
  const [sourceCluster, setSourceCluster] = useState({ sourceClusterType: 1 } as any);
  const [targetCluster, setTargetCluster] = useState({ targetClusterType: 1 } as any);
  const [loading, setLoading] = useState(false);
  const [sourceClusterList, setSourceClusterList] = useState([]);
  const [targetClusterList, setTargetClusterList] = useState([]);
  const [address, setAddress] = useState(undefined);

  const dataTypeRef = useRef(null);
  const sourceClusterRef = useRef(null);
  const targetClusterRef = useRef(null);
  const otherInfoRef = useRef(null);

  useEffect(() => {
    getData();
  }, []);

  useEffect(() => {
    setSourceCluster({ sourceClusterType: 1 });
    setTargetCluster({ targetClusterType: 1 });
  }, [dataType]);

  const getData = async () => {
    let source = await getSourceClusterList();
    let target = await getTargetClusterList();
    let sourceList = [];
    let sourceClusterList = [];
    (source || []).forEach((item) => {
      // 过滤重复集群
      if (!sourceList.includes(item.cluster)) {
        sourceClusterList.push({ label: item.cluster, value: item.cluster, version: item.esVersion });
        sourceList.push(item.cluster);
      }
    });
    let targetList = [];
    let targetClusterList = [];
    (target || []).forEach((item) => {
      // 过滤重复集群
      if (!targetList.includes(item.cluster)) {
        targetClusterList.push({ label: item.cluster, value: item.cluster, version: item.esVersion });
        targetList.push(item.cluster);
      }
    });
    setSourceClusterList(sourceClusterList);
    setTargetClusterList(targetClusterList);
  };

  const _getAddress = async (name) => {
    let res = await getAddress(name);
    res && setAddress(res);
  };

  const steps = [
    {
      title: "资源类型",
      content: "dataType",
    },
    {
      title: "源集群",
      content: "sourceCluster",
    },
    {
      title: "目标集群",
      content: "targetCluster",
    },
    {
      title: "其他信息",
      content: "other",
    },
  ];

  const dataTypeFormMap = () => {
    return [
      {
        key: "dataType",
        label: (
          <div className="dataType">
            <span className="title">资源类型</span>
            <Tooltip title={"提供基于索引模板和索引两种类型的数据迁移方式，请根据所需进行选择"}>
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: DATA_TYPE_LIST,
        attrs: { placeholder: "请选择" },
        rules: [{ required: true, message: "请选择资源类型" }],
      },
    ] as IFormItem[];
  };

  const handleCancel = () => props.setDrawerId("");

  const onSubmit = async (value) => {
    setLoading(true);
    let taskList = [];
    if (targetCluster?.relationType === 2) {
      // one to one
      (targetCluster?.targetTaskList || []).forEach((item) => {
        let task = {
          mappings: item.mapping,
          settings: item.setting,
          sourceTemplateId: item.id,
          targetName: item.targetName,
          sourceIndexList: dataType === 2 ? [{ indexTypes: item?.indexType || [], resourceNames: item.index }] : undefined,
        };
        taskList.push(task);
      });
    } else {
      // all to one, 只有资源类型为索引才有 all to one
      let sourceIndexList = [];
      (targetCluster?.targetTaskList || []).forEach((item) => {
        let sourceItem = { indexTypes: item?.indexType || [], resourceNames: item.index };
        sourceIndexList.push(sourceItem);
      });
      let task = {
        mappings: targetCluster?.targetTaskList?.[0]?.mapping,
        settings: targetCluster?.targetTaskList?.[0]?.setting,
        sourceTemplateId: targetCluster?.targetTaskList?.[0]?.id,
        targetName: targetCluster?.targetTaskList?.[0]?.targetName,
        sourceIndexList,
      };
      taskList.push(task);
    }
    const params = {
      dataType,
      relationType: targetCluster.relationType,
      sourceCluster: sourceCluster.sourceCluster?.value,
      sourceClusterType: sourceCluster.sourceClusterType,
      sourceLogicClusterId: sourceCluster.sourceLogic?.value,
      sourceProjectId: sourceCluster.sourceProject?.value,
      targetCluster: targetCluster.targetCluster?.value,
      targetClusterType: targetCluster.targetClusterType,
      targetIndexType: targetCluster?.targetCluster?.version?.[0] >= 7 ? undefined : value?.targetIndexType,
      targetLogicClusterId: targetCluster.targetLogic?.value,
      targetProjectId: targetCluster.targetProject?.value,
      taskList,
      taskReadRate: value.taskReadRate && Number(value.taskReadRate),
      taskStartTime: value.taskStartTime ? moment(value.taskStartTime).valueOf() : undefined,
      taskSubmitAddress: value.taskSubmitAddress,
      transfer: value.transfer ? value.transfer === "1" : undefined,
      writeType: value.writeType,
    };
    try {
      let res = await queryFastIndex(params);
      showSubmitTaskSuccessModal(res, props.params?.history);
      props.setDrawerId("");
    } finally {
      setLoading(false);
    }
  };

  const clickPrev = () => {
    setCurrent(current - 1);
  };

  const clickNext = () => {
    if (current === 0) {
      dataTypeRef?.current!.validateFields().then((values) => {
        setCurrent(1);
        setDataType(values.dataType);
      });
    } else if (current === 1) {
      sourceClusterRef?.current!.validateFields().then((values) => {
        setCurrent(2);
        setSourceCluster(values);
        _getAddress(values?.sourceCluster?.value);
        if (targetCluster?.targetTaskList) {
          let target = JSON.parse(JSON.stringify(targetCluster));
          target.targetTaskList = values?.sourceTaskList;
          setTargetCluster(target);
        }
      });
    } else if (current === 2) {
      targetClusterRef?.current!.validateFields().then((values) => {
        setCurrent(3);
        setTargetCluster(values);
      });
    } else if (current === 3) {
      otherInfoRef?.current!.validateFields().then((values) => {
        onSubmit(values);
      });
    }
  };

  const renderBtn = () => {
    return [
      current < 3 ? (
        <Button key="next" className="next" type="primary" onClick={clickNext}>
          下一步
        </Button>
      ) : (
        ""
      ),
      current === 3 ? (
        <Button loading={loading} key="submit" className="submit" type="primary" onClick={clickNext}>
          完成
        </Button>
      ) : (
        ""
      ),
      current > 0 ? (
        <Button key="back" className="back" onClick={clickPrev}>
          上一步
        </Button>
      ) : (
        ""
      ),
      <Button key="cancel" onClick={handleCancel}>
        取消
      </Button>,
    ];
  };

  const renderContent = () => {
    if (steps[current].content === "dataType") {
      return <XFormComponent formData={{ dataType }} formMap={dataTypeFormMap()} wrappedComponentRef={dataTypeRef} layout="vertical" />;
    } else if (steps[current].content === "sourceCluster") {
      return (
        <SourceCluster
          sourceClusterRef={sourceClusterRef}
          sourceCluster={sourceCluster}
          dataType={dataType}
          clusterList={sourceClusterList}
        />
      );
    } else if (steps[current].content === "targetCluster") {
      return (
        <TargetCluster
          targetClusterRef={targetClusterRef}
          targetCluster={targetCluster}
          dataType={dataType}
          clusterList={targetClusterList}
          targetTaskList={targetCluster?.targetTaskList || sourceCluster?.sourceTaskList}
        />
      );
    } else if (steps[current].content === "other") {
      return <OtherInfo otherInfoRef={otherInfoRef} dataType={dataType} targetCluster={targetCluster} address={address} />;
    } else {
      return steps[current].content;
    }
  };

  return (
    <div>
      <Drawer
        className="fast-index-drawer"
        visible={true}
        title={
          <div className="title">
            <span className="text">数据迁移</span>
            <Tooltip title={"数据迁移仅支持支持同版本迁移，且必须满足低版本向高版本迁移，版本限制6.6.1 和7.6.0。"}>
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        }
        width={750}
        onClose={handleCancel}
        maskClosable={false}
      >
        <Steps current={current} style={{ margin: "24px 0 24px 0" }}>
          {steps.map((item) => (
            <Step key={item.title} title={item.title} />
          ))}
        </Steps>
        <div className={`content ${current === 0 || current === 3 ? "small-content" : ""}`}>
          <div className="steps-content">{renderContent()}</div>
          <div className="footer-content">{renderBtn()}</div>
        </div>
      </Drawer>
    </div>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(FastIndex);
