import "@babel/polyfill";
import "whatwg-fetch";
import React, { useState } from "react";
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom";
import _ from "lodash";
import { ConfigProvider } from "antd";
import zhCN from "antd/lib/locale/zh_CN";
import antdZhCN from "antd/lib/locale/zh_CN";
import antdEnUS from "antd/lib/locale/en_US";
import { IntlProvider } from "react-intl";
import intlZhCN, { permissions } from "./locales/zh";
import intlEnUS from "./locales/en";
import {LayoutHeaderNav} from "./d1-packages";
import { Page403, Page404 } from "./d1-packages";
import { LeftMenu } from "./d1-packages";
import { formatMessage } from './d1-packages'
import { leftMenus, systemKey, urlPrefix } from "./constants/menu";
import { Provider as ReduxProvider } from "react-redux";
import store, { useGlobalPathStatus } from "./store";
const { InjectIntlContext } = formatMessage;
import "./styles/common.less";
import { Login } from "./packages/login";
import { getNoCodeLoginAppList } from "api/app-api";
import { getCookie, getCurrentProject, setCookie } from "lib/utils";
import { userLogout } from "api/user-api";
import { RouterTabs } from './d1-packages'
import { dealPathname } from "lib/utils";
import './assets/icon/iconfont.css';
import './assets/icon/iconfont.js';
import { UserCenter } from "./container/layout-element/UserCenter"
import * as actions from 'actions';
import { RouteGuard } from './packages/route-guard/';
import { PageRoutes } from './pages/index';
import { dropByCacheKey } from 'react-router-cache-route';
import AllModalInOne from "container/AllModalInOne";
import FullScreen from "container/full-screen";
import { cachePage } from './pages/cachePage';
interface ILocaleMap {
  [index: string]: any;
}

const localeMap: ILocaleMap = {
  zh: {
    antd: antdZhCN,
    intl: "zh",
    intlMessages: intlZhCN,
    title: systemKey,
  },
  en: {
    antd: antdEnUS,
    intl: "en",
    intlMessages: intlEnUS,
    title: systemKey,
  },
};

export const { Provider, Consumer } = React.createContext("zh");

const defaultLanguage = window.localStorage.getItem("language") || "zh"; // navigator.language.substr(0, 2)
const feConfig = require('../config/feConfig.json')

const App = () => {
  const [language, setLanguage] = useState(defaultLanguage);
  const [projectList, setProjectList] = useState([]); // 项目列表
  const [currentProject, setCurrentProject] = useState(getCurrentProject());

  const intlMessages = _.get(localeMap[language], "intlMessages", intlZhCN);
  const title = _.get(localeMap[language], "title");

  const initCollapsed = getCookie('siderMenuCollapsed');
  const [siderMenuCollapsed, setSiderMenuCollapsed] = React.useState(initCollapsed === 'true');

  React.useEffect(() => {
    const secret = `${currentProject?.id}:${currentProject?.verifyCode}`;
    if (currentProject) {
      setCookie([{ key: "Authorization", value: btoa(secret) }]);
      localStorage.setItem("current-project", JSON.stringify(currentProject));
    } else {
      //解决刷新后所属项目变更
      setCurrentProject(
        JSON.parse(localStorage.getItem("current-project") || "null")
      );
    }
  }, [currentProject]);

  const changeCurrentProject = (value) => {
    localStorage.setItem("current-project", JSON.stringify(value));
    setCurrentProject(value);
  }

  React.useEffect(() => {
    if (window.location.pathname.indexOf("login") === -1) {
      const domainAccount = getCookie("domainAccount");
      if (!domainAccount) {
        window.location.href = "/login";
        return;
      }
      getNoCodeLoginAppList(domainAccount).then((data) => {
        if (
          !localStorage.getItem("current-project") ||
          localStorage.getItem("current-project") === "{}" ||
          localStorage.getItem("current-project") === "null"
        ) {
          localStorage.setItem(
            "current-project",
            JSON.stringify(data?.[0] || {})
          );
          setCurrentProject(data?.[0] || {});
        } else {
          //项目是否存在于当前项目列表
          const app = JSON.parse(localStorage.getItem("current-project"));
          const arr = data?.filter((item) => item.name === app.name);
          if (!arr.length) {
            localStorage.setItem(
              "current-project",
              JSON.stringify(data?.[0] || {})
            );
          }
        }
        setProjectList(data);
      });
    }
  }, []);

  const logout = () => {
    userLogout().then(() => {
      // window.location.href = '/login';
      localStorage.setItem("current-project", JSON.stringify({}));
    });
  };
  const [removePath, setRemovePaths] = useGlobalPathStatus();

  const CacheFilter = (path: string) => {
    return cachePage.includes(path);
  }

  return (
    <IntlProvider locale={_.get(localeMap[language], "intl", "zh")} messages={intlMessages}>
      <ConfigProvider locale={zhCN}>
        <InjectIntlContext>
          <Provider value={language}>
            <ReduxProvider store={store}>
              <BrowserRouter basename={systemKey}>
                <Switch>
                  <Route exact={true} path={"/login"} component={Login} />
                  <Route exact={true} path="/403" component={Page403} />
                  <Route exact={true} path="/404" component={Page404} />
                  <LayoutHeaderNav
                    logout={logout}
                    feConf={feConfig}
                    language={language}
                    projectList={projectList}
                    onLanguageChange={setLanguage}
                    currentProject={currentProject}
                    setCurrentProject={changeCurrentProject}
                    onMount={() => {}}
                    UserCenter={UserCenter}
                  >
                    <LeftMenu
                      siderMenuVisible={true}
                      systemName={systemKey}
                      systemNameChn={title}
                      menus={leftMenus}
                      intlMessages={intlMessages}
                      locale={_.get(localeMap[language], "intl", "zh")}
                      onSiderMenuChange={setSiderMenuCollapsed}
                    >
                      <RouterTabs
                        siderMenuCollapsed={siderMenuCollapsed}
                        tabList={[]}
                        intlZhCN={intlZhCN}
                        systemKey={systemKey}
                        dealPathname={dealPathname}
                        removePaths={removePath}
                        defaultTab={{
                          key: `menu.${systemKey}.cluster.physics`,
                          label: "物理集群",
                          href: "/cluster/physics",
                          show: true,
                        }}
                        permissions={permissions}
                        currentProject={currentProject}
                        pageEventList={[
                          {
                            key: `menu.${systemKey}.index.create`,
                            onCloseCallback: () => {
                              dropByCacheKey('index/create');
                              store.dispatch(actions.setClearCreateIndex());
                            },
                          },
                        ]}
                      />
                      <AllModalInOne />
                      <FullScreen />
                      <RouteGuard pathRule={CacheFilter} routeList={PageRoutes} routeType="cache" attr={{ setRemovePaths: setRemovePaths }} />
                    </LeftMenu>
                  </LayoutHeaderNav>
                  <Route render={() => <Redirect to="/404" />} />
                </Switch>
              </BrowserRouter>
            </ReduxProvider>
          </Provider>
        </InjectIntlContext>
      </ConfigProvider>
    </IntlProvider>
  );
};

export default App;
