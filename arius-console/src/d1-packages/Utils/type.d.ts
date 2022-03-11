import { AxiosRequestConfig } from 'axios';

export interface IResData {
  bizData: any;
  pagination?: any
}

export interface IRes {
  code: number;
  message: string;
  data: any;
  pagination?: any
}

export interface RequestInit extends AxiosRequestConfig {
  init?: IInit;
}

export interface IInit {
  errorNoTips?: boolean; // 错误信息不提示 true - 不提示 false - 提示
  needCode?: boolean; // 是否需要code，不传或为false默认直接返回(res.data.data)
  needDuration?: boolean; // 错误信息提示持续时间 true - 一直存在 false - 默认值（3）
}

export interface IMap { // 获取Cookie对应的接口
  [index: string]: string;
}

export interface ICookie {
  key: string;
  value?: string;
  time?: number;
  expires?: any;
}

export interface IDuration {
  days: number | string;
  hours: number | string;
  minutes: number | string;
  seconds: number | string;
}


export interface IOffset {
  year?: string;
  month?: string;
  week?: string;
  day?: string;
  hour?: string;
  minute?: string;
}