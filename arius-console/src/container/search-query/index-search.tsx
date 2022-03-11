import React from "react";
import { Select, Menu } from "antd";
import { LeftMenuLayout } from "component/left-menu-layout";
import { leftMenuKeys, SEARCH_PROPERTY_MENU } from "./../cluster/logic-detail/config";
import { PageIFrameContainer } from "container/iframe-page";
import Url from "lib/url-parser";
import { getClusterList } from "api/cluster-api";
import { getCookie, setCookie } from 'lib/utils';
import { SQLQuery } from './../cluster/logic-detail/sql-query';
import './index.less';

const Option = Select.Option;

export class IndexSearch extends React.Component {
  public state;
  constructor(props: any) {
    super(props);
    const url = Url();
    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
    this.state = {
      currentRegion: getCookie("idc") || "cn",
      id: null,
      list: [],
      phyClusterName: null,
      currLeftMenu: url.hash.menu || ("kibana" as leftMenuKeys),
      activeKey: url.hash.menu || ("kibana" as leftMenuKeys),
    };
  }

  public renderContent = () => {
    const { currentRegion, currLeftMenu } = this.state;
    if (currLeftMenu === leftMenuKeys.kibana)
      return (
        <PageIFrameContainer
          className="iframe-search"
          src={`/console/arius/kibana7${
            currentRegion === "us01" ? "-us" : currentRegion === "ru01" ? "-ru" : ""
          }/app/kibana#/discover?_g=()`}
        />
      );
    if (currLeftMenu === leftMenuKeys.DSL)
      return (
        <PageIFrameContainer
          className="iframe-search"
          src={`/console/arius/kibana7${
            currentRegion === "us01" ? "-us" : currentRegion === "ru01" ? "-ru" : ""
          }/app/kibana#/dev_tools/console?embed=true`}
        />
      );
    if (currLeftMenu === leftMenuKeys.SQL) return <SQLQuery phyClusterName={this.state.phyClusterName} />;
    return null;
  };

  public updateLogicMenu() {
    this.setState({
      menu: window.location.hash.replace("#", "") || "kibana",
    });
  }

  public changeMenu = (e) => {
    this.setState({
      currLeftMenu: e.key,
      activeKey: e.key
    })
    let href = window.location.pathname;
    const search = window.location.search;
    const url = href + search + `#menu=${e.key}`;

    window.history.pushState(
      {
        url,
      },
      "",
      url
    );
    this.updateLogicMenu();
  };

  public setCookiePhyClusterName(clusterInfo) {
    const value = clusterInfo?.associatedPhyClusterName?.[0] || 'no bind phyCluster';
    setCookie([{ key: "kibanaPhyClusterName", value }]);
  }

  // 注释调下拉请求
  public componentDidMount() {
    getClusterList().then((res) => {
      this.setState({ list: res });
      if (res && res.length) {
        this.setCookiePhyClusterName(res[0]);
        this.setState({ id: res[0].id });
        this.changeUrl(res[0]);
      }
    });
  }

  public changeUrl = (item) => {
    this.setState({ phyClusterName: item.associatedPhyClusterName?.[0] || null });
    const href = window.location.href;
    let search = window.location.search;
    let url = href.split("?")?.[0];
    if (href.split("?")?.[1]) {
      return 
    }
    url = url.split('#')[0];
    let hash = window.location.hash.split('#');
    const urlParams = Url();
    search = `?clusterId=${item.id}&type=${item.type}&currLeftMenu=${urlParams.search.currLeftMenu || "kibana"}`;
    url = url + search + '#' + hash[hash.length - 1];

    window.history.pushState(
      {
        url,
      },
      "",
      url
    );
  };

  public onChange = (e) => {
    const data = this.state.list.filter((item) => {
      if (item.id === e) {
        return true;
      }
      return false;
    })

    if (data && data.length) {
      this.setCookiePhyClusterName(data[0]);
      this.setState({ id: e });
      this.changeUrl(data[0]);
    }
  };

  public render() {
    const { id, list, currLeftMenu } = this.state;
    return (
      <div className="hiddenMenuHeader">
        <div style={{ position: 'absolute', right: 24, top: 16 }}>
          <span>逻辑集群 </span>
          <Select value={id} onChange={this.onChange} style={{ width: 350, borderRadius: 4 }} showSearch optionFilterProp="children">
            {list?.map((item) => (
              <Option value={item.id} key={item.id}>
                {item.name}
              </Option>
            ))}
          </Select>
        </div>
        {/* <LeftMenuLayout menu={SEARCH_PROPERTY_MENU} selectedKey={this.currLeftMenu} onMenuClick={this.changeMenu} menuWidth={96}>
          {this.renderContent()}
        </LeftMenuLayout> */}
        <div className="detail-tabs">
          {SEARCH_PROPERTY_MENU.map(m => <div onClick={() => this.changeMenu(m)} className={this.state.activeKey == m.key ? 'detail-tabs-item check' : 'detail-tabs-item'}>{m.label}</div>)}
        </div>
         <Menu
          mode="horizontal"
          // className="menu-wrapper"
          selectedKeys={[currLeftMenu]}
          onClick={this.changeMenu}
        >
          {SEARCH_PROPERTY_MENU.map(m => <Menu.Item key={m.key}>{m.label}</Menu.Item>)}
        </Menu>
        <div style={{ minHeight: 800,  overflow: 'scoll', position: 'relative' }}>
          {this.renderContent()}
        </div>
      </div>
    );
  }
}
