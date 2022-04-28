import { TreeSelect } from "antd";
import { DataNode } from "rc-tree-select/lib/interface";
import React from "react";

export const CustomTreeSelect = (props: { treeData: DataNode[]; onChange?: (value) => void }): React.ReactNode => {
  const onChange = (value) => {
    const { onChange } = props;
    onChange && onChange(value);
  };
  return (
    <TreeSelect
      style={{ width: "100%" }}
      dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
      treeData={props.treeData}
      placeholder="Please select"
      treeDefaultExpandAll
      onChange={onChange}
    />
  );
};
