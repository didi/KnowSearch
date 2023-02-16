import fetch from "../lib/fetch";

export const getLogicClusterNodeList = (clusterId: number) => {
  return fetch(`/v3/cluster/logic/node/${clusterId}`);
};
