import React, { useState, useEffect, useRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { AppState, UserState } from "store/type";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { EditConfigGroup } from "component/config-group";
import { getPackageVersion, getPackageGroupConfigsByName, installClusterPlug, getPhyConfigFile } from "api/cluster-api";
import { uuid } from "lib/utils";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const InstallClusterPlugin = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [nameList, setNameList] = useState([]);
  const [plugin, setPlugin] = useState({} as any);
  const [configList, setConfigList] = useState([]);
  const [ipDetail, setIpDetail] = useState({} as any);
  const [version, setVersion] = useState({} as any);

  const formRef = useRef(null);

  useEffect(() => {
    getPluginName();
    _getPhyPlugins();
  }, []);

  useEffect(() => {
    version?.value && version?.type === "platform" ? getConfigDetail() : _getPhyPlugins();
  }, [version]);

  const _getPhyPlugins = async () => {
    let ipDetail = {} as any;
    let configList = [];
    let res = await getPhyConfigFile(props.params.id);
    (res || []).forEach((item, index) => {
      let hosts = item.hosts.split(",");
      let installDirectoryConfig = JSON.parse(item.installDirectoryConfig);
      let processNumConfig = JSON.parse(item.processNumConfig);
      hosts.forEach((ele) => {
        ipDetail[ele] = {};
        ipDetail[ele].install = installDirectoryConfig[ele];
        ipDetail[ele].processNum = processNumConfig[ele];
      });
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
      let itemHost = getHosts(ipDetail, item.roleWithNodes);
      configList.push({
        groupName: item.groupName,
        fileConfig,
        systemConfig: [{ value: item.systemConfig, key: `systemConfig-1}`, label: "systemConfig" }],
        runningConfig: [{ value: item.runningConfig, key: `runningConfig-1}`, label: "runningConfig" }],
        key: `${item.groupName}-${index}`,
        hosts: itemHost,
      });
    });
    setIpDetail(ipDetail);
    setConfigList(configList);
  };

  const getPluginName = async () => {
    let nameList = [];
    let names = [];
    let engine = await getPackageVersion("es-engine-plugin");
    let platform = await getPackageVersion("es-platform-plugin");
    (engine || []).forEach((item) => {
      if (names.includes(item?.name)) {
        nameList.forEach((ele) => {
          if (ele.name === item.name) {
            ele.versionOptions.push({ value: item.version, label: item.version, type: "engine" });
          }
        });
      } else {
        nameList.push({
          ...item,
          type: "engine",
          label: item?.name,
          value: item?.id,
          versionOptions: [{ value: item.version, label: item.version, type: "engine" }],
        });
        names.push(item.name);
      }
    });
    (platform || []).forEach((item) => {
      if (names.includes(item?.name)) {
        nameList.forEach((ele) => {
          if (ele.name === item.name) {
            ele.versionOptions.push({ value: item.version, label: item.version, type: "platform" });
          }
        });
      } else {
        nameList.push({
          ...item,
          type: "platform",
          label: item?.name,
          value: item?.id,
          versionOptions: [{ value: item.version, label: item.version, type: "platform" }],
        });
        names.push(item.name);
      }
    });
    setNameList(nameList);
  };

  const getHosts = (detail?: any, esClusterRoles?: any) => {
    let esClusterRoleVOS = props.params?.esClusterRoleVOS;
    let hosts = [];
    (esClusterRoles || esClusterRoleVOS || []).forEach((item) => {
      let node = item.role;
      (item?.esClusterRoleHostVO || []).forEach((ele) => {
        let ip = `${ele?.ip}:${ele?.port}`;
        let hasHost = false;
        hosts.forEach((host) => {
          if (host.ip === ip) {
            host.node = host.node + "，" + node;
            hasHost = true;
          }
        });
        if (!hasHost && ele?.status === 1) {
          hosts.push({
            id: `${ip}-${node}`,
            ip,
            node,
            host: ele?.ip,
            install: detail?.[ele?.ip]?.install || ipDetail?.[ele?.ip]?.install,
            processNum: detail?.[ele?.ip]?.processNum || ipDetail?.[ele?.ip]?.processNum || 1,
          });
        }
      });
    });
    return hosts;
  };

  const getConfigDetail = async () => {
    let res = await getPackageGroupConfigsByName(
      plugin?.name,
      version.value,
      version?.type === "platform" ? "es-platform-plugin" : "es-engine-plugin"
    );
    let hosts = getHosts();
    let configList = (res || []).map((item, index) => {
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
      let itemHost = [];
      let length = res?.length;
      let hostLength = hosts?.length;
      let num = Math.ceil(hostLength / length);
      if (length >= hostLength) {
        hosts[index] && itemHost.push(hosts[index]);
      } else {
        hosts[index] && itemHost.push(hosts[index]);
        for (let i = 1; i < num; i++) {
          hosts[index + num + i] && itemHost.push(hosts[index + num + i]);
        }
      }
      if (res?.length === 1) {
        itemHost = hosts;
      }
      return {
        groupName: item.groupName,
        fileConfig,
        systemConfig: [{ value: item.systemConfig, key: `systemConfig-1}`, label: "systemConfig" }],
        runningConfig: [{ value: item.runningConfig, key: `runningConfig-1}`, label: "runningConfig" }],
        key: `${item.groupName}-${index}`,
        hosts: itemHost,
      };
    });
    if (!configList.length) {
      const newGroupData = {
        key: uuid(),
        groupName: "",
        fileConfig: [{ label: "newFile", key: "newFile-1", value: "" }],
        systemConfig: [{ label: "systemConfig", key: "systemConfig-1", value: "" }],
        runningConfig: [{ label: "runningConfig", key: "runningConfig-1", value: "" }],
        hosts,
      };
      configList.push(newGroupData);
    }
    setConfigList(configList);
  };

  const getFormMap = () => {
    let formMap = [
      [
        {
          key: "pluginName",
          label: "插件名称",
          type: FormItemType.select,
          options: nameList,
          rules: [{ required: true, message: "请选择" }],
          attrs: {
            placeholder: "请选择",
            onChange: (val) => {
              let plugin = nameList.filter((item) => item.id === val)?.[0];
              setPlugin(plugin);
              setVersion({});
              formRef.current.setFieldsValue({ version: undefined });
            },
          },
        },
        {
          key: "version",
          label: "版本号",
          invisible: !plugin?.name,
          options: plugin?.versionOptions,
          type: FormItemType.select,
          className: "plugin-version",
          rules: [{ required: true, message: "请选择" }],
          attrs: {
            placeholder: "请选择",
            onChange: (val) => {
              let version = (plugin.versionOptions || []).filter((item) => item.value === val)[0];
              setVersion(version);
            },
          },
        },
      ],
      {
        key: "config",
        type: FormItemType.custom,
        invisible: !version.value,
        customFormItem: (
          <>
            {configList.map((item, index) => {
              return (
                <EditConfigGroup
                  type="installPlugin"
                  key={item.key}
                  groupKey={item.key}
                  data={item}
                  index={index}
                  configList={configList}
                  setConfigList={setConfigList}
                  plugType={version?.type}
                />
              );
            })}
          </>
        ),
        rules: [
          {
            required: true,
            validator: () => Promise.resolve(),
          },
        ],
      },
    ] as IFormItem[];
    return formMap;
  };

  const xFormModalConfig = {
    formMap: getFormMap(),
    visible: true,
    title: "插件安装",
    formData: {},
    width: 800,
    needBtnLoading: true,
    formRef: formRef,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async () => {
      let groupConfigList = [];
      configList.forEach((item) => {
        let fileConfig = {};
        (item.fileConfig || []).forEach((file) => {
          fileConfig[file.label] = file?.editor?.getValue() || file.value;
        });
        let hosts = [];
        let installDirectoryConfig = {};
        let processNumConfig = {};
        (item?.hosts || []).forEach((host) => {
          let key = host.host;
          if (!hosts.includes(key)) {
            hosts.push(key);
            installDirectoryConfig[key] = host.install;
            processNumConfig[key] = 1;
          }
        });
        let config = {
          groupName: item?.groupName,
          systemConfig: item.systemConfig?.[0]?.value || "",
          runningConfig: item.runningConfig?.[0]?.value || "",
          fileConfig,
          hosts: hosts.join(","),
          installDirectoryConfig: JSON.stringify(installDirectoryConfig),
          processNumConfig: JSON.stringify(processNumConfig),
          dependConfigComponentId: props.params?.componentId,
        };
        groupConfigList.push(config);
      });
      let params = {
        name: plugin?.name,
        pluginType: version?.type === "platform" ? 1 : 2,
        packageId: plugin?.id,
        dependComponentId: props.params?.componentId,
        groupConfigList,
      };
      let expandData = { expandData: JSON.stringify(params) };
      let res = await installClusterPlug(expandData);
      props.dispatch(actions.setModalId(""));
      showSubmitTaskSuccessModal(res, props.params?.history);
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

export default connect(mapStateToProps)(InstallClusterPlugin);
