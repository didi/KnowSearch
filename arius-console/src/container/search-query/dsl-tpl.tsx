import React from "react";
import { connect } from "react-redux";
import { HashMenu } from "knowdesign";
import { getTabList, menuMap, MENU_MAP } from "./config";
import store from "store";
import { AppState } from "store/type";
import * as actions from "actions";
import { getCookie } from "lib/utils";
import { getProjectListByUserId } from "api/app-api";
import { IMenuItem } from "typesPath/base-types";
import "./index.less";

const mapStateToProps = (state) => ({
  app: state.app,
});

export const DslTpl = connect(mapStateToProps)((props: { app: AppState }) => {
  const [reloadPage, setReloadPage] = React.useState<string>("");
  let FILTER_TAB_LIST = getTabList(setReloadPage).filter((item) => {
    if (item.visible) {
      return true;
    }
    return false;
  });

  const menuMap = new Map<string, IMenuItem>();

  getTabList(setReloadPage).forEach((d) => {
    menuMap.set(d.key, d);
  });

  const MENU_MAP = menuMap;

  React.useEffect(() => {
    const projectList = props.app.projectList;
    if (!projectList?.length) {
      const userId = getCookie("userId");
      getProjectListByUserId(+userId).then((res = []) => {
        store.dispatch(actions.setProjectList(res));
      });
    }
  }, []);

  return (
    <div className="hash-menu-container dsl-tpl">
      <HashMenu prefix="dsl-tpl" TAB_LIST={FILTER_TAB_LIST} MENU_MAP={MENU_MAP} defaultHash={FILTER_TAB_LIST[0].key} data={reloadPage} />
    </div>
  );
});
