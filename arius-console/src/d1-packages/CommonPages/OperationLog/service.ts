import { request, post, put, delete as deleteMethod } from "../../Utils/request";

export const queryProjectList = (params): any => {
  // return Promise.resolve({
  //   bizData: [
  //     {
  //       createTime: 0,
  //       detail: "string",
  //       id: 0,
  //       operatePage: "string",
  //       operateType: "string",
  //       operatorIp: "string",
  //       operatorUsername: "string",
  //       target: "string",
  //       targetType: "string",
  //     },
  //   ],
  //   pagination: {
  //     pageNo: 0,
  //     pageSize: 0,
  //     pages: 0,
  //     total: 0,
  //   },
  // });
  return post(`/v1/oplog/page`, params);
};

export const queryOpLogDetail = (id): any => {
  // return Promise.resolve({
  //   createTime: 0,
  //   detail: "string",
  //   id: 0,
  //   operatePage: "string",
  //   operateType: "string",
  //   operatorIp: "string",
  //   operatorUsername: "string",
  //   target: "string",
  //   targetType: "string",
  // });
  return request(`/v1/oplog/${id}`);
};

export const createProject = (params): any => {
  return post("/v1/project", params);
};

export const updateProject = (params): any => {
  return put("/v1/project", params);
};

export const deleteProject = (id: number): any => {
  return deleteMethod(`/v1/project/${id}`);
};

export const switchProjectStatus = (id: number): any => {
  return put(`/v1/project/switch/${id}`);
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
