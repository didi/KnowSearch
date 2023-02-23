import React, { FC, memo, useEffect, useState } from "react";
import { getSetting } from "api/index-admin";
import urlParser from "lib/url-parser";
import { getFormatJsonStr } from "lib/utils";
import { Spin } from "antd";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
import "./index.less";

interface propsType {
  data: any;
}

export const Setting: FC<propsType> = memo(({ data }) => {
  const { search } = urlParser();
  const clusterName = data.cluster || search.cluster;
  const indexName = data.index || search.index;

  const [settingData, setSettingData] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const getAsyncSetting = async () => {
    setIsLoading(true);
    try {
      const res = await getSetting(clusterName, indexName);
      setSettingData(getFormatJsonStr(res?.properties));
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAsyncSetting();
  }, []);

  return (
    <div className="json-editor-wrapper">
      <Spin spinning={isLoading}>{!isLoading && <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={settingData} />}</Spin>
    </div>
  );
});
