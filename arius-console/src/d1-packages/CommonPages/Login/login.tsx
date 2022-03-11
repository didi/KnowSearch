import React from "react";
import { Form, Button, Input, Row, Col, InputNumber, message } from "antd";
import { FormMap } from "./config";
import JSEncrypt from "jsencrypt";
import api from "../../api";
import { request } from "../../Utils/request";

export enum FormItemType {
  input = "input",
  inputpwd = "inputpwd",
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
      return <Input key={item.key} {...item.attrs} />;
    case FormItemType.inputpwd:
      return <Input key={item.key} {...item.attrs} />;
    case FormItemType.inputNumber:
      return <InputNumber key={item.key} {...item.attrs} />;
    case FormItemType.custom:
      return (item as IFormItem).customFormItem;
  }
};

export const LoginForm: React.FC<any> = (props) => {
  const [form] = Form.useForm();

  React.useEffect(() => {}, []);

  const handleSubmit = async (e: { userName: string; pwd: string }) => {
    let pubKey;
    try {
      pubKey = await request(api.publicKey);
    } catch {
      return;
    }

    if (!pubKey) {
      message.error("网络错误, 请稍后刷新重试！");
      return;
    }
    /*@测试加密、解密
     *获取公钥、私钥，Mac系统内置OpenSSL(开源加密库),所以可以直接在终端上使用命令。
     *生成私钥，密钥长度为1024bit 终端执行命令 openssl genrsa -out private.pem 1024
     *从私钥中提取公钥  命令 openssl rsa -in private.pem -pubout -out public.pem
     *这样生成了private.pem（私钥） 和 public.pem（公钥）两个文件
     * 命令 vi public.pem 复制出公钥 注意删除空格
     * 命令 vi private.pem 复制出私钥 注意删除空格
     */
    // // 加密
    const encryptor = new JSEncrypt({}); // 创建加密对象实例
    //接口获取公钥或通过以上教程获取
    // MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSnbsnKpj/wOB0NcYzTg3UOoG42u/wKPbfkf/z1SMwfak/MAdZWPEATuWSxs+b3WxPXueWxAkUsHKIfsZo8h7pJClbb+ly462bQjihEsCahLpD1O4tSlwEdWLilu2nGS/DnWwQVJxgj6N73HaTE/I6sIi8VGHHZz7bpo7sLR1k/wIDAQAB
    // MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDNEd6kJlXqJDUtJpWjjw1FvLlMQpJJDi8PPuOUyH1N4k6Dv+NuY4dxWm5lDdzmz9BF91gl6gUgXr90UL/c1PzG9JlwaeEvRrsVyUTa2IfJeXZdG4ZznnuPgmbeQGZe3waR5ZIl8gy498Z1vaQbGtONp4G36nFaBFO7z5uxswIkxQIDAQAB
    // const pubKey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCebVoUgQwN04eylpdAtm/jgg1zcu3apuprKvP0ionF5yG/k0ta+xIy6AzPCgGglXUcYnLhuOZD9qj1qxHTnzX0DXHafIe8biZAS4uXk9gfa0McaU8JjWF5mZ8m3zj0Wv5tPSbusT9QWCH+qwCeLbXLdfA0iJyjbUxNYTgNVKmBQIDAQAB'
    encryptor.setPublicKey(pubKey); //设置公钥
    e.pwd = encryptor.encrypt(e.pwd) as string; // 对内容进行加密
    // console.log('加密', rsapwd, 'base64加密', Base64.encode(rsapwd), 'atob', Base64.atob('123'));
    // // 解密
    // const priKey  = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBdhVrXeuExAccQ2tezTlsu6NG+L02TEyDR8TmcfpWZiMMD9oBTr+vgjpbVCRvb33SbPf1C95BDy2d+t15nLomOvZv0yVmvH/yIm/Du0kw8QunMO1SNpek9vYkgl33IXECNHLxGLwgFIrEdu622dsYExaFhkf1fLvcWKO64NgYEwIDAQAB'
    // encryptor.setPrivateKey(priKey);//设置秘钥
    // const uncrypted = encryptor.decrypt(rsapwd);//解密之前拿公钥加密的内容
    // console.log('解密', uncrypted);
    try {
      await request(api.opensourceLogin, {
        method: "POST",
        params: JSON.stringify({
          pwd: e.pwd,
          domainAccount: e.userName,
        }),
      });
      message.success("登录成功");
      window.location.href = "/";
    } catch (error) {
      message.error("登录失败");
    }
  };

  return (
    <>
      <Form
        name="normal_login"
        form={form}
        className="login-form"
        onFinish={handleSubmit}
        layout={"vertical"}
      >
        {FormMap.map((formItem) => {
          return (
            <Row key={formItem.key}>
              <Form.Item
                key={formItem.key}
                name={formItem.key}
                label={formItem.label}
                rules={formItem.rules}
                style={{ width: "100%" }}
              >
                {renderFormItem(formItem)}
              </Form.Item>
            </Row>
          );
        })}
        <Form.Item key={"submit"}>
          <Row>
            <Button
              style={{ width: "100%", height: 40 }}
              type="primary"
              htmlType="submit"
            >
              登录
            </Button>
          </Row>
        </Form.Item>
      </Form>
    </>
  );
};
