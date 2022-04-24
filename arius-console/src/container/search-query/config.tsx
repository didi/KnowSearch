import React from "react";
import { Tooltip, message, Modal } from 'antd';
import { IMenuItem } from "typesPath/base-types";
import { renderOperationBtns } from 'container/custom-component';
import { QueryTpl } from './query-tpl';
import moment from "moment";
import { changeStatus  } from 'api/search-query';

export enum TAB_LIST_KEY {
  queryTpl = "query-tpl",
  errorQuery = "error-query",
  slowQuery = "slow-query",
}

const { confirm } = Modal;

import { ErrorQuery } from "./error-query";
import { SlowQuery } from "./slow-query";
import { copyString } from "lib/utils";

export const TAB_LIST = [
  {
    name: "查询模板列表",
    key: TAB_LIST_KEY.queryTpl,
    content: () => <QueryTpl />,
  },
  {
    name: "异常查询列表",
    key: TAB_LIST_KEY.errorQuery,
    content: () => <ErrorQuery />,
  },
  {
    name: "慢查询列表",
    key: TAB_LIST_KEY.slowQuery,
    content: () => <SlowQuery />,
  },
];
const menuMap = new Map<string, IMenuItem>();

TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;


export const renderText = (text) => {
  return (
    <div className="dsl-overflow-auto">
      {text}
    </div>
  )
}

export const cherryList = ['searchCount', 'totalCostAvg', 'totalShardsAvg', 'totalHitsAvg', 'responseLenAvg', 'ariusCreateTime', 'timeStamp'];

export const getQueryTplColumns = (reloadData: Function, showDrawer: Function, showEditLimit: Function) => {
  const getCongigBtnList = (reloadData:Function, record: any, showEditLimit: Function) => {
    return [
      {
        label: `${record.enable || record.enable === null ? '禁用' : '启用'}`,
        clickFunc: () => {
          confirm({
            title: "提示",
            content: `确定${record.enable || record.enable === null ? '禁用' : '启用'}查询模板${record.dslTemplateMd5}？`,
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              changeStatus(record.dslTemplateMd5)
                .then((res) => {
                  if (res) {
                    reloadData();
                    message.success('操作成功');
                  }
                })
            },
          });
          
        },
      },
      {
        label: '修改限流值',
        clickFunc: () => {
          showEditLimit(record);
        }
      },
    ];
  }
  const orderColumns = [
    {
      title: '查询模板',
      dataIndex: 'dslTemplate',
      fixed: "left",
      render: (text: any, record: any) => {
        const btns: any = [
          {
            label: (<Tooltip placement="right" title={renderText(text)}>
            <div className="two-row-ellipsis dsl-vw-5">{text}</div>
          </Tooltip>),
            clickFunc: () => {
              showDrawer(record);
            }
          },
        ]
        return renderOperationBtns(btns, record);
      },
    },
    {
      title: '所属查询索引',
      dataIndex: 'indices',
      render: (text) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div className="two-row-ellipsis dsl-vw-5">{text}</div>
        </Tooltip>
      ),
    },
    {
      title: '查询模板MD5',
      dataIndex: 'dslTemplateMd5',
      render: (text) => (
        <Tooltip placement="right" title={renderText(text)}>
          <div className="two-row-ellipsis dsl-vw-5">{text}</div>
        </Tooltip>
      ),
    },
    {
      title: '请求数(次/分钟)',
      dataIndex: 'searchCount',
      width: 140,
      sorter: (a, b) => a.searchCount - b.searchCount,
    },
    {
      title: '耗时(ms)',
      dataIndex: 'totalCostAvg',
      sorter: (a, b) => a.totalCostAvg - b.totalCostAvg,
      render: (text) => {
        return Number(text).toFixed(2)
      }
    },
    {
      title: '总shard数',
      dataIndex: 'totalShardsAvg',
      sorter: (a, b) => a.totalShardsAvg - b.totalShardsAvg,
    },
    {
      title: '总命中数',
      dataIndex: 'totalHitsAvg',
      sorter: (a, b) => a.totalHitsAvg - b.totalHitsAvg,
      render: (text, record) => {
        return Number(text * record.searchCount).toFixed(2)
      }
    },
    {
      title: '响应长度',
      dataIndex: 'responseLenAvg',
      sorter: (a, b) => a.responseLenAvg - b.responseLenAvg,
      render: (text, record) => {
        return Number(text * record.searchCount).toFixed(2)
      }
    },
    {
      title: '创建时间',
      dataIndex: 'ariusCreateTime',
      sorter: (a, b) => a.ariusCreateTime - b.ariusCreateTime,
      render: (text) => {
        return moment(text).format("YYYY-MM-DD HH:mm:ss");
      }
    },
    {
      title: '最近使用',
      dataIndex: 'timeStamp',
      sorter: (a, b) => a.timeStamp - b.timeStamp,
      render: (text) => {
        return moment(text).format("YYYY-MM-DD HH:mm:ss");
      }
    },
    {
      title: '限流值(s)',
      dataIndex: 'queryLimit',
      sorter: (a, b) => a.queryLimit - b.queryLimit,
      render: (text: string) => {
        return Number(text).toFixed(2)
      }
    },
    {
      title: '状态',
      dataIndex: 'enable',
      sorter: (a, b) => a.status - b.status,
      render: (text) => {
        if (text === null || text) {
          return '启用'
        } else {
          return '禁用'
        }
      }
    },
    {
      title: '操作',
      dataIndex: 'operation',
      width: 130,
      key: 'operation',
      fixed: 'right',
      render: (text: any, record: any) => {
        const btns: any = getCongigBtnList(reloadData, record, showEditLimit);
        return (
          <div>
            {renderOperationBtns(btns, record)}
          </div>
        );
      },
    }
  ];
  return orderColumns;
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
  return <Tooltip placement="right" title={renderText(item)}><div className="error-query-container-table-cell two-row-ellipsis"> {item || '-'}</div></Tooltip>;
};

export const errorQueryColumns = [
  {
    title: "请求URL",
    dataIndex: "uri",
    render: (item) => errorQueryColumnsRender(item),
  },
  {
    title: "查询语句",
    dataIndex: "dsl",
    render: (item) => errorQueryColumnsRender(item),
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
      return <div className="error-query-container-table-cell two-row-ellipsis"> <Tooltip placement="right" title={renderText(moment(text).format("YYYY-MM-DD HH:mm:ss"))}>{moment(text).format("YYYY-MM-DD HH:mm:ss")}</Tooltip></div>;
    }
  },
  {
    title: "错误信息",
    dataIndex: "exceptionName",
    render: (item) => errorQueryColumnsRender(item),
  },
];

export const slowQueryColumns = [
  {
    title: "查询语句(点击复制)",
    dataIndex: "dsl",
    render: (text, data) => (
      <Tooltip placement="right" title={renderText(text)}>
        <div className="two-row-ellipsis slow-query-container-table-cell" onClick={() => {
          copyString(`GET ${data?.indices}/_search\n${data?.dsl}`)
        }}><a href="javascript:;">{text}</a></div>
      </Tooltip>
    ),
  },
  {
    title: "所属查询索引",
    dataIndex: "indices",
    render: (text) => (
      <Tooltip placement="right" title={renderText(text)}>
        <div className="two-row-ellipsis slow-query-container-table-cell">{text}</div>
      </Tooltip>
    ),
  },
  {
    title: "响应时间(ms)",
    dataIndex: "esCost",
    sorter: (a, b) => a.esCost - b.esCost,
  },
  {
    title: "总耗时(ms)",
    dataIndex: "totalCost",
    sorter: (a, b) => a.totalCost - b.totalCost,
  },
  {
    title: "总命中数",
    dataIndex: "totalHits",
    sorter: (a, b) => a.totalHits - b.totalHits,
  },
  {
    title: "响应长度",
    dataIndex: "responseLen",
    sorter: (a, b) => a.responseLen - b.responseLen,
  },
  {
    title: "查询时间",
    dataIndex: "timeStamp",
    render: (text) => {
      return moment(text).format("YYYY-MM-DD HH:mm:ss");
    }
  },
  {
    title: "是否超时",
    dataIndex: "isTimedOut",
  },
];
