import { IFormItem, FormItemType } from "component/x-form";
import { REGION_LIST, DATA_TYPE_LIST, BOOLEAN_LIST, LEVEL_MAP } from "constants/common";
import {
  CYCLICAL_ROLL_TYPE_LIST,
  fieldTypes,
  yesOrNoOptions,
  cascaderTypes,
  complexType,
  participleList,
  TEMP_FORM_MAP_KEY,
} from "./constant";
import { KEEP_LIVE_LIST } from "constants/time";
import React from "react";
import { StaffSelect } from "container/staff-select";
import { staffRuleProps } from "constants/table";
import { nounClusterType, nounPartitionCreate } from "container/tooltip";
import { RenderText } from "container/custom-form";
import { LogicCluserSelect } from "container/custom-form/logic-cluser-select";
import store from "store";
import { PlusCircleOutlined } from "@ant-design/icons";
import { AppState } from "store/type";
import { getNameCheck, getSizeCheck } from 'api/cluster-index-api';
import _ from 'lodash';
import { HotTime } from './hotTime';
import { InfoCircleOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';

const app = {
  app: store.getState().app as AppState,
};

const indexStore = store.getState().createIndex;

export const getStepOneFormMap = (
  clusterType: number,
  cyclicalRoll: string,
  isModifyPage: boolean,
  form?: any,
  hotTimeState?: boolean,
): IFormItem[] => {
  const mapList = [
    {
      key: "name",
      label: "索引模板名称",
      attrs: {
        disabled: isModifyPage,
        placeholder: "请填写索引模板名称，支持小写字母、数字、-、_4-128位字符",
      },
      // formAttrs: {
      //   dependencies: ['clusterInfo'],
      // },
      rules: [
        {
          required: isModifyPage ? false : true,
          validator: async (rule: any, value: string) => {
            // const clusterInfo = await form.getFieldValue('clusterInfo');
            if (isModifyPage) return Promise.resolve();
            const reg = /^[.a-z0-9_-]{1,}$/g;
            if (!value || value?.trim().length > 128 || value?.trim().length < 4) {
              return Promise.reject("请填写索引模板名称，支持小写字母、数字、-、_4-128位字符");
            }
            if (!reg.test(value)) {
              return Promise.reject("请填写正确索引模板名称，支持小写字母、数字、-、_4-128位字符");
            }
            if (value) {
              const checkName = await getNameCheck(value);
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "appName",
      label: "所属项目",
      type: FormItemType.custom,
      customFormItem: <RenderText text={app.app.appInfo()?.name} />,
    },
    {
      key: "clusterInfo",
      label: "所属集群",
      type: FormItemType.custom,
      customFormItem: <LogicCluserSelect isModifyPage={isModifyPage} />,
      rules: [
        {
          required: isModifyPage ? false : true,
          message: "请填写完整",
          validator: (rule: any, value: object) => {
            if (isModifyPage) return Promise.resolve();
            if (!value) return Promise.reject();
            let b = true;
            Object.keys(value || {}).map((item) => {
              if (!value[item]) {
                b = false;
              }
            });
            if (b) {
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
          message: '请选择业务等级',
        },
      ],
      attrs: {
        disabled: isModifyPage,
      },
      options: LEVEL_MAP,
    },
    {
      key: "cyclicalRoll",
      label: "是否分区",
      type: FormItemType.select,
      rules: [
        { required: isModifyPage ? false : true, message: "请选择是否分区" },
      ],
      attrs: {
        disabled: isModifyPage,
      },
      options: CYCLICAL_ROLL_TYPE_LIST,
      extraElement: <>{nounPartitionCreate}</>,
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
      attrs: {
        disabled: isModifyPage,
      },
    },
    {
      key: "hotTime",
      label: "热节点保存周期(天)",
      invisible: cyclicalRoll !== "more",
      formAttrs: {
        dependencies: ['expireTime'],
      },
      type: FormItemType.custom,
      customFormItem: <HotTime disabled={hotTimeState} />,
      rules: [
        {
          required: false,
          validator: async (rule: any, value) => {
            const expireTime = await form?.getFieldValue('expireTime');

            if (window.location.pathname == '/es/index/modify') {
              return Promise.resolve();
            }
            if (value < 0 || (value > expireTime && expireTime !== -1)) return Promise.reject('请输入热节点保存周期，自然数，大于等于0，小于等于【保存周期】');
            return Promise.resolve();
          },
        }
      ],
      attrs: {
        // disabled: isModifyPage,
        style: { width: '100%' },
        placeholder: '请输入热节点保存周期',
      },
    },
    {
      key: "disableIndexRollover",
      label: (
        <div style={{ position: 'relative' }}>
          <Tooltip title={'开启后会影响索引Update和Delete能力以及指定id 写入，更新，删除。'}>
            <InfoCircleOutlined style={{ color: 'green', position: 'absolute', left: -28, top: 2 }} />
          </Tooltip>
          <span> 索引模板是否开启RollOver</span>
        </div>
      ),
      defaultValue: 'true',
      invisible: isModifyPage,
      type: FormItemType.select,
      rules: [{ required: true, message: "请选择索引模板是否开启RollOver" }],
      options: BOOLEAN_LIST,
    },
    {
      key: "quota",
      label: "数据大小(GB)",
      // invisible: !Boolean(cyclicalRoll),
      formAttrs: {
        dependencies: ['clusterInfo'],
      },
      defaultValue: 30,
      type: FormItemType.inputNumber,
      rules: [
        {
          required: true,
          validator: async (rule: any, value: number) => {
            const clusterInfo = await form?.getFieldValue('clusterInfo');
            if (isModifyPage) return Promise.resolve();
            if (typeof value !== "number") {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }
            if (value === Infinity || value < 0) {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }

            if (value < 3 || value >= 3072) {
              return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
            }

            if (value && clusterInfo?.cluster) {
              const checkSize = await getSizeCheck(clusterInfo.cluster, value);
            }
            return Promise.resolve();
          },
        },
      ],
      attrs: {
        disabled: isModifyPage,
      },
    },
    {
      key: "responsible",
      label: "业务负责人",
      type: FormItemType.custom,
      customFormItem: <StaffSelect />,
      rules: [
        {
          required: true,
          ...staffRuleProps,
        },
      ],
    },
    {
      key: "dataType",
      label: "业务类型",
      type: FormItemType.select,
      rules: [{ required: true, message: "请选择业务类型" }],
      options: DATA_TYPE_LIST,
    },
    {
      key: "desc",
      label: "模板描述",
      type: FormItemType.textArea,
      attrs: {
        placeholder: '请输入模板描述',
      },
      rules: [
        {
          validator: (rule: any, value: string) => {
            if (!value) {
              return Promise.resolve();
            } else if (value?.trim().length > 100) {
              return Promise.reject("请输入0-100字描述信息");
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "description",
      type: FormItemType.textArea,
      label: "申请原因",
      invisible: isModifyPage,
      rules: [
        {
          required: true,
          validator: (rule: any, value: string) => {
            if (!value || value?.trim().length > 100) {
              return Promise.reject("请输入1-100字申请原因");
            } else {
              return Promise.resolve();
            }
          },
        },
      ],
      attrs: {
        placeholder: "请输入1-100字申请原因",
      },
    },
  ] as IFormItem[];
  return mapList;
};

export const getStepTowFormItems = (parmas: {
  kIndex?: string;
  cb?: any;
  isCyclicalRoll?: boolean;
  isDetailPage?: boolean;
  isModifyPage?: boolean;
}) => {
  const { kIndex, isCyclicalRoll, cb, isDetailPage, isModifyPage } =
    parmas || {};
  const type = indexStore.fieldTypeMap[`type_${kIndex}`];
  const values = indexStore.temporaryFormMap.get(
    TEMP_FORM_MAP_KEY.tempTableMappingValues
  );
  const partition = values?.[`partition_${kIndex}`];
  const { secondLevel, id, firstLevel } = getFormItemKeyLevel(kIndex);

  return [
    {
      key: "name",
      label: "字段名称",
      colSpan: 3,
      customClassName: "name-box",
      formAttrs: {
        dependencies: Object.keys(values || {}).filter(key => key.includes('name')),
      },
      rules: [
        {
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if (!value) {
              return Promise.reject("请输入字段名称");
            }
            let messageText = '';
            if (values) {
              Object.keys(values).map((row) => {
                if (
                  row.includes("name") &&
                  row !== `name_${kIndex}` &&
                  value === values[row]
                ) {
                  messageText = "已存在同名字段";
                  // const {
                  //   secondLevel: rowSecLevel,
                  //   firstLevel: rowFirLevel,
                  // } = getFormItemKeyLevel(row.replace("name_", ""));
                  // const rowType = values[row.replace("name_", "type_")];
                  // const kType = values[`type_${kIndex}`];
                  // const isSameType =
                  //   rowType?.[0] === "date"
                  //     ? kType?.[0] === "date"
                  //     : rowType?.[0] === kType?.[0] ||
                  //       rowType?.[1] === kType?.[1];
                  // console.log(values, value, row,rowSecLevel, rowFirLevel, firstLevel, secondLevel, isSameType, rowType, kType  )
                  // if (
                  //   rowFirLevel === firstLevel &&
                  //   rowSecLevel === secondLevel &&
                  //   !isSameType
                  // ) {
                  //   // 同一层级
                  //   messageText = "已存在同名字段";
                  // } else if (!isSameType) {
                  //   messageText = ("同名字段类型需保持一致");
                  // }
                }
              });
            }
            if (messageText) {
              return Promise.reject(messageText);
            } else {
              return Promise.resolve();
            }
          },
        },
      ],
      attrs: {
        placeholder: "请输入字段名称",
        disabled: isDetailPage,
      },
    },
    {
      key: "type",
      label: "字段类型",
      type: FormItemType.cascader,
      colSpan: 3,
      rules: [{ required: true, message: "请选择字段类型" }],
      attrs: {
        placeholder: "请选择字段类型",
        disabled: isDetailPage,
      },
      defaultValue: [],
      options: fieldTypes.map((row) => {
        let children = cascaderTypes[row]?.map((key) => ({
          label: key,
          value: key,
        }));
        if (
          (!isCyclicalRoll && row === "date") ||
          (!partition && row === "date")
        ) {
          children = [];
        }
        return {
          label: row,
          value: row,
          children: children || [],
        };
      }),
      extraElement:
        (firstLevel === 0 || secondLevel === 0) &&
          complexType.indexOf(type) > -1 ? (
          <>
            <PlusCircleOutlined
              onClick={() => {
                if (cb) {
                  cb(firstLevel, secondLevel, id);
                }
              }}
            />
          </>
        ) : null,
    },
    {
      key: "search",
      label: "是否检索",
      type: FormItemType.select,
      customClassName: "center",
      defaultValue: '1',
      colSpan: 2,
      rules: [{ required: true, message: "请选择是否检索" }],
      options: yesOrNoOptions,
      attrs: {
        placeholder: "请选择",
        disabled: isDetailPage,
      },
    },
    {
      key: "analyzer",
      label: "分词器",
      type: FormItemType.select,
      customClassName: "center",
      defaultValue: "standard",
      colSpan: 2,
      options: participleList,
      rules: [{ required: true, message: "请选择分词器" }],
      attrs: {
        disabled:
          isDetailPage ||
          (type === "text" &&
            indexStore.formValueMap[`search_${kIndex}`] == 0),
        placeholder: "请选择",
      },
    },
    {
      key: "sort",
      label: "是否排序/聚合",
      type: FormItemType.select,
      customClassName: "center",
      defaultValue: '1',
      colSpan: 2,
      rules: [{ required: true, message: "请选择是否排序/聚合" }],
      options: yesOrNoOptions,
      attrs: {
        placeholder: "请选择",
        disabled: isDetailPage,
      },
    },
    {
      key: "primaryKey",
      label: "主键",
      customClassName: "center",
      attrs: {
        size: "small",
        disabled: isDetailPage,
      },
      formAttrs: {
        dependencies: Object.keys(values || {}).filter(key => key.includes('primaryKey')),
      },
      type: FormItemType._switch,
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            const primaryKeyArr = [];
            Object.keys(values).forEach((key) => {
              if (key.includes('primaryKey') && values[key]) {
                primaryKeyArr.push(values[key]);
              }
            })
            if (primaryKeyArr?.length > 1 && value) {
              return Promise.reject('主键是唯一的')
            }
            return Promise.resolve()
          },
        },
      ],
      colSpan: 2,
    },
    {
      key: "routing",
      label: "Routing",
      customClassName: "center",
      colSpan: 1,
      attrs: {
        size: "small",
        disabled: isDetailPage,
      },
      formAttrs: {
        dependencies: Object.keys(values || {}).filter(key => key.includes('routing')),
      },
      type: FormItemType._switch,
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            const routingArr = [];
            Object.keys(values).forEach((key) => {
              if (key.includes('routing') && values[key]) {
                routingArr.push(values[key]);
              }
            })
            if (routingArr?.length > 1 && value) {
              return Promise.reject('routing是唯一的')
            }
            return Promise.resolve()
          },
        },
      ],
    },
    {
      key: "partition",
      label: "分区字段",
      customClassName: "center",
      colSpan: 2,
      invisible: isModifyPage ? false : !Boolean(isCyclicalRoll),
      attrs: {
        disabled:
          isDetailPage ||
          (type === "date" &&
            indexStore.formValueMap[`search_${kIndex}`] == 0),
        size: "small",
      },
      type: FormItemType._switch,
    },
    {
      key: "dynamic",
      label: "Dynamic",
      customClassName: "center",
      colSpan: 2,
      defaultValue: '0',
      type: FormItemType.select,
      attrs: {
        placeholder: "请选择",
        disabled: isDetailPage,
      },
      options: yesOrNoOptions,
    },
  ] as unknown as IFormItem[];
};

export const getFormItemKeyLevel = (key: string) => {
  if (!key) return { level: -1, id: -1 };
  const firstLevel = +key.split("_")[1].split("-")[1];
  const secondLevel = +key.split("_")[1].split("-")[2];
  const id = +key.split("_")[0];
  return { id, firstLevel, secondLevel };
};

export const judgeHasTwoOrMoreItems = (keys: string[]) => {
  let i = 0;
  while (i < keys.length - 1) {
    const { id: currId } = getFormItemKeyLevel(keys[i]);
    const { id: nextId } = getFormItemKeyLevel(keys[i + 1]);
    if (currId !== nextId) {
      break;
    }
    i++;
  }
  return i < keys.length - 1;
};

export const getMappingPreviewInfo = (isCyclicalRoll: boolean) => {
  if (isCyclicalRoll) {
    return mappingPreviewInfo;
  }
  return mappingPreviewInfo.filter((item) => item.key !== "expireTime");
};

export const mappingPreviewInfo = [
  {
    label: "模板名称",
    key: "name",
    render: (value: any) => <span>{`${value?.name || ""}`}</span>,
  },
  {
    label: "所属应用",
    key: "app",
    render: (value: any) => (
      <span>{`${app.app.appInfo()?.name || ""}(${app.app.appInfo()?.id || ""
        })`}</span>
    ),
  },
  {
    label: "数据中心",
    key: "region",
    render: (value: any) => (
      <span>
        {REGION_LIST.filter((item) => item.value === value?.region)?.[0]
          ?.label || "cn"}
      </span>
    ),
  },
  {
    label: "所属集群",
    key: "clusterInfo",
    render: (value: any) => <span>{value.clusterInfo?.clusterName}</span>,
  },
  {
    label: "业务等级",
    key: "level",
    render: (value: any) => <span>{LEVEL_MAP[Number(value?.level) - 1]?.label}</span>,
  },
  {
    label: "容量设置",
    key: "cyclicalRoll",
    render: (value: any) => (
      <span>
        {CYCLICAL_ROLL_TYPE_LIST.filter(
          (item) => item.value === value?.cyclicalRoll
        )?.[0]?.label || ""}
      </span>
    ),
  },
  {
    label: "索引模板是否开启RollOver",
    key: "disableIndexRollover",
    render: (value: any) => (
      value?.disableIndexRollover === 'false' ? '是' : '否'
    ),
  },
  {
    label: "热节点保存周期",
    key: "hotTime",
  },
  {
    label: "分区字段",
    key: "dateField",
  },
  {
    label: "主键字段",
    key: "idField",
  },
  {
    label: "routing字段",
    key: "routingField",
  },
  {
    label: "保存周期",
    key: "expireTime",
  },
  {
    label: "数据大小",
    key: "quota",
    unit: "GB",
  },
  // {
  //   label: "成本估算",
  //   key: "quotaMoney",
  // },
  {
    label: "负责人",
    key: "responsible",
  },
  {
    label: "业务类型",
    key: "dataType",
    render: (value: any) => (
      <span>
        {DATA_TYPE_LIST.filter((item) => item.value === value?.dataType)?.[0]
          ?.label || ""}
      </span>
    ),
  },
  {
    label: "描述",
    key: "desc",
  },
  {
    label: "申请原因",
    key: "description",
  },
];

export const getTableMappingData = (postData: any) => {
  const tableMappingValue =
    indexStore.temporaryFormMap.get(TEMP_FORM_MAP_KEY.tableMappingValues) || {};
  let dateFieldFormat = null;
  let dateField = "";
  const idFieldList = [] as string[];
  const routingFieldList = [] as string[];
  Object.keys(tableMappingValue).map((item) => {
    if (item.includes("partition") && tableMappingValue[item]) {
      dateFieldFormat =
        tableMappingValue[item.replace("partition", "type")]?.[1];
      dateField = tableMappingValue[item.replace("partition", "name")];
    }
    if (item.includes("primaryKey") && tableMappingValue[item]) {
      idFieldList.push(tableMappingValue[item.replace("primaryKey", "name")]);
    }
    if (item.includes("routing") && tableMappingValue[item]) {
      routingFieldList.push(tableMappingValue[item.replace("routing", "name")]);
    }
  });

  Object.assign(postData?.typeProperties?.[0] || {}, {
    idField: idFieldList.join(),
    dateField,
    routingField: routingFieldList.join(),
    dateFieldFormat,
  });

  Object.assign(postData || {}, {
    idField: idFieldList.join(),
    dateField,
    routingField: routingFieldList.join(),
    dateFieldFormat,
  });
};

export const getJsonMappingData = (postData: any) => {
  const formData =
    indexStore.temporaryFormMap.get(TEMP_FORM_MAP_KEY.jsonMappingFormData) ||
    {};
  Object.assign(postData, {
    idField: formData.primaryKey,
    dateField: formData.partition,
    routingField: formData.routing,
    dateFieldFormat: formData.timeFormat,
  });
  Object.assign(postData?.typeProperties?.[0] || {}, {
    idField: formData.primaryKey,
    dateField: formData.partition,
    routingField: formData.routing,
    dateFieldFormat: formData.timeFormat,
  });
};


export const strNumberToBoolean = (value) => { 
  // undefined null 0 表示为fasle
  return !(value === undefined || value === null || value == '0');
}