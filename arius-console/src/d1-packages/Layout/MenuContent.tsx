import React, { Component } from "react";
import { Link, matchPath, withRouter, RouteComponentProps } from "react-router-dom";
import { FormattedMessage, injectIntl, WrappedComponentProps } from "react-intl";
import queryString from "query-string";
import { Menu } from "knowdesign";
import _ from "lodash";
import * as utils from "./utils";
import { prefixCls } from "./config";
import { MenuConfItem } from "./interface";
import { isSuperApp } from "lib/utils";

interface Props {
  systemName: string;
  systemNameChn: string;
  isroot?: boolean;
  className?: string;
  menuMode?: "vertical" | "vertical-left" | "vertical-right" | "horizontal" | "inline" | undefined;
  menuTheme?: "dark" | "light" | undefined;
  menuStyle?: React.CSSProperties | any;
  menuConf?: MenuConfItem[];
  collapsed: boolean;
  permissionPoints: any;
  getPermission: any;
  redirectPath?: (permissionPoints: any, history: any) => string;
}

const { Item: MenuItem, Divider: MenuDivider, SubMenu, ItemGroup } = Menu;

class LayoutMenu extends Component<Props & RouteComponentProps & WrappedComponentProps> {
  static defaultProps: any = {
    menuMode: "inline",
    menuTheme: "light",
    menuStyle: undefined,
    menuConf: [], // TODO
  };

  defaultOpenKeys: string[] = [];

  selectedKeys: string[] = [];

  componentWillReceiveProps() {
    this.selectedKeys = [];
  }

  getNavMenuItems(navs: MenuConfItem[], prefix: string) {
    const { location, collapsed, permissionPoints, getPermission } = this.props;
    const permissionedNavs = _.filter(navs, (nav) => {
      if (nav.visible !== undefined && nav.visible === false) {
        return false;
      }
      if (!this.props.isroot && nav.rootVisible) {
        return false;
      }

      if (nav.permissionPoint && typeof getPermission === "function") {
        return !!getPermission(nav.permissionPoint, permissionPoints);
      }
      if (nav.permissionPoint && !permissionPoints[nav.permissionPoint]) {
        return false;
      }
      return true;
    });
    if (location?.pathname === "/") {
      const { redirectPath, permissionPoints, history } = this.props;
      redirectPath(permissionPoints, history);
    }
    return _.map(permissionedNavs, (nav, index) => {
      if (nav.divider) {
        return <MenuDivider key={index} />;
      }

      const icon = nav.icon ? (
        <svg className={`${prefixCls}-layout-left-menus-icon`} aria-hidden="true">
          <use xlinkHref={nav.icon}></use>
        </svg>
      ) : null;

      const linkProps = {} as { target: string; href: string; to: { pathname?: string; search?: string } };
      let link;

      if (_.isArray(nav.children) && utils.hasRealChildren(nav.children)) {
        const menuKey = nav.name;
        if (this.isActive(nav.to)) {
          this.defaultOpenKeys = _.union(this.defaultOpenKeys, [menuKey]) as any;
        }

        if (nav.type === "group") {
          return (
            <ItemGroup key={menuKey as any} title={collapsed ? "/" : this.props.intl.formatMessage({ id: `${prefix}.${nav.name}` })}>
              {this.getNavMenuItems(nav.children, `${prefix}.${nav.name}`)}
            </ItemGroup>
          );
        }

        const childList = this.getNavMenuItems(nav.children, `${prefix}.${nav.name}`);

        if (!childList.length) return null;

        return (
          <SubMenu
            key={menuKey as any}
            title={
              <>
                {icon}
                <span className="menu-name">{<FormattedMessage id={`${prefix}.${nav.name}`} />}</span>
              </>
            }
          >
            {childList}
          </SubMenu>
        );
      }

      if (nav.target) {
        linkProps.target = nav.target;
      }

      if (nav.to && (utils.isAbsolutePath(nav.to) || nav.isAbsolutePath)) {
        linkProps.href = nav.to;
        link = (
          <a {...linkProps}>
            {icon}
            <span className="menu-name">{<FormattedMessage id={`${prefix}.${nav.name}`} />}</span>
          </a>
        );
      } else {
        if (nav.to && this.isActive(nav.to)) this.selectedKeys = [nav.to];

        linkProps.to = {
          pathname: nav.to,
        };

        if (_.isFunction(nav.getQuery)) {
          const query = nav.getQuery(queryString.parse(location.search));
          linkProps.to.search = queryString.stringify(query);
        }

        link = (
          <Link to={linkProps.to}>
            {icon}
            <span className="menu-name">{<FormattedMessage id={`${prefix}.${nav.name}`} />}</span>
          </Link>
        );
      }
      return <MenuItem key={nav.to}>{link}</MenuItem>;
    });
  }

  isActive(path?: string) {
    const { location } = this.props;
    return !!matchPath(location.pathname, { path });
  }

  render() {
    const { menuMode, menuTheme, menuStyle, location } = this.props;
    const { menuConf, className, systemName, collapsed } = this.props;
    const realMenuConf = _.isFunction(menuConf) ? menuConf(location) : menuConf;
    const normalizedMenuConf = utils.normalizeMenuConf(realMenuConf);
    const menus = this.getNavMenuItems(normalizedMenuConf, `menu.${systemName}`);

    return (
      <Menu
        defaultOpenKeys={collapsed ? [] : this.defaultOpenKeys}
        selectedKeys={this.selectedKeys}
        theme={menuTheme}
        mode={menuMode}
        style={menuStyle}
        className={className}
      >
        {menus}
      </Menu>
    );
  }
}

export default injectIntl(withRouter(LayoutMenu));
