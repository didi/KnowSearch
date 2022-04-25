import React from "react";
import { LeftMenuLayout } from "component/left-menu-layout";
import { leftMenuKeys, SEARCH_PROPERTY_MENU } from "./config";
import { PageIFrameContainer } from "container/iframe-page";
import Url from "lib/url-parser";
import { getCookie } from "lib/utils";
import { SQLQuery } from "./sql-query";

export class IndexSearch extends React.Component {
  public currLeftMenu = "";

  constructor(props: any) {
    super(props);
    const url = Url();
    this.currLeftMenu = url.search.currLeftMenu || ("kibana" as leftMenuKeys);
    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    currentRegion: getCookie("idc") || "cn",
  };

  public renderContent = () => {
    const { currentRegion } = this.state;
    if (this.currLeftMenu === leftMenuKeys.kibana)
      return (
        <PageIFrameContainer
          src={`/console/arius/kibana7${
            currentRegion === "us01"
              ? "-us"
              : currentRegion === "ru01"
              ? "-ru"
              : ""
          }/app/kibana#/discover?_g=()`}
        />
      );
    if (this.currLeftMenu === leftMenuKeys.DSL)
      return (
        <PageIFrameContainer
          src={`/console/arius/kibana7${
            currentRegion === "us01"
              ? "-us"
              : currentRegion === "ru01"
              ? "-ru"
              : ""
          }/app/kibana#/dev_tools/console`}
        />
      );
    if (this.currLeftMenu === leftMenuKeys.SQL) return <SQLQuery />;
    return null;
  };

  public updateLogicMenu() {
    this.setState({
      menu: window.location.hash.replace("#", "") || "info",
    });
  }

  public changeMenu = (e) => {
    this.currLeftMenu = e.key;
    const href = window.location.href;
    let search = window.location.search;
    let url = href.split("?")?.[0];
    const index = search.indexOf("currLeftMenu");
    if (search.includes("currLeftMenu")) {
      search = search.substring(0, index - 1);
    }

    search = search + `&currLeftMenu=${e.key}`;
    url = url + search + window.location.hash;

    window.history.pushState(
      {
        url,
      },
      "",
      url
    );
    this.updateLogicMenu();
  };

  public render() {
    return (
      <LeftMenuLayout
        menu={SEARCH_PROPERTY_MENU}
        selectedKey={this.currLeftMenu}
        onMenuClick={this.changeMenu}
        menuWidth={96}
      >
        {this.renderContent()}
      </LeftMenuLayout>
    );
  }
}
