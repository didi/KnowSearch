import React, { useEffect, useState } from "react";
import _ from "lodash";
import { prefixCls } from "./config";
import "./style.less";
import { Layout } from "knowdesign";
import { IProject } from "./ProjectSelect";
import HeaderConditionComponent from "./HeaderConditionComponent";
import { withRouter } from "react-router-dom";
import { isSuperApp } from "lib/utils";

interface Props {
  children: React.ReactNode;
  language?: string;
  onLanguageChange?: (language: string) => void;
  setCurrentProject?: any;
  projectList?: IProject[];
  projectLoding?: boolean;
  currentProject?: IProject;
  onMount?: () => void;
  feConf: any;
  logout: (params?: any) => any;
  setLeftIndex?: (index: number) => void;
  leftIndex?: number;
  setHeaderClick?: (bool: boolean) => void;
}

const { Header } = Layout;
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
const index = withRouter((props: any) => {
  const cPrefixCls = `${prefixCls}-layout`;
  const [feConf] = useState(props.feConf || {});

  useEffect(() => {
    if (props.leftIndex === 0 && props.history.location.pathname === "/work-order/task/detail") {
      props.setLeftIndex(1);
    }
  }, [props.history.location.pathname]);

  return (
    <Layout className={cPrefixCls}>
      <Header className={`${cPrefixCls}-header ${_.get(feConf, "header.theme")}`}>
        <div
          className={`${cPrefixCls}-header-left`}
          onClick={() => {
            isSuperApp() ? props.history.push(_.get(feConf, "header.logohref") || "/") : props.history.push("/cluster/logic");
            props.setLeftIndex(0);
          }}
        >
          <div className={`${cPrefixCls}-header-left-logo`}></div>
        </div>
        <div className={`${cPrefixCls}-header-right`}>
          {_.get(feConf, "header.rightEle").map((item, index) => HeaderConditionComponent(item, index, props))}
        </div>
      </Header>
      <div
        style={{
          overflow: "hidden",
          position: "relative",
          width: "100%",
        }}
      >
        <div className={`${cPrefixCls}-main`}>{props.children}</div>
      </div>
    </Layout>
  );
});
export default index;
