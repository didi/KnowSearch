import { IPhyConfig } from '@types/cluster/physics-type';
import fetch from '../lib/fetch';

export const addConfig = (params: IPhyConfig) => {
  return fetch(`/v3/normal/ecm/config`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

// 获取ES集群配置接口
export const getPhyClusterConfigList = (clusterId: number) => {
  return fetch(`/v3/op/cluster/config/${clusterId}/list`);
};

// 获取ES集群配置接口
export const getPhyClusterConfig = (configId: number) => {
  return fetch(`/v3/op/cluster/config/${configId}`);
};

// 删除ES集群配置接口
export const delConfig = (configId: number) => {
  return fetch(`/v3/op/cluster/config/${configId}`, {
    method: 'DELETE',
  });
};

// 创建ES集群配置接口
export const creatConfig = (params: any) => {
  return fetch(`/v3/op/cluster/config`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

// 更新ES集群配置接口
export const updateConfig = (config: any) => {
  return fetch(`/v3/op/cluster/config`, {
    method: 'PUT',
    body: JSON.stringify(config),
  });
};

// 获取可操作配置文件的ES集群角色接口
export const getConfigRole = (configId: number) => {
  return fetch(`/v3/op/cluster/config/${configId}/roles`);
};

// 获取ES集群模板配置接口
export const getClusterTemplateCentent = (type: string) => {
  return fetch(`/v3/op/cluster/config/${type}/template`);
};

// 更新ES集群配置描述
export const updateCongig = (config: any) => {
  return fetch(`/v3/op/cluster/config`, {
    method: 'PUT',
    body: JSON.stringify(config),
  });
};
