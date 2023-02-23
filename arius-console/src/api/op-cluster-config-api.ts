import fetch from "../lib/fetch";

// 获取ES集群配置接口
export const getPhyClusterConfigList = (clusterId: number) => {
  return fetch(`/v3/cluster/phy/config-file?clusterId=${clusterId}`);
};

// 获取ES集群配置接口
export const getPhyClusterConfig = (configId: number) => {
  return fetch(`/v3/cluster/phy/config-file/${configId}`);
};

// 获取ES集群模板配置接口
export const getClusterTemplateCentent = (type: string) => {
  return fetch(`/v3/cluster/phy/config-file/template/${type}`);
};
