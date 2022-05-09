import { request, post, put, delete as deleteMethod } from "../../Utils/request";

export const queryMonitorRules = (params): any => {
  params.appId = 1;
  // return Promise.resolve({
  //   bizData: [
  //     {
  //       id: 1,
  //       name: 1,
  //       appId: 1,
  //       operator: 1,
  //       updateTime: "2021-04-01 09:08:07",
  //       priority: 1,
  //       catetgoryName: 1,
  //       objectNames: ["1用户1sdfsdfsd", "2用户2", "用户3sdf", "用户11dsfsd", "用户21sdfasdf", "用户31sdfsdfs"],
  //       status: 1,
  //     },
  //   ],
  //   pagination: {
  //     total: 100,
  //     pageNo: 2,
  //     pageSize: 10,
  //   },
  // });
  return post(`/v3/normal/monitor/monitorRules`, params);
};
export const queryAlarmSettingList = (params): any => {
  return post(`/v3/normal/monitor/notifyGroups`, params);
};
export const queryProjectDetail = (id): any => {
  return request(`/v3/normal/monitor/monitorRule/${id}`);
  // return Promise.resolve({
  //   chargeUserIdList: [],
  //   deptId: [1],
  //   description: "的目光；什么大概；吗的目光；的吗；老大哥父母官；里面的；廊坊美国；某个",
  //   isRunning: false,
  //   projectName: "测试001",
  // });
};

export const createMonitorRule = (params): any => {
  return post("/v3/normal/monitor/monitorRule", params);
};
export const updateMonitorRule = (params): any => {
  return put("/v3/normal/monitor/monitorRule", params);
};
//获取不同的告警指标
export const queryMetrics = (): any => {
  return request("/v3/normal/monitor/monitorRule/ssss/metrics");
};
//获取告警对象类型
export const queryMetricsMonitorRule = (): any => {
  return request("/v3/normal/monitor/monitorRule/category");
};
//获取统计类型
export const queryStatstype = (): any => {
  return request("/v3/normal/monitor/monitorRule/statsType");
};
//获取度量方式
export const queryOperator = (): any => {
  return request("/v3/normal/monitor/monitorRule/operator");
};
//获取日志
export const queryObjectNamesList = (url): any => {
  return request(url);
};



export const deleteMonitorRule = (id: number): any => {
  return deleteMethod(`/v3/normal/monitor/monitorRule/${id}`);
};

export const switchMonitorRuleStatus = (params): any => {
  // return Promise.resolve({
  //   code: 200,
  //   data: true,
  //   message: "成功",
  //   tips: "",
  // });
  return post(`/v3/normal/monitor/monitorRule/switch/${params.id}?status=${params.status}`, params);
};

export const queryProjectStatus = (id: number): any => {
  return Promise.resolve(true);
};

export const queryUserList = (name = ""): any => {
  // return request(`/v1/user/list/name/${name}`)
  return Promise.resolve([
    {
      id: 1,
      name: "测试1",
    },
    {
      id: 2,
      name: "测试2",
    },
  ]);
};

export const queryDeptTreeData = (): any => {
  // return request("/v1/dept/tree");
  return Promise.resolve([
    {
      id: 1,
      deptName: "测试1",
      childList: [
        {
          id: 11,
          deptName: "测试112",
          childList: [],
        },
      ],
    },
    {
      id: 2,
      deptName: "测试2",
      childList: [
        {
          id: 22,
          deptName: "测试114",
          childList: [],
        },
      ],
    },
  ]);
};
