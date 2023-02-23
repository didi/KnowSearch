import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { getRollbackConfig, updatePhyClusterConfig } from "api/cluster-api";
import { getGatewayRollbackConfig, editGatewayConfig } from "api/gateway-manage";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import Url from "lib/url-parser";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const RollbackConfig = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const { record } = params;
  const [versionList, setVersionList] = useState([]);

  useEffect(() => {
    getVersionList();
  }, []);

  const getVersionList = async () => {
    let res: any;
    if (params.cluster === "gateway") {
      res = await getGatewayRollbackConfig(+Url().search.id, record.id);
    } else {
      res = await getRollbackConfig(+Url().search.physicsClusterId, record.id);
    }
    let list = (res || []).map((item) => ({ ...item, label: item.version, value: item.version }));
    setVersionList(list);
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "version",
        label: "回滚版本",
        type: FormItemType.select,
        options: versionList,
        attrs: {
          placeholder: "请选择回滚版本",
        },
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        rules: [{ required: true, message: "请选择回滚版本" }],
      },
    ] as IFormItem[],
    visible: true,
    title: "回滚",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      let version = result.version;
      let config = versionList.filter((item) => item.value === version)[0];
      const params = {
        componentId: record.componentId,
        groupConfigList: [
          {
            id: record.id,
            groupName: config.groupName,
            systemConfig: config.systemConfig,
            runningConfig: config.runningConfig,
            fileConfig: config.fileConfig,
            installDirectoryConfig: config.installDirectoryConfig,
            processNumConfig: config.processNumConfig,
            hosts: config.hosts,
            version,
          },
        ],
      };
      let expandData = { expandData: JSON.stringify(params) };
      let res = props.params.cluster === "gateway" ? await editGatewayConfig(expandData) : await updatePhyClusterConfig(expandData);
      dispatch(actions.setModalId(""));
      showSubmitTaskSuccessModal(res, props.params.history);
      cb && cb();
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
