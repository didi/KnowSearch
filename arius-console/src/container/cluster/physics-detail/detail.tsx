import React, { useState, useEffect } from "react";
import { Menu, Spin, Tag, Divider } from "knowdesign";
import { PHYSICE_DESC_LIST, DETAIL_MENU_MAP, TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhysicsClusterDetail } from "api/cluster-api";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { StatusMap } from "constants/status-map";
import "./index.less";
import "styles/detail.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});
export const PhyClusterDetail = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const [menu, setMenu] = useState(window.location.hash.replace("#", "") || "info");
  const [clusterInfo, setClusterInfo] = useState({} as IOpPhysicsClusterDetail);
  const [loading, setLoading] = useState(false);

  const url = Url();
  const clusterId = Number(url.search.physicsClusterId);

  useEffect(() => {
    reloadData();
    window.addEventListener("hashchange", () => {
      updateLogicMenu();
    });
    return () => {
      window.removeEventListener("hashchange", () => {
        updateLogicMenu();
      });
    };
  }, []);

  const reloadData = () => {
    setLoading(true);
    getPhysicsClusterDetail(clusterId)
      .then((res) => {
        if (res) {
          setClusterInfo(res);
        }
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const updateLogicMenu = () => {
    setMenu(window.location.hash.replace("#", "") || "info");
  };

  const renderPageHeader = () => {
    return (
      <div className="detail-header">
        <div className="left-content">
          <span className="icon iconfont iconarrow-left" onClick={() => props.history.push("/cluster/physics")}></span>
          <Divider type="vertical"></Divider>
          <div className="title">
            <span className="text">{clusterInfo?.cluster || ""}</span>
            <Tag className={`tag ${StatusMap[clusterInfo.health]}`} color={StatusMap[clusterInfo.health]}>
              {StatusMap[clusterInfo.health]}
            </Tag>
          </div>
        </div>
        <div className="right-content">
          {PHYSICE_DESC_LIST.map((row, index) => {
            if (!clusterInfo) return null;
            if (row.key === "tags" && Number(JSON.parse(clusterInfo[row.key] || "{}")?.createSource) !== 0) {
              return null;
            }
            return (
              <span className="detail" key={index}>
                <span className="label">{row.label}：</span>
                {/* @ts-ignore */}
                <span className="value">{row.render ? row.render(clusterInfo?.[row.key]) : `${clusterInfo?.[row.key] || "-"}`}</span>
              </span>
            );
          })}
          <div className="reload-icon" onClick={reloadData}>
            <span className="icon iconfont iconshuaxin2"></span>
          </div>
        </div>
      </div>
    );
  };

  const changeMenu = (e) => {
    window.location.hash = e.key;
  };

  const renderContent = () => {
    return DETAIL_MENU_MAP.get(menu)?.content({
      clusterInfo: clusterInfo,
      reloadData: reloadData,
      loading: loading,
    });
  };

  return (
    <div className="detail-container">
      <Spin spinning={loading}>
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
