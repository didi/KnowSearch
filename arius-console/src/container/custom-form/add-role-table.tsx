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
          <div style={{ paddingBottom: 5, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>{item}</span>
            {item === nodeTypeList[2] ? (
              <span style={{ marginLeft: 60, display: "inline-block" }}>
                <Checkbox
                  checked={isCold}
                  onChange={() => {
                    setIsCold((b) => !b);
                  }}
                >
                  配置Cold节点
                </Checkbox>
                <Tooltip title={"如需开启冷热分离服务，需要将部分data节点的rack属性值设为Cold。注：cold节点不能划分region，请谨慎设置"}>
                  <svg className="icon svg-icon" aria-hidden="true">
                    <use xlinkHref="#iconinfo"></use>
                  </svg>
                </Tooltip>
              </span>
            ) : null}
          </div>
          <TableFormAddRow type={item} onChange={handleChange} />
        </div>
      </div>
    );
  };

  return (
    <>
      <TableFormAddRow type={nodeTypeList[0]} onChange={handleChange} />
      <div style={{ marginTop: 10 }}>
        {isShow ? (
          <Button
            type="primary"
            size="small"
            onClick={() => {
              setIsShow((b) => !b);
            }}
          >
            <PlusOutlined /> 添加节点类型
          </Button>
        ) : (
          <Button
            type="primary"
            size="small"
            onClick={() => {
              setIsShow((b) => !b);
            }}
            style={{ marginBottom: 10 }}
          >
            <MinusOutlined /> 删除节点类型
          </Button>
        )}
      </div>
      {!isShow ? addNodeTypeList.map((item) => renderItem(item)) : null}
      {!isShow && isCold ? coldNodeTypeList.map((item) => renderItem(item)) : null}
    </>
  );
};
