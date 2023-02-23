import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { INode } from "typesPath/cluster/cluster-types";
import { AppState, UserState } from "store/type";
import { creatCluster, getPackageTypeDescVersion, getPackageGroupConfigsByName } from "api/cluster-api";
import { RESOURCE_TYPE_LIST } from "constants/common";
import Senior from "./senior";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { EditConfigGroup } from "component/config-group";
import { uuid } from "lib/utils";

export interface INodeListObjet {
  masternode: INode[];
  clientnode: INode[];
  datanode: INode[];
  datanodeceph: INode[];
}

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ApplyPhyClusterModal = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [versionList, setVersionList] = useState([]);
  const [esVersion, setEsVersion] = useState("");
  const [configList, setConfigList] = useState([]);
  const [defaultGroupNames, setDefaultGroupNames] = useState(undefined);

  useEffect(() => {
    getVersionList();
  }, []);

  useEffect(() => {
    esVersion && getGroupConfigs();
  }, [esVersion]);

  const getVersionList = async () => {
    let res = await getPackageTypeDescVersion("es-install-package");
    let list = (res || []).map((item) => ({ ...item, title: item.version, value: item.version, id: item.id }));
    setVersionList(list);
  };

  const getGroupConfigs = async () => {
    let version = versionList.filter((item) => item.value === esVersion)[0];
    let res = await getPackageGroupConfigsByName(version.name, version.value, "es-install-package");
    let list = (res || []).map((item, index) => {
      let file = JSON.parse(item.fileConfig) || {};
      let keys = Object.keys(file);
      let fileConfig = keys.map((fileItem, index) => {
        return {
          label: fileItem,
          key: `newFile-${index}`,
          isEdit: false,
          value: file[fileItem],
        };
      });
      return {
        groupName: item.groupName,
        fileConfig,
        systemConfig: [{ value: item.systemConfig, key: `systemConfig-${index}`, label: "systemConfig" }],
        runningConfig: [{ value: item.runningConfig, key: `runningConfig-${index}`, label: "runningConfig" }],
        key: uuid(),
      };
    });
    setConfigList(list);
  };

  const getGroupConfigList = () => {
    let list = [];
    configList.forEach((item) => {
      let fileConfig = {};
      (item.fileConfig || []).forEach((file) => {
        fileConfig[file.label] = file.editor.getValue() || file.value;
      });
      let hosts = [];
      let installDirectoryConfig = {};
      let processNumConfig = {};
      let machineSpec = {};
      (item?.ipList || []).forEach((ip) => {
        let key = ip.ip;
        hosts.push(key);
        installDirectoryConfig[key] = ip.install;
        processNumConfig[key] = 1;
        machineSpec[key] = ip.machineSpec;
      });
      let config = {
        groupName: item.groupName,
        fileConfig: JSON.stringify(fileConfig),
        systemConfig: item.systemConfig?.[0]?.value || "",
        runningConfig: item.runningConfig?.[0]?.value || "",
        hosts: hosts.join(","),
        installDirectoryConfig: JSON.stringify(installDirectoryConfig),
        processNumConfig: JSON.stringify(processNumConfig),
        machineSpec: JSON.stringify(machineSpec),
      };
      list.push(config);
    });
    return list;
  };

  const getFormMap = () => {
    let formMap = [
      [
        {
          key: "phyClusterName",
          label: "集群名称",
          attrs: {
            placeholder: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
          },
          isCustomStyle: true,
          CustomStyle: { marginBottom: 0 },
          rules: [
            {
              required: true,
              message: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
              validator: async (rule: any, value: string) => {
                const reg = /^[a-zA-Z0-9_-]{1,}$/g;
                if (!reg.test(value) || value?.length > 32 || !value) {
                  return Promise.reject("请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符");
                }
                return Promise.resolve();
              },
            },
          ],
        },
        {
          key: "esVersion",
          label: "集群版本",
          type: FormItemType.select,
          options: versionList,
          isCustomStyle: true,
          CustomStyle: { marginBottom: 0 },
          rules: [
            {
              required: true,
              message: "请选择集群版本",
            },
          ],
          attrs: {
            placeholder: "请选择版本",
            onChange: (val) => {
              setEsVersion(val);
            },
          },
        },
      ],
      {
        key: "config",
        type: FormItemType.custom,
        invisible: !esVersion,
        customFormItem: configList.length ? (
          <>
            {configList.map((item, index) => {
              return (
                <EditConfigGroup
                  type="applyCluster"
                  key={item.key}
                  groupKey={item.key}
                  data={item}
                  index={index}
                  configList={configList}
                  setConfigList={setConfigList}
                  machineList={props.params?.machineList || []}
                  defaultGroupNames={defaultGroupNames}
                  setDefaultGroupNames={setDefaultGroupNames}
                />
              );
            })}
          </>
        ) : (
          <EditConfigGroup
            type="applyCluster"
            setConfigList={setConfigList}
            configList={configList}
            machineList={props.params?.machineList || []}
            defaultGroupNames={defaultGroupNames}
            setDefaultGroupNames={setDefaultGroupNames}
          />
        ),
        rules: [
          {
            required: true,
            validator: () => Promise.resolve(),
          },
        ],
      },
      {
        key: "memo",
        type: FormItemType.textArea,
        label: "集群描述",
        rules: [
          {
            required: false,
            message: "请输入0-100个字描述信息",
            validator: async (rule: any, value: string) => {
              if (!value || value?.trim().length <= 100) {
                return Promise.resolve();
              } else {
                return Promise.reject();
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入集群描述",
        },
      },
      {
        key: "senior",
        className: "apply-senior",
        type: FormItemType.custom,
        customFormItem: <Senior type="apply" />,
      },
    ] as IFormItem[];
    return formMap;
  };

  const xFormModalConfig = {
    formMap: getFormMap(),
    visible: true,
    title: "新建集群",
    formData: { resourceType: RESOURCE_TYPE_LIST[1].value },
    width: 800,
    needBtnLoading: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      let packageId = versionList.filter((item) => item.value === esVersion)[0]?.id;
      let groupConfigList = getGroupConfigList();

      let params = {
        clusterType: Number(result?.clusterType) || 2, // 集群类型默认为独立集群
        dataCenter: result.dataCenter || "",
        groupConfigList,
        name: result.phyClusterName,
        packageId,
        password: result.password,
        username: result.usename,
        defaultGroupNames: defaultGroupNames ? [defaultGroupNames] : undefined,
        proxyAddress: result?.proxyAddress,
        kibanaAddress: result.kibanaAddress,
        cerebroAddress: result.cerebroAddress,
        memo: result?.memo,
      };
      let expandData = JSON.stringify(params);
      let ret = await creatCluster({ expandData });
      props.dispatch(actions.setModalId(""));
      showSubmitTaskSuccessModal(ret, props.params?.history);
    },
  };

  const { loading } = props.params;

  const onHandleValuesChange = (value: any, allValues: object) => {
    (window as any).formData = {
      value,
      allValues,
    };
  };

  return (
    <>
      {!loading ? (
        <XFormWrapper onHandleValuesChange={onHandleValuesChange} type={"drawer"} visible={true} {...xFormModalConfig} />
      ) : (
        <span>loading...</span>
      )}
    </>
  );
};

export default connect(mapStateToProps)(ApplyPhyClusterModal);
