export * from "../config";

import React from "react";
import { IMenuItem } from "typesPath/base-types";
import { OverviewView } from "./overview-view";
import { IndexView } from "./index-view";
import { NodeView } from "./node-view";
import { IndexTemplateView } from "./index-template-view";
import moment from "moment";

export enum TAB_LIST_KEY {
  overview = "overview",
  node = "node",
  index = "index",
  template = "template",
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
    name: "索引视图",
    key: TAB_LIST_KEY.index,
    content: () => <IndexView />,
  },
  {
    name: "索引模板视图",
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
