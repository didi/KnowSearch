export * from "../config";

import React from "react";
import { IMenuItem } from "typesPath/base-types";
import { OverviewView } from "./overview-view";
import { IndexView } from "./index-view";
import { NodeView } from "./node-view";
import { IndexTemplateView } from "./index-template-view";
import moment from "moment";
import { isSuperApp } from "lib/utils";

export enum TAB_LIST_KEY {
  overview = "overview",
  node = "node",
  index = "index",
  template = "template",
}


export const CLUSTER_KANBAN_MENU = () => {
  //普通侧和运维测tab需要更改
  const superApp = isSuperApp();
  if (!superApp) {
    return [
      {
        name: "索引",
        key: TAB_LIST_KEY.index,
      },
      {
        name: "索引模板",
        key: TAB_LIST_KEY.template,
      },
      {
        name: "总览",
        key: TAB_LIST_KEY.overview,
      },
      {
        name: "节点",
        key: TAB_LIST_KEY.node,
      },
    ];
  }
  return [
    {
      name: "总览",
      key: TAB_LIST_KEY.overview,
    },
    {
      name: "节点",
      key: TAB_LIST_KEY.node,
    },
    {
      name: "索引",
      key: TAB_LIST_KEY.index,
    },
    {
      name: "索引模板",
      key: TAB_LIST_KEY.template,
    },
  ];
};
export const TAB_LIST = [
  {
    name: "总览",
    key: TAB_LIST_KEY.overview,
    content: () => <OverviewView />,
  },
  {
    name: "节点",
    key: TAB_LIST_KEY.node,
    content: () => <NodeView />,
  },
  {
    name: "索引",
    key: TAB_LIST_KEY.index,
    content: () => <IndexView />,
  },
  {
    name: "索引模板",
    key: TAB_LIST_KEY.template,
    content: () => <IndexTemplateView />,
  },
];
const menuMap = new Map<string, IMenuItem>();

TAB_LIST.forEach((d) => {
  menuMap.set(d.key, d);
});

export const MENU_MAP = menuMap;

// 总览视图饼图
export const getContrastChartProps = (
  name,
  eChartsData,
  subtext,
  colors = ["#21CAB8", "#D3DAE7"]
) => {
  return {
    name: `${name}对比图`,
    eChartsData: eChartsData,
    colors: colors,
    text: name,
    subtext: subtext,
  };
};
