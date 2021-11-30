import fetch from '../lib/fetch';
import { objTransUrlParams } from '../lib/utils';
const Prefix = "admin";
const POST = "POST";

interface IErrorList {
  queryIndex: string;
  startDate: string;
  endDate: string;
}

interface ISlowList {
  queryIndex: string;
  startDate: string;
  endDate: string;
  from: number;
  size: number;
}

interface IDslList {
  endTime: number;
  startTime: number;
  from: number;
  size: number;
  dslTemplateMd5?: string;
  queryIndex?: string;
  sortInfo?: string,
  orderByDesc?: boolean,
}

interface IDetail {
  dslTemplateMd5: string;
  endDate: number;
  startDate: number;
}

interface IqueryLimit {
  dslTemplateMd5List: string;
  queryLimit: string;
}

export const getErrorQueryList = (params: IErrorList) => {
  return fetch(`/v3/op/dsl/template/error/list`, {
    method: 'POST',
    body: params
  });
};

export const getSlowQueryList = (params: ISlowList) => {
  return fetch(`/v3/op/dsl/template/slow/list`, {
    method: 'POST',
    body: params
  });
};

// 根据查询条件获取查询模板数据
export const getDslList = (params: IDslList) => {
  return fetch('/v3/op/dsl/template/page', {
    method: 'POST',
    body: params
  });
};

// 根据查询条件获取查询模板数据
export const changeStatus = (dslTemplateMd5: string) => {
  return fetch(`/v3/op/dsl/template/change/status/${dslTemplateMd5}`, {
    method: 'PUT',
  });
};

// 修改查询模版限流值
export const queryLimit = (params: IqueryLimit) => {
  return fetch(`/v3/op/dsl/template/update/queryLimit?${objTransUrlParams(params)}`, {
    method: 'PUT'
  });
};

// 获取dsl的指标信息
export const getDslDetail = (dslTemplateMd5: string) => {
  return fetch(`/v3/op/dsl/template/detail/${dslTemplateMd5}`);
};

// 获取账号下已配置的列
export const getCheckedList = (secondMetricsType: string) => {
  return fetch("/v3/op/phy/cluster/metrics/configMetrics", {
    prefix: Prefix,
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "user_show",
      secondMetricsType: secondMetricsType,
      metricsTypes: []
    }
  });
}

// 设置列
export const setCheckedList = (secondMetricsType: string, metricsTypes: string[]) => {
  return fetch("/v3/op/phy/cluster/metrics/updateConfigMetrics", {
    prefix: Prefix,
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "user_show",
      secondMetricsType: secondMetricsType,
      metricsTypes: metricsTypes
    }
  });
}