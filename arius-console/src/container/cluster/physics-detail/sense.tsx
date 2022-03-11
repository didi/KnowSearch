import React from "react";
import { PageIFrameContainer } from 'container/iframe-page';
import Url from 'lib/url-parser';
import { setCookie } from "lib/utils";

export const Sense = (props: any) => {
  const physicsCluster: string = Url().search?.physicsCluster
  setCookie([{ key: "kibanaPhyClusterName", value: physicsCluster }]);

  return (
    <div style={{ height: '700px' }}>
      <PageIFrameContainer src={`/console/arius/kibana7/app/kibana#/dev_tools/console`} />
    </div>
  )
}