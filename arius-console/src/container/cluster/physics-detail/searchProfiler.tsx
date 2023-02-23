import React, { useEffect } from "react";
import { DevToolsPage } from "@knowdesign/kbn-sense";

import Url from "lib/url-parser";
import { isSuperApp, setCookie } from "lib/utils";
import { XNotification } from "component/x-notification";
import { IndexSelect } from "../../IndexSelect";

export const SearchProfiler = (props: any) => {
  const physicsCluster: string = Url().search?.physicsCluster;
  setCookie([{ key: "kibanaPhyClusterName", value: physicsCluster }]);

  useEffect(() => {
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

  return (
    <div className="sense-page">
      <DevToolsPage
        noNeedTab={true}
        activeId={"searchprofiler"}
        currentCluster={{ id: physicsCluster }}
        notifications={XNotification}
        IndexSelect={IndexSelect}
        isSuperApp={!!isSuperApp()}
      />
    </div>
  );
};
