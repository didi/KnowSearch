import React, { useEffect, useState } from "react";
import { withRouter } from "react-router-dom";
import { Menu, Spin, Divider } from "knowdesign";
import { TAB_LIST, MENU_MAP } from "./management/config";
import { getIndexDetail } from "api/index-admin";
import urlParser from "lib/url-parser";
import { isSuperApp } from "lib/utils";

export const IndexAdminDetail = withRouter((props) => {
  const [indexBaseInfo, setIndexBaseInfo] = useState({} as any);
  const [isLoading, setIsLoading] = useState(false);
  const [menu, setMenu] = useState(window.location.hash.replace("#", "") || "baseInfo");

  useEffect(() => {
    getData();
    window.addEventListener("hashchange", () => {
      updateMenu();
    });
    return () => {
      window.removeEventListener("hashchange", () => {
        updateMenu();
      });
    };
  }, []);

  const getData = async () => {
    const { search } = urlParser();
    setIsLoading(true);
    try {
      const data = (await getIndexDetail(search.cluster, search.index)) || {};
      data.cluster = isSuperApp() ? data.cluster : data.clusterLogic;
      setIndexBaseInfo(data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const updateMenu = () => {
    setMenu(window.location.hash.replace("#", "") || "info");
  };

  const renderContent = () => {
    return MENU_MAP.get(menu)?.content({
      indexBaseInfo: indexBaseInfo,
      reloadData: getData,
      loading: isLoading,
    });
  };

  const changeMenu = (e) => {
    window.location.hash = e.key;
  };

  const renderPageHeader = () => {
    const { search } = urlParser();
    return (
      <div className="detail-header">
        <div className="left-content">
          <span className="icon iconfont iconarrow-left" onClick={() => props.history.push("/index-admin/management")}></span>
          <Divider type="vertical"></Divider>
          <div className="title">
            <span className="text">{indexBaseInfo?.index || search?.index || ""}</span>
          </div>
        </div>
        <div className="right-content">
          <span className="detail">
            <span className="label">所属集群：</span>
            <span className="value">{indexBaseInfo?.cluster || search?.cluster || "-"}</span>
          </span>
          <div className="reload-icon" onClick={getData}>
            <span className="icon iconfont iconshuaxin2"></span>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="index-detail detail-container">
      <Spin spinning={isLoading}>
        {renderPageHeader()}
        <div className="content">
          <div className="menu-container">
            <Menu className="menu" selectedKeys={[menu]} mode="horizontal" onClick={changeMenu}>
              {TAB_LIST.map((d) => (
                <Menu.Item key={d.key}>{d.name}</Menu.Item>
              ))}
            </Menu>
          </div>
          <div className="detail-wrapper">{renderContent()}</div>
        </div>
      </Spin>
    </div>
  );
});
