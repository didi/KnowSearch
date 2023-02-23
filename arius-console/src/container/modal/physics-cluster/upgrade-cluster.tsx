import React, { useEffect, useRef, useState } from "react";
import { Drawer, Button } from "antd";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { getPackageTypeDescVersion } from "api/cluster-api";
import { clusterUpgrade } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { RenderText } from "container/custom-form";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  user: state.user,
  app: state.app,
});

export const UpgradeCluster = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any; setDrawerId: any }) => {
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [options, setOptions] = useState([]);

  const formRef = useRef(null);

  useEffect(() => {
    getOptions();
  }, []);

  const getOptions = async () => {
    let data = await getPackageTypeDescVersion("es-install-package");
    const list = data.filter((data, indx, self) => {
      return self.findIndex((ele) => ele.esVersion === data.esVersion) !== indx;
    });
    const options = list.map((ele) => {
      return { label: ele.version, value: ele.id };
    });
    setOptions(options);
  };

  const handleOk = () => {
    setConfirmLoading(true);
    formRef.current!.validateFields().then(async (result) => {
      let params = {
        componentId: props.params.componentId,
        packageId: result.packageId,
      };
      let expandData = JSON.stringify(params);
      try {
        let ret = await clusterUpgrade({ expandData });
        props.dispatch(actions.setDrawerId(""));
        showSubmitTaskSuccessModal(ret, props.params?.history);
      } finally {
        setConfirmLoading(false);
      }
    });
  };

  const formMapItem = [
    {
      key: "tips",
      label: "",
      type: FormItemType.custom,
      customFormItem: (
        <div className="warning-container" style={{ margin: -24 }}>
          <span className="icon iconfont iconbiaogejieshi"></span>
          <span>请确保版本间通讯协议及存储协议相同</span>
        </div>
      ),
    },
    {
      key: "cluster",
      label: "集群名称",
      type: FormItemType.text,
      className: "cluster-name",
      customFormItem: <RenderText text={props.params.cluster} />,
    },
    {
      key: "esVersion",
      label: "现有ES版本",
      type: FormItemType.text,
      className: "cluster-version",
      customFormItem: <RenderText text={props.params.esVersion} />,
    },
    {
      key: "packageId",
      label: "升级至ES版本",
      type: FormItemType.select,
      options,
      rules: [
        {
          required: true,
        },
      ],
      attrs: {
        style: { width: "50%" },
      },
    },
  ] as IFormItem[];

  return (
    <Drawer
      destroyOnClose={true}
      onClose={() => props.dispatch(actions.setDrawerId(""))}
      maskClosable={false}
      closable={true}
      visible={true}
      title="集群升级"
      width={660}
      footer={
        <div className="footer-btn">
          <Button style={{ marginRight: 10 }} loading={confirmLoading} type="primary" onClick={handleOk}>
            确定
          </Button>
          <Button onClick={() => props.dispatch(actions.setDrawerId(""))}>取消</Button>
        </div>
      }
    >
      <div className="upgrade-cluster-form">
        <XFormComponent formData={{}} formMap={formMapItem} wrappedComponentRef={formRef} layout={"vertical"} />
      </div>
    </Drawer>
  );
});
