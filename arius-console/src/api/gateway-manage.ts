import fetch from "../lib/fetch";
const prefix = "/v3";

// 获取gateway集群列表
export const getGatewayList = (data) => {
  return fetch(`${prefix}/gateway/page`, {
    method: "POST",
    body: data,
  });
};

// 接入gateway集群
export const joinGateway = (data) => {
  return fetch(`${prefix}/gateway/join`, {
    method: "POST",
    body: data,
  });
};

// 新建gateway集群
export const addGateway = (data) => {
  return fetch(`${prefix}/op-task/gateway-create`, {
    method: "POST",
    body: data,
  });
};

// 获取gateway版本信息
export const getGatewayVersion = () => {
  return fetch(`${prefix}/op/software/package/gatewayInstallPackage/version/list`);
};

// 获取gateway配置组信息
export const getGatewayConfig = (version) => {
  return fetch(`${prefix}/gateway/config/${version}`);
};

// 升级gateway版本
export const updateGatewayVersion = (data) => {
  return fetch(`${prefix}/op-task/gateway-upgrade`, {
    method: "POST",
    body: data,
  });
};

// 回滚gateway版本
export const rollbackGatewayVersion = (data) => {
  return fetch(`${prefix}/op-task/gateway-config-rollback`, {
    method: "POST",
    body: data,
  });
};

// 回滚gateway
export const rollbackGateway = (data) => {
  return fetch(`${prefix}/op-task/gateway-rollback`, {
    method: "POST",
    body: data,
  });
};

// 编辑gateway集群
export const updateGateway = (id, data) => {
  return fetch(`${prefix}/gateway/${id}`, {
    method: "PUT",
    body: data,
  });
};

// 下线gateway集群
export const deleteGateway = (id: number) => {
  return fetch(`${prefix}/gateway/${id}`, {
    method: "DELETE",
  });
};

// 获取指定gateway集群的节点列表信息
export const getGatewayNode = (gatewayId, data) => {
  return fetch(`${prefix}/gateway/node/${gatewayId}/page`, {
    method: "POST",
    body: data,
  });
};

// 重启gateway集群
export const resetGateway = (data) => {
  return fetch(`${prefix}/op-task/gateway-restart`, {
    method: "POST",
    body: data,
  });
};

// gateway扩容
export const expandGateway = (data) => {
  return fetch(`${prefix}/op-task/gateway-expand `, {
    method: "POST",
    body: data,
  });
};

// gateway缩容
export const shrinkGateway = (data) => {
  return fetch(`${prefix}/op-task/gateway-shrink`, {
    method: "POST",
    body: data,
  });
};

// 获取gateway集群详细信息
export const getGatewayClusterDetail = (id: number) => {
  return fetch(`/v3/gateway/${id}`);
};

// 获取gateway集群上一个版本
export const getGatewayLastVersion = (id: number) => {
  return fetch(`/v3/gateway/${id}/lower-versions`);
};

// 获取gateway集群配置列表
export const getGatewayConfigList = (gatewayClusterId: number, data) => {
  return fetch(`${prefix}/gateway/config/${gatewayClusterId}/page`, {
    method: "POST",
    body: data,
  });
};

// 根据 gatewayClusterId 获取 gateway 配置信息
export const getGatewayConfigDetail = (id: number) => {
  return fetch(`/v3/gateway/config/${id}/configs`);
};

// GATEWAY集群配置编辑
export const editGatewayConfig = (params: any) => {
  return fetch(`${prefix}/op-task/gateway-config-edit`, {
    method: "POST",
    body: params,
  });
};

// 根据 gatewayClusterId 和configId获取可以回滚的配置信息
export const getGatewayRollbackConfig = (gatewayClusterId: number, configId: number) => {
  return fetch(`/v3/gateway/config/${gatewayClusterId}/${configId}/rollback`);
};

// GATEWAY集群配置回滚
export const rollbackGatewayConfig = (params) => {
  return fetch(`/v3/op-task/gateway-config-rollback`, {
    method: "POST",
    body: params,
  });
};

// 获取当前操作项目下的集群是否绑定gateway
export const checkClusterBindGateway = () => {
  return fetch(`/v3/security/project/check-cluster-bind-gateway`);
};
