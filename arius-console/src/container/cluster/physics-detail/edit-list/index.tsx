import React, { useEffect, useState } from "react";
import "./index.less";
import { clusterSetting } from "./config";
import { EditListGrid } from "./edit-list-grid";
import { getDynamicConfig, getClusterAttributes, getClusterNode } from "../../../../api/cluster-api";
import { Button, Skeleton, Input } from "antd";
import urlParser from "lib/url-parser";

export const EditList = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [dynamicConfig, setDynamicConfig] = useState({});
  const [configData, setConfigData] = useState(clusterSetting);
  const [keyList, setKeyList] = useState([]);
  const [inputValue, setInputValue] = useState("");

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
      clusterSetting.ALLOCATION["cluster.routing.allocation.awareness.attributes"].selectList = clusterAttributes.map((item) => ({
        value: item,
        name: item,
      }));
      setConfigData({ ...clusterSetting });
    } catch (error) {
      console.log(error);
    } finally {
      getAsyncDynamicConfig(clusterName);
    }
  };

  const getAsyncClusterNode = async (clusterId: number) => {
    try {
      const clusterNode = await getClusterNode(clusterId);
      clusterSetting.ALLOCATION["cluster.routing.allocation.exclude._name"].selectList = (clusterNode || [])
        .filter((item) => item.role === 1)
        .map((item) => ({
          value: item.nodeSet,
          name: item.nodeSet,
        }));
      setConfigData({ ...clusterSetting });
    } catch (error) {
      console.log(error);
    }
  };

  const refreshData = (title: string, name: string, value: string | number) => {
    if (dynamicConfig?.[title]) {
      dynamicConfig[title][name] = value;
    }
  };

  useEffect(() => {
    setIsLoading(true);
    const clusterName = urlParser().search.physicsCluster;
    const clusterId = urlParser().search.physicsClusterId;
    getAsyncClusterAttributes(clusterName);
    getAsyncClusterNode(Number(clusterId));
  }, []);

  useEffect(() => {
    setKeyList(Object.keys(dynamicConfig));
  }, [dynamicConfig]);

  const renderEditList = () => {
    let list = keyList.map((item, index) => {
      let data = JSON.parse(JSON.stringify(dynamicConfig[item]));
      return (
        <EditListGrid
          reloadData={refreshData}
          key={item + index}
          title={item}
          data={data}
          configData={configData[item]}
          inputValue={inputValue}
        />
      );
    });
    return list;
  };

  const inputChange = (e) => {
    let value = e.target.value;
    setInputValue(value);
  };

  return (
    <>
      <div className="detail-edit-list">
        <div className="edit-list-title">
          <Button
            type="link"
            onClick={() => window.open("https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-update-settings.html")}
          >
            官方文档链接
          </Button>
          <Input
            allowClear
            className={inputValue ? "hasclear" : ""}
            onChange={inputChange}
            placeholder="请输入关键词"
            prefix={
              <svg className="icon svg-icon" aria-hidden="true">
                <use xlinkHref="#icontubiao-sousuo"></use>
              </svg>
            }
          />
        </div>
        {isLoading ? (
          <>
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
            <Skeleton active />
          </>
        ) : (
          renderEditList()
        )}
      </div>
    </>
  );
};
