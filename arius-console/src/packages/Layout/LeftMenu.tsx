import React, { useEffect } from "react";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import { withRouter, RouteComponentProps } from "react-router-dom";
import { Layout } from "antd";
import classNames from "classnames";
import _ from "lodash";
import { prefixCls } from "./config";
import MeunContent from "./MenuContent";
import "./assets/iconfont/iconfont.css";
import "./assets/iconfont/iconfont.js";
import { setSlierMenu } from "../../actions/silderMenu";
import { asyncMicroTasks, resize } from "lib/utils";

interface Props {
  noBackground?: boolean;
  menus: any;
  treeVisible?: boolean;
  systemName: string;
  systemNameChn: string;
  children: React.ReactNode;
  siderMenuVisible?: boolean; // 是否需要菜单
  intlInfo?: any;
}

const { Content, Sider } = Layout;

const MenuLayout = (props: Props & RouteComponentProps) => {
  const { sliderMenuCollapsed } = useSelector(
    (state) => ({
      sliderMenuCollapsed: (state as any).sliderMenu.sliderMenuCollapsed,
    }),
    shallowEqual
  );
  const dispatch = useDispatch();

  const { systemName, menus, systemNameChn, siderMenuVisible } = props;
  const [menuCollapsed, setMenuCollapsed] = React.useState(sliderMenuCollapsed);
  const [permissionPoints, setPermissionPoints] = React.useState(null);
  const currentSystemMenuConf = _.get(menus, "children");
  const cPrefixCls = `${prefixCls}-layout`;

  const renderContent = () => {
    const { noBackground = false } = props;
    const cPrefixCls = `${prefixCls}-layout`;

    return (
      <Layout
        className={classNames({
          [`${cPrefixCls}-container`]: true,
        })}
        style={{ height: "100%" }}
      >
        <Content className={`${cPrefixCls}-content`} style={{ position: "relative" }}>
          <div
            className={classNames({
              [`${cPrefixCls}-main`]: true,
              [`${cPrefixCls}-main-noBg`]: noBackground,
            })}
            id={`${cPrefixCls}-main`}
          >
            {props.children}
          </div>
        </Content>
      </Layout>
    );
  };
  return (
    <>
      <Layout className={cPrefixCls}>
        {siderMenuVisible && (
          <Sider
            theme="light"
            width={190}
            collapsedWidth={56}
            className={classNames({
              [`${cPrefixCls}-sider-nav`]: true,
            })}
            trigger={null}
            collapsible
            collapsed={menuCollapsed}
          >
            <MeunContent
              systemName={systemName}
              systemNameChn={systemNameChn}
              menuConf={currentSystemMenuConf}
              className={`${cPrefixCls}-menu`}
              collapsed={menuCollapsed}
              permissionPoints={permissionPoints}
            />
            <div
              className={`${prefixCls}-layout-sider-nav-bottom`}
              onClick={() => {
                setMenuCollapsed(!menuCollapsed);
                dispatch(setSlierMenu(!menuCollapsed));
                asyncMicroTasks(resize);
                window.localStorage.setItem(
                  "siderMenuCollapsed",
                  String(!menuCollapsed)
                );
              }}
            >
              <svg
                className={`${prefixCls}-layout-menus-icon`}
                aria-hidden="true"
              >
                <use
                  xlinkHref={
                    menuCollapsed ? "#iconzhankaiicon" : "#iconshouqiicon"
                  }
                />
              </svg>
            </div>
          </Sider>
        )}
        <Content
          style={{
            marginLeft: siderMenuVisible ? (menuCollapsed ? 56 : 190) : 0,
            // overflow: 'hidden',
          }}
        >
          {renderContent()}
        </Content>
      </Layout>
    </>
  );
};

export default withRouter(MenuLayout);
