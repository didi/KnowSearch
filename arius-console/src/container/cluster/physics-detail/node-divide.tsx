import React, { useState, useEffect, useCallback } from "react";
import "styles/search-filter.less";
import { getNodeDivideColumns } from "./config";
import Url from "lib/url-parser";
import { DTable } from "component/dantd/dtable";
import { getPhyNodeDivideList, deleteNode } from "api/op-cluster-index-api";
import { Modal, message, Button } from "antd";
import { QuestionCircleOutlined, ExclamationCircleOutlined } from "@ant-design/icons";
import _ from "lodash";
import "./index.less";

export const NodeDivide = (props) => {
  const [searchKey, setSearchKey] = useState("");
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [clusterId, setClusterId] = useState(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);

  useEffect(() => {
    const url = Url();
    setClusterId(Number(url.search.physicsClusterId));
  }, []);

  useEffect(() => {
    clusterId && reloadData();
  }, [clusterId]);

  const reloadData = () => {
    setLoading(true);
    getPhyNodeDivideList(clusterId)
      .then((res) => {
        setData(res);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const getData = (origin?: any[]) => {
    let key = (searchKey + "").trim().toLowerCase();
    let data = key
      ? origin.filter((d) => {
          let flat = false;
          Object.keys(d).forEach((objectKey) => {
            if (typeof objectKey === "string" || typeof objectKey === "number") {
              if ((d[objectKey] + "").toLowerCase().includes((key + "") as string)) {
                flat = true;
                return;
              }
            }
          });
          return flat;
        })
      : origin;
    data = data.sort((a, b) => b.role - a.role);
    return data;
  };

  const handleSubmit = (value) => {
    setSearchKey(value);
  };

  const getOpBtns = useCallback(() => {
    let content: string | React.ReactNode;
    let icon: React.ReactNode;
    let hasRegionList = selectedRows?.filter((item) => item?.regionId !== -1);
    if (hasRegionList.length) {
      let text = hasRegionList?.map((item) => item?.nodeSet);
      content = `节点${text?.join("，")}已被划分Region，请解绑后再下线。`;
      icon = <ExclamationCircleOutlined />;
    } else {
      content =
        selectedRows?.length === 1 ? (
          `确定下线节点${selectedRows[0]?.nodeSet}吗？`
        ) : (
          <div className="delete-batch-node">
            <div className="delete-title">确定批量下线节点？</div>
            <div className="delete-content">
              <div className="delete-label">操作对象</div>
              <div className="delete-labels-box">
                {selectedRows?.map((item) => (
                  <div className="delete-node">{item.nodeSet}</div>
                ))}
              </div>
            </div>
          </div>
        );
      icon = <QuestionCircleOutlined />;
    }
    return selectedRows && selectedRows.length > 0 ? (
      <Button
        onClick={() => {
          Modal.confirm({
            icon,
            content,
            okText: "确认",
            cancelText: "取消",
            onOk: async () => {
              if (hasRegionList.length) return;
              await deleteNode(selectedRowKeys);
              message.success("下线成功");
              reloadData();
            },
          });
        }}
        style={{ marginRight: 0 }}
        type={"primary"}
        disabled={selectedRows && selectedRows.length ? false : true}
      >
        批量下线
      </Button>
    ) : (
      <></>
    );
  }, [selectedRows]);

  return (
    <div className="node-divide-content">
      <DTable
        loading={loading}
        rowKey="id"
        dataSource={getData(data)}
        columns={getNodeDivideColumns()}
        reloadData={reloadData}
        tableHeaderSearchInput={{ submit: handleSubmit }}
        attrs={{
          bordered: true,
          scroll: { x: "max-content" },
          rowSelection: {
            selectedRowKeys,
            onChange: (selectedRowKeys, selectedRows) => {
              setSelectedRowKeys(selectedRowKeys);
              setSelectedRows(selectedRows);
            },
            getCheckboxProps: (record) => {
              return {
                disabled: record.status !== 2,
              };
            },
          },
        }}
        renderInnerOperation={getOpBtns}
      />
    </div>
  );
};
