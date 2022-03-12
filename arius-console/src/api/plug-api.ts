import { AlertTwoTone } from '@ant-design/icons';
import fetch, { formFetch } from '../lib/fetch';

/*
 * 插件 相关接口
 */
export const getPlugList = () => {
  return fetch(`/v3/normal/ecm/plugin`);
};

export const getClusterPlugList = (id: number) => {
  return fetch(`/v2/console/cluster/plugins?clusterId=${id}`);
};

export const getOpClusterPlugList = (name: string) => {
  const cluster = name;
  // return fetch(`/v2/op/cluster/plugins?cluster=${name}`);
  return fetch(`/v3/op/cluster/phy/plugins/${cluster}/get`);
};

export const addPlug = (params: any) => {
  const { creator, desc, name, md5, uploadFile, fileName, pDefault, logicClusterId, physicsClusterId } = params;
  const formData = new FormData();
  formData.append('uploadFile', uploadFile);
  formData.append('desc', desc || '');
  formData.append('md5', md5);
  formData.append('name', name);
  formData.append('creator', creator);
  formData.append('fileName', fileName);
  formData.append('pDefault', pDefault ? 'true' : 'false');
  // 后端需要返回值 physicClusterId， 没有 s
  formData.append('physicClusterId', physicsClusterId);
  if (logicClusterId) {
    formData.append('logicClusterId', logicClusterId + '');
  }
  return formFetch(`/v3/op/cluster/phy/plugins`, {
    method: 'POST',
    body: formData,
  })
};

export const editPlug = (id: number | string, desc: string) => {
  return fetch(`/v3/op/cluster/phy/plugins`, {
    method: 'PUT',
    body: {
      id: id,
      desc: desc
    }
  });
}

export const delPlug = (id: number) => {
  return fetch(`/v3/op/phy/cluster/plugin/${id}`, {
    method: 'DELETE',
  });
};

export const userDelPlug = (id: number) => {
  return fetch(`/v3/op/phy/cluster/plugin/${id}`, {
    method: 'DELETE',
  });
};
