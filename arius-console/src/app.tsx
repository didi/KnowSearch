import React, { useState } from "react";
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom";
import _ from "lodash";
import { ConfigProvider } from "antd";
import zhCN from "antd/lib/locale/zh_CN";
import antdZhCN from "antd/lib/locale/zh_CN";
import antdEnUS from "antd/lib/locale/en_US";
import { IntlProvider } from "react-intl";
import intlZhCN from "./locales/zh";
import intlEnUS from "./locales/en";
import LayoutHeaderNav from "@pkgs/Layout";
import { InjectIntlContext } from "@pkgs/hooks/useFormatMessage";
import { Page403, Page404 } from "@pkgs/Exception";
import LayoutMain from "@pkgs/Layout/LeftMenu";
import { leftMenus, systemKey, urlPrefix } from "./constants/menu";
import { Provider as ReduxProvider } from "react-redux";
import store from "./store";

import { ClusterAdmin } from "./pages/cluster-admin";
import "./styles/common.less";
import { ClusterIndex } from "./pages/cluster-index";
import { ClusterSystem } from "./pages/cluster-system";
import { IndicatorsKanban } from "./pages/indicators-kanban";
import { Login } from "./packages/login";
import { UserManagement } from "./pages/user-management";
import { WorkOrder } from "./pages/work-order";
import { Scheduling } from "./pages/scheduling";
import { SearchQuery } from "./pages/search-query";
import { IndexAdminPage } from "./pages/index-admin";
import { IndexTplManagementPage } from "./pages/index-tpl-management";
import { getNoCodeLoginAppList } from "api/app-api";
import { getCookie, getCurrentProject, setCookie } from "lib/utils";
import './assets/icon/iconfont.css';
import './assets/icon/iconfont.js';

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

const App = () => {
  const [language, setLanguage] = useState(defaultLanguage);
  const [projectList, setProjectList] = useState([]); // 项目列表
  const [currentProject, setCurrentProject] = useState(getCurrentProject());

  const intlMessages = _.get(localeMap[language], "intlMessages", intlZhCN);
  const title = _.get(localeMap[language], "title");

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

  return (
    <IntlProvider
      locale={_.get(localeMap[language], "intl", "zh")}
      messages={intlMessages}
    >
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
                    language={language}
                    projectList={projectList}
                    onLanguageChange={setLanguage}
                    tenantProjectVisible={true}
                    documentGuideVisible={true}
                    currentProject={currentProject}
                    setCurrentProject={setCurrentProject}
                    onMount={() => {}}
                  >
                    <LayoutMain
                      siderMenuVisible={true}
                      systemName={systemKey}
                      systemNameChn={title}
                      menus={leftMenus}
                    >
                      {
                        <Switch>
                          <Route
                            path="/"
                            exact={true}
                            component={ClusterAdmin}
                          />
                          <Route
                            path="/cluster"
                            exact={true}
                            component={ClusterAdmin}
                          />
                          <Route
                            path="/cluster/:page"
                            exact={true}
                            component={ClusterAdmin}
                          />
                          <Route
                            path="/cluster/:page/:page"
                            exact={true}
                            component={ClusterAdmin}
                          />

                          <Route path="/index" exact component={ClusterIndex} />
                          <Route
                            path="/index/:page"
                            exact
                            component={ClusterIndex}
                          />
                          <Route
                            path="/index/:page/:page"
                            exact
                            component={ClusterIndex}
                          />

                          <Route
                            path="/indicators"
                            exact
                            component={IndicatorsKanban}
                          />
                          <Route
                            path="/indicators/:page"
                            exact
                            component={IndicatorsKanban}
                          />

                          <Route
                            path="/system"
                            exact
                            component={ClusterSystem}
                          />
                          <Route
                            path="/system/:page"
                            exact
                            component={ClusterSystem}
                          />

                          <Route
                            path="/user"
                            exact
                            component={UserManagement}
                          />
                          <Route
                            path="/user/:page"
                            exact
                            component={UserManagement}
                          />
                          <Route
                            path="/user/:page/:page"
                            exact
                            component={UserManagement}
                          />

                          <Route
                            path="/work-order"
                            exact
                            component={WorkOrder}
                          />
                          <Route
                            path="/work-order/:page"
                            exact
                            component={WorkOrder}
                          />
                          <Route
                            path="/work-order/:page/:page"
                            exact
                            component={WorkOrder}
                          />
                          <Route
                            path="/scheduling"
                            exact
                            component={Scheduling}
                          />
                          <Route
                            path="/scheduling/:page"
                            exact
                            component={Scheduling}
                          />
                          <Route
                            path="/scheduling/:page/:page"
                            exact
                            component={Scheduling}
                          />
                          <Route
                            path="/search-query/:page"
                            exact
                            component={SearchQuery}
                          />
                          <Route
                            path="/index-admin"
                            exact
                            component={IndexAdminPage}
                            />
                          <Route
                            path="/index-admin/:page"
                            exact
                            component={IndexAdminPage}
                            />
                          <Route 
                            path="/index-tpl-management"
                            exact
                            component={IndexTplManagementPage}
                          />
                          <Route
                            path="/index-tpl-management/:page"
                            exact
                            component={IndexTplManagementPage}
                          />
                          <Route component={Page404} />
                        </Switch>
                      }
                    </LayoutMain>
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
