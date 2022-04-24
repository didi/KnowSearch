import React, { useEffect, useState } from 'react';
import _ from 'lodash';
import { prefixCls } from './config';
import './style.less';
import './assets/iconfont-es/iconfont.js';
import { Layout } from 'antd';
import { IProject } from './ProjectSelect';
import { request } from '../Utils/request';
import HeaderConditionComponent from './HeaderConditionComponent';
interface Props {
  children: React.ReactNode;
  language: string;
  onLanguageChange: (language: string) => void;
  setCurrentProject?: any;
  projectList?: IProject[];
  projectLoding?: boolean;
  currentProject?: IProject;
  onMount: () => void;
  feConf: any;
  logout: (params?: any) => any;
  UserCenter?: React.ReactNode;
}

const { Header } = Layout;
// const getSymbolByLanguage = (language: string) => {
//   if (language === 'zh') return '#iconzhongwenicon';
//   if (language === 'en') return '#iconyingwenicon';
//   return '';
// };
const normalizeTenantProjectData = (data: any[], tenantIdent?: string, tenantId?: number, tenantName?: string): any => {
  return _.map(data, (item) => {
    if (item.children) {
      return {
        ...item,
        tenantIdent: tenantIdent || item.ident,
        tenantId: tenantId || item.id,
        tenantName: tenantName || item.name,
        children: normalizeTenantProjectData(item.children, tenantIdent || item.ident, tenantId || item.id, tenantName || item.name),
      };
    }
    return {
      ...item,
      tenantIdent,
      tenantId,
      tenantName,
    };
  });
};
export default function index(props: Props) {
  const cPrefixCls = `${prefixCls}-layout`;
  const [feConf] = useState(props.feConf || {});
  // const [dispname, setDipname] = useState('');
  // const [userLoggedIn, setUserlogged] = useState<Boolean>(false);


  // useEffect(() => {
  //   if (userLoggedIn) {
  //     props.onMount();
  //   }
  // }, [userLoggedIn]);

  // useEffect(() => {
  //   request('/api/v2/ckm/sysUser/getUserInfo').then((res: any) => {
  //     console.log('userInfo:', res);
  //     setDipname(res.realname || '');
  //     setUserlogged(true);
  //   });
  // }, []);

  return (
    <Layout className={cPrefixCls}>
      <Header className={`${cPrefixCls}-header ${_.get(feConf, 'header.theme')}`}>
        <div className={`${cPrefixCls}-header-left`}>
          <a href="/" className={`${cPrefixCls}-logo`}>
            <img
              src={require('../../assets/nav-logo.png')}
              alt="logo"
            />
            {/* {_.get(feConf, 'header.subTitle')} */}
          </a>
        </div>
        <div className={`${cPrefixCls}-header-right`}> 
          {_.get(feConf, "header.rightEle").map((item, index) => HeaderConditionComponent(item, index, props))}
        </div>
      </Header>
      <div
        style={{
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <div className={`${cPrefixCls}-main`}>{props.children}</div>
      </div>
    </Layout>
  );
}
