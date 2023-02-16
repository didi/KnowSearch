import React from "react";
export * from "../config";
import { IMenuItem } from "typesPath/base-types";
import { OverviewView } from "./overview-view";
import { IndexView } from "./index-view";
import { NodeView } from "./node-view";
import { ClientNodeView } from "./clientnode-view";
import { ProjectView } from "./project-view";
import { QueryTemplate } from "./query-template";
import InfoTooltip from "component/infoTooltip";

export enum TAB_LIST_KEY {
  overview = "overview",
  node = "node",
  clientNode = "clientNode",
  index = "index",
  project = "project",
  queryTemplate = "queryTemplate",
}

export const TAB_LIST = [
  {
    name: "节点",
    key: TAB_LIST_KEY.node,
    content: () => <NodeView />,
  },
  {
    name: "ClientNode",
    key: TAB_LIST_KEY.clientNode,
    content: () => <ClientNodeView />,
  },
  {
    name: "索引",
    key: TAB_LIST_KEY.index,
    content: () => <IndexView />,
  },
  {
    name: "应用",
    key: TAB_LIST_KEY.project,
    content: () => <ProjectView />,
  },
  {
    name: "查询模板",
    key: TAB_LIST_KEY.queryTemplate,
    content: () => <QueryTemplate />,
  },
  {
    name: "总览",
    key: TAB_LIST_KEY.overview,
    content: () => <OverviewView />,
  },
];
const menuMap = new Map<string, IMenuItem>();

TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;

export const getRenderToolTip = (item) => {
  return item?.currentCalLogic || item?.price || item?.threshold ? (
    <InfoTooltip
      className="indicators-info"
      currentCalLogic={item?.currentCalLogic}
      price={item?.price}
      threshold={item?.threshold}
    ></InfoTooltip>
  ) : null;
};
