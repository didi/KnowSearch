import React from 'react';
import { Menu } from 'antd';
import { IMenuItem } from 'typesPath/base-types';
import './index.less';


export interface ILMLayoutProps {
  menu: IMenuItem[];
  menuWidth?: number;
  selectedKey?: string;
  onMenuClick?: (param: any) => any;
  className?: string;
}

export class LeftMenuLayout extends React.Component<ILMLayoutProps> {
  public render() {
    const { selectedKey, onMenuClick, menu, menuWidth, children, className } = this.props;
    const w = menuWidth || 72;
    return (
      <div className={`layout-wrapper ${className || ''}`}>
        <Menu
          mode="vertical"
          className="menu-wrapper"
          selectedKeys={[selectedKey]}
          onClick={onMenuClick}
          style={{width: `${w}px`}}
        >
        {menu.map(m => <Menu.Item key={m.key}>{m.label}</Menu.Item>)}
        </Menu>
        <div className="menu-content-wrapper" style={{marginLeft: `${w}px`}}>
        {children}
        </div>
      </div>
    );
  }
}
