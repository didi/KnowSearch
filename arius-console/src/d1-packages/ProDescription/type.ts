import React, { ReactNode } from 'react';

export enum BASIS_TYPE {
  /* 展示标签 */
  tag = 'tag',
  /* 展示内容高亮 */
  highlight = 'highlight',
  /* 是否可编辑 */
  editable = 'editable',
}

export interface optionItemType {
  label: string | ReactNode;
  key?: string | number;
  /* 内容 */
  content?: any;
  /* 是否支持复制 */
  copy?: boolean;
  /* 单条详情所占行数，默认是一行 */
  span?: number;
  /* 自定义配置显示内容，提供展示tag */
  customType?: BASIS_TYPE;
  /* 可自定义lable的行内样式 */
  labelStyle?: React.CSSProperties;
  /**
   * @render 可以自定义渲染的内容，展示的逻辑，customType为custom时
   * @ct 当前详情的内容
   */
  render?: (ct: any) => any;
}

export interface propsType {
  /* 是否有标题 */
  title?: string | ReactNode;
  /* 详情数据 */
  // optionList: optionItemType[];
  dataSource: any;
  /* 需要显示的配置 */
  config: optionItemType[];
  /* lable的宽度，默认80px,可自定义 */
  labelWidth?: string;
  /* 可自定义lable的行内样式 */
  labelStyle?: React.CSSProperties;
  /* 需要 ':' 符号, 默认不显示 */
  needColon?: boolean;
  /* 标题样式 */
  titleStyle?: React.CSSProperties;
  /* 屏幕宽度小于1920的时候一行展示几条 */
  xl?: number;
  /* 屏幕宽度大于1920的时候一行展示几条 */
  xxl?: number;
  /**
   * @param data 详情数据
   * @param config 需要显示的配置
   */
  getBasisInfoConfig?: (data: any, config: optionItemType[]) => optionItemType[];
}