import React, { memo, useEffect, useRef, useState } from "react";
import "./search-query-form.less";
import store from "store";
import { isSuperApp, filterOption } from "lib/utils";
import DRangeTime from "../../../d1-packages/d-range-time";
import { QueryForm } from "knowdesign";
import moment from "moment";
import { IColumnsType } from "../../../d1-packages/ProForm/QueryForm";
import { regNonnegativeInteger } from "constants/reg";

interface SearchQueryFormPropsType {
  setSearchQuery: (params) => void;
  reload?: boolean;
  isSlow?: boolean;
  value?: any;
}
const CN = "dsl-query-tpl";
const customTimeOptions = [
  {
    label: "最近 2 小时",
    value: 2 * 60 * 60 * 1000,
  },
  {
    label: "最近 1 天",
    value: 24 * 60 * 60 * 1000,
  },
  {
    label: "最近 7 天",
    value: 7 * 24 * 60 * 60 * 1000,
  },
  {
    label: "最近 1 月",
    value: 30 * 24 * 60 * 60 * 1000,
  },
];

const getQueryFormConfig = (data = [], handleTimeChange, filterOption, superApp, isShow, error) => {
  //export const getEditionQueryXForm = (data, handleTimeChange, resetAllValue: Function) => {
  const customTimeOptions = [
    {
      label: "最近 2 小时",
      value: 2 * 60 * 60 * 1000,
    },
    {
      label: "最近 1 天",
      value: 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 7 天",
      value: 7 * 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 1 月",
      value: 30 * 24 * 60 * 60 * 1000,
    },
  ];
  const formMap = [
    {
      dataIndex: "queryIndex",
      title: "查询索引:",
      type: "input",
      placeholder: "请输入",
      rules: [
        {
          required: false,
          validator: (rule, value) => {
            if (value?.length > 128) {
              error.current = true;
              return Promise.reject("上限128字符");
            }
            error.current = false;
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "queryIndexSearch",
      title: "查询时间:",
      type: "custom",
      //component: <RangePicker showTime={{ format: "HH:mm" }} format="YYYY-MM-DD HH:mm" />,
      component: (
        <DRangeTime
          timeChange={handleTimeChange}
          popoverClassName="dashborad-popover"
          //resetAllValue={resetAllValue}
          customTimeOptions={customTimeOptions}
          defaultRangeKey={0}
        />
      ),
    },
  ] as IColumnsType[];

  isShow &&
    formMap.unshift({
      dataIndex: "totalCost",
      title: "总耗时:",
      type: "input",
      placeholder: "请输入",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value?.length > 6) {
              return Promise.reject("上限6字符");
            }
            return Promise.resolve();
          },
        },
      ],
    });

  superApp &&
    formMap.unshift({
      dataIndex: "clusterName",
      title: "所属集群:",
      type: "input",
      placeholder: "请输入",
      rules: [
        {
          required: false,
          validator: (rule, value) => {
            if (value?.length > 128) {
              error.current = true;
              return Promise.reject("上限128字符");
            }
            error.current = false;
            return Promise.resolve();
          },
        },
      ],
    });

  superApp &&
    formMap.unshift({
      dataIndex: "projectId",
      title: "所属应用:",
      type: "select",
      options: data?.map((item) => ({
        title: `${item.projectName}(${item.id})`,
        value: item.id,
      })),
      componentProps: {
        showSearch: true,
        filterOption: filterOption,
      },
      placeholder: "请选择",
    });

  return formMap;
};

export const SearchQueryForm: React.FC<SearchQueryFormPropsType> = memo(({ setSearchQuery, reload, isSlow = false, value }) => {
  // 默认近2小时
  const defaultRangeKey = 0;

  const queryParams = useRef({
    startTime: moment(new Date().getTime() - customTimeOptions[defaultRangeKey]?.value).valueOf(),
    endTime: moment().valueOf(),
    projectId: undefined,
    queryIndex: undefined,
    totalCost: undefined,
    clusterName: undefined,
  });
  const [form, setForm] = useState<any>();
  const buttonTime = useRef(null);
  const error = useRef(false);
  const superApp = isSuperApp();

  const onSearch = (result) => {
    // 校验不通过时不发送请求
    if (error.current) {
      return;
    }
    const { projectId = undefined, queryIndex, totalCost, queryIndexSearch, clusterName } = result || {};
    const { rangeTime } = queryIndexSearch || {};
    const time = rangeTime?.[1]?.valueOf() - rangeTime?.[0]?.valueOf();
    const currentTime = new Date().getTime();
    let _queryParams = {
      projectId,
      queryIndex,
      clusterName,
      startTime: queryIndexSearch ? (buttonTime.current ? currentTime - time : rangeTime?.[0].valueOf()) : undefined,
      endTime: queryIndexSearch ? (buttonTime.current ? currentTime : rangeTime?.[1].valueOf()) : undefined,
      totalCost,
    };
    queryParams.current = _queryParams;
    setSearchQuery && setSearchQuery(_queryParams);
  };

  const onReset = (result) => {
    queryParams.current = {} as any;
    setSearchQuery && setSearchQuery({});
  };

  useEffect(() => {
    sessionStorage.setItem(value, JSON.stringify(queryParams.current));
  }, [queryParams.current]);

  useEffect(() => {
    let params: any = sessionStorage.getItem(value);
    let _queryParams = queryParams.current;
    if (params) {
      params = JSON.parse(params);
      _queryParams = params;
      queryParams.current = _queryParams;
    }
    if (form) {
      form.setFieldsValue(_queryParams);
      setSearchQuery && setSearchQuery(_queryParams);
    }
  }, [form]);

  const handleTimeChange = (times: number[], periodOrPicker: boolean) => {
    if (times) {
      buttonTime.current = periodOrPicker;
    }
  };

  return (
    <>
      <QueryForm
        defaultCollapse
        columns={getQueryFormConfig(store.getState().app?.projectList, handleTimeChange, filterOption, superApp, isSlow, error)}
        onChange={() => null}
        getFormInstance={(form) => setForm(form)}
        onReset={onReset}
        onSearch={onSearch}
        isResetClearAll
      />
    </>
  );
});
