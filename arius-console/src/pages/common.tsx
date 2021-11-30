import * as React from "react";
import "../styles/common.less";
import "./index.less";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import { urlPrefix } from "../constants/menu";
import AllModalInOne from "container/AllModalInOne";
import FullScreen from "../container/full-screen";
import { RouterTabs } from "component/router-tabs";
import { setRouterPath } from "lib/utils";
import { Page404 } from "../packages/Exception";
import DetailToList from "component/detail-to-list";
import { permissions } from "../locales/zh";

const CommonRoutePage = (props) => {
  const { pageRoute } = props;
  // 记录要从routerTab中移除的路径
  const [removePaths, setRemovePaths] = React.useState([]);
  React.useEffect(() => {
    const removePath = window.localStorage.getItem('removePath');
    setRemovePaths([removePath]);
    window.localStorage.setItem('removePath', '');
  }, [(window as any)?.location?.pathname])
  setRouterPath(location.pathname, location.search);

  return (
    <>
      <div className="router-nav" style={{ paddingTop: 45 }}>
        <RouterTabs removePaths={removePaths} permissions={permissions} />
        <Switch>
          {pageRoute.map((item, key: number) => (
            <Route
              key={key}
              path={item.path}
              exact={item.exact}
              component={item.component}
            />
          ))}
          <Route component={Page404} />
        </Switch>
      </div>
      <AllModalInOne />
      <FullScreen />
      <DetailToList />
    </>
  );
};
export default CommonRoutePage;
