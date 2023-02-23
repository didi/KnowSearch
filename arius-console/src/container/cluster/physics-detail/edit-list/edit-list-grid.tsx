import React, { memo, useEffect, useState, Fragment } from "react";
import { Empty } from "antd";
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
  inputValue?: string;
  reloadData: any;
}

export const EditListGrid: React.FC<propsType> = memo(({ reloadData, title, data, configData, inputValue }) => {
  const [keyList, setKeyList] = useState(Object.keys(data));

  useEffect(() => {
    let keys = Object.keys(data);
    let list = keys.filter((item) => item?.includes(inputValue) || data[item]?.includes(inputValue));
    setKeyList(list);
  }, [inputValue]);

  return (
    <div className="ant-list ant-list-split ant-list-bordered">
      <div className="ant-list-header">{title}</div>
      <div className="ant-list-item ant-row detail-edit-list-row">
        {keyList.map((item, index) => {
          const configItemData = configData?.[item];
          if (!configItemData) {
            console.error(`动态配置, ${item} 属性配置不存在`);
            return;
          }
          let value;
          if (configItemData.unit) {
            value = parseFloat(data[item]);
          } else if (configItemData.splitFn) {
            value = configItemData.splitFn(data[item]);
          } else {
            value = data[item];
          }
          return (
            <Fragment key={item + index}>
              {index !== 0 && index < keyList.length && index % 2 == 0 ? (
                <div className="detail-edit-list-line" key={"detail-edit-list-line" + item + index}></div>
              ) : (
                ""
              )}
              <EditListItem
                setBackground={(index + 1) % 4 === 0 || (index + 1) % 4 === 3}
                name={item}
                title={title}
                reloadData={reloadData}
                info={configItemData.info}
                value={value}
                type={configItemData.type}
                unit={configItemData.unit}
                mode={configItemData.mode}
                check={configItemData.check}
                selectList={configItemData.selectList}
                confirmMessage={configItemData.confirmMessage}
              />
              {index === keyList.length - 1 && (index + 1) % 4 === 3 && (
                <div className={`ant-col ant-col-12 detail-edit-list-row-col background`}></div>
              )}
            </Fragment>
          );
        })}
        {!keyList.length && <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="数据为空" />}
      </div>
    </div>
  );
});
