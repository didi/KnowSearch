import { AlertTwoTone } from "@ant-design/icons";
import fetch, { formFetch } from "../lib/fetch";

/*
 * 插件 相关接口
 */
export const getPlugList = () => {
  return fetch(`/v3/normal/ecm/plugin`);
};

// 获取插件列表
export const getPhyClusterPlugList = (id) => {
  return fetch(`/v3/cluster/phy/plugins?clusterPhyId=${id}`);
};

// 添加插件
export const addPlug = (params: any) => {
  const { creator, desc, name, md5, uploadFile, fileName, pDefault, logicClusterId, physicsClusterId } = params;
  const formData = new FormData();
  formData.append("uploadFile", uploadFile);
  formData.append("desc", desc || "");
  formData.append("md5", md5);
  formData.append("name", fileName);
  formData.append("creator", creator);
  formData.append("fileName", fileName);
  formData.append("pDefault", pDefault);
  // 后端需要返回值 physicClusterId， 没有 s
  formData.append("physicClusterId", physicsClusterId);
  if (logicClusterId) {
    formData.append("logicClusterId", logicClusterId + "");
  }
  return formFetch(`/v3/cluster/phy/plugins`, {
    method: "POST",
    body: formData,
  });
};

// 编辑插件
export const editPlug = (id: number | string, desc: string) => {
  return fetch(`/v3/cluster/phy/plugins`, {
    method: "PUT",
    body: {
      id: id,
      desc: desc,
    },
  });
};

// 删除插件
export const userDelPlug = (id: number) => {
  return fetch(`/v3/cluster/phy/plugins/${id}`, {
    method: "DELETE",
  });
};

// 获取插件状态列表（插件重启）
export const getPluginStatus = (pluginId: number) => {
  return fetch(`/v3/cluster/phy/plugins/${pluginId}/configs`);
};

// 获取插件版本列表
export const pluginVersions = () => {
  return fetch(`/v3/cluster/phy/plugins`);
};

// 升级插件
export const upgradePlug = (data: any) => {
  return fetch(`/v3/cluster/phy/plugins/${data.id}`, {
    method: "POST",
    body: data,
  });
};

// 重启插件
export const resetPlug = (data: any) => {
  return fetch(`/v3/cluster/phy/plugins/${data.id}`, {
    method: "POST",
    body: data,
  });
};

// 卸载插件
export const uninstallPlug = (params) => {
  return fetch(`/v3/op-task/es-cluster-plug-uninstall`, {
    method: "POST",
    body: params,
  });
};

// 变更插件配置信息
export const updatePluginConfig = (data) => {
  return fetch(`/v3/op-task/es-cluster-plugin-config`, {
    method: "POST",
    body: data,
  });
};

// 集群插件升级
export const opUpgradePlug = (data) => {
  return fetch(`/v3/op-task/es-cluster-plug-upgrade`, {
    method: "POST",
    body: data,
  });
};

// 集群插件重启
export const opResetPlug = (data) => {
  return fetch(`/v3/op-task/es-cluster-plug-restart`, {
    method: "POST",
    body: data,
  });
};

// 集群插件配置回滚
export const opRollbackPluginConfig = (data) => {
  return fetch(`/v3/op-task/es-cluster-plugin-config-rollback`, {
    method: "POST",
    body: data,
  });
};

// 集群插件升级回滚
export const opRollbackUpdatePlugin = (data) => {
  return fetch(`/v3/op-task/es-cluster-plugin-rollback`, {
    method: "POST",
    body: data,
  });
};
