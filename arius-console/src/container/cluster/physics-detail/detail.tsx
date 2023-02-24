import React, { useState, useEffect } from "react";
import { Menu, Spin, Tag, Divider, PageHeader } from "knowdesign";
import { PHYSICE_DESC_LIST, DETAIL_MENU_MAP, TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhysicsClusterDetail } from "api/cluster-api";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { StatusMap } from "constants/status-map";
import { getPhysicsBtnList } from "../config";
import { renderMoreBtns } from "container/custom-component";
import { InfoItem } from "component/info-item";
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
  const renderKibanaIcon = () => {
    return (
      <div className="kibana-icon-container">
        {clusterInfo?.kibanaAddress ? (
          <Tag className={`tag`} color={"blue"}>
            <a target="_blank" href={clusterInfo?.kibanaAddress} style={{ color: "#096dd9" }}>
              kibana
            </a>
          </Tag>
        ) : null}
        {clusterInfo?.cerebroAddress ? (
          <Tag className={`tag`} color={"blue"}>
            <a target="_blank" href={clusterInfo?.cerebroAddress} style={{ color: "#096dd9" }}>
              cerebro
            </a>
          </Tag>
        ) : null}
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
  const getOpBtns = () => {
    const arr = getPhysicsBtnList(clusterInfo as any, props.setModalId, props.setDrawerId, reloadData);
    return renderMoreBtns(arr, clusterInfo);
  };

  // const renderPageHeader = () => {
  //   return (
  //     <PageHeader
  //       className="detail-header"
  //       backIcon={false}
  //       title={clusterInfo?.cluster || ""}
  //       tags={
  //         <Tag className={`tag ${StatusMap[clusterInfo.health]}`} color={StatusMap[clusterInfo.health]}>
  //           {StatusMap[clusterInfo.health]}
  //         </Tag>
  //       }
  //       extra={
  //         clusterInfo?.currentAppAuth === 1 || clusterInfo?.currentAppAuth === 0 ? (
  //           <>
  //             {getOpBtns()}
  //             {renderKibanaIcon()}
  //           </>
  //         ) : (
  //           renderKibanaIcon()
  //         )
  //       }
  //     >
  //       {PHYSICE_DESC_LIST.map((row, index) => {
  //         if (!clusterInfo) return null;
  //         if (row.key === "tags" && Number(JSON.parse(clusterInfo[row.key] || "{}")?.createSource) !== 0) {
  //           return null;
  //         }
  //         return (
  //           <InfoItem
  //             key={index}
  //             label={row.label}
  //             // @ts-ignore
  //             value={row.render ? row.render(clusterInfo?.[row.key]) : `${clusterInfo?.[row.key] || "-"}`}
  //             width={250}
  //           />
  //         );
  //       })}
  //     </PageHeader>
  //   );
  // };
  return (
    <div className="detail-container">
      <Spin spinning={loading}>
        {renderPageHeader()}
        <div className="content cluster-menu-container">
          {renderKibanaIcon()}
          <div className="menu-container cluster-menu">
            <Menu className="menu" selectedKeys={[menu]} mode="horizontal" onClick={changeMenu}>
              {TAB_LIST.map((d) => {
                let ecmAccess = clusterInfo?.ecmAccess;
                if (!ecmAccess && d.key === "config") return null;
                return <Menu.Item key={d.key}>{d.name}</Menu.Item>;
              })}
            </Menu>
          </div>
          <div className="detail-wrapper">{renderContent()}</div>
        </div>
      </Spin>
    </div>
  );
});
