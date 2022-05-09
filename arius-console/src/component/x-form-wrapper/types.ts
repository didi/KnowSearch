import { IFormItem } from 'component/x-form';

export interface IXFormWrapper {
  type?: string;
  title: string | React.ReactNode;
  onSubmit: (result: any) => any;
  onCancel?: () => any;
  visible: boolean;
  spinning?: boolean;
  onChangeVisible?: (visible: boolean) => any;
  formMap?: IFormItem[];
  formData?: any;
  width?: number;
  formLayout?: any;
  okText?: string;
  cancelText?: string;
  customRenderElement?: React.ReactNode;
  noform?: boolean;
  nofooter?: boolean;
  isWaitting?: boolean;
  okButtonProps?: object;
  errorTip?: string;
  renderExtraElement?: () => string | React.ReactNode;
  action?: {onHandleValuesChange: (value: object, allValues: object) => any; };
  actionAfterSubmit?: (res?: any) => any;
  actionAfterFailedSubmit?: () => any;
}
