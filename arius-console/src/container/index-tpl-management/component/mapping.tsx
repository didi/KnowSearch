import { getMapping } from "api/index-admin";
import React, { FC, memo, useEffect, useState } from "react";
import urlParser from "lib/url-parser";
import { getFormatJsonStr } from "lib/utils";
import { Spin } from "antd";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
import "./index.less";

interface propsType {
  data: any;
}

export const Mapping: FC<propsType> = memo(({ data }) => {
  const { search } = urlParser();
  const clusterName = data.cluster || search.cluster;
  const indexName = data.index || search.index;

  const [mappingData, setMappingData] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const getAsyncMapping = async () => {
    setIsLoading(true);
    try {
      const res = await getMapping(clusterName, indexName);
      setMappingData(getFormatJsonStr(JSON.parse(res?.mappings)));
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAsyncMapping();
  }, []);

  return (
    <div className="json-editor-wrapper">
      <Spin spinning={isLoading}>{!isLoading && <ACEJsonEditor className={"mapping-detail"} readOnly={true} data={mappingData} />}</Spin>
    </div>
  );
});
