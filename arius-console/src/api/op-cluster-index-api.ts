import fetch, { formFetch } from "../lib/fetch";

export const getPhyNodeDivideList = (clusterId: number) => {
  return fetch(`/v3/cluster/phy/node/${clusterId}`); // regioninfo
};

// 批量下线离线节点
export const deleteNode = (params) => {
  return fetch(`/v3/cluster/phy/node`, {
    method: "DELETE",
    body: JSON.stringify(params),
  });
};
