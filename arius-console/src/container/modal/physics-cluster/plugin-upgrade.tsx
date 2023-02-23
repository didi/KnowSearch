import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { opUpgradePlug, upgradePlug } from "api/plug-api";
import { getHigherPacksgeVersion } from "api/cluster-api";
import Url from "lib/url-parser";
import { showSubmitTaskSuccessModal } from "container/custom-component";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const UpgradePlugin = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const [versionList, setVersionList] = useState([]);

  const getVersionList = () => {
    getHigherPacksgeVersion(params.packageId, params.version).then((res) => {
      let list = (res || []).map((item) => ({ value: item.id, label: item.version }));
      setVersionList(list);
    });
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "versionId",
        label: "插件版本",
        type: FormItemType.select,
        options: versionList,
        attrs: {
          placeholder: "请选择插件版本",
        },
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        rules: [{ required: true, message: "请选择插件版本" }],
      },
    ] as IFormItem[],
    visible: true,
    title: "插件升级",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const data = {
        dependComponentId: +Url().search.componentId,
        packageId: result.versionId,
        componentId: params.componentId,
      };
      let expandData = { expandData: JSON.stringify(data) };
      return opUpgradePlug(expandData).then((res) => {
        showSubmitTaskSuccessModal(res, props.params?.history);
        dispatch(actions.setModalId(""));
        cb && cb();
      });
    },
  };

  useEffect(() => {
    getVersionList();
  }, []);

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
