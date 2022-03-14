import { EditTwoTone } from '@ant-design/icons';
import { Descriptions, Input, Form, Row, Button, notification } from 'antd';
import { getDomainAccount, getPublicKey, updataPassword, upUser } from 'api/user-api';
import JSEncrypt from 'jsencrypt';
import { deleteCookie, getCookie } from 'lib/utils';
import React from 'react';
import { FormItemType, FormMap, IFormItem } from './config';
import moment from 'moment';

const {Search} = Input;

export const UserCenter = () => {
  const [isSwitch, setSwitch] = React.useState(false);
  const [userInfo, setUserInfo] = React.useState({} as any);
  const [pubKey, setPubKey] = React.useState(null);
  const [nameSwitch, setNameSwitch] = React.useState(false);
  const [mobileSwitch, setMobileSwitch] = React.useState(false);
  const [emailSwitch, setEmailSwitch] = React.useState(false);

  const [form] = Form.useForm();
  
  React.useEffect(() => {
    getData();
  }, []);

  React.useEffect(() => {
    getPublicKey().then((res) => {
      setPubKey(res)
    });
  }, []);

  const getData = () => {
    if(!getCookie('domainAccount')) return;
    getDomainAccount(getCookie('domainAccount')).then((res) => {
      setUserInfo(res || {})
    });
  }

  const editUser = (value: string, type: string) =>  {
    let req = {} as any;
    if (type === 'name') {
      req = {name: value}
    } else if (type === 'mobile') {
      req = {mobile: value}
    } else if (type === 'email') {
      req = {email: value}
    };
    req.domainAccount = userInfo.domainAccount
    upUser(req).then(() => {
      notification.success({ message: '更新成功', duration: 1500 });
      getData();
      if (type === 'name') {
        setNameSwitch(!nameSwitch);
      } else if (type === 'mobile') {
        setMobileSwitch(!mobileSwitch);
      } else if (type === 'email') {
        setEmailSwitch(!emailSwitch);
      };
    });
  }

  const getNameInput = (type: string , value: string) => {
    let ele = (<Search style={{width: '50%'}} key={type + value} enterButton="确认" defaultValue={value} onSearch={value => editUser(value, type)}/>);
    return (
      <>
        {
          nameSwitch ? ele :
          (<span style={{color: '#A8ADBD', marginRight: 10}}>{value || "请输入真实姓名"}</span>)
        }
        <EditTwoTone onClick={() => setNameSwitch(!nameSwitch)}/>
      </>
    )
  }

  const getMobileInput = (type: string , value: string) => {
    let ele = (<Search style={{width: '50%'}} key={type + value} enterButton="确认" defaultValue={value} onSearch={value => editUser(value, type)}/>);
    return (
      <>
        {
          mobileSwitch ? ele :
          (<span style={{color: '#A8ADBD', marginRight: 10}}>{value || "请输入手机号"}</span>)
        }
        <EditTwoTone onClick={() => setMobileSwitch(!mobileSwitch)}/>
      </>
    )
  }

  const getEmailInput = (type: string , value: string) => {
    let ele = (<Search style={{width: '50%'}} key={type + value} enterButton="确认" defaultValue={value} onSearch={value => editUser(value, type)}/>);
    return (
      <>
        {
          emailSwitch ? ele :
          (<span style={{color: '#A8ADBD', marginRight: 10}}>{value || "请输入邮箱"}</span>)
        }
        <EditTwoTone onClick={() => setEmailSwitch(!emailSwitch)}/>
      </>
    )
  }

  const handleSubmit = (e: {password: string, newPassword: string}) => {
    const encryptor = new JSEncrypt({});  // 创建加密对象实例
    encryptor.setPublicKey(pubKey);//设置公钥
    e.password = encryptor.encrypt(e.password) || '' as string;  // 对内容进行加密
    e.newPassword = encryptor.encrypt(e.newPassword) || '' as string;  // 对内容进行加密
    const req = {
      domainAccount: userInfo.domainAccount,
      oldPassWord: e.password,
      newPassWord: e.newPassword
    }
    updataPassword(req).then(() => {
      notification.success({ message: '修改密码成功！', duration: 1500 });
      deleteCookie(['domainAccount']);
      window.location.href = '/login';
    })
  }

  const renderFormItem = (item: IFormItem) => {
    switch (item.type) {
      default:
      case FormItemType.input:
        return <Input key={item.key} {...item.attrs} />;
      case FormItemType.inputPassword:
        return <Input.Password key={item.key} {...item.attrs} />;
    }
  }

  const changePassword = () => {
    return (
      <div className="change_password_form">
        <Form
          name="change_password"
          form={form}
          onFinish={handleSubmit}
          layout={'vertical'}
        >
          {FormMap.map(formItem => {
            return (
              <Row key={formItem.key}>
                <Form.Item
                  key={formItem.key}
                  name={formItem.key}
                  label={formItem.label}
                  rules={formItem.rules}
                  style={{ width: '100%' }}
                >
                  {renderFormItem(formItem)}
                </Form.Item>
              </Row>)
          })
          },
          <Form.Item key={'submit'}>
            <Row>
              <Button type="primary" htmlType="submit">
                修改
              </Button>
              <Button style={{ marginLeft: 10 }} onClick={() => setSwitch(!isSwitch)}>
                取消
              </Button>
            </Row>
          </Form.Item>
        </Form>
      </div>
    )
  }

  return (
    <Descriptions>
      <Descriptions.Item span={24} label="用户账号">{userInfo.domainAccount}</Descriptions.Item>
      <Descriptions.Item span={24} label="注册时间">{moment(userInfo.createTime).format('YYYY-MM-DD')}</Descriptions.Item>
      <Descriptions.Item span={24} label="用户实名">{getNameInput('name', userInfo.name)}</Descriptions.Item>
      <Descriptions.Item span={24} label="手机号码">{getMobileInput('mobile', userInfo.mobile)}</Descriptions.Item>
      <Descriptions.Item span={24} label="邮箱地址">{getEmailInput('email', userInfo.email)}</Descriptions.Item>
      <Descriptions.Item span={24} label="登录密码">{isSwitch ? changePassword() : <span>……… &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a onClick={() => setSwitch(!isSwitch)} >修改密码</a></span> }</Descriptions.Item>
      <Descriptions.Item span={24} label="所属项目">无</Descriptions.Item>
    </Descriptions>
  )
};