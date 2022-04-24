import React, { memo } from "react";
import { Tooltip } from "antd";
import "../style";

const classPrefix = "state-config";

interface ListType {
  name: string;
  count?: number;
  state?: string;
  id?: number;
}

interface StateConfigPropsType {
  list: ListType[];
  title: String;
}

const RenderModule = (props) => {
  let status = "";
  if (props.state != null) {
    if (props.state === "green") {
      status = "Green";
    } else if (props.state === "yellow") {
      status = "Yellow";
    } else if (props.state === "unknown") {
      status = "Unknown";
    } else [(status = "Red")];
  }

  const ellipsis = (str: string | number, num: number, unit: string = "") => {
    return String(str).length > num ? (
      <Tooltip placement="top" title={`${str}${unit || ""}`}>
        {String(str).substring(0, num) + "..."}
      </Tooltip>
    ) : (
      str
    );
  };

  return (
    <div className={`${classPrefix}-box-content-module`} key={props.name}>
      <p className={`${classPrefix}-box-config-info`}>{props.name}</p>
      {status ? (
        <p
          className={`${classPrefix}-box-config-state ${classPrefix}-box-config-state-${status}`}
        >
          <span>{status}</span>
        </p>
      ) : (
        <p className={`${classPrefix}-box-config-content`}>
          {ellipsis(props.count, 6, props.unit)}
          <span className={`${classPrefix}-box-config-content-sub`}>
            {props.unit || ""}
          </span>
        </p>
      )}
    </div>
  );
};
export const StateConfig: React.FC<StateConfigPropsType> = memo(
  ({ list, title }) => {
    return (
      <div className={`${classPrefix}-box`}>
        <h4 className={`${classPrefix}-box-title`}>{title}</h4>
        <div className={`${classPrefix}-box-content`}>
          {list.map((item, index) => (
            <RenderModule {...item} key={item.id} />
          ))}
        </div>
      </div>
    );
  }
);
