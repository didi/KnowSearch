import React, { useState, useEffect } from "react";
import { Descriptions, Input, Form, Row, Button } from "antd";
import moment from "moment";
import { updateUserInfo } from "api/logi-security";
import { EditOutlined, CloseOutlined } from "@ant-design/icons";
import { deleteCookie } from "lib/utils";
import { FormItemType, FormMap, IFormItem } from "./config";
import { XNotification } from "component/x-notification";
import "./index.less";
import { renderAttributes } from "container/custom-component";

const imgSrc = require("../../../assets/emjio@3x.png");

export default function UseCenter(props: any) {
  const { userInfo, getData, logout, close } = props;

  const [isSwitch, setSwitch] = useState(false);
  const [name, setName] = useState("");
  const [nameSwitch, setNameSwitch] = useState(false);
  const [nameError, setNameError] = useState("");
  const [phone, setPhone] = useState("");
  const [phoneSwitch, setPhoneSwitch] = useState(false);
  const [phoneError, setPhoneError] = useState("");
  const [email, setEmail] = useState("");
  const [emailSwitch, setEmailSwitch] = useState(false);
  const [emailError, setEmailError] = useState("");

  const [form] = Form.useForm();

  useEffect(() => {
    setEditValue();
  }, [userInfo]);

  const setEditValue = () => {
    setName(userInfo?.realName);
    setPhone(userInfo?.phone);
    setEmail(userInfo?.email);
  };

  const editUser = async (value: string, type: string) => {
    let req = {} as any;
    if (type === "realName") {
      req = { realName: value };
    } else if (type === "phone") {
      req = { phone: value };
    } else if (type === "email") {
      req = { email: value };
    }
    req.userName = userInfo.userName;
    await updateUserInfo(req).then(() => {
      XNotification({ type: "success", message: "更新成功", duration: 1.5 });
      getData();
      if (type === "realName") {
        setNameSwitch(!nameSwitch);
      } else if (type === "phone") {
        setPhoneSwitch(!phoneSwitch);
      } else if (type === "email") {
        setEmailSwitch(!emailSwitch);
      }
    });
  };

  const getNameInput = (type: string) => {
    let ele = (
      <Input
        className={`edit-input ${nameError ? "error-input" : ""}`}
        placeholder="请输入用户实名"
        defaultValue={name}
        onChange={(e) => {
          let value = e.target.value;
          setName(value);
          if (!value) {
            setNameError("请输入用户实名");
          }
          let flat_1_50 = value && value.length > 0 && value.length <= 50;
          const reg = /^[a-zA-Z\u4e00-\u9fa5]+$/;
          if (!reg.test(value)) {
            setNameError("请输入中文或英文");
          } else if (!flat_1_50) {
            setNameError("1-50字符");
          } else {
            setNameError("");
          }
        }}
      />
    );
    return (
      <div className={`edit-content ${nameSwitch ? "edit" : ""}`}>
        {nameSwitch ? ele : <span>{userInfo?.realName || "-"}</span>}
        {nameSwitch ? (
          <div className="button">
            <Button
              type="link"
              onClick={async () => {
                if (nameError || name === userInfo?.realName) {
                  setNameSwitch(!nameSwitch);
                  return;
                }
                await editUser(name, type);
                setNameSwitch(!nameSwitch);
              }}
            >
              确认
            </Button>
            <Button
              type="link"
              onClick={async () => {
                setNameSwitch(!nameSwitch);
                setNameError("");
                setName(userInfo?.realName);
              }}
            >
              取消
            </Button>
          </div>
        ) : (
          <EditOutlined onClick={() => setNameSwitch(!nameSwitch)} />
        )}
        <div className="error">{nameError}</div>
      </div>
    );
  };

  const getMobileInput = (type: string, value: string) => {
    let ele = (
      <Input
        className={`edit-input ${phoneError ? "error-input" : ""}`}
        placeholder="请输入手机号码"
        defaultValue={phone}
        onChange={(e) => {
          let value = e.target.value;
          setPhone(value);
          const reg = /^[1][3-9][0-9]{9}$/;
          if (value && !reg.test(value)) {
            setPhoneError("请输入正确手机号码");
          } else {
            setPhoneError("");
          }
        }}
      />
    );
    return (
      <div className="edit-content">
        {phoneSwitch ? ele : <span>{value || "-"}</span>}
        {phoneSwitch ? (
          <div className="button">
            <Button
              type="link"
              onClick={async () => {
                if (phoneError || phone === userInfo?.phone) {
                  setPhoneSwitch(!phoneSwitch);
                  return;
                }
                await editUser(phone, type);
                setPhoneSwitch(!phoneSwitch);
              }}
            >
              确认
            </Button>
            <Button
              type="link"
              onClick={async () => {
                setPhoneSwitch(!phoneSwitch);
                setPhoneError("");
                setPhone(userInfo?.phone);
              }}
            >
              取消
            </Button>
          </div>
        ) : (
          <EditOutlined onClick={() => setPhoneSwitch(!phoneSwitch)} />
        )}
        <div className="error">{phoneError}</div>
      </div>
    );
  };

  const getEmailInput = (type: string, value: string) => {
    let ele = (
      <Input
        className={`edit-input ${emailError ? "error-input" : ""}`}
        defaultValue={email}
        placeholder="请输入邮箱地址"
        onChange={(e) => {
          let value = e.target.value;
          setEmail(value);
          const reg = /^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$/;
          if (value && !reg.test(value)) {
            setEmailError("请输入正确邮箱地址");
          } else {
            setEmailError("");
          }
        }}
      />
    );
    return (
      <div className="edit-content">
        {emailSwitch ? ele : <span>{value || "-"}</span>}
        {emailSwitch ? (
          <div className="button">
            <Button
              type="link"
              onClick={async () => {
                if (emailError || email === userInfo?.email) {
                  setEmailSwitch(!emailSwitch);
                  return;
                }
                await editUser(email, type);
                setEmailSwitch(!emailSwitch);
              }}
            >
              确认
            </Button>
            <Button
              type="link"
              onClick={async () => {
                setEmailSwitch(!emailSwitch);
                setEmailError("");
                setEmail(userInfo?.email);
              }}
            >
              取消
            </Button>
          </div>
        ) : (
          <EditOutlined onClick={() => setEmailSwitch(!emailSwitch)} />
        )}
        <div className="error">{emailError}</div>
      </div>
    );
  };

  const handleSubmit = (e: { password: string; newPassword: string }) => {
    updateUserInfo({ oldPw: e.password, pw: e.newPassword, userName: userInfo.userName }).then(() => {
      XNotification({ type: "success", message: "修改密码成功！", duration: 1.5 });
      deleteCookie(["userId", "userName"]);
      window.location.href = "/login";
    });
  };

  const renderFormItem = (item: IFormItem) => {
    switch (item.type) {
      default:
      case FormItemType.input:
        return <Input key={item.key} {...item.attrs} />;
      case FormItemType.inputPassword:
        return <Input.Password key={item.key} {...item.attrs} />;
    }
  };

  const changePassword = () => {
    return (
      <div className="change_password_form">
        <Form name="change_password" form={form} onFinish={handleSubmit} layout={"vertical"}>
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
              <Button type="primary" htmlType="submit">
                确定
              </Button>
              <Button style={{ marginLeft: 10 }} onClick={() => setSwitch(!isSwitch)}>
                取消
              </Button>
            </Row>
          </Form.Item>
        </Form>
      </div>
    );
  };

  const renderProject = () => {
    let list = userInfo?.projectList || [];
    let data = list.map((item: any) => item && item.projectName);
    return renderAttributes({ data, limit: 10, placement: "bottomLeft" });
  };

  const renderContent = () => {
    return (
      <Descriptions>
        <Descriptions.Item span={24} label="用户账号" key="userName">
          {userInfo.userName}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="注册时间" key="createTime">
          {moment(userInfo.createTime).format("YYYY-MM-DD")}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="用户实名" key="realName">
          {getNameInput("realName")}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="手机号码" key="phone">
          {getMobileInput("phone", userInfo.phone)}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="邮箱地址" key="email">
          {getEmailInput("email", userInfo.email)}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="登录密码" className="password" key="password">
          {isSwitch ? (
            changePassword()
          ) : (
            <span>
              ………
              <a className="ml-10" onClick={() => setSwitch(!isSwitch)}>
                修改密码
              </a>
            </span>
          )}
        </Descriptions.Item>
        <Descriptions.Item span={24} label="所属应用" key="project">
          <div className="project">{renderProject()}</div>
        </Descriptions.Item>
      </Descriptions>
    );
  };

  return (
    <div className="user-container">
      <div className="user-title">
        <img src={imgSrc}></img>
        <span>嗨，你好，{userInfo?.userName || "-"}</span>
        <CloseOutlined onClick={close} />
      </div>
      <div className="user-content">{renderContent()}</div>
      <div className="user-logout" onClick={() => logout()}>
        <span className={`icon iconfont icontuichudenglu`}></span>
        <span>退出登录</span>
      </div>
    </div>
  );
}
