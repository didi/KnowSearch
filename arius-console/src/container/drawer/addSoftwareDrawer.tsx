import React, { useEffect, useState, useRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType } from "component/x-form";
import { Select, Tooltip } from "antd";
import { UploadFile } from "container/custom-form/upload-file";
import { addSoftware, updateSoftware } from "api/software-admin";
import { UserState } from "store/type";
import { EditConfigGroup } from "component/config-group";
import { uuid } from "lib/utils";
import { PACKAGE_TYPE } from "constants/common";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
});

const AddSoftwareDrawer = (props: { dispatch: any; cb: Function; user: UserState; params: any }) => {
  const { params } = props;
  const formRef = useRef(null);

  const [isPlug, setIsPlug] = useState(true);
  const [configList, setConfigList] = useState([]);
  const [disabled, setDisabled] = useState(true);
  const [packageType, setPackageType] = useState(null);

  useEffect(() => {
    getConfigList();
  }, []);

  useEffect(() => {
    if (packageType === 1 && params.addSoftware) {
      setConfigList([
        {
          key: uuid(),
          groupName: "Master",
          fileConfig: [{ label: "newFile", key: "newFile-1", value: "" }],
          systemConfig: [{ label: "systemConfig", key: "systemConfig-1", value: "" }],
          runningConfig: [{ label: "runningConfig", key: "runningConfig-1", value: "" }],
        },
        {
          key: uuid(),
          groupName: "Client",
          fileConfig: [{ label: "newFile", key: "newFile-1", value: "" }],
          systemConfig: [{ label: "systemConfig", key: "systemConfig-1", value: "" }],
          runningConfig: [{ label: "runningConfig", key: "runningConfig-1", value: "" }],
        },
        {
          key: uuid(),
          groupName: "Data",
          fileConfig: [{ label: "newFile", key: "newFile-1", value: "" }],
          systemConfig: [{ label: "systemConfig", key: "systemConfig-1", value: "" }],
          runningConfig: [{ label: "runningConfig", key: "runningConfig-1", value: "" }],
        },
      ]);
    }
  }, [packageType]);

  // 将接口的 groupConfigList 处理成展示的 configList 格式
  const getConfigList = () => {
    if (!params.addSoftware) {
      let list = (params.groupConfigList || []).map((item, index) => {
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
      let isPlug = params.packageType === 3 || params.packageType === 4;
      setIsPlug(isPlug);
      setConfigList(list);
    }
  };

  // 将 configList 处理成接口 groupConfigList 传参格式
  const getGroupConfigList = () => {
    let list = configList.map((item) => {
      let fileConfig = {};
      (item.fileConfig || []).forEach((file) => {
        fileConfig[file.label] = file.editor.getValue() || file.value;
      });
      return {
        groupName: item.groupName,
        fileConfig: JSON.stringify(fileConfig),
        systemConfig: item.systemConfig?.[0]?.value || "",
        runningConfig: item.runningConfig?.[0]?.value || "",
      };
    });
    return list;
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "uploadFile",
        label: "软件包上传",
        type: FormItemType.custom,
        className: "package",
        customFormItem: (
          <UploadFile
            url={params?.url}
            accept=".zip,.gz"
            msg="单击或拖动文件上传，只能上传一个文件，且为.gz，.zip格式文件"
            onChange={(value) => {
              const { fileList } = value;
              let length = fileList.length;
              let name = fileList[length - 1]?.name?.split(".gz")[0];
              if (name.includes(".tar")) {
                name = name.split(".tar")[0];
              }
              formRef.current.setFieldsValue({ name });
              let list = name?.split("-");
              for (let i = 0; i < list.length; i++) {
                let version = list[i];
                if (/^[0-9.]{4,32}$/g.test(version)) {
                  formRef.current.setFieldsValue({ version });
                  break;
                }
              }
              setDisabled(false);
            }}
          />
        ),
        rules: [
          {
            required: true,
            validator: async (rule: any, value: any) => {
              if (params?.url && value == null) {
                return Promise.resolve();
              }
              if (!value) return Promise.reject("仅支持上传单个文件且文件小于 500 MB");
              const { fileList } = value;
              const flag = fileList?.some((item) => item.size / 1024 / 1024 >= 500);
              if (!fileList || fileList.length !== 1 || flag) {
                return Promise.reject("仅支持上传单个文件且文件小于 500 MB");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      [
        {
          key: "name",
          label: "软件名称",
          attrs: { placeholder: "请输入软件名称", disabled: !params.addSoftware && disabled },
          className: "name",
          rules: [
            {
              required: true,
              message: "请填写软件名称，支持中英文、数字、-、_、.、4-128位字符",
              validator: async (rule: any, value: string) => {
                const reg = /^[a-zA-Z0-9\u4e00-\u9fa5\._-]{4,128}$/g;
                if (!reg.test(value) || !value) {
                  return Promise.reject("请填写软件名称，支持中英文、数字、-、_、.、4-128位字符");
                }
                return Promise.resolve();
              },
            },
          ],
        },
        {
          key: "version",
          label: "版本",
          attrs: { placeholder: "请输入版本信息", disabled: !params.addSoftware && disabled },
          className: "version",
          rules: [
            {
              required: true,
              message: "请输入版本信息，支持数字、.、4-32位字符",
              validator: async (rule: any, value: string) => {
                const reg = /^[0-9.]{4,32}$/g;
                if (!reg.test(value) || !value) {
                  return Promise.reject("请输入版本信息，支持数字、.、4-32位字符");
                }
                return Promise.resolve();
              },
            },
          ],
        },
      ],
      [
        {
          key: "scriptId",
          label: "脚本选择",
          type: FormItemType.custom,
          customFormItem:
            !params.addSoftware && params.isUsing ? (
              <Tooltip title={"软件使用中，不支持变更。"}>
                <Select options={params.scriptList} value={params.scriptId} disabled></Select>
              </Tooltip>
            ) : (
              <Select placeholder="请选择" options={params.scriptList}></Select>
            ),
          rules: [{ required: true, message: "请选择脚本" }],
        },
        {
          key: "packageType",
          label: "软件包类型",
          type: FormItemType.select,
          attrs: {
            onChange: (value) => {
              let isPlug = value === 3 || value === 4;
              setIsPlug(isPlug);
              setConfigList([]);
              setPackageType(value);
            },
          },
          options: PACKAGE_TYPE,
          rules: [{ required: true, message: "请选择" }],
        },
      ],
      {
        key: "config",
        label: "默认配置组",
        type: FormItemType.custom,
        invisible: isPlug,
        customFormItem: configList.length ? (
          <>
            {configList.map((item, index) => (
              <EditConfigGroup
                type="addSoftware"
                addSoftware={params.addSoftware}
                key={item.key}
                groupKey={item.key}
                data={item}
                index={index}
                configList={configList}
                setConfigList={setConfigList}
              />
            ))}
          </>
        ) : (
          <EditConfigGroup type="addSoftware" addSoftware={params.addSoftware} setConfigList={setConfigList} configList={configList} />
        ),
        rules: [
          {
            required: true,
            validator: () => Promise.resolve(),
          },
        ],
      },
      {
        key: "describe",
        label: "描述",
        type: FormItemType.textArea,
        rules: [
          {
            required: false,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (!value || value?.trim().length <= 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入0-100字描述");
              }
            },
          },
        ],
        className: "software-desc",
        attrs: { placeholder: "请输入0-100字描述" },
      },
    ],
    type: "drawer",
    className: "add-software",
    visible: true,
    title: params && !params.addSoftware ? "编辑软件" : "新增软件",
    formData: params || {},
    isWaitting: true,
    width: 800,
    formRef,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    actionAfterSubmit: () => {
      props.dispatch(actions.setDrawerId(""));
      props.cb && props.cb();
    },
    onSubmit: async (result) => {
      result.groupConfigList = getGroupConfigList();
      if (result.uploadFile?.fileList) {
        const file = result.uploadFile?.fileList[0].originFileObj;
        result.uploadFile = file;
      } else {
        result.uploadFile = null;
      }
      // 判断是新增脚本还是编辑脚本
      if (!params.addSoftware) {
        result.id = params.id;
        result.url = result.uploadFile ? result.url : params?.url;
        return updateSoftware(result).catch(() => {
          throw new Error("编辑失败");
        });
      }
      return addSoftware(result).catch((req: any) => {
        throw new Error("上传失败");
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(AddSoftwareDrawer);
