import { IMenuItem } from 'interface/common';
import { FormItemType, LoginForm } from './login';
import React from 'react';
import { CloseCircleFilled, LockOutlined, UserOutlined } from '@ant-design/icons';
import { RegisterFrom } from './register';
import { Input } from 'antd';
import { checkUserNameApi } from 'api/user-api';
import { debounce } from 'lodash';

export enum LOGIN_TAB_KEY {
  login = 'login',
  register = 'register'
}

export const LOGIN_MENU = [
  {
    label: '账号密码登录',
    key: LOGIN_TAB_KEY.login,
    render: fn => <LoginForm fn={fn} />
  },
  {
    label: '账号注册',
    key: LOGIN_TAB_KEY.register,
    render: fn => <RegisterFrom fn={fn} />
  }
] as IMenuItem[];

const menuMap = new Map<string, IMenuItem>();
LOGIN_MENU.forEach(d => {
  menuMap.set(d.key, d);
});

export const LOGIN_MENU_MAP = menuMap;

export const FormMap = [
  {
    key: 'userName',
    label: '用户账号',
    type: FormItemType.input,
    rules: [{
      required: true,
      message: <><CloseCircleFilled />请输入用户账号</>,
    }],
    attrs: {
      placeholder: '账号',
      prefix: <UserOutlined style={{ color: 'rgba(0,0,0,.25)' }} />,
    }
  },
  {
    key: 'password',
    type: FormItemType.inputPassword,
    label: '密码',
    rules: [{
      required: true,
      message: <><CloseCircleFilled />请输入密码</>,
    }],
    attrs: {
      placeholder: '密码',
      prefix: <LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />,
    }
  }
];

// 用户校验
const UserNameCheck = (props) => {
  const onChange = (e) => {
    checkUserNameRepeat(e.target.value);  
  }

  const checkUserNameRepeat = debounce(async(value) => {
    await checkUserNameApi(value).then(() => {
      props?.onChange(value);
    }).catch(() => {
      props?.onChange('-1');
    });
  }, 1000) as any;

  return (
    <>
      <Input key={'user-name1'}  placeholder={'6-20个字符，支持英文字母、数字、标点符号（除空格）'} prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} onChange={onChange}/>
    </>
  )
};

export const RegisterFormMap = [
  {
    key: 'userName',
    label: '用户账号',
    type: FormItemType.custom,
    customFormItem: <UserNameCheck />,
    rules: [
      {
        required: true,
        validator: (rule: any, value: string) => {
          let flat_5_50 = (value && value.length > 4 && value.length <= 50);
          const reg = /^[0-9a-zA-Z_]{1,}$/;
          if (value === '-1') {
            return Promise.reject('账号重复');
          }
          if (flat_5_50 && reg.test(value)) {
            return Promise.resolve();
          } else {
            return Promise.reject('账号设置不符合要求');
          }
        },
      },
    ],
  },
  {
    key: 'password',
    type: FormItemType.inputPassword,
    label: '密码',
    rules: [{
      required: true,
      message: '密码设置不符合要求',
      validator: (rule: any, value: string) => {
        let flat_6_20 = (value && value.length > 5 && value.length <= 20);
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
      prefix: <LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />,
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
        if (!value || getFieldValue('password') === value) {
          return Promise.resolve();
        }
        return Promise.reject('两次密码不统一');
      },
    }),],
    attrs: {
      placeholder: '请再次输入密码',
      prefix: <LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />,
    }
  },
  {
    key: 'realName',
    label: '真实姓名',
    type: FormItemType.input,
    rules: [{
      required: false,
      validator: (rule: any, value: string) => {
        if (!value) {
          return Promise.resolve();
        }
        let flat_1_50 = (value && value.length > 0 && value.length <= 50);
        const reg = /^[a-zA-Z\u4e00-\u9fa5]+$/;
        if(!reg.test(value)){
          return Promise.reject('请输入中文或英文');
        } else if(!flat_1_50) {
          return Promise.reject('1-50字符');
        } else {
          return Promise.resolve();
        }
      }, 
    }],
    attrs: {
      placeholder: '真实姓名',
    }
  },
  {
    key: 'phone',
    label: '手机号',
    type: FormItemType.input,
    rules: [{
      required: false,
      validator: (rule: any, value: string) => {
        if (!value) {
          return Promise.resolve();
        }
        const reg = /^[1][3-9][0-9]{9}$/;
        if(!reg.test(value)){
          return Promise.reject('请输入正确手机号码');
        } else {
          return Promise.resolve();
        }
      }, 
    }],
    attrs: {
      placeholder: '手机号',
    }
  },
  {
    key: 'mailbox',
    label: '邮箱',
    type: FormItemType.input,
    rules: [{
      required: false,
      validator: (rule: any, value: string) => {
        if (!value) {
          return Promise.resolve();
        }
        const reg = /^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$/;
        if(!reg.test(value)){
          return Promise.reject('请输入完整的邮件格式');
        } else {
          return Promise.resolve();
        }
      }, 
    }],
    attrs: {
      placeholder: '邮箱',
    }
  }
];
