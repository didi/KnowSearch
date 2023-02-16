import React, { useState, useEffect, useRef } from "react";
import { DevToolsPage } from "@knowdesign/kbn-sense";
import { clearSubscriptions } from "@knowdesign/kbn-sense/lib/plugin/console/public/lib/mappings/mappings";
import { useDidCache, useDidRecover } from "react-router-cache-route";
import { getCookie, setCookie, getCurrentProject, uuid, isSuperApp } from "lib/utils";
import { XNotification } from "component/x-notification";
import { Select } from "knowdesign";
import { newClusterList, getSenseOperate, setSenseOperate } from "api/cluster-api";
import Url from "lib/url-parser";
import { IndexSelect } from "../IndexSelect";

const Option = Select.Option;

export const DslPage = () => {
  const [id, setId] = useState(null);
  const [list, setList] = useState([]);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [listLoading, setListLoading] = useState(true);
  const currentClusterInfoRef = useRef({});

  useEffect(() => {
    _getSenseOperate();
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
        setListLoading(false);
      })
      .catch(() => {
        setCookiePhyClusterName(null);
        setListLoading(false);
      });
    document.addEventListener("visibilitychange", onHandleVisibleChange);
    return () => {
      currentClusterInfoRef.current = {};
      document.removeEventListener("visibilitychange", onHandleVisibleChange);
    };
  }, []);

  const _getSenseOperate = async () => {
    let params = { userName: getCookie("userName") || "", content: "", projectId: getCurrentProject()?.id || "" };
    let data = await getSenseOperate(params);
    let content = "";
    (data || []).forEach((item) => {
      content += `${item}\n`;
    });
    setContent(content);
    setLoading(false);
  };

  const setCookiePhyClusterName = (clusterInfo) => {
    //cookie设置是为了和DSL和kibana进行通信
    const value = clusterInfo?.v2?.cluster || "no bind phyCluster";
    setCookie([{ key: "kibanaPhyClusterName", value }]);
  };

  const changeUrl = (item) => {
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

  const onInputEditorChange = (content) => {
    let contentArr = content?.split("\n");
    if (contentArr?.length > 1000) {
      content = contentArr?.slice(0, 1000)?.join("\n");
    }
    let params = { userName: getCookie("userName") || "", content, projectId: getCurrentProject()?.id || "" };
    setSenseOperate(params);
  };

  useDidCache(() => {
    // 只有 keepalive 才会有路由跳转事件
    clearSubscriptions();
  });

  useDidRecover(() => {
    // TODO: 恢复定时器
  });

  const showNoClusterTip = () => {
    XNotification({ type: "error", message: `执行错误，无集群信息。` });
  };

  return (
    <>
      <div className="common-page">
        {/* <div className="title">DSL</div> */}
        <div className="query-panel dsl">
          <span className="label"></span>
          <Select className="select" placeholder="我的集群" value={id} onChange={onChange} showSearch optionFilterProp="children">
            {list?.map((item) => (
              <Option value={item.v1} key={uuid()}>
                {item.v1}
              </Option>
            ))}
          </Select>
        </div>
        <div className="content dsl">
          {!loading && !listLoading ? (
            <DevToolsPage
              consoleEditorValue={content}
              onInputEditorChange={onInputEditorChange}
              currentCluster={{ id, noInfoAction: showNoClusterTip }}
              prefix={`${getCookie("userName") || ""}-${getCurrentProject()?.id || ""}`}
              notifications={XNotification}
              IndexSelect={IndexSelect}
              isSuperApp={!!isSuperApp()}
            />
          ) : null}
        </div>
      </div>
    </>
  );
};
