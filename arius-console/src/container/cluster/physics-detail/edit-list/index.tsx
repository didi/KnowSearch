import React, { useEffect, useState } from "react";
import "./index.less";
import { clusterSetting } from "./config";
import { EditListGrid } from "./edit-list-grid";
import {
  getDynamicConfig,
  getClusterAttributes,
} from "../../../../api/cluster-api";
import { Skeleton } from "antd";
import urlParser from "lib/url-parser";

export const EditList = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [dynamicConfig, setDynamicConfig] = useState({});
  const [configData, setConfigData] = useState(clusterSetting);
  const [keyList, setKeyList] = useState([]);

  const getAsyncDynamicConfig = async (clusterName: string) => {
    try {
      const dynamicConfig = await getDynamicConfig(clusterName);
      setDynamicConfig(dynamicConfig);
      setIsLoading(false);
    } catch (error) {
      setIsLoading(false);
      console.log(error);
    }
  };

  const getAsyncClusterAttributes = async (clusterName: string) => {
    try {
      const clusterAttributes = await getClusterAttributes(clusterName);
      clusterSetting.ROUTING[
        "cluster.routing.allocation.awareness.attributes"
      ].selectList = clusterAttributes.map((item) => ({
        value: item,
        name: item,
      }));
      // console.log(clusterSetting);
      setConfigData({ ...clusterSetting });
    } catch (error) {
      console.log(error);
    } finally {
      getAsyncDynamicConfig(clusterName);
    }
  };

  useEffect(() => {
    setIsLoading(true);
    const clusterName = urlParser().search.physicsCluster;
    getAsyncClusterAttributes(clusterName);
  }, []);

  useEffect(() => {
    setKeyList(Object.keys(dynamicConfig));
  }, [dynamicConfig]);

  return (
    <>
      <div className="detail-edit-list">
        {isLoading ? (
          <>
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
          </>
        ) : (
          keyList.map((item, index) => (
            <EditListGrid
              key={item + index}
              title={item}
              data={dynamicConfig[item]}
              configData={configData[item]}
            />
          ))
        )}
      </div>
    </>
  );
};
