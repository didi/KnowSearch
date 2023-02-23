import React, { useState, useEffect } from "react";
import { getNodeColumns } from "./config";
import Url from "lib/url-parser";
import { DTable } from "component/dantd/dtable";
import { getGatewayNode } from "api/gateway-manage";
import _ from "lodash";
import "styles/search-filter.less";
import "./index.less";

export const NodeList = (props) => {
  const [searchKey, setSearchKey] = useState("");
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [clusterId, setClusterId] = useState(null);
  const [paginationProps, setPaginationProps] = useState({ current: 1, size: 10 });
  const [total, setTotal] = useState(0);

  useEffect(() => {
    const url = Url();
    setClusterId(Number(url.search.id));
  }, []);

  useEffect(() => {
    clusterId && reloadData();
  }, [clusterId, paginationProps, searchKey]);

  const reloadData = () => {
    setLoading(true);
    let params = {
      page: paginationProps.current,
      size: paginationProps.size,
      nodeName: searchKey,
    };
    getGatewayNode(clusterId, params)
      .then((res) => {
        const { total = 0 } = res?.pagination;
        setData(res.bizData || []);
        setTotal(total);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const handleSubmit = (value) => {
    setSearchKey(value);
  };

  const onChange = (pagination) => {
    setPaginationProps({ current: pagination.current, size: pagination.pageSize });
  };

  return (
    <div className="node-divide-content">
      <DTable
        loading={loading}
        rowKey="id"
        dataSource={data}
        columns={getNodeColumns()}
        reloadData={reloadData}
        tableHeaderSearchInput={{ submit: handleSubmit }}
        attrs={{ onChange }}
        paginationProps={{
          total,
          pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
          showTotal: (total) => `共 ${total} 条`,
          current: paginationProps.current,
        }}
      />
    </div>
  );
};
