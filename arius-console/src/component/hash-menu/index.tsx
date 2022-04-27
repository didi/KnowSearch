import React, { memo, useState, useEffect } from "react";
import { Menu } from "antd";

export interface IMenuItem {
  name?: string;
  key: string;
  show?: boolean;
  type?: number;
  label?: string;
  content?: (data: any) => JSX.Element;
}

interface propsType {
  TAB_LIST: IMenuItem[];
  MENU_MAP: Map<string, IMenuItem>;
  defaultHash: string;
  data?: any;
  prefix?: string;
}

export const HashMenu: React.FC<propsType> = memo(
  ({ TAB_LIST, MENU_MAP, defaultHash, data, prefix }) => {
    const [menu, setMenu] = useState<string>("");
    const changeMenu = (e) => {
      setMenu(e.key);
      window.location.hash = e.key;
    };
    const updateMenu = () => {
      const hashValue = window.location.hash.replace("#", "") || defaultHash;
      setMenu(hashValue);
    };
    useEffect(() => {
      updateMenu();
    }, []);

    const renderContent = () => {
      return MENU_MAP.get(menu)?.content(data);
    };
    return (
      <div style={{ background: prefix ? 'none' : "#fff" }}>
        <div className={`${prefix || ''}table-content`}>
          <Menu selectedKeys={[menu]} mode="horizontal" onClick={changeMenu}>
            {TAB_LIST.map((d) => (
              <Menu.Item key={d.key}>{d.name}</Menu.Item>
            ))}
          </Menu>
        </div>
        <div className={`${prefix || ''}detail-wrapper`} style={{ paddingTop: 0 }}>
          {renderContent()}
        </div>
      </div>
    );
  }
);
