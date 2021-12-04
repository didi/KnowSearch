import React, { useEffect, useState } from "react";
import _ from "lodash";
import { FormattedMessage } from "react-intl";
import { Dropdown, Menu, Divider, TreeSelect, Popover, Drawer } from "antd";
import { prefixCls } from "./config";
import "./style.less";
// import './assets/iconfont/iconfont.css';
// import './assets/iconfont/iconfont.js';
import "./assets/iconfont-es/iconfont.js";
import projectLogo from "../../assets/es-logo.png";
import { CustomAppDropDown } from "../ProjectSelect";
import { IProject } from "interface/project";
import { Layout } from "antd";
import { UserCenter } from "./UserCenter";
import { userLogout } from "api/user-api";
import { isOpenUp } from "constants/common";

const feConfig = require("../../../config/feConfig.json");

interface Props {
  tenantProjectVisible: boolean;
  children: React.ReactNode;
  language: string;
  onLanguageChange: (language: string) => void;
  setCurrentProject: any;
  projectList: IProject[];
  currentProject: IProject;
  onMount: () => void;
  documentGuideVisible?: boolean;
}

const userIconSrc = require("./assets/avatars.png");
const { Header } = Layout;
const getSymbolByLanguage = (language: string) => {
  if (language === "zh") return "#iconzhongwenicon";
  if (language === "en") return "#iconyingwenicon";
  return "";
};
const normalizeTenantProjectData = (
  data: any[],
  tenantIdent?: string,
  tenantId?: number,
  tenantName?: string
): any => {
  return _.map(data, (item) => {
    if (item.children) {
      return {
        ...item,
        tenantIdent: tenantIdent || item.ident,
        tenantId: tenantId || item.id,
        tenantName: tenantName || item.name,
        children: normalizeTenantProjectData(
          item.children,
          tenantIdent || item.ident,
          tenantId || item.id,
          tenantName || item.name
        ),
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
const treeIcon: (node: any) => JSX.Element = (node) => (
  <span
    style={{
      display: "inline-block",
      backgroundColor: node.icon_color,
      width: 16,
      height: 16,
      lineHeight: "16px",
      borderRadius: 16,
      color: "#fff",
    }}
  >
    {node.icon_char}
  </span>
);
const renderTreeNodes = (nodes: any[]) => {
  return _.map(nodes, (node) => {
    if (_.isArray(node.children)) {
      return (
        <TreeSelect.TreeNode
          icon={treeIcon(node)}
          title={node.name}
          fullTitle={`${node.tenantName}-${node.name}`}
          key={String(node.id)}
          value={node.id}
          path={node.path}
          node={node}
          selectable={false}
        >
          {renderTreeNodes(node.children)}
        </TreeSelect.TreeNode>
      );
    }
    return (
      <TreeSelect.TreeNode
        icon={treeIcon(node)}
        title={node.name}
        fullTitle={`${node.tenantName}-${node.name}`}
        key={String(node.id)}
        value={node.id}
        path={node.path}
        isLeaf={true}
        node={node}
      />
    );
  });
};

export default function index(props: Props) {
  const cPrefixCls = `${prefixCls}-layout`;
  const [dispname, setDispname] = useState("");
  const [feConf, setFeConf] = useState(feConfig as any);
  const [visible, setVisible] = useState(false);

  const logout = () => {
    userLogout().then(() => {
      // window.location.href = '/login';
      localStorage.setItem("current-project", JSON.stringify({}));
    });
  };

  return (
    <Layout className={cPrefixCls}>
      <Header
        className={`${cPrefixCls}-header ${_.get(feConf, "header.theme")}`}
      >
        <div className={`${cPrefixCls}-header-left`}>
          <a href="/" className={`${cPrefixCls}-logo`}>
            <img
              src={projectLogo}
              alt="logo"
              style={{
                height: 24,
              }}
            />
            {_.get(feConf, "header.subTitle")}
          </a>
        </div>
        <div className={`${cPrefixCls}-header-right`}>
          {_.get(feConf, "header.mode") === "complicated" ? (
            <>
              {props.documentGuideVisible ? (
                <Dropdown
                  placement="bottomRight"
                  overlay={
                    <Menu style={{ width: 100 }}>
                      <Menu.Divider />
                      <Menu.Item>
                        <a href="https://logi-em.s3.didiyunapi.com/LogiEM用户指南.pdf">
                          <FormattedMessage id="user-guide" />
                        </a>
                      </Menu.Item>
                      <Menu.Item>
                        <a href="https://logi-em.s3.didiyunapi.com/LogiEM最佳实践.pdf ">
                          <FormattedMessage id="op-manual" />
                        </a>
                      </Menu.Item>
                      <Menu.Item>
                        <a href="https://logi-em.s3.didiyunapi.com/常见FAQ.pdf">
                          <FormattedMessage id="common-qa" />
                        </a>
                      </Menu.Item>
                    </Menu>
                  }
                >
                  <span className={`${cPrefixCls}-doc`}>
                    <span style={{ paddingRight: 5 }}>
                      <FormattedMessage id="doc-center" />
                    </span>
                    <svg
                      className={`${prefixCls}-layout-menus-icon small`}
                      aria-hidden="true"
                    >
                      <use xlinkHref="#iconxuanzekuangzhankai"></use>
                    </svg>
                  </span>
                </Dropdown>
              ) : null}
              {props.tenantProjectVisible ? (
                <>
                  <CustomAppDropDown
                    setCurrentProject={props.setCurrentProject}
                    projectList={props.projectList}
                    currentProject={props.currentProject}
                  />
                </>
              ) : null}
              <div className={`${cPrefixCls}-header-right-links`}>
                {Array.isArray(_.get(feConf, "header.right_links")) &&
                  _.get(feConf, "header.right_links").map(
                    (
                      item: {
                        icon: string | undefined;
                        href: string;
                        text: string;
                      },
                      index: number
                    ) => {
                      return item.icon ? (
                        <a href={item.href} key={index}>
                          <Popover content={item.text}>
                            <svg
                              className={`${cPrefixCls}-header-menus-icon`}
                              aria-hidden="true"
                            >
                              <use xlinkHref={item.icon}></use>
                            </svg>
                          </Popover>
                        </a>
                      ) : (
                        <a href={item.href} key={index}>
                          {item.text}
                        </a>
                      );
                    }
                  )}
              </div>
              <Divider
                className={`${cPrefixCls}-header-right-divider`}
                type="vertical"
              />
              {/* <div className={`${cPrefixCls}-header-right-icons`}>
                <a
                    onClick={() => {
                      const newLanguage = props.language == 'en' ? 'zh' : 'en';
                      props.onLanguageChange(newLanguage);
                    }}
                  >
                    <svg style={{ width: 20, height: 14 }} aria-hidden="true">
                      <use xlinkHref={getSymbolByLanguage(props.language)}></use>
                    </svg>
                  </a>
              </div>
              <Divider
                className={`${cPrefixCls}-header-right-divider`}
                type="vertical"
              /> */}
            </>
          ) : null}
          <Dropdown
            placement="bottomRight"
            overlay={
              <Menu style={{ width: 125 }}>
                <Menu.Item>
                  <a onClick={() => logout()}>
                    <FormattedMessage id="logout" />
                  </a>
                </Menu.Item>
              </Menu>
            }
          >
            <span className={`${cPrefixCls}-username`}>
              <img
                src={userIconSrc}
                alt=""
                onClick={() => {
                  setVisible(true);
                }}
              />
              <span style={{ paddingRight: 5 }}>{dispname}</span>
              <svg
                className={`${prefixCls}-layout-menus-icon`}
                aria-hidden="true"
              >
                <use xlinkHref="#iconxuanzekuangzhankai"></use>
              </svg>
            </span>
          </Dropdown>
        </div>
      </Header>
      <div
        style={{
          overflow: "hidden",
          position: "relative",
        }}
      >
        <div className={`${cPrefixCls}-main`}>{props.children}</div>
      </div>
      <Drawer
        title={<FormattedMessage id="user-center" />}
        placement="right"
        onClose={() => {
          setVisible(false);
        }}
        visible={visible}
        mask={false}
        width={516}
        className={"user-center-drawer"}
      >
        <UserCenter />
      </Drawer>
    </Layout>
  );
}
