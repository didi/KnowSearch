export interface IStringMap {
  [index: string]: string;
}

export interface ICookie {
  key: string;
  value?: string;
  time?: number;
}

export interface IMenuItem {
  name?: string;
  key: string;
  show?: boolean;
  type?: number;
  label?: string;
  content?: JSX.Element | string;
  render?: (params: any) => JSX.Element;
}

export interface IFilter {
  text: string;
  value: string;
}

export interface IStatusMap {
  [key: number]: string;
}

export interface ILableKey<T> {
  label: string;
  key: string;
  render?: (text: any, record?: T) => any;
}
export interface ILeftMenuItem {
  href: string;
  i?: string;
  title: string;
}

export interface IRes extends Response {
  code: number;
  status: number;
  message: string;
  data: any;
  pagination?: IPagination;
}

export interface IFileType {
  value: number;
  parser?: "Presto" | "Hive" | "Spark";
  title: string;
  language?: string;
  decomment?: boolean;
  type?: number[];
  timer?: boolean;
}

export interface ITag {
  tagId: string;
  tag: string;
  offset: IOffset;
}

export interface IOffset {
  year?: string;
  month?: string;
  week?: string;
  day?: string;
  hour?: string;
  minute?: string;
}

export interface IPreviewRow {
  [key: number]: string;
  key?: string | number;
}

export interface ILabelValue {
  value: string | number;
  label?: string;
  name?: string;
  key?: number;
  text?: string;
}

export interface IXFormWrapper {
  className?: string;
  type?: string;
  title: string;
  onSubmit: (result: any) => any;
  onCancel?: () => any;
  visible: boolean;
  onChangeVisible?: (visible: boolean) => any;
  formMap?: any[];
  formData?: any;
  width?: number;
  formLayout?: any;
  okText?: string;
  cancelText?: string;
  customRenderElement?: React.ReactNode;
  noform?: boolean;
  nofooter?: boolean;
  isWaitting?: boolean;
  needBtnLoading?: boolean;
  needSuccessMessage?: boolean;
  layout?: "inline" | "horizontal" | "vertical";
  actionAfterFailedSubmit?: () => any;
  actionAfterSubmit?: (res: any) => any;
  onHandleValuesChange?: (value: any, allValues: object) => any;
  formRef?: any;
}
/*
 *@needSuccessMessage:控制成功之后的提示信息
 */
export interface IAction {
  type: string;
  payload: any;
}

export interface IPagination {
  pageNo: number;
  pageSize: number;
  total: number;
}

export interface IClusterKanBanAction {
  type: string;
  clusterForm: {
    clusterName?: string;
    startTime?: number;
    endTime?: number;
    isMoreDay?: boolean;
    isUpdate?: boolean;
    bigShardThreshold?: number;
  };
}
export interface IGatewayKanbanAction {
  type: string;
  gatewayForm: {
    startTime: number;
    endTime: number;
    isMoreDay: boolean;
  };
}

export interface IDashBoardAction {
  type: string;
  payload: {
    startTime: number;
    endTime: number;
    isOperation: boolean;
  };
}
