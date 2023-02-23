import React, { useEffect, useState } from "react";
import { DevToolsPage } from "@knowdesign/kbn-sense";
import Url from "lib/url-parser";
import { setCookie, getCookie, getCurrentProject, isSuperApp } from "lib/utils";
import { XNotification } from "component/x-notification";
import { IndexSelect } from "../../IndexSelect";
import { getSenseOperate, setSenseOperate } from "api/cluster-api";

export const Sense = (props: any) => {
  const physicsCluster: string = Url().search?.physicsCluster;
  setCookie([{ key: "kibanaPhyClusterName", value: physicsCluster }]);
  const clusterId = Number(Url().search?.physicsClusterId);

  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    _getSenseOperate();
    const onHandleVisibleChange = () => {
      if (!document.hidden) {
        const physicsCluster: string = Url().search?.physicsCluster;
        setCookie([{ key: "kibanaPhyClusterName", value: physicsCluster }]);
      }
    };
    document.addEventListener("visibilitychange", onHandleVisibleChange);

    return () => {
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

  const onInputEditorChange = (content) => {
    let contentArr = content?.split("\n");
    if (contentArr?.length > 1000) {
      content = contentArr?.slice(0, 1000)?.join("\n");
    }
    let params = { userName: getCookie("userName") || "", content, projectId: getCurrentProject()?.id || "" };
    setSenseOperate(params);
  };

  return (
    <div className="sense-page">
      {loading ? null : (
        <DevToolsPage
          consoleEditorValue={content}
          onInputEditorChange={onInputEditorChange}
          noNeedTab={true}
          currentCluster={{ id: physicsCluster }}
          notifications={XNotification}
          IndexSelect={IndexSelect}
          isSuperApp={!!isSuperApp()}
        />
      )}
    </div>
  );
};
