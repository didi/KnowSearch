import { Table, Transfer, Tag, Popover, Tooltip } from "antd";
import difference from "lodash/difference";
import React, { useEffect, useState } from "react";
import { renderAttributes } from "../../container/custom-component";
import "./index.less";

interface IRegionTransferProps {
  targetKeys?: number[];
  dataSource: any[];
  selectKeys: (keys: number[]) => void;
  listStyle?: object;
  style?: object;
  isExpand?: boolean;
  attribute?: string;
}

const TableTransfer = ({ leftColumns, rightColumns, isExpand, ...restProps }) => (
  <Transfer {...restProps}>
    {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys: listSelectedKeys }) => {
      const columns = direction === "left" ? leftColumns : rightColumns;
      const rowSelection = {
        onSelectAll(selected, selectedRows) {
          const treeSelectedKeys = selectedRows.filter((item) => !item.disabled).map(({ key }) => key);
          const diffKeys = selected ? difference(treeSelectedKeys, listSelectedKeys) : difference(listSelectedKeys, treeSelectedKeys);
          onItemSelectAll(diffKeys, selected);
        },
        onSelect({ key }, selected) {
          onItemSelect(key, selected);
        },
        selectedRowKeys: listSelectedKeys,
        renderCell(checked, record, index, node) {
          if (record?.disabled) {
            return <Tooltip title={`${isExpand ? "扩容" : "缩容"}工单，不允许${isExpand ? "缩容" : "扩容"}节点`}>{node}</Tooltip>;
          }
          return node;
        },
        getCheckboxProps: (item) => ({
          disabled: item.disabled,
        }),
      };

      return (
        <Table
          className="region-transfer-table"
          rowSelection={rowSelection}
          columns={columns}
          dataSource={filteredItems}
          size="small"
          pagination={false}
          onRow={({ key, disabled: itemDisabled }) => ({
            onClick: () => {
              if (itemDisabled) return;
              onItemSelect(key, !listSelectedKeys.includes(key));
            },
          })}
        />
      );
    }}
  </Transfer>
);

const RegionTransfer = (props: IRegionTransferProps) => {
  const [targetKeys, setTargetKeys] = useState(props?.targetKeys || []);

  const { dataSource, selectKeys, isExpand, attribute } = props;

  useEffect(() => {
    setTargetKeys(props?.targetKeys || []);
  }, [dataSource]);

  //解决父组件挂在完成之前props?.targetKeys为空值
  useEffect(() => {
    setTargetKeys(props?.targetKeys);
  }, [props?.targetKeys]);

  const tableColumns = () => {
    let columns = [];
    let nodeSet = {
      dataIndex: "nodeSet",
      title: "节点名称",
      width: `${attribute ? 150 : 90}`,
      ellipsis: true,
      render: (val: string) => {
        return (
          <Tooltip title={val} placement="topLeft">
            <span>{val}</span>
          </Tooltip>
        );
      },
    };
    let ip = {
      dataIndex: "ip",
      title: "ip",
      width: 120,
    };
    let attributes = {
      dataIndex: `${attribute ? "attributeValue" : "attributes"}`,
      title: "attribute",
      render: (val: string) => {
        if (!val) return "-";
        let attrArray = val.split(",");
        let limit = 1;
        if (attrArray?.length === 2 && attrArray[0]?.length + attrArray[1]?.length <= 35) {
          limit = 2;
        } else if (attrArray?.length === 3 && attrArray[0]?.length + attrArray[1]?.length + attrArray[2]?.length <= 35) {
          limit = 3;
        }
        return renderAttributes({ data: val, limit });
      },
    };
    if (attribute) {
      columns.push(attributes, ip, nodeSet);
    } else {
      columns.push(nodeSet, ip, attributes);
    }
    return columns;
  };

  const onChange = (targetKeys: []) => {
    setTargetKeys(targetKeys);
    selectKeys(targetKeys);
  };

  return (
    <>
      <TableTransfer
        listStyle={props.listStyle || {}}
        className="region-transfer"
        dataSource={dataSource}
        targetKeys={targetKeys}
        showSearch={true}
        onChange={onChange}
        filterOption={(inputValue: string, item: any) => {
          let nodeSet = item.nodeSet.indexOf(inputValue) !== -1;
          let ipPort = item.ip.indexOf(inputValue) !== -1;
          let attributes = item.attributes.indexOf(inputValue) !== -1;
          return nodeSet || ipPort || attributes;
        }}
        leftColumns={tableColumns()}
        rightColumns={tableColumns()}
        style={props?.style}
        isExpand={isExpand}
      />
    </>
  );
};

export default RegionTransfer;
