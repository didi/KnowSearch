import React from "react";
import { IMenuItem } from "../../../CommonComponents/HashMenu/hash-menu";

export enum TAB_LIST_KEY {
  adminAuthor = "adminAuthor",
  seeAuthor = "seeAuthor",
}

export const TAB_LIST = [
  {
    name: "管理权限",
    key: TAB_LIST_KEY.adminAuthor,
    content: <></>,
  },
  {
    name: "查看权限",
    key: TAB_LIST_KEY.seeAuthor,
    content: <></>,
  },
];

const menuMap = new Map<string, IMenuItem>();
TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});
export const MENU_MAP = menuMap;

export enum VALUE_KEY {
  project = "project",
  resourceCategory = "resourceCategory",
  resourceDetails = "resourceDetails",
}

export const optionsWithDisabled = [
  { label: "项目", value: VALUE_KEY.project, showLevel: 1 },
  { label: "资源类别", value: VALUE_KEY.resourceCategory, showLevel: 2 },
  { label: "资源明细", value: VALUE_KEY.resourceDetails, showLevel: 3 },
];

export const TITLE_MAP = {
  [VALUE_KEY.project]: ["未分配项目", "已分配项目"],
  [VALUE_KEY.resourceCategory]: ["未分配类别", "已分配类别"],
  [VALUE_KEY.resourceDetails]: ["未分配资源", "已分配资源"],
};

export const ParticleType = ["项目", "资源类别", "资源明细"];
