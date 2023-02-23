import React, { useState, useEffect, createRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { RenderText } from "container/custom-form";
import { updateGatewayVersion, getGatewayLastVersion } from "api/gateway-manage";
import { getPackageTypeDescVersion } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const GatewayVersion = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const {
    params: { type, record },
    dispatch,
    cb,
  } = props;

  const [versionList, setVersionList] = useState([]);

  const $formRef: any = createRef();

  const title = type === "rollback" ? "回滚" : "升级";

  useEffect(() => {
    getVersionList();
  }, []);

  const getVersionList = async () => {
    const res = await getPackageTypeDescVersion("gateway-install-package");
    const list = (res || []).filter((data, indx, self) => {
      return self.findIndex((ele) => ele.esVersion === data.esVersion) !== indx;
    });
    const packageList = list.map((ele) => {
      return { label: ele.version, value: ele.id };
    });
    setVersionList(packageList);
    if (packageList.length && type === "rollback") {
      // 默认回滚上个版本
      const lastVersion = await getGatewayLastVersion(record.id);
      const nowIndex = packageList.findIndex((item) => item.value === lastVersion);
      $formRef?.current?.form.setFieldsValue({ newVersion: packageList[nowIndex > 0 ? nowIndex - 1 : 0]?.value });
    }
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "gatewayClusterName",
        label: "Gateway集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={record.clusterName} />,
      },
      {
        key: "version",
        label: "现有软件版本",
        type: FormItemType.text,
        customFormItem: <RenderText text={record.version} />,
      },
      {
        key: "newVersion",
        label: `${title}至软件版本`,
        type: FormItemType.select,
        options: versionList,
        attrs: {
          placeholder: "请选择",
        },
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        rules: [{ required: true, message: "请选择软件版本" }],
      },
    ] as IFormItem[],
    visible: true,
    title: `Gateway集群${title}`,
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      const params = {
        componentId: record.componentId,
        packageId: result.newVersion,
        upgradeType: type === "rollback" ? 1 : 0,
      };
      let expandData = JSON.stringify(params);
      let res = await updateGatewayVersion({ expandData });
      dispatch(actions.setModalId(""));
      showSubmitTaskSuccessModal(res, record?.history);
    },
  };

  return (
    <>
      <XFormWrapper ref={$formRef} {...xFormModalConfig} />
    </>
  );
});
