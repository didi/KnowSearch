import { IMenuItem } from "interface/common";
import { LoginForm } from "./LoginForm";
import React from "react";
import { CloseCircleFilled, LockOutlined, UserOutlined } from "@ant-design/icons";
import { RegisterForm } from "./RegisterForm";
import { Input, InputNumber } from "knowdesign";
import { checkRegisterUser } from "api/logi-security";
import { regUserPassword } from "constants/reg";

const CHECK_TYPE = {
  user: 1,
  phone: 2,
  email: 3,
};

export enum LOGIN_TAB_KEY {
  login = "login",
  register = "register",
}

export const LOGIN_MENU = [
  {
    label: "账号密码登录",
    key: LOGIN_TAB_KEY.login,
    render: (fn) => <LoginForm fn={fn} />,
  },
  {
    label: "账号注册",
    key: LOGIN_TAB_KEY.register,
    render: (fn) => <RegisterForm fn={fn} />,
  },
] as IMenuItem[];

const menuMap = new Map<string, IMenuItem>();
LOGIN_MENU.forEach((d) => {
  menuMap.set(d.key, d);
});

export const LOGIN_MENU_MAP = menuMap;

export enum FormItemType {
  input = "input",
  inputPassword = "inputPassword",
  inputNumber = "inputNumber",
  custom = "custom",
}
export interface IFormItem {
  key: string;
  type: FormItemType;
  attrs?: any;
  rules?: any[];
  invisible?: boolean;
  customFormItem?: any;
}

export const renderFormItem = (item: IFormItem) => {
  switch (item.type) {
    default:
    case FormItemType.input:
      return <Input allowClear key={item.key} {...item.attrs} />;
    case FormItemType.inputPassword:
      return <Input.Password allowClear key={item.key} {...item.attrs} />;
    case FormItemType.inputNumber:
      return <InputNumber key={item.key} {...item.attrs} />;
    case FormItemType.custom:
      return (item as IFormItem).customFormItem;
  }
};

export const FormMap = [
  {
    key: "userName",
    label: "账号",
    type: FormItemType.input,
    rules: [
      {
        required: true,
        message: (
          <>
            <CloseCircleFilled />
            请输入用户账号
          </>
        ),
      },
    ],
    attrs: {
      placeholder: "请输入账号",
      prefix: <UserOutlined style={{ color: "rgba(0,0,0,.25)" }} />,
    },
  },
  {
    key: "password",
    type: FormItemType.inputPassword,
    label: "密码",
    rules: [
      {
        required: true,
        message: (
          <>
            <CloseCircleFilled />
            请输入密码
          </>
        ),
      },
    ],
    attrs: {
      placeholder: "请输入密码",
      prefix: <LockOutlined style={{ color: "rgba(0,0,0,.25)" }} />,
    },
  },
];

// 用户校验
const UserInfoCheck = (props) => {
  const onChange = (e) => {
    props?.onChange(e.target.value);
  };

  const onBlur = (e) => {
    const value = e.target.value;
    if (props.checkFn && !props.checkFn(value)) {
      return;
    }

    checkRegisterUser(props.type, value)
      .then(() => {
        props?.onChange({ checked: true, value });
      })
      .catch(() => {
        props?.onChange("-9999");
      });
  };

  return (
    <Input size="large" allowClear key={props.type + "user"} placeholder={props.placeholder || ""} onChange={onChange} onBlur={onBlur} />
  );
};

const userNameCheck = (value) => {
  let flat_5_50 = value && value.length > 4 && value.length <= 50;
  const reg = /^[0-9a-zA-Z_]{1,}$/;

  return flat_5_50 && reg.test(value);
};

const userPhoneCheck = (value) => {
  const reg = /^[1][3-9][0-9]{9}$/;

  return reg.test(value);
};

const userEmailCheck = (value) => {
  const reg = /^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$/;

  return reg.test(value);
};

export const RegisterFormMap = [
  {
    key: "userName",
    label: "用户账号",
    type: FormItemType.custom,
    customFormItem: <UserInfoCheck checkFn={userNameCheck} placeholder="请输入用户账号" type={CHECK_TYPE.user} />,
    rules: [
      {
        required: true,
        validator: (rule: any, value: string) => {
          if (value === "-9999") {
            return Promise.reject("账号已存在，请重新填写");
          }
          if (value === "-1" || !value) {
            return Promise.reject("请输入用户账号");
          }

          if (typeof value === "object") {
            return Promise.resolve();
          }

          if (!userNameCheck(value)) {
            return Promise.reject("账号设置不符合要求");
          }
          return Promise.resolve();
        },
      },
    ],
  },
  {
    key: "password",
    type: FormItemType.inputPassword,
    label: "密码",
    rules: [
      {
        required: true,
        message: "密码设置不符合要求",
        validator: (rule: any, value: string) => {
          if (!value) return Promise.reject("请输入密码");
          let flat_6_20 = value && value.length > 5 && value.length <= 20;
          if (flat_6_20 && regUserPassword.test(value)) {
            return Promise.resolve();
          } else {
            return Promise.reject();
          }
        },
      },
    ],
    attrs: {
      placeholder: "请输入密码",
    },
  },
  {
    key: "confirm",
    type: FormItemType.inputPassword,
    label: "确认密码",
    rules: [
      {
        required: true,
        message: "两次密码不统一",
      },
      ({ getFieldValue }) => ({
        validator(_, value) {
          if (!value || getFieldValue("password") === value) {
            return Promise.resolve();
          }
          return Promise.reject("两次密码不统一");
        },
      }),
    ],
    attrs: {
      placeholder: "请再次输入密码",
    },
  },
  {
    key: "realName",
    label: "用户实名",
    type: FormItemType.input,
    rules: [
      {
        required: true,
        validator: (rule: any, value: string) => {
          if (!value) {
            return Promise.reject("请输入用户实名");
          }
          let flat_1_50 = value && value.length > 0 && value.length <= 50;
          const reg = /^[a-zA-Z\u4e00-\u9fa5]+$/;
          if (!reg.test(value)) {
            return Promise.reject("请输入中文或英文");
          } else if (!flat_1_50) {
            return Promise.reject("1-50字符");
          } else {
            return Promise.resolve();
          }
        },
      },
    ],
    attrs: {
      placeholder: "请输入用户实名",
    },
  },
  {
    key: "phone",
    label: "手机号",
    type: FormItemType.custom,
    customFormItem: <UserInfoCheck checkFn={userPhoneCheck} placeholder="请输入手机号码" type={CHECK_TYPE.phone} />,
    rules: [
      {
        required: false,
        validator: (rule: any, value: string) => {
          if (!value) {
            return Promise.resolve();
          }
          if (value === "-9999") {
            return Promise.reject("该手机号已存在，请重新输入");
          }
          if (typeof value === "object") {
            return Promise.resolve();
          }
          if (!userPhoneCheck(value)) {
            return Promise.reject("请输入正确手机号码");
          } else {
            return Promise.resolve();
          }
        },
      },
    ],
  },
  {
    key: "mailbox",
    label: "邮箱",
    type: FormItemType.custom,
    customFormItem: <UserInfoCheck checkFn={userEmailCheck} placeholder="请输入邮箱地址" type={CHECK_TYPE.email} />,
    rules: [
      {
        required: false,
        validator: (rule: any, value: string) => {
          if (!value) {
            return Promise.resolve();
          }
          if (value === "-9999") {
            return Promise.reject("该邮箱地址已存在，请重新输入");
          }
          if (typeof value === "object") {
            return Promise.resolve();
          }
          if (!userEmailCheck(value)) {
            return Promise.reject("请输入完整的邮件格式");
          } else {
            return Promise.resolve();
          }
        },
      },
    ],
  },
];
