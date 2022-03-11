/* eslint-disable react/display-name */
import React, { memo, useState, useEffect } from 'react';
import { Menu } from 'antd';
import './index.less';
export interface IMenuItem {
  name?: string;
  key: string;
  show?: boolean;
  type?: number;
  label?: string;
  content?: JSX.Element;
}

interface propsType {
  TAB_LIST: IMenuItem[];
  MENU_MAP: Map<string, IMenuItem>;
  theme?: boolean; // 不传则原生antd，传则灰低主题
}

export const HashMenu: React.FC<propsType> = memo(({ TAB_LIST, MENU_MAP, theme }) => {
  const [menu, setMenu] = useState<string>("");
  const changeMenu = (e: any) => {
    setMenu(e.key);
    window.location.hash = e.key;
  }
  const updateMenu = () => {
    const hashValue = window.location.hash.replace("#", "") || TAB_LIST[0]?.key;
    setMenu(hashValue);
  }
  useEffect(() => {
    updateMenu();
  }, []);

  const renderContent = () => {
    return (MENU_MAP as any).get(menu)?.content;
  }
  return (
    <div className={theme ? 'hash-menu' : ''}>
      <div className={theme ? 'hash-menu-content' : ''}>
        <Menu
          className="hash-menu-head"
          selectedKeys={[menu]}
          mode="horizontal"
          onClick={changeMenu}
        >
          {
            TAB_LIST.map((d) => <Menu.Item key={d.key}>{d.name}</Menu.Item>)
          }
        </Menu>
      </div>
      <div className="hash-detail-wrapper">{renderContent()}</div>
    </div>
  )
})
