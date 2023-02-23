import "@babel/polyfill";
import "whatwg-fetch";
import React, { useMemo, useState } from "react";
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom";
import _ from "lodash";
import { ConfigProvider, Modal, Spin } from "antd";
import zhCN from "antd/lib/locale/zh_CN";
import antdZhCN from "antd/lib/locale/zh_CN";
import antdEnUS from "antd/lib/locale/en_US";
import { IntlProvider } from "react-intl";
import intlZhCN, { permissions } from "./locales/zh";
import intlEnUS from "./locales/en";
import { LayoutHeaderNav } from "./d1-packages";
import { Page403, Page404 } from "./d1-packages";
import { LeftMenu } from "./d1-packages";
import { InjectIntlContext } from "knowdesign/lib/hook/use-format-message";
import { leftMenus, systemKey } from "./constants/menu";
import { Provider as ReduxProvider } from "react-redux";
import store, { useGlobalPathStatus, useGlobalLoginStatus } from "./store";
import "./styles/common.less";
import { LoginOrRegister } from "./d1-packages/CommonPages/Login";
import { getCookie, getCurrentProject, setCookie, dealPathname, isSuperApp, currentLeftIndex, redirectPath } from "lib/utils";
import { getUser, userLogout } from "api/logi-security";
import "./assets/icon/iconfont.css";
import "./assets/icon/iconfont.js";
import * as actions from "actions";
import { RouteGuard } from "./d1-packages/RouterGuard";
import { PageRoutes } from "./pages/index";
import { dropByCacheKey } from "react-router-cache-route";
import AllModalInOne from "container/AllModalInOne";
import FullScreen from "container/full-screen";
import { mulityPage } from "./pages/cachePage";
import { CURRENT_PROJECT_KEY } from "constants/common";
import { IProject } from "interface/project";
import { getProjectListByUserId, getNoCodeLoginAppList, checkBindGateway } from "api/app-api";
import { getPagePermission } from "lib/permission";
import { IPermission } from "store/type";
import { judgeAdminUser } from "api/user-api";
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
const feConfig = require("../config/feConfig.json");

const App = () => {
  const [language, setLanguage] = useState(defaultLanguage);
  const [projectList, setProjectList] = useState([]); // 项目列表
  const [loading, setLoading] = useState(true);
  const [currentProject, setCurrentProject] = useState(getCurrentProject());
  const [permissionPoints, setPermissionPoints] = useState<IPermission[]>([]);
  const [leftIndex, setLeftIndex] = useState(currentLeftIndex(isSuperApp()));

  const intlMessages = _.get(localeMap[language], "intlMessages", intlZhCN);
  const title = _.get(localeMap[language], "title");
  const initCollapsed = getCookie("siderMenuCollapsed");
  const [siderMenuCollapsed, setSiderMenuCollapsed] = React.useState(initCollapsed === "true");
  const [removePath, setRemovePaths] = useGlobalPathStatus();
  const [loginStatus] = useGlobalLoginStatus();
  const setHeaderClick = (key, props: any) => {
    if (currentLeftIndex(isSuperApp()) !== key) {
      props.history.push(key ? "/system/project" : isSuperApp() ? "/dashboard" : "/cluster/logic");
    }
  };

  const getPermissions = () => {
    const userId = getCookie("userId");
    setLoading(true);
    getUser(+userId)
      .then((res) => {
        const _permissionPoints = res.permissionTreeVO?.childList || [];
        _permissionPoints.push({
          permissionName: "dashboard",
          has: getCookie("isAdminUser") === "yes" && isSuperApp(),
        });
        setPermissionPoints(_permissionPoints);
        store.dispatch(actions.setUserPermissionTree(_permissionPoints));
      })
      .finally(() => {
        setLoading(false);
        store.dispatch(actions.setIsAdminUser(getCookie("isAdminUser") === "yes"));
      });
  };

  const setProjectInfo = (currentProject) => {
    if (currentProject?.id) {
      window.localStorage.setItem(CURRENT_PROJECT_KEY, JSON.stringify(currentProject));
      getNoCodeLoginAppList(currentProject?.id).then((res) => {
        let filterData = res.filter((item) => item.defaultDisplay);
        let secret = `${(filterData[0] || res[0])?.id}:${(filterData[0] || res[0])?.verifyCode}`;
        setCookie([{ key: "Authorization", value: btoa(secret) }]);
      });
      checkBindGateway().then((res) => {
        store.dispatch(actions.setGatewayBindStatus(res));
      });
    }
    getPermissions();
  };

  const changeCurrentProject = (value, props: any) => {
    setCurrentProject(value);
    setProjectInfo(value);
    setLeftIndex(0);
    console.log(isSuperApp());
    props.history.push(isSuperApp() ? "/dashboard" : "/cluster/logic");
  };

  React.useEffect(() => {
    const userId = getCookie("userId");
    if (!userId && !window.location.pathname.includes("/login")) {
      window.location.href = "/login";
      return;
    }
    if (window.location.pathname.includes("/login")) {
      return;
    }
    setLoading(true);
    store.dispatch(actions.setGlobalUserInfo({ id: userId }));

    getProjectListByUserId(+userId).then((data: IProject[]) => {
      data = data.map((item) => ({
        ...item,
        name: item.projectName,
      })) as unknown as IProject[];
      const app = getCurrentProject();
      const arr = data?.filter((item) => item.id === app.id);
      let _currentProject = (arr.length ? arr[0] : data?.[0]) || ({} as IProject);

      setCurrentProject(_currentProject);
      setProjectInfo(_currentProject);
      setProjectList(data);
      store.dispatch(actions.setProjectList(data));
    });
    judgeAdminUser().then((res) => {
      setCookie([{ key: "isAdminUser", value: res.code === 0 ? "yes" : "no" }]);
    });
  }, [loginStatus]);

  const logout = () => {
    userLogout().then(() => {
      window.localStorage.setItem(CURRENT_PROJECT_KEY, JSON.stringify({}));
      store.dispatch(actions.setGlobalUserInfo({}));
      store.dispatch(actions.setProjectList([]));

      setCookie([
        { key: "userName", value: "" },
        { key: "userId", value: "" },
        { key: "isAdminUser", value: "" },
      ]);
    });
  };

  // 路由前置守卫
  const routeBeforeEach = (props: any) => {
    const { permissionPoint, history, path } = props;
    if (path.includes("/dashboard") && (getCookie("isAdminUser") === "no" || !isSuperApp())) {
      return Promise.reject(false);
    }
    if (permissionPoint) {
      const hasPagePermission = getPagePermission(permissionPoint, permissionPoints);
      if (!hasPagePermission) {
        return Promise.reject(false);
      }
      return Promise.resolve(true);
    }
    return Promise.resolve(true);
  };

  const renderRouteGuard = () => {
    const routeList = PageRoutes.map((item) => {
      return item.needCache
        ? {
            ...item,
            cacheKey: `menu.${systemKey}${item.path.split("/").join(".")}`,
          }
        : item;
    });
    return (
      <RouteGuard beforeEach={routeBeforeEach} routeList={routeList} attr={{ setRemovePaths: setRemovePaths }} mulityPage={mulityPage} />
    );
  };

  const renderNoProjectModal = () => {
    Modal.warning({
      title: "抱歉，请先创建应用！",
      content: "您的账户还不具备应用信息",
      okText: "创建应用",
      onOk: () => {
        store.dispatch(actions.setDrawerId("addOrEditProjectModal", { type: "create", callback: renderNoProjectModal }));
      },
    });
  };

  const renderPageLoading = () => {
    return <Spin className="spin-name" spinning={loading} />;
  };

  const RenderContent = useMemo(() => {
    return loading ? renderPageLoading() : !projectList.length ? renderNoProjectModal() : renderRouteGuard();
  }, [loading, projectList, permissionPoints]);

  return (
    <IntlProvider locale={_.get(localeMap[language], "intl", "zh")} messages={intlMessages}>
      <ConfigProvider locale={zhCN}>
        <InjectIntlContext>
          <Provider value={language}>
            <ReduxProvider store={store}>
              <BrowserRouter basename={systemKey}>
                <Switch>
                  <Route
                    path="/"
                    exact={true}
                    component={() => <Redirect to={getCookie("isAdminUser") === "yes" ? "/dashboard" : "/cluster/physics"} />}
                  />
                  <Route exact={true} path={"/login"} component={LoginOrRegister} />
                  <Route exact={true} path={"/register"} component={() => <LoginOrRegister type="register" />} />
                  <LayoutHeaderNav
                    logout={logout}
                    feConf={feConfig}
                    projectList={projectList}
                    currentProject={currentProject}
                    setCurrentProject={changeCurrentProject}
                    setLeftIndex={setLeftIndex}
                    leftIndex={leftIndex}
                    setHeaderClick={setHeaderClick}
                  >
                    <LeftMenu
                      siderMenuVisible={true}
                      systemName={systemKey}
                      systemNameChn={title}
                      menus={leftMenus[leftIndex]}
                      intlMessages={intlMessages}
                      locale={_.get(localeMap[language], "intl", "zh")}
                      onSiderMenuChange={setSiderMenuCollapsed}
                      permissionPoints={permissionPoints}
                      redirectPath={redirectPath}
                      getPermission={getPagePermission}
                    >
                      {RenderContent}
                      <Route exact={true} path="/403" component={Page403} />
                      <Route exact={true} path="/404" component={Page404} />
                      <AllModalInOne />
                      <FullScreen />
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
