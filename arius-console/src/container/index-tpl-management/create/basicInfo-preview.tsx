import * as React from "react";
import { getMappingPreviewInfo } from "./config";
import { IBaseInfo } from "typesPath/base-types";
import { Descriptions } from "knowdesign";
import "./index.less";

export class BasicInfoPreview extends React.Component<any> {
  public render() {
    const { baseInfo, dataTypeList } = this.props;

    return (
      <>
        <Descriptions className="base-info" size="middle" column={3} layout="vertical">
          {getMappingPreviewInfo(baseInfo?.isCyclicalRoll, dataTypeList).map((item: IBaseInfo, index: number) => (
            <Descriptions.Item key={index} label={item.label}>
              {item.render ? item.render(baseInfo) : `${baseInfo?.[item.key] || "-"}${item.unit || ""}`}
            </Descriptions.Item>
          ))}
        </Descriptions>
      </>
    );
  }
}
