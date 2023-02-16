import { AlertTwoTone } from "@ant-design/icons";
import fetch, { formFetch } from "../lib/fetch";

/*
 * 插件 相关接口
 */
export const getPlugList = () => {
  return fetch(`/v3/normal/ecm/plugin`);
};

// 获取插件列表
export const getOpClusterPlugList = (name: string) => {
  const cluster = name;
  return fetch(`/v3/cluster/phy/plugins/${cluster}`);
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
