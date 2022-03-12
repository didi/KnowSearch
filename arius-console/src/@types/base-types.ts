export type HttpMethod = 'POST' | 'PUT' | 'GET' | 'DELETE';

export interface IFilter {
  text: string;
  value: string;
}

export interface IDeteilId {
  id: number;
}

export interface ILabelValue {
  disabled?: boolean;
  value: string | number;
  label?: string;
  name?: string;
  key?: number;
  text?: string;
}

export interface IStatusMap {
  [key: number]: string;
}

export interface ILeftMenu {
  href: string;
  i: string;
  title: string;
  className?: string;
  hide?: boolean;
  class?: string;
  status?: string;
  children?: ILeftMenu[];
}

export interface IMenuItem {
  name?: string;
  key: string;
  show?: boolean;
  type?: number;
  label?: string;
  content?: (data: any) => JSX.Element;
}

export interface IBaseInfo {
  key: string;
  label: string;
  unit?: string;
  invisible?: boolean;
  render?: (text: any) => any;
}

export interface IUNSpecificInfo {
  [key: string]: any;
}

export interface IStringMap {
  [key: string]: string;
}

export interface IStringArray {
  [key: string]: string[];
}

interface IBtn {
  icon?: string;
  name: string;
  group?: number[];
  show?: boolean;
  key?: string;
  className?: string;
  onClick: (...args: any) => any;
  title?: string;
  disable?: boolean;
}

export type btnFunc = () => IBtn[];