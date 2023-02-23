import React from "react";
import { Tooltip } from "antd";
import "./index.less";

export default function InfoTooltip(props) {
  const { price, currentCalLogic, threshold, className } = props;

  const renderTitle = () => {
    let priceText = price ? (
      <div className="price">
        <span className="title">指标价值：</span>
        <div className="content">{price}</div>
      </div>
    ) : (
      ""
    );
    let currentCalLogicText = currentCalLogic ? (
      <div className="currentCalLogic">
        <span className="title">计算逻辑：</span>
        <div className="content">{currentCalLogic}</div>
      </div>
    ) : (
      ""
    );
    let thresholdText = threshold ? (
      <div className="threshold">
        <span className="title">阈值线：</span>
        <div className="content">{threshold}</div>
      </div>
    ) : (
      ""
    );
    return (
      <div>
        {priceText}
        {currentCalLogicText}
        {thresholdText}
      </div>
    );
  };

  return (
    <div className={`info-tooltip ${className ? className : ""}`}>
      <Tooltip overlayClassName="info-content" title={renderTitle()} placement="topLeft" getPopupContainer={(node) => node.parentElement}>
        <svg className="icon" aria-hidden="true">
          <use xlinkHref="#iconinfo"></use>
        </svg>
      </Tooltip>
    </div>
  );
}
