import fetch from "../lib/fetch";

// GET /v3/cluster/phy/region/${clusterName}/${clusterLogicType} 获取物理集群region列表接口
export const getPhyClusterRegionList = (clusterName: string, clusterLogicType, params) => {
  return fetch(`/v3/cluster/phy/region/${clusterName}/${clusterLogicType}`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const logicClusterRegionList = (logicClusterId: any) => {
  return fetch(`/v3/cluster/logic/region?logicClusterId=${logicClusterId}`);
};

export const logicClusterNodesList = (id) => {
  return fetch(`/v3/cluster/phy/region/${id}/nodes`);
};

export const getRegionNodeSpec = (logicClusterId: any) => {
  return fetch(`/v3/cluster/logic/dataNodeSpec/${logicClusterId}`);
};
