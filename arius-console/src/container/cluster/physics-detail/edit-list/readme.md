```js
import React, { useEffect, useState } from "react";
import { EditOutlined } from "@ant-design/icons";
import { Button, Col, Form, List, Row, Tooltip } from "antd";
import {
  FormItemType,
  handleFormItem,
  IFormItem,
  renderFormItem,
} from "component/x-form";
import { cloneDeep, isEqual } from "lodash";
import "./index.less";
import { clusterSetting } from "./config";
import { EditListGrid } from "./edit-list-grid";
import { getDynamicConfig } from "../../../../api/cluster-api";
import Item from "antd/lib/list/Item";

export const EditList = () => {
  const [dynamicConfig, setDynamicConfig] = useState({});
  const [keyList, setKeyList] = useState([]);
  const getAsyncDynamicConfig = async () => {
    const dynamicConfig = await getDynamicConfig("");
    setDynamicConfig(dynamicConfig);
  };

  useEffect(() => {
    getAsyncDynamicConfig();
    clusterSetting.routing[
      "cluster.routing.allocation.awareness.attributes"
    ].selectList = ["1", "2", "3"];
  }, []);

  useEffect(() => {
    setKeyList(Object.keys(dynamicConfig));
  }, [dynamicConfig]);

  return (
    <>
      <div className="base-info-title">动态配置</div>
      <div className="detail-edit-list">
        {keyList.map((item) => (
          <EditListGrid
            title={item}
            data={dynamicConfig[item]}
            configData={clusterSetting[item]}
          />
        ))}
      </div>
    </>
  );
};
```
