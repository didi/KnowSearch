import React from 'react';
import { Form, Button, Input, Row, Tooltip, message } from 'antd';
import { RegisterFormMap } from './config';
import { renderFormItem } from './login';
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { getPublicKey, userRegister } from 'api/user-api';
import JSEncrypt from 'jsencrypt';

export const RegisterFrom: React.FC<any> = (props: { fn: (t: string) => any }) => {
  const [userNameCheckColorlength, setUserNameCheckColorlength] = React.useState('#A8ADBD');
  const [userNameCheckColorReg, setUserNameCheckColorReg] = React.useState('#A8ADBD');
  const [userNameCheckRepeat, setUserNameCheckRepeat] = React.useState('#A8ADBD');
  const [passwordCheckColorlength, setPasswordCheckColorlength] = React.useState('#A8ADBD');
  const [passwordCheckColorReg, setPCheckColorReg] = React.useState('#A8ADBD');
  const [form] = Form.useForm();

  React.useEffect(() => {
    //
  }, []);

  const handleSubmit = async (e: any) => {
    let pubKey = '';
    await getPublicKey().then((res) => {
      pubKey = res;
    }).catch(() => {
      message.error('网络错误, 请稍后刷新重试！');
    });
    if (!pubKey) {
      message.error('网络错误, 请稍后刷新重试！');
      return;
    }
    const encryptor = new JSEncrypt({});  // 创建加密对象实例
    encryptor.setPublicKey(pubKey);//设置公钥
    e.password = encryptor.encrypt(e.password) as string;  // 对内容进行加密
    const req = {
      domainAccount: e.userName,
      password: e.password,
      name: e.realName,
      email: e.mailbox,
      mobile: e.phone
    }
    userRegister(req).then(() => {
      message.success('注册成功');
      props.fn('login');
    }).catch(() => {
      message.error('注册失败');
    })
  }

  const renderTip = (key: string) => {
    let ele = null;
    if (key === 'userName') {
      ele = (
        <>
          <div>
            {
              userNameCheckColorlength !== '#EF645C' ? <CheckCircleOutlined style={{ color: userNameCheckColorlength }} />
                : <CloseCircleOutlined style={{ color: userNameCheckColorlength }} />
            }
            &nbsp;长度为5-50个字符
          </div>
          <div>
            {
              userNameCheckColorReg !== '#EF645C' ? <CheckCircleOutlined style={{ color: userNameCheckColorReg }} />
                : <CloseCircleOutlined style={{ color: userNameCheckColorReg }} />
            }
            &nbsp;支持英文字母、数字、下划线
          </div>
          <div>
            {
              userNameCheckRepeat !== '#EF645C' ? <CheckCircleOutlined style={{ color: userNameCheckRepeat }} />
                : <CloseCircleOutlined style={{ color: userNameCheckRepeat }} />
            }
            &nbsp;账号不可重复
          </div>
        </>)
    } else {
      ele = (<>
        <div>
          {
            passwordCheckColorlength !== '#EF645C' ? <CheckCircleOutlined style={{ color: passwordCheckColorlength }} />
              : <CloseCircleOutlined style={{ color: passwordCheckColorlength }} />
          }
          &nbsp;6-20个字符
        </div>
        <div>
          {
            passwordCheckColorReg !== '#EF645C' ? <CheckCircleOutlined style={{ color: passwordCheckColorReg }} />
              : <CloseCircleOutlined style={{ color: passwordCheckColorReg }} />
          }
          &nbsp;英文字母、数字、标点符号（除空格）
        </div>
      </>)
    }
    return ele;
  }

  const onValuesChange = (value, allValue) => {
    Object.keys(value).forEach(key => {
      switch (key) {
        case 'userName':
          checkUserName(value[key]);
          break;
        case 'password':
          checkUserPassword(value[key]);
          break;
        default:
          break;
      }
    });
  }

  const checkUserName = (value) => {
    if (!value) return;
    const flat_5_50 = (value && value.length > 4 && value.length <= 50);
    const reg = /^[0-9a-zA-Z_]{1,}$/;
    const flat = reg.test(value);
    if (flat_5_50 && userNameCheckColorlength !== '#46D677') {
      setUserNameCheckColorlength('#46D677');
    } else if (!flat_5_50 && userNameCheckColorlength !== '#EF645C') {
      setUserNameCheckColorlength('#EF645C');
    }
    if (flat && userNameCheckColorReg !== '#46D677') {
      setUserNameCheckColorReg('#46D677');
    } else if (!flat && userNameCheckColorReg !== '#EF645C') {
      setUserNameCheckColorReg('#EF645C');
    }
    if (!(value === '-1') && userNameCheckRepeat !== '#46D677') {
      setUserNameCheckRepeat('#46D677');
    } else if (value === '-1' && userNameCheckRepeat !== '#EF645C') {
      setUserNameCheckRepeat('#EF645C');
    }
  }

  const checkUserPassword = (value) => {
    if (!value) return;
    const flat_6_20 = (value && value.length > 5 && value.length <= 20);
    const reg = /^[a-zA-Z0-9\_-]*$/;
    const flat = reg.test(value);
    if (flat_6_20 && passwordCheckColorlength !== '#46D677') {
      setPasswordCheckColorlength('#46D677');
    } else if (!flat_6_20 && passwordCheckColorlength !== '#EF645C') {
      setPasswordCheckColorlength('#EF645C');
    }
    if (flat && passwordCheckColorReg !== '#46D677') {
      setPCheckColorReg('#46D677')
    } else if (!flat && passwordCheckColorReg !== '#EF645C') {
      setPCheckColorReg('#EF645C')
    }
  }

  return (
    <>
      <Form
        name="normal_login"
        form={form}
        className="login-form"
        onFinish={handleSubmit}
        layout={'vertical'}
        onValuesChange={onValuesChange}
      >
        {RegisterFormMap.map(formItem => {
          return (
            <>
              {
                (formItem.key === 'userName' || formItem.key === 'password') ?
                  <Tooltip key={formItem.key} color="#fff" overlayClassName="custom-login-style" placement="right" title={renderTip(formItem.key)} >
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
                    </Row>
                  </Tooltip>
                  :
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
                  </Row>
              }
            </>
          )
        })
        }
        <Form.Item key={'submit'}>
          <Row>
            <Button style={{ width: '100%', height: 40 }} type="primary" htmlType="submit">
              立即注册
            </Button>
          </Row>
        </Form.Item>
      </Form>
    </>
  )
}