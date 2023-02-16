import React from "react";
import { Form, Button, Row, message } from "knowdesign";
import { FormMap, renderFormItem } from "./config";
import { userLogin } from "api/logi-security";
import { encryptAES } from "./crypto";
import { Utils } from "knowdesign";
const setCookie = Utils.setCookie;
import { useGlobalLoginStatus } from "../../../store";
import { judgeAdminUser } from "api/user-api";

export const LoginForm: React.FC<any> = (props: any) => {
  const [form] = Form.useForm();
  const [loginStatus, setLoginStatus] = useGlobalLoginStatus();

  const handleSubmit = async (e: { userName: string; password: string }) => {
    try {
      const res = await userLogin({
        pw: encryptAES(e.password),
        userName: e.userName,
      });
      localStorage.setItem("current-project", JSON.stringify({})); //清空原有项目数据
      setCookie([
        { key: "userName", value: res.userName },
        { key: "userId", value: res.id },
      ]);
      message.success("登录成功");

      judgeAdminUser()
        .then((res) => {
          setCookie([{ key: "isAdminUser", value: res.code === 0 ? "yes" : "no" }]);
          props.fn(res.code === 0);
        })
        .finally(() => {
          setLoginStatus(!loginStatus);
        });
    } catch (err) {
      // do nothing
    }
  };

  return (
    <>
      <Form name="normal_login" form={form} className="login-form" onFinish={handleSubmit} layout={"vertical"}>
        {FormMap.map((formItem) => {
          return (
            <Row key={formItem.key}>
              <Form.Item key={formItem.key} name={formItem.key} label={formItem.label} rules={formItem.rules} style={{ width: "100%" }}>
                {renderFormItem(formItem)}
              </Form.Item>
            </Row>
          );
        })}
        <Form.Item key={"submit"}>
          <Row>
            <Button style={{ width: "100%", height: 56 }} type="primary" htmlType="submit">
              登录
            </Button>
          </Row>
        </Form.Item>
      </Form>
    </>
  );
};
