import React from "react";
export * from "../config";
import { IMenuItem } from "typesPath/base-types";
import { OverviewView } from "./overview-view";
import { IndexView } from "./index-view";
import { NodeView } from "./node-view";
import { ClientNodeView } from "./clientnode-view";
import { ProjectView } from "./project-view";
import { QueryTemplate } from "./query-template";

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
    name: "总览视图",
    key: TAB_LIST_KEY.overview,
    content: () => <OverviewView />,
  },
  {
    name: "节点视图",
    key: TAB_LIST_KEY.node,
    content: () => <NodeView />,
  },
  {
    name: "ClientNode视图",
    key: TAB_LIST_KEY.clientNode,
    content: () => <ClientNodeView />,
  },
  {
    name: "索引视图",
    key: TAB_LIST_KEY.index,
    content: () => <IndexView />,
  },
  {
    name: "项目视图",
    key: TAB_LIST_KEY.project,
    content: () => <ProjectView />,
  },
  {
    name: "查询模版",
    key: TAB_LIST_KEY.queryTemplate,
    content: () => <QueryTemplate />,
  },
];
const menuMap = new Map<string, IMenuItem>();

TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;
