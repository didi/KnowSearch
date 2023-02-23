import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { Button } from "antd";
import { DoubleRightOutlined } from "@ant-design/icons";
import { getDataCenter, getNodeSpecification, getPackageTypeDescVersion, getPackageGroupConfigsByName } from "api/cluster-api";
import { addGateway } from "api/gateway-manage";
import { EditConfigGroup } from "component/config-group";
import { uuid } from "lib/utils";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const AddGateway = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { dispatch, cb } = props;
  const [versionList, setVersionList] = useState([]);
  const [configList, setConfigList] = useState([]);
  const [fold, setFold] = useState(false);
  const [dataCenter, setDataCenter] = useState([]);
  const [gatewayVersion, setGatewayVersion] = useState("");
  const [machineList, setMachineList] = useState([]);

  useEffect(() => {
    getVersionList();
    _getDataCenter();
    getMachineList();
  }, []);

  useEffect(() => {
    gatewayVersion && getGroupConfigs();
  }, [gatewayVersion]);

  const getVersionList = async () => {
    let res = await getPackageTypeDescVersion("gateway-install-package");
    let list = (res || []).map((item) => ({ ...item, title: item.version, value: item.version, id: item.id }));
    setVersionList(list);
  };

  const _getDataCenter = async () => {
    let res = await getDataCenter();
    let data = (res || []).map((item: string) => {
      return { value: item, label: item };
    });
    setDataCenter(data);
  };

  const getMachineList = () => {
    getNodeSpecification().then((res) => {
      setMachineList(res.map((item) => ({ value: item })));
    });
  };

  const getGroupConfigs = async () => {
    let version = versionList.filter((item) => item.value === gatewayVersion)[0];
    let res = await getPackageGroupConfigsByName(version.name, version.value, "gateway-install-package");
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

  const xFormModalConfig = {
    formMap: [
      [
        {
          key: "name",
          label: "Gateway集群名称",
          type: FormItemType.input,
          attrs: {
            placeholder: "请填写Gateway集群名称，支持大、小写字母、数字、-、_，1-32位字符",
          },
          rules: [
            {
              required: true,
              validator: async (rule: any, value: string) => {
                const reg = /^[a-zA-Z0-9_-]{1,}$/g;
                if (!reg.test(value) || value?.length > 32 || !value) {
                  return Promise.reject("请填写Gateway集群名称，支持大、小写字母、数字、-、_，1-32位字符");
                }
                return Promise.resolve();
              },
            },
          ],
        },
        {
          key: "version",
          label: "软件版本",
          type: FormItemType.select,
          options: versionList,
          attrs: {
            placeholder: "请选择",
            onChange: (val) => setGatewayVersion(val),
          },
          rules: [{ required: true, message: "请选择软件版本" }],
        },
      ],
      {
        key: "config",
        label: "默认配置组",
        type: FormItemType.custom,
        invisible: !gatewayVersion,
        customFormItem: configList.length ? (
          <>
            {configList.map((item, index) => {
              return (
                <EditConfigGroup
                  type="addGateway"
                  key={item.key}
                  groupKey={item.key}
                  data={item}
                  index={index}
                  configList={configList}
                  setConfigList={setConfigList}
                  machineList={machineList || []}
                />
              );
            })}
          </>
        ) : (
          <EditConfigGroup
            type="addGateway"
            setConfigList={setConfigList}
            configList={configList}
            machineList={props.params?.machineList || []}
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
        label: "集群描述",
        type: FormItemType.textArea,
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
        className: "access-senior",
        type: FormItemType.custom,
        customFormItem: (
          <div className="access-cluster-senior">
            <Button type="link" style={{ paddingLeft: 0 }} onClick={() => setFold(!fold)}>
              高级
              <DoubleRightOutlined className={fold ? "up" : "down"} />
            </Button>
          </div>
        ),
      },
      {
        key: "dataCenter",
        label: "数据中心",
        type: FormItemType.select,
        invisible: !fold,
        options: dataCenter,
        attrs: {
          placeholder: "请选择",
        },
        rules: [{ required: false, message: "请选择数据中心" }],
      },
      {
        key: "proxyAddress",
        label: "代理地址",
        type: FormItemType.input,
        invisible: !fold,
        attrs: {
          placeholder: "请填写代理地址",
        },
        rules: [{ required: false, message: "请填写代理地址" }],
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: "新建Gateway",
    needBtnLoading: true,
    width: 800,
    onCancel: () => {
      dispatch(actions.setDrawerId(""));
    },
    onSubmit: async (result: any) => {
      let packageId = versionList.filter((item) => item.value === gatewayVersion)[0]?.id;
      let groupConfigList = getGroupConfigList();
      let params = {
        memo: result.memo,
        dataCenter: result.dataCenter || "",
        proxyAddress: result.proxyAddress || "",
        name: result.name,
        packageId,
        groupConfigList,
      };
      let expandData = JSON.stringify(params);
      let res = await addGateway({ expandData });
      dispatch(actions.setDrawerId(""));
      showSubmitTaskSuccessModal(res, props.params?.history);
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
