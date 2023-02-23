import { MinusOutlined, PlusOutlined } from "@ant-design/icons";
import { Button, Checkbox, Tooltip } from "antd";
import React from "react";
import "./index.less";
import { TableFormAddRow } from "./tpl-table-add-row/TableFormAddRow";

export const nodeTypeList = ["masternode", "clientnode", "datanode", "Cold属性节点"];

export const AddRoleTable = (props) => {
  const [isShow, setIsShow] = React.useState(true);
  const [isCold, setIsCold] = React.useState(false);

  const addNodeTypeList = [nodeTypeList[1], nodeTypeList[2]];
  const coldNodeTypeList = [nodeTypeList[3]];

  const handleChange = (params) => {
    const { onChange } = props;
    onChange && onChange(params);
  };

  const renderItem = (item) => {
    return (
      <div key={item}>
        <div className="add-role-table-header">
          <div className="role-table-text">
            <span>{item}</span>
          </div>
          <TableFormAddRow type={item} onChange={handleChange} machineList={props.machineList} />
        </div>
      </div>
    );
  };

  return (
    <>
      <TableFormAddRow type={nodeTypeList[0]} onChange={handleChange} machineList={props.machineList} />
      <div className="add-role-button">
        {isShow ? (
          <Button
            className="add-node"
            type="link"
            size="small"
            onClick={() => {
              setIsShow((b) => !b);
            }}
          >
            <PlusOutlined />
            添加节点类型
          </Button>
        ) : (
          <Button
            className="delete-node"
            type="link"
            size="small"
            onClick={() => {
              setIsShow((b) => !b);
            }}
          >
            <MinusOutlined />
            删除节点类型
          </Button>
        )}
      </div>
      {!isShow ? addNodeTypeList.map((item) => renderItem(item)) : null}
      {!isShow && isCold ? coldNodeTypeList.map((item) => renderItem(item)) : null}
    </>
  );
};
