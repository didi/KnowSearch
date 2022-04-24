import React from 'react';

export interface MenuConfItem {
  key?: string,
  name?: string | React.ReactNode,
  path?: string,
  icon?: string,
  type?: 'group',
  component?: React.ReactNode,
  children?: MenuConfItem[],
  visible?: boolean,
  rootVisible?: boolean,
  networkopsVisible?: boolean,
  to?: string,
  divider?: boolean,
  target?: string,
  getQuery?: (query: any) => any,
  permissionPoint?: string,
  isAbsolutePath?: boolean,
}

export interface TreeNode {
  id: number,
  pid: number,
  ident: string,
  name: string,
  path: string,
  type: number,
  leaf: number,
  cate?: string,
  note?: string,
  children?: TreeNode[],
  icon_color: string,
  icon_char: string,
  selectable?: boolean,
}

export interface ResponseDat {
  list: any[],
  total: number,
}

export interface Response {
  err: string,
  dat: any | ResponseDat,
}

export interface UserProfile {
  id: number,
  username: string,
  dispname: string,
  email: string,
  phone: string,
  im: string,
  isroot: boolean,
}

export interface Tenant {
  id: number,
  ident: string,
  name: string,
  note: string,
}

export interface IRouterTab {
  key: string,
  label: string,
  href: string,
  show: boolean,
}
