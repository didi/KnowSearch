import React from "react";
import { Form, Button, Input, Row, Tooltip, message } from "knowdesign";
import { RegisterFormMap, renderFormItem } from "./config";
import { CheckCircleOutlined, CloseCircleOutlined } from "@ant-design/icons";
import { userRegister } from "api/logi-security";
import { encryptAES } from "./crypto";

const redTip = "#EF645C";
const greenTip = "#46D677";
const grayTip = "#A8ADBD";

export const RegisterForm: React.FC<any> = (props: { fn: (t: string) => any }) => {
  const [userNameCheckColorlength, setUserNameCheckColorlength] = React.useState(grayTip);
  const [userNameCheckColorReg, setUserNameCheckColorReg] = React.useState(grayTip);
  const [userNameCheckRepeat, setUserNameCheckRepeat] = React.useState(grayTip);
  const [passwordCheckColorlength, setPasswordCheckColorlength] = React.useState(grayTip);
  const [passwordCheckColorReg, setPCheckColorReg] = React.useState(grayTip);
  const [form] = Form.useForm();

  React.useEffect(() => {
    //
  }, []);

  const handleSubmit = async (e: any) => {
    const userName = typeof e.userName === "object" ? e.userName.value : e.userName;
    const email = typeof e.mailbox === "object" ? e.mailbox.value : e.mailbox;
    const phone = typeof e.phone === "object" ? e.phone.value : e.phone;
    const req = {
      userName,
      pw: e.password,
      realName: e.realName,
      email,
      phone,
    };
    userRegister(req)
      .then(() => {
        message.success("注册成功");
        props.fn("login");
      })
      .catch(() => {
        message.error("注册失败");
      });
  };

  const renderTip = (key: string) => {
    let ele = null;
    if (key === "userName") {
      ele = (
        <>
          <div>
            {userNameCheckColorlength !== redTip ? (
              <CheckCircleOutlined style={{ color: userNameCheckColorlength }} />
            ) : (
              <CloseCircleOutlined style={{ color: userNameCheckColorlength }} />
            )}
            &nbsp;长度为5-50个字符
          </div>
          <div>
            {userNameCheckColorReg !== redTip ? (
              <CheckCircleOutlined style={{ color: userNameCheckColorReg }} />
            ) : (
              <CloseCircleOutlined style={{ color: userNameCheckColorReg }} />
            )}
            &nbsp;支持英文字母、数字、下划线
          </div>
          <div>
            {userNameCheckRepeat !== redTip ? (
              <CheckCircleOutlined style={{ color: userNameCheckRepeat }} />
            ) : (
              <CloseCircleOutlined style={{ color: userNameCheckRepeat }} />
            )}
            &nbsp;账号不可重复
          </div>
        </>
      );
    } else {
      ele = (
        <>
          <div>
            {passwordCheckColorlength !== redTip ? (
              <CheckCircleOutlined style={{ color: passwordCheckColorlength }} />
            ) : (
              <CloseCircleOutlined style={{ color: passwordCheckColorlength }} />
            )}
            &nbsp;6-20个字符
          </div>
          <div>
            {passwordCheckColorReg !== redTip ? (
              <CheckCircleOutlined style={{ color: passwordCheckColorReg }} />
            ) : (
              <CloseCircleOutlined style={{ color: passwordCheckColorReg }} />
            )}
            &nbsp;英文字母、数字、标点符号（除空格）
          </div>
        </>
      );
    }
    return ele;
  };

  const onValuesChange = (value, allValue) => {
    Object.keys(value).forEach((key) => {
      switch (key) {
        case "userName":
          checkUserName(value[key]);
          break;
        case "password":
          checkUserPassword(value[key]);
          break;
        default:
          break;
      }
    });
  };

  const checkUserName = (value) => {
    if (!value) {
      setUserNameCheckColorlength(redTip);
      setUserNameCheckColorReg(redTip);
      setUserNameCheckRepeat(redTip);
      return;
    }
    const isRed = typeof value === "string" || value === "-9999";
    const _value = typeof value === "string" ? value : value.value;

    const flat_5_50 = _value && _value.length > 4 && _value.length <= 50;
    const reg = /^[0-9a-zA-Z_]{1,}$/;
    const flat = reg.test(_value);
    setUserNameCheckColorlength(flat_5_50 ? greenTip : redTip);
    setUserNameCheckColorReg(flat ? greenTip : redTip);
    setUserNameCheckRepeat(isRed ? redTip : greenTip);
  };

  const checkUserPassword = (value) => {
    if (!value) return;
    const flat_6_20 = value && value.length > 5 && value.length <= 20;
    const reg = /^[a-zA-Z0-9\_-]*$/;
    const flat = reg.test(value);
    if (flat_6_20 && passwordCheckColorlength !== greenTip) {
      setPasswordCheckColorlength(greenTip);
    } else if (!flat_6_20 && passwordCheckColorlength !== redTip) {
      setPasswordCheckColorlength(redTip);
    }
    if (flat && passwordCheckColorReg !== greenTip) {
      setPCheckColorReg(greenTip);
    } else if (!flat && passwordCheckColorReg !== redTip) {
      setPCheckColorReg(redTip);
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
        onValuesChange={onValuesChange}
      >
        {RegisterFormMap.map((formItem) => {
          return formItem.key === "userName" || formItem.key === "password" ? (
            <Tooltip
              key={formItem.key}
              color="#fff"
              overlayClassName="custom-login-style"
              placement="right"
              title={renderTip(formItem.key)}
            >
              <Row key={formItem.key}>
                <Form.Item key={formItem.key} name={formItem.key} label={formItem.label} rules={formItem.rules} style={{ width: "100%" }}>
                  {renderFormItem(formItem)}
                </Form.Item>
              </Row>
            </Tooltip>
          ) : (
            <Row key={formItem.key}>
              <Form.Item key={formItem.key} name={formItem.key} label={formItem.label} rules={formItem.rules} style={{ width: "100%" }}>
                {renderFormItem(formItem)}
              </Form.Item>
            </Row>
          );
        })}
        <Form.Item key={"submit"}>
          <Row>
            <Button className="submit-btn" type="primary" htmlType="submit">
              立即注册
            </Button>
          </Row>
        </Form.Item>
      </Form>
    </>
  );
};
