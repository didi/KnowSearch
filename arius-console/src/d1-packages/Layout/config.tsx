export interface MenuChild {
  name: string;
  nameEn: string;
  path: string;
  icon: string;
}
export interface Menu {
  name: string;
  nameEn: string;
  type: 'group';
  children: MenuChild[];
}

export const prefixCls = 'd1';

export enum FormItemType {
  input = 'input',
  inputPassword = 'inputPassword',
  inputNumber = 'inputNumber'
}

export interface IFormItem {
  key: string;
  type: FormItemType;
  attrs?: any;
  rules?: any[];
  invisible?: boolean;
}


export const FormMap = [
  {
    key: 'password',
    type: FormItemType.inputPassword,
    label: '当前密码',
    rules: [{
      required: true,
    }],
    attrs: {
      placeholder: '当前密码',
    }
  },
  {
    key: 'newPassword',
    type: FormItemType.inputPassword,
    label: '新密码',
    rules: [{
      required: true,
      message: '密码设置不符合要求',
      validator: (rule: any, value: string) => {
        let flat_6_20 = (value && value.length > 5 && value.length < 20);
        const reg = /^[a-zA-Z0-9\_-]*$/;
        if (flat_6_20 && reg.test(value)) {
          return Promise.resolve();
        } else {
          return Promise.reject();
        }
      },
    }],
    attrs: {
      placeholder: '6-20个字符，支持英文字母、数字、标点符号（除空格）',
    }
  },
  {
    key: 'confirm',
    type: FormItemType.inputPassword,
    label: '确认密码',
    rules: [{
      required: true,
      message: '两次密码不统一',
    },
    ({ getFieldValue }) => ({
      validator(_, value) {
        if (!value || getFieldValue('newPassword') === value) {
          return Promise.resolve();
        }
        return Promise.reject('两次密码不统一');
      },
    }),],
    attrs: {
      placeholder: '请再次输入密码',
    }
  },
]