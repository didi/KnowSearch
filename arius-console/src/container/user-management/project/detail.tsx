import { Alert, Menu, PageHeader, Spin } from "antd";
import * as React from "react";
import { baseInfo, DETAIL_MENU, DETAIL_MENU_MAP } from "./config";
import url from "lib/url-parser";
import "../../../styles/detail.less";
import "./index.less";
import { InfoItem } from "component/info-item";

export const ProjectDetail = () => {
  const syncTaskId = +url().search.syncTaskId;
  const defaultKey =
    window.location.hash.replace("#", "") || DETAIL_MENU[0].key;
  const [selectedKeys, setSelectedKeys] = React.useState([defaultKey]);
  const [projectInfo, setTaskInfo] = React.useState({} as any);

  const listenHashChange = () => {
    window.addEventListener(
      "hashchange",
      function (this: Window, ev: HashChangeEvent) {
        setSelectedKeys([window.location.hash.replace("#", "")]);
      }
    );
  };

  const handleMenuClick = (e) => {
    setSelectedKeys([e.key]);
    window.location.hash = e.key;
  };

  const getTaskDetail = async () => {
    if (isNaN(syncTaskId)) return;

    try {
      // const data = await getTaskInfo(syncTaskId);
      setTaskInfo({
        name: "xxx",
        members: 4,
        resources: 9,
        startTime: 222222 - 22,
        desc: "desc",
      });
    } catch (e) {
      //
    }
  };

  React.useEffect(() => {
    getTaskDetail();
    listenHashChange();
  }, []);

  const renderContent = () => {
    // if (!projectInfo) return null;

    return (
      DETAIL_MENU_MAP.get(selectedKeys?.[0])?.content ||
      DETAIL_MENU_MAP.get(selectedKeys?.[0])?.render(projectInfo)
    );
  };

  const renderPageHeader = () => {
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={<h2>{projectInfo.title || "dce基础平台部"}</h2>}
      >
        {baseInfo.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(projectInfo?.[row.key])
                : `${projectInfo?.[row.key] || ""}`
            }
            width={250}
          />
        ))}
      </PageHeader>
    );
  };

  return (
    <>
      {renderPageHeader()}
      <Menu
        selectedKeys={selectedKeys}
        mode="horizontal"
        onClick={handleMenuClick}
      >
        {DETAIL_MENU.map((d) => (
          <Menu.Item key={d.key}>{d.label}</Menu.Item>
        ))}
      </Menu>
      <div className="detail-wrapper">{renderContent()}</div>
    </>
  );
};
