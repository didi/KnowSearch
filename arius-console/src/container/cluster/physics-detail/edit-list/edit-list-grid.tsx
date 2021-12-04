import React, { memo, useEffect, useState, Fragment } from "react";
import { Divider } from "antd";
import "./index.less";

import { EditListItem } from "./edit-list-item";

interface propsType {
  title: string;
  data: {
    [key: string]: any;
  };
  configData: {
    [key: string]: any;
  };
}

export const EditListGrid: React.FC<propsType> = memo(
  ({ title, data, configData }) => {
    const [keyList, setKeyList] = useState(Object.keys(data));
    return (
      <div className="ant-list ant-list-split ant-list-bordered">
        <div className="ant-list-header">{title}</div>
        <div className="ant-list-item ant-row detail-edit-list-row">
          {keyList.map((item, index) => {
            const configItemData = configData[item];
            if (!configItemData) {
              console.error(`动态配置, ${item} 属性配置不存在`);
              return;
            }
            let value;
            if (configItemData.unit) {
              value = parseFloat(data[item]);
            } else {
              value = data[item];
            }
            return (
              <Fragment key={item + index}>
                {index !== 0 && index < keyList.length && index % 2 == 0 ? (
                  <div
                    className="detail-edit-list-line"
                    key={"detail-edit-list-line" + item + index}
                  ></div>
                ) : (
                  ""
                )}
                <EditListItem
                  name={item}
                  info={configItemData.info}
                  value={value}
                  type={configItemData.type}
                  unit={configItemData.unit}
                  mode={configItemData.mode}
                  check={configItemData.check}
                  selectList={configItemData.selectList}
                  confirmMessage={configItemData.confirmMessage}
                />
              </Fragment>
            );
          })}
        </div>
      </div>
    );
  }
);
