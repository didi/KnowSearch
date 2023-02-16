import fetch from "../lib/fetch";
import { objTransUrlParams } from "../lib/utils";
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
  page: number;
  size: number;
  dslTemplateMd5?: string;
  queryIndex?: string;
  sortInfo?: string;
  orderByDesc?: boolean;
}

interface IDetail {
  dslTemplateMd5: string;
  endDate: number;
  startDate: number;
}

interface IqueryLimit {
  dslTemplateMd5: string;
  queryLimit: string;
  projectId: number;
}

export const getErrorQueryList = (params: IErrorList) => {
  return fetch(`/v3/dsl/template/error/page`, {
    method: "POST",
    body: params,
  });
};

export const getSlowQueryList = (params: ISlowList) => {
  return fetch(`/v3/dsl/template/slow/page`, {
    method: "POST",
    body: params,
  });
};

// 根据查询条件获取查询模板数据
export const getDslList = (params: IDslList) => {
  return fetch("/v3/dsl/template/page", {
    method: "POST",
    body: params,
  });
};

// 根据查询条件获取查询模板数据
export const changeStatus = (dslTemplateMd5: string, projectId) => {
  return fetch(`/v3/dsl/template/status/${dslTemplateMd5}?projectId=${projectId}`, {
    method: "PUT",
  });
};

// 修改查询模板限流值
export const queryLimit = (params: IqueryLimit[]) => {
  return fetch(`/v3/dsl/template/query-limit`, {
    method: "PUT",
    body: params,
  });
};

// 获取dsl的指标信息
export const getDslDetail = (dslTemplateMd5: string, projectId) => {
  return fetch(`/v3/dsl/template/detail/${dslTemplateMd5}?projectId=${projectId}`);
};

// 获取dsl的语句
export const getDslByIndex = (indexName: string) => {
  return fetch(`/v3/dsl/template/${indexName}`);
};

// 获取账号下已配置的列
export const getCheckedList = () => {
  return fetch("/v3/dsl/template/dsl-template-config", {
    prefix: Prefix,
    method: "POST",
    body: {
      userName: "",
      firstUserConfigType: "searchQuery",
      secondUserConfigType: "searchTemplate",
      userConfigTypes: [],
    },
  });
};

// 设置列
export const setCheckedList = (userConfigTypes: string[]) => {
  return fetch("/v3/dsl/template/dsl-template-config", {
    prefix: Prefix,
    method: "PUT",
    body: {
      userName: "",
      firstUserConfigType: "searchQuery",
      secondUserConfigType: "searchTemplate",
      userConfigTypes,
    },
  });
};
