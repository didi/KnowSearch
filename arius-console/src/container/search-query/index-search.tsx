import React from 'react';
import { Select } from 'antd';
import { LeftMenuLayout } from 'component/left-menu-layout';
import { leftMenuKeys, SEARCH_PROPERTY_MENU } from './../cluster/logic-detail/config';
import { PageIFrameContainer } from 'container/iframe-page';
import Url from 'lib/url-parser';
import { getClusterList } from "api/cluster-api";
import { getCookie } from 'lib/utils';
import { SQLQuery } from './../cluster/logic-detail/sql-query';

const Option = Select.Option

export class IndexSearch extends React.Component {
  public currLeftMenu = '';

  constructor(props: any) {
    super(props);
    const url = Url();
    this.currLeftMenu = url.search.currLeftMenu || 'kibana' as leftMenuKeys;
    window.addEventListener('hashchange', () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    currentRegion: getCookie('idc') || 'cn',
    id: null,
    list: [],
  }

  public renderContent = () => {
    const { currentRegion } = this.state;
    if (this.currLeftMenu === leftMenuKeys.kibana) return <PageIFrameContainer src={`/console/arius/kibana7${currentRegion === 'us01' ? '-us' : currentRegion === 'ru01' ? '-ru' : ''}/app/kibana#/discover?_g=()`} />;
    if (this.currLeftMenu === leftMenuKeys.DSL) return <PageIFrameContainer src={`/console/arius/kibana7${currentRegion === 'us01' ? '-us' : currentRegion === 'ru01' ? '-ru' : ''}/app/kibana#/dev_tools/console`} />;
    if (this.currLeftMenu === leftMenuKeys.SQL) return <SQLQuery />;
    return null;
  }

  public updateLogicMenu() {
    this.setState({
      menu: window.location.hash.replace('#', '') || 'info'
    })
  }

  public changeMenu = e => {
    this.currLeftMenu = e.key;
    const href = window.location.href;
    let search = window.location.search;
    let url = href.split('?')?.[0];
    const index = search.indexOf('currLeftMenu');
    if (search.includes('currLeftMenu')) {
      search = search.substring(0, index - 1);
    }

    search = search + `?currLeftMenu=${e.key}`;
    url = url + search + window.location.hash;

    window.history.pushState({
      url,
    }, '', url);
    this.updateLogicMenu();
  }

  // 注释调下拉请求
  public componentDidMount() {
    getClusterList().then((res) => {
      this.setState({list: res });
      if (res && res.length) {
        this.setState({id: res[0].id });
        this.changeUrl(res[0]);
      }
    })
  }

  public changeUrl = (item) => {
    const href = window.location.href;
    let search = window.location.search;
    let url = href.split('?')?.[0];
    const urlParams = Url();
    search = `?clusterId=${item.id}&type=${item.type}&currLeftMenu=${urlParams.search.currLeftMenu || 'kibana'}`;
    url = url + search + window.location.hash;

    window.history.pushState({
      url,
    }, '', url);
  }

  public onChange = (e) => {
    const data = this.state.list.filter((item) => {
      if (item.id === e) {
        return true;
      }
      return false;
    })
    if (data && data.length) {
      this.setState({id: e });
      this.changeUrl(data[0]);
    }
  }

  public render() {
    const { id, list } = this.state;
    return (
      <div style={{ background: '#fff' }}>
        <div style={{ padding: 20, minWidth: 400 }}>
          <span>逻辑集群：</span>
          <Select value={id} onChange={this.onChange} style={{ minWidth: 200 }}>
            {list?.map((item) => <Option value={item.id} key={item.id}>{item.name}</Option>)}
          </Select>
        </div>
        <LeftMenuLayout
          menu={SEARCH_PROPERTY_MENU}
          selectedKey={this.currLeftMenu}
          onMenuClick={this.changeMenu}
          menuWidth={96}
        >
          {this.renderContent()}
        </LeftMenuLayout>
      </div>
    );
  }
}
