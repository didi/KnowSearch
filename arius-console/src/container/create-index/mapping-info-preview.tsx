import * as React from "react";
import "./index.less";
import { getMappingPreviewInfo } from "./config";
import { TEMP_FORM_MAP_KEY } from "./constant";
import { IBaseInfo } from "@types/base-types";
import  { Descriptions } from 'antd';
import * as actions from "actions";
import { connect } from "react-redux";


const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});

@connect(mapStateToProps)
export class MappingInfoPreview extends React.Component<any> {
  public render() {
    const firstStepFormData =
      this.props.createIndex.temporaryFormMap.get(
        TEMP_FORM_MAP_KEY.firstStepFormData
      ) || {};
    const isCyclicalRoll = !!this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.isCyclicalRoll
    );

    return (
      <>
        <Descriptions className="base-info" size="middle" column={2}>
          {getMappingPreviewInfo(isCyclicalRoll).map(
            (item: IBaseInfo, index: number) => (
              <Descriptions.Item key={index} label={item.label}>
                {item.render
                  ? item.render(firstStepFormData)
                  : `${firstStepFormData?.[item.key] || "-"}${item.unit || ""}`}
              </Descriptions.Item>
            )
          )}
        </Descriptions>
      </>
    );
  }
}
