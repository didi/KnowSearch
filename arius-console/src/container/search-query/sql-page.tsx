import React, { useState, useEffect, useRef } from "react";
import { isSuperApp, setCookie, uuid } from "lib/utils";
import { XNotification } from "component/x-notification";
import { Select } from "antd";
import { newClusterList } from "api/cluster-api";
import Url from "lib/url-parser";
import { SQLQuery } from "./sql-query";
import { connect } from "react-redux";
import { AppState } from "store/type";
import { RenderEmpty } from "component/LogClusterEmpty";

const Option = Select.Option;

const mapStateToProps = (state) => ({
  app: state.app,
});
export const SqlPage = connect(mapStateToProps)((props: { app: AppState }) => {
  const [id, setId] = useState(null);
  const [list, setList] = useState([]);
  const currentClusterInfoRef = useRef({});

  const [phyClusterName, setPhyClusterName] = useState(null);
  const [actPhyCluster, setActPhyCluster] = useState(null);

  useEffect(() => {
    newClusterList()
      .then((res) => {
        setList(res);
        if (res && res.length) {
          setCookiePhyClusterName(res[0]);
          setId(res[0]?.v1);
          currentClusterInfoRef.current = res[0];
          changeUrl(res[0]);
        } else {
          setCookiePhyClusterName(null);
        }
      })
      .catch(() => {
        setCookiePhyClusterName(null);
      });
    document.addEventListener("visibilitychange", onHandleVisibleChange);
    return () => {
      currentClusterInfoRef.current = {};
      document.removeEventListener("visibilitychange", onHandleVisibleChange);
    };
  }, []);

  const setCookiePhyClusterName = (clusterInfo) => {
    //cookie设置是为了和DSL和kibana进行通信
    const value = clusterInfo?.v2?.cluster || "no bind phyCluster";
    setCookie([{ key: "kibanaPhyClusterName", value }]);
  };

  const changeUrl = (item) => {
    // 集群健康状态为-1 需要弹框提示
    if (item?.v2 && item?.v2?.health === -1) {
      XNotification({ type: "error", message: `${item?.v1 || ""}集群异常，无法获取mapping信息` });
    }
    // v2是真实物理集群
    setPhyClusterName(item.v1 || null);
    setActPhyCluster(item.v2?.cluster || null);
    const href = window.location.href;
    let search = window.location.search;
    let url = href.split("?")?.[0];
    if (!href.split("?")?.[1]) {
      return;
    }
    url = url.split("#")[0];
    let hash = window.location.hash.split("#");
    const urlParams = Url();
    search = `?clusterId=${item.v2.id}&type=${item.v2.type}&currLeftMenu=${urlParams.search.currLeftMenu || "kibana"}`;
    url = url + search + "#" + hash[hash.length - 1];

    window.history.pushState(
      {
        url,
      },
      "",
      url
    );
  };

  const onHandleVisibleChange = () => {
    if (!document.hidden) {
      setCookiePhyClusterName(currentClusterInfoRef.current);
    }
  };

  const onChange = (e) => {
    const data = (list || []).filter((item) => item.v1 === e);
    if (data && data.length) {
      setCookiePhyClusterName(data[0]);
      setId(e);
      currentClusterInfoRef.current = data[0];
      changeUrl(data[0]);
    }
  };

  if (props.app.gatewayStatus === false && !isSuperApp()) {
    // if (isSuperApp()) {
    //   return (
    //     <RenderEmpty
    //       {...props}
    //       title="未部署Gateway集群"
    //       desc="请前往集群管理 ———— 「Gateway集群」，进行Gateway部署"
    //       href="/cluster/gateway"
    //       btnText="接入Gateway"
    //     />
    //   );
    // }
    return <RenderEmpty {...props} title="未部署Gateway集群，请联系管理员进行Gateway部署" desc="" href="" btnText="" />;
  }

  return (
    <>
      <div className="common-page">
        <div className="title">SQL查询</div>
        <div className="query-panel">
          <span className="label"></span>
          <Select className="select" value={id} onChange={onChange} showSearch optionFilterProp="children">
            {list?.map((item) => (
              <Option value={item.v1} key={uuid()}>
                {item.v1}
              </Option>
            ))}
          </Select>
        </div>
        <div className="content sql">
          <SQLQuery currentClusterInfo={currentClusterInfoRef.current} phyClusterName={phyClusterName} actPhyCluster={actPhyCluster} />
        </div>
      </div>
    </>
  );
});
