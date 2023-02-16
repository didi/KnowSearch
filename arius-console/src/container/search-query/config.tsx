import React from "react";
import { Tooltip, message, Modal } from "antd";
import { IMenuItem } from "typesPath/base-types";
import { renderOperationBtns } from "container/custom-component";
import moment from "moment";
import { changeStatus } from "api/search-query";
import { SearchQueryPermissions, SearchTemplatePermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { ErrorQuery } from "./error-query";
import { SlowQuery } from "./slow-query";
import { copyString, transTimeFormat, formatDecimalPoint, getFormatJsonStr, isSuperApp } from "lib/utils";
import store from "store";
import { copyContentFn } from "knowdesign/lib/utils/tools";
import DRangeTime from "../../d1-packages/d-range-time";
import { IColumnsType } from "d1-packages/ProForm/QueryForm";
import { IconFont } from "@knowdesign/icons";
import { IWorkOrder } from "@types/params-types";
import { submitWorkOrder } from "api/common-api";

export enum TAB_LIST_KEY {
  queryTpl = "query-tpl",
  errorQuery = "error-query",
  slowQuery = "slow-query",
}
const appInfo = {
  app: store.getState().app.appInfo,
  user: store.getState().user.getName,
};

const { confirm } = Modal;

export const getTabList = (setReload?: any) => {
  return [
    {
      name: "慢查询列表",
      key: TAB_LIST_KEY.slowQuery,
      visible: hasOpPermission(SearchQueryPermissions.PAGE, SearchQueryPermissions.SLOW_LIST),
      content: (menu) => <SlowQuery menu={menu} />,
      renderCustomElement: (menu) => {
        return (
          <div className="search-query-reload" onClick={() => setReload(menu)}>
            <IconFont type="icon-shuaxin1" />{" "}
          </div>
        );
      },
      renderCustomElementClick: setReload,
    },
    {
      name: "异常查询列表",
      key: TAB_LIST_KEY.errorQuery,
      visible: hasOpPermission(SearchQueryPermissions.PAGE, SearchQueryPermissions.ERROR_LIST),
      content: (menu) => <ErrorQuery menu={menu} />,
      renderCustomElement: (menu) => {
        return (
          <div className="search-query-reload" onClick={() => setReload(menu)}>
            <IconFont type="icon-shuaxin1" />{" "}
          </div>
        );
      },
      renderCustomElementClick: setReload,
    },
  ];
};

export const menuMap = new Map<string, IMenuItem>();

getTabList().forEach((d) => {
  menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;

export const renderText = (text) => {
  return <div className="dsl-overflow-auto">{text}</div>;
};

export const filterColumnsList = [
  "searchCount",
  "totalCostAvg",
  "totalShardsAvg",
  "totalHitsAvg",
  "responseLenAvg",
  "ariusCreateTime",
  "timeStamp",
];

const formatProject = (id: number) => {
  const projectList = store.getState().app?.projectList;
  const projectInfo = projectList.find((item) => item.id === id);
  return projectInfo?.projectName || id;
};

export const customTimeOptions = [
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

export const getQueryTplColumns = (
  reloadData: Function,
  showDrawer: Function,
  showEditLimit: Function,
  superApp: boolean,
  setVisible,
  props
) => {
  const getCongigBtnList = (reloadData: Function, record: any, showEditLimit: Function) => {
    return [
      {
        label: `${record.enable || record.enable === null ? "禁用" : "启用"}`,
        invisible: !hasOpPermission(SearchTemplatePermissions.PAGE, SearchTemplatePermissions.DISABLE),
        clickFunc: () => {
          confirm({
            title: "提示",
            content: `确定${record.enable || record.enable === null ? "禁用" : "启用"}查询模板${record.dslTemplateMd5}？`,
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk: async () => {
              if (superApp) {
                return changeStatus(record.dslTemplateMd5, record.projectId).then((res) => {
                  reloadData();
                  message.success("操作成功");
                });
              }
              const params: IWorkOrder = {
                contentObj: {
                  name: record.dslTemplate,
                  projectId: record.projectId,
                  operator: appInfo.user("userName") || "",
                  dslTemplateMd5: record.dslTemplateMd5,
                },
                submitorProjectId: appInfo.app()?.id,
                submitor: appInfo.user("userName") || "",
                description: "",
                type: "dslTemplateStatusChange",
              };
              await submitWorkOrder(params, props?.history, () => reloadData(), 500);
            },
          });
        },
      },
      {
        label: "修改限流值",
        invisible: !hasOpPermission(SearchTemplatePermissions.PAGE, SearchTemplatePermissions.MODIFY_LIMIT),
        clickFunc: () => {
          showEditLimit(record);
        },
      },
    ];
  };
  const orderColumns = [
    {
      title: "查询模板",
      dataIndex: "dslTemplate",
      fixed: "left",
      width: 100,
      render: (text: any, record: any) => {
        const btns: any = [
          {
            label: (
              <Tooltip placement="right" title={renderText(text)}>
                <div className="two-row-ellipsis dsl-vw-5">{text}</div>
              </Tooltip>
            ),
            clickFunc: () => {
              showDrawer({ ...record, projectName: formatProject(record.projectId) });
            },
          },
        ];
        return renderOperationBtns(btns, record);
      },
    },
    {
      title: "所属应用",
      dataIndex: "projectId",
      invisible: !superApp,
      width: 100,
      render: (text) => {
        const projectName = formatProject(text);
        return (
          <Tooltip placement="right" title={renderText(projectName)}>
            <div className="two-row-ellipsis dsl-vw-5">{projectName}</div>
          </Tooltip>
        );
      },
    },
    {
      title: "所属查询索引",
      dataIndex: "indices",
      width: 130,
      render: (text) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div className="two-row-ellipsis dsl-vw-5">{text}</div>
        </Tooltip>
      ),
    },
    {
      title: "查询模板MD5",
      dataIndex: "dslTemplateMd5",
      width: 150,
      render: (text) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div className="two-row-ellipsis dsl-vw-5">{text}</div>
        </Tooltip>
      ),
    },
    {
      title: "请求数(次/分钟)",
      dataIndex: "searchCount",
      width: 140,
      sorter: true,
    },
    {
      title: "耗时(ms)",
      dataIndex: "totalCostAvg",
      sorter: true,
      width: 100,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "总shard数",
      dataIndex: "totalShardsAvg",
      width: 100,
      sorter: true,
    },
    {
      title: "单次命中数",
      dataIndex: "totalHitsAvg",
      sorter: true,
      width: 130,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "单次响应长度",
      dataIndex: "responseLenAvg",
      sorter: true,
      width: 130,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "创建时间",
      dataIndex: "ariusCreateTime",
      sorter: true,
      width: 150,
      render: (text) => transTimeFormat(text),
    },
    {
      title: "最近使用时间",
      dataIndex: "timeStamp",
      sorter: true,
      width: 150,
      render: (text) => transTimeFormat(text),
    },
    {
      title: "限流值(s)",
      dataIndex: "queryLimit",
      sorter: true,
      width: 100,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "状态",
      dataIndex: "enable",
      sorter: true,
      width: 100,
      render: (text) => {
        if (text === null || text) {
          return "启用";
        } else {
          return "禁用";
        }
      },
    },
    {
      title: (
        <div className="option-filter-columns">
          <span>操作</span>
        </div>
      ),
      dataIndex: "operation",
      filterTitle: true,
      width: 180,
      key: "operation",
      fixed: "right",
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, record, showEditLimit);
        return <div>{renderOperationBtns(btns, record)}</div>;
      },
    },
  ];
  return orderColumns.filter((item) => !item.invisible);
};
export interface IPeriod {
  label: string;
  key: string;
  dateRange: [moment.Moment, moment.Moment];
}

export const PERIOD_RADIO = [
  {
    label: "近1天",
    key: "oneDay",
    get dateRange() {
      return [moment().subtract(1, "day"), moment()];
    },
  },
  {
    label: "近7天",
    key: "sevenDay",
    get dateRange() {
      return [moment().subtract(1, "week"), moment()];
    },
  },
  {
    label: "近1月",
    key: "thirtyDay",
    get dateRange() {
      return [moment().subtract(1, "month"), moment()];
    },
  },
] as IPeriod[];

const periodRadioMap = new Map<string, IPeriod>();

PERIOD_RADIO.forEach((p) => {
  periodRadioMap.set(p.key, p);
});

export const PERIOD_RADIO_MAP = periodRadioMap;

const errorQueryColumnsRender = (item) => {
  return (
    <Tooltip placement="right" title={renderText(item)}>
      <div className="error-query-container-table-cell two-row-ellipsis"> {item || "-"}</div>
    </Tooltip>
  );
};

export const errorQueryColumns = (superApp: boolean) => {
  const columns = [
    {
      title: "请求URL",
      dataIndex: "uri",
      render: (item) => errorQueryColumnsRender(item),
      fixed: "left",
    },
    {
      title: "查询语句",
      dataIndex: "dsl",
      render: (item) => errorQueryColumnsRender(item),
    },
    {
      title: "所属应用",
      dataIndex: "projectId",
      invisible: !superApp,
      render: (text) => errorQueryColumnsRender(formatProject(text)),
    },
    {
      title: "所属集群",
      dataIndex: "clusterName",
      invisible: !superApp,
      render: (text) => errorQueryColumnsRender(text),
    },
    {
      title: "所属查询索引",
      dataIndex: "indices",
      render: (item) => errorQueryColumnsRender(item),
    },
    {
      title: "查询时间",
      dataIndex: "timeStamp",
      render: (text) => {
        const content = transTimeFormat(text);
        return (
          <div className="error-query-container-table-cell two-row-ellipsis">
            <Tooltip placement="right" title={renderText(content)}>
              {content}
            </Tooltip>
          </div>
        );
      },
    },
    {
      title: "错误信息",
      dataIndex: "exceptionName",
      render: (item) => errorQueryColumnsRender(item),
    },
  ];
  return columns.filter((item) => !item.invisible);
};

export const slowQueryColumns = (superApp: boolean) => {
  const columns = [
    {
      title: "查询语句",
      dataIndex: "dsl",
      render: (text, data) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div
            className="two-row-ellipsis slow-query-container-table-cell"
            onClick={() => {
              copyString(`GET ${data?.indices}/_search\n${data?.dsl}`);
            }}
          >
            <a href="javascript:;">{text}</a>
          </div>
        </Tooltip>
      ),
      fixed: "left",
    },
    {
      title: "所属应用",
      dataIndex: "projectId",
      invisible: !superApp,
      render: (text) => {
        const projectName = formatProject(text);
        return (
          <Tooltip placement="right" title={renderText(projectName)}>
            <div className="two-row-ellipsis slow-query-container-table-cell">{projectName}</div>
          </Tooltip>
        );
      },
    },
    {
      title: "所属集群",
      dataIndex: "clusterName",
      invisible: !superApp,
      render: (text) => errorQueryColumnsRender(text),
    },
    {
      title: "所属查询索引",
      dataIndex: "indices",
      render: (text) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div className="two-row-ellipsis slow-query-container-table-cell">{text || "-"}</div>
        </Tooltip>
      ),
    },
    {
      title: "响应时间(ms)",
      dataIndex: "esCost",
      sorter: (a, b) => a.esCost - b.esCost,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "总耗时(ms)",
      dataIndex: "totalCost",
      sorter: (a, b) => a.totalCost - b.totalCost,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "单次命中数",
      dataIndex: "totalHits",
      sorter: (a, b) => a.totalHits - b.totalHits,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "单次响应长度",
      dataIndex: "responseLen",
      sorter: (a, b) => a.responseLen - b.responseLen,
      render: (text) => formatDecimalPoint(text),
    },
    {
      title: "查询时间",
      dataIndex: "timeStamp",
      render: (text) => {
        return transTimeFormat(text);
      },
    },
    {
      title: "是否超时",
      dataIndex: "isTimedOut",
    },
  ];
  return columns.filter((item) => !item.invisible);
};

export const getIndexColumns = () => {
  return [
    {
      title: "字段名",
      dataIndex: "name",
      key: "name",
      width: 70,
      render: (value) => {
        return (
          <>
            <Tooltip title={value}>
              <div
                className="mapping-name"
                onClick={() => {
                  copyContentFn(value);
                }}
              >
                {value || "-"}
              </div>
            </Tooltip>
          </>
        );
      },
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 65,
    },
    {
      title: "是否排序/聚合",
      dataIndex: "doc_value",
      key: "doc_value",
      width: 95,
      render: (value) => {
        return (
          <>
            <span>{value ? "是" : "否"}</span>
          </>
        );
      },
    },
    {
      title: "检索方式",
      dataIndex: "search",
      key: "search",
      width: 65,
    },
  ];
};
export const TAB_JSON = [
  {
    label: "TABLE",
    key: "TABLE",
  },
  {
    label: "JSON",
    key: "JSON",
  },
];

export const getResTableColumns = (checkedList: any[] = []) => {
  const arr = [];
  for (const key of checkedList) {
    if (key) {
      arr.push({
        title: key,
        dataIndex: key,
        width: 150,
        key,
        render: (text) => {
          if (typeof text === "object") {
            let str = "-";
            try {
              str = getFormatJsonStr(text);
            } catch (err) {
              console.log(err);
            }
            return (
              <>
                <Tooltip
                  title={
                    <div className="tooltip-title">
                      <pre>{str}</pre>
                    </div>
                  }
                >
                  <div className="text-oh">{str}</div>
                </Tooltip>
              </>
            );
          }
          return (
            <>
              <Tooltip title={text}>
                <div className="text-oh">{text}</div>
              </Tooltip>
            </>
          );
        },
      });
    }
  }
  return arr;
};

export type selectDt = {
  value: string;
  label: string;
};

export const getQueryFormConfig = (data = [], handleTimeChange, filterOption, superApp, error) => {
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
      dataIndex: "MD5",
      title: "查询模板MD5:",
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
      dataIndex: "queryIndexTime",
      title: "最近使用时间:",
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
