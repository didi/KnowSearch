export enum FormItemType {
  input = "input",
  inputPassword = "inputPassword",
  inputNumber = "inputNumber",
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
    key: "password",
    type: FormItemType.inputPassword,
    label: "当前密码",
    rules: [
      {
        required: true,
      },
    ],
    attrs: {
      placeholder: "请输入当前密码",
    },
  },
  {
    key: "newPassword",
    type: FormItemType.inputPassword,
    label: "新密码",
    rules: [
      {
        required: true,
        validator: (rule: any, value: string) => {
          if (!value) return Promise.reject("请输入新密码");
          let flat_6_20 = value && value.length > 5 && value.length <= 20;
          const reg = /^[a-zA-Z0-9`~!@#$%^&*()_\-+=<>?:"{}|,.\/;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘'，。、]*$/;
          if (flat_6_20 && reg.test(value)) {
            return Promise.resolve();
          } else {
            return Promise.reject("6-20个字符，支持英文字母、数字、标点符号（除空格）");
          }
        },
      },
      ({ getFieldValue }) => ({
        validator(_, value) {
          if (getFieldValue("password") === value) {
            return Promise.reject("新密码不能与当前密码相同");
          }
          return Promise.resolve();
        },
      }),
    ],
    attrs: {
      placeholder: "请输入新密码",
    },
  },
  {
    key: "confirm",
    type: FormItemType.inputPassword,
    label: "确认密码",
    rules: [
      {
        required: true,
        message: "请再次输入密码",
      },
      ({ getFieldValue }) => ({
        validator(_, value) {
          let flat_6_20 = value && value.length > 5 && value.length <= 20;
          const reg = /^[a-zA-Z0-9`~!@#$%^&*()_\-+=<>?:"{}|,.\/;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘'，。、]*$/;
          if (value && (!flat_6_20 || !reg.test(value))) {
            return Promise.reject("6-20个字符，支持英文字母、数字、标点符号（除空格）");
          }
          if (!value || getFieldValue("newPassword") === value) {
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
];
