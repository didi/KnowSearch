import { CopyOutlined } from '@ant-design/icons';
import React, { useState } from 'react';
import { LOGIN_MENU, LOGIN_MENU_MAP } from './config';
import { debounce } from 'lodash';
import './index.less';

export const Login: React.FC<any> = () => {
  const defaultKey = window.location.hash.replace('#', '') || LOGIN_MENU[0].key;
  const [selectedKeys, setSelectedKeys] = useState([defaultKey]);
  const [critical, setCritical] = useState(false);

  React.useEffect(() => {
    if (window.innerWidth < 1366) {
      setCritical(true);
    }
  }, [critical]);

  React.useEffect(() => {
    window.addEventListener('resize', handleResize); //监听窗口大小改变
    return () => window.removeEventListener('resize', debounce(handleResize, 500));
  }, []);

  const handleResize = e => {
    if (e.target.innerWidth < 1366) {
      setCritical(true);
    } else {
      setCritical(false);
    }
  }

  const renderContent = () => {
    return LOGIN_MENU_MAP.get(selectedKeys[0])?.render(handleMenuClick) || LOGIN_MENU_MAP.get(LOGIN_MENU[0].key)?.render(handleMenuClick);
  }

  const handleMenuClick = (e: string) => {
    setSelectedKeys([e]);
    window.location.hash = e;
  }

  /*
  *屏幕宽度大于等于1366
  *@ 图片宽度占页面宽度比三分之一，等比缩放
  *@ 表单区域占比三分之二，区域内水平居中
  *屏幕宽度小于1366
  *@ 不展示图片
  *@ 水平居中
  */

  const renderHeader = () => {
    return (
      <>
        {
          critical === true ?
            <div className='login-box-form-center-header-left'>
              <div className='login-box-form-center-header-left-log'></div>
              ElasticSearch Manager
            </div>
            :
            <div></div>
        }
        <div className='login-box-form-center-header-right'>
          <CopyOutlined />
          说明文档
        </div>
      </>
    )
  }

  const isLogin = (defaultKey === 'login');

  return (
    <div className='login-box'>
      <div className='login-box-left' style={{ display: critical ? 'none' : null }}>
        <div className='login-box-left-img'></div>
        <div className='login-box-left-title'>Elastic Search Manage</div>
        <div className='login-box-left-text'>
          <div className='login-box-left-text-leftline'></div>
          滴滴开源
          <div className='login-box-left-text-rithline'></div>
        </div>
        <div className='login-box-left-conter'>分布式搜索管控平台</div>
      </div>
      <div className='login-box-form'>
        <div className='login-box-form-center'>
          <div className='login-box-form-center-header'>
            {renderHeader()}
          </div>
          <div className='login-box-form-center-title' style={isLogin ? { marginTop: '6%' } : null}>
            <div className='login-box-form-center-title-left'>
              {
                isLogin ? '登录' :
                  '账号注册'
              }
            </div>
            <div className='login-box-form-center-title-right'>
              {
                defaultKey === 'register' ? <div>已有账号，<a onClick={() => handleMenuClick('login')}>直接登录</a></div>
                  : <div> 还没账号，<a onClick={() => handleMenuClick('register')}>立即注册</a></div>
              }
            </div>
          </div>
          <div className="login-box-form-center-content">
            {renderContent()}
          </div>
          <div className="login-box-form-center-bottom">
            <p>地址地址地址地址地址 邮编：101010 </p>
            <p>电话：00-10-0001212 传真：00-10-00011223 Email：esyunwei@epri.sgcc.com.cn</p>
          </div>
        </div>
      </div>
    </div>
  )
}