import { IFormItem, FormItemType } from "component/x-form";
import { LEVEL_MAP } from "constants/common";
import { CYCLICAL_ROLL_TYPE_LIST } from "./constant";
import { KEEP_LIVE_LIST } from "constants/time";
import React from "react";
// import { StaffSelect } from "container/staff-select";
// import { staffRuleProps } from "constants/table";
import { nounPartitionCreate } from "container/tooltip";
import { LogicCluserSelect } from "container/custom-form/logic-cluser-select";
import store from "store";
import { AppState } from "store/type";
import { getNameCheck, getClusterCheck } from "api/cluster-index-api";
import { CopyOutlined } from "@ant-design/icons";
import { copyString } from "lib/utils";

const app = {
  app: store.getState().app as AppState,
};

export const getStepOneFormMap = (
  cyclicalRoll: string,
  isModifyPage: boolean,
  form: any,
  timeFormatList = [],
  dataTypeList = []
): IFormItem[] => {
  const mapList = [
    {
      key: "name",
      label: "索引模板名称",
      attrs: {
        disabled: isModifyPage,
        placeholder: "请填写索引模板名称，支持小写字母、数字、-、_4-128位字符",
      },
      rules: [
        {
          required: isModifyPage ? false : true,
          validator: async (rule: any, value: string) => {
            if (isModifyPage) return Promise.resolve();
            value = value?.trim();
            const reg = /^[.a-z0-9_-]{1,}$/g;
            if (!value) {
              return Promise.reject("请填写索引模板名称");
            }
            if (!reg.test(value) || value.length > 128 || value.length < 4) {
              return Promise.reject("请正确填写索引模板名称，支持小写字母、数字、-、_4-128位字符");
            }
            if (value) {
              try {
                const res = await getNameCheck(value);
                if (res.code !== 0 && res.code !== 200) {
                  return Promise.reject(res.message);
                }
              } catch (err) {
                return Promise.reject("索引模板名称校验失败");
              }
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "clusterInfo",
      label: "所属集群",
      type: FormItemType.custom,
      customFormItem: <LogicCluserSelect isModifyPage={isModifyPage} $form={form} />,
      rules: [
        {
          required: isModifyPage ? false : true,
          message: "请填写完整",
          validator: async (rule: any, value: any) => {
            if (isModifyPage) return Promise.resolve();
            if (!value) return Promise.reject();
            if (value.cluster) {
              try {
                const res = await getClusterCheck(value.cluster);
                if (res.code !== 0 && res.code !== 200) {
                  return Promise.reject(res.message);
                }
              } catch (err) {
                return Promise.reject("所属集群校验失败");
              }
              return Promise.resolve();
            }
            return Promise.reject();
          },
        },
      ],
    },
    {
      key: "level",
      label: "业务等级",
      type: FormItemType.select,
      rules: [
        {
          required: isModifyPage ? false : true,
          message: "请选择业务等级",
        },
      ],
      attrs: {
        disabled: isModifyPage,
        placeholder: "请选择业务等级",
      },
      options: LEVEL_MAP,
    },
    {
      key: "cyclicalRoll",
      label: "是否分区",
      type: FormItemType.select,
      rules: [{ required: isModifyPage ? false : true, message: "请选择是否分区" }],
      attrs: {
        disabled: isModifyPage,
        placeholder: "请选择是否分区",
      },
      options: CYCLICAL_ROLL_TYPE_LIST,
      extraElement: <>{nounPartitionCreate}</>,
    },
    {
      key: "dateField",
      label: "分区字段",
      invisible: cyclicalRoll !== "more",
      attrs: {
        disabled: isModifyPage,
        placeholder: "请输入分区字段",
      },
      rules: [
        {
          required: isModifyPage ? false : true,
          validator: async (rule: any, value: string) => {
            if (isModifyPage) return Promise.resolve();
            value = value?.trim();
            if (!value || value.length > 20) {
              return Promise.reject("请输入分区字段，不超过20位字符");
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "dateFieldFormat",
      label: "时间格式",
      invisible: cyclicalRoll !== "more",
      type: FormItemType.select,
      rules: [{ required: isModifyPage ? false : true, message: "请选择时间格式" }],
      options: timeFormatList.map((item) => ({
        label: item,
        value: item,
      })),
      attrs: {
        disabled: isModifyPage,
        placeholder: "请选择时间格式",
      },
    },
    {
      key: "diskSize",
      label: "索引模板数据大小(GB)",
      formAttrs: {
        dependencies: ["clusterInfo"],
      },
      defaultValue: 30,
      type: FormItemType.inputNumber,
      rules: [
        {
          required: true,
          validator: (rule: any, value: number) => {
            if (isModifyPage) return Promise.resolve();
            if (typeof value !== "number") {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }
            if (value === Infinity || value < 0) {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }

            if (value < 3 || value > 3072) {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }
            return Promise.resolve();
          },
        },
      ],
      attrs: {
        disabled: isModifyPage,
        placeholder: "请输入",
      },
    },
    {
      key: "expireTime",
      label: "保存周期(天)",
      invisible: cyclicalRoll !== "more",
      type: FormItemType.select,
      defaultValue: KEEP_LIVE_LIST[0],
      rules: [{ required: true, message: "请选择保存周期" }],
      options: KEEP_LIVE_LIST.map((item) => {
        return {
          label: item === -1 ? "永不过期" : item,
          value: item,
        };
      }),
    },
    {
      key: "dataType",
      label: "业务类型",
      type: FormItemType.select,
      rules: [{ required: true, message: "请选择业务类型" }],
      options: dataTypeList,
      attrs: {
        placeholder: "请选择业务类型",
      },
    },
    {
      key: "desc",
      label: "模板描述",
      type: FormItemType.textArea,
      attrs: {
        placeholder: "请输入0-1000字模板描述",
      },
      rules: [
        {
          validator: (rule: any, value: string) => {
            value = value?.trim();
            if (!value) {
              return Promise.resolve();
            } else if (value.length > 1000) {
              return Promise.reject("请输入0-1000字模板描述");
            }
            return Promise.resolve();
          },
        },
      ],
    },
  ] as IFormItem[];
  return mapList;
};

export const getMappingPreviewInfo = (isCyclicalRoll: boolean, dataTypeList = []) => {
  if (isCyclicalRoll) {
    return mappingPreviewInfo(dataTypeList);
  }
  return mappingPreviewInfo(dataTypeList).filter(
    (item) => item.key !== "expireTime" && item.key !== "dateField" && item.key !== "dateFieldFormat"
  );
};

export const mappingPreviewInfo = (dataTypeList) => {
  return [
    {
      label: "模板名称",
      key: "name",
      render: (value: any) => <span>{`${value?.name || "-"}`}</span>,
    },
    {
      label: "所属应用",
      key: "app",
      render: (value: any) => <span>{`${app.app.appInfo()?.name || "-"}(${app.app.appInfo()?.id || "-"})`}</span>,
    },
    {
      label: "数据中心",
      key: "dataCenter",
    },
    {
      label: "所属集群",
      key: "clusterInfo",
      render: (value: any) => <span>{value.clusterInfo?.clusterName || "-"}</span>,
    },
    {
      label: "业务等级",
      key: "level",
      render: (value: any) => <span>{LEVEL_MAP[Number(value?.level) - 1]?.label || "-"}</span>,
    },
    {
      label: "是否分区",
      key: "cyclicalRoll",
      render: (value: any) => (
        <span>{CYCLICAL_ROLL_TYPE_LIST.filter((item) => item.value === value?.cyclicalRoll)?.[0]?.label || "-"}</span>
      ),
    },
    {
      label: "保存周期",
      key: "expireTime",
      render: (value: any) => <span>{value.expireTime === -1 ? "永不过期" : `${value.expireTime}天`}</span>,
    },
    {
      label: "分区字段",
      key: "dateField",
    },
    {
      label: "时间格式",
      key: "dateFieldFormat",
    },
    {
      label: "数据大小",
      key: "diskSize",
      unit: "GB",
    },
    {
      label: "业务类型",
      key: "dataType",
      render: (value: any) => <span>{dataTypeList.filter((item) => item.value === value?.dataType)?.[0]?.label || "-"}</span>,
    },
    {
      label: "描述",
      key: "desc",
    },
  ];
};
