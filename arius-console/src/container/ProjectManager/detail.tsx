import { PageHeader } from "antd";
import { Menu } from "knowdesign";
import * as React from "react";
import { baseInfo, DETAIL_MENU } from "./config";
import url from "lib/url-parser";
import "../../styles/detail.less";
import "./index.less";
import { InfoItem } from "component/info-item";
import { getApp, getProject } from "api";
import { MemberList } from "./MemberList";
import { AccessSetting } from "./AccessSetting";
import { getCookie } from "lib/utils";

export const ProjectDetail = () => {
  const projectId = +url().search.projectId;
  const defaultKey = window.location.hash.replace("#", "") || DETAIL_MENU[0].key;
  const [selectedKey, setSelectedKey] = React.useState(defaultKey);
  const [projectInfo, setProjectInfo] = React.useState({} as any);

  const listenHashChange = () => {
    window.addEventListener("hashchange", function (this: Window, ev: HashChangeEvent) {
      setSelectedKey(window.location.hash.replace("#", ""));
    });
  };

  const handleMenuClick = (e) => {
    setSelectedKey(e.key);
    window.location.hash = e.key;
  };

  const getProjectInfo = async () => {
    if (isNaN(projectId)) return;

    try {
      const pInfo = await getProject(projectId);
      const info = pInfo?.config || {};
      setProjectInfo({ ...info, ...pInfo });
    } catch (e) {
      //
    }
  };

  React.useEffect(() => {
    getProjectInfo();
    listenHashChange();
  }, []);

  const renderContent = () => {
    let isAdmin = getCookie("isAdminUser");
    return selectedKey === "access" ? (
      <AccessSetting />
    ) : (
      <MemberList list={isAdmin === "yes" ? projectInfo.userListWithBelongProjectAndAdminRole : projectInfo.userList} />
    );
  };

  const renderPageHeader = () => {
    return (
      <PageHeader className="project-detail-header" backIcon={false} title={<h2>{projectInfo.projectName || ""}</h2>}>
        {baseInfo(projectInfo)?.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(projectInfo?.[row.key])
                : row.key === "slowQueryTimes"
                ? `${projectInfo?.[row.key] + "ms" || ""}`
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
      <div className="hash-menu-container menu-container project-manager">
        <Menu selectedKeys={[selectedKey]} mode="horizontal" onClick={handleMenuClick}>
          {DETAIL_MENU.map((d) => (
            <Menu.Item key={d.key}>{d.label}</Menu.Item>
          ))}
        </Menu>
      </div>
      <div className="detail-wrapper project-manager-detail">{renderContent()}</div>
    </>
  );
};
