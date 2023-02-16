import { Col, Row } from "antd";
import * as React from "react";
import "./index.less";

export interface IBaseDetail {
  title?: string | JSX.Element;
  columns: IItem[][];
  baseDetail: object;
}

interface IItem {
  label: string;
  key: string;
  render?: (value: any) => string | JSX.Element;
}

export const BaseDetail = (props: IBaseDetail) => {
  return (
    <>
      {props.title ? <div className="base-info-title">{props.title}</div> : null}
      <div className="base-info-box">
        {(props.columns || []).map((items: any, index: number) =>
          items instanceof Array ? (
            <Row className={`base-info-box-row ${index % 2 != 0 ? "base-info-box-activ" : ""}`} key={index + "row"}>
              {items.map((item: IItem, indexC: number) => (
                <Col span={12} className="base-info-box-row-col" key={indexC + "col"}>
                  <div className="base-info-box-row-col-label">{item?.label}</div>
                  <div className="base-info-box-row-col-content">
                    {item?.render ? item.render(props.baseDetail?.[item.key]) : props.baseDetail?.[item.key] ?? "-"}
                  </div>
                </Col>
              ))}
            </Row>
          ) : (
            <div className="base-info-box-row-col" key={index + "col"}>
              <div className="base-info-box-row-col-label">{items?.label}：</div>
              <div className="base-info-box-row-col-content">
                {items?.render ? items.render(props.baseDetail?.[items.key]) : props.baseDetail?.[items.key] ?? "-"}
              </div>
            </div>
          )
        )}
      </div>
    </>
  );
};
