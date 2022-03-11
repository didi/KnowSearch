import { Menu, message, Switch } from "antd";
import React from "react";
import { getStatus, updataStatus } from "./api";
import { MENU_MAP, TAB_LIST } from "./config";
import "./index.less";

export interface MenuInfo {
  key: string;
  keyPath: React.Key[];
  item: React.ReactInstance;
  domEvent: React.MouseEvent<HTMLElement>;
}

export const ResourcesManagement: React.FC = () => {
  const [menu, setMenu] = React.useState<string>(TAB_LIST[0]?.key);
  const [checked, setChecked] = React.useState<boolean>(false);
  const ref = React.useRef();

  React.useEffect(() => {
    reloadData();
  }, []);

  const reloadData = () => {
    getStatus()
      .then((res) => {
        setChecked(res);
      })
      .catch(() => {
        message.error("获取查看权限状态失败！");
      });
  };

  const changeMenu = (info: MenuInfo) => {
    setMenu(info.key);
  };

  const viewPermissionControl = (checked: boolean) => {
    updataStatus()
      .then(() => {
        setChecked(checked);
        (ref as any).current.getResourceData();
        message.success(checked ? "开启成功" : "关闭成功");
      })
      .catch(() => {
        message.error("操作失败！");
      });
  };

  const renderContent = () => {
    return (MENU_MAP as any).get(menu)?.content(ref);
  };

  return (
    <div className="resources-management">
      <div className="resources-management-header">
        <div className="resources-management-header-title">资源权限管理</div>
        <div className="resources-management-header-control">
          查看权限控制 <Switch size="small" checked={checked} onChange={viewPermissionControl} />
        </div>
      </div>
      <div className="resources-management-content">
        <div>
          <Menu selectedKeys={[menu]} mode="horizontal" onClick={changeMenu}>
            {TAB_LIST.map((d) => (
              <Menu.Item key={d.key}>{d.name}</Menu.Item>
            ))}
          </Menu>
        </div>
        <div className="hash-detail-wrapper" style={{ paddingTop: 0 }}>
          {renderContent()}
        </div>
      </div>
    </div>
  );
};
