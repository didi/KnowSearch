import React, { useState, useEffect } from "react";
import { DTable } from "component/dantd/dtable";
import { ErrorSvg } from "./svg";
import { useResize } from "../../../../lib/utils";

const IndicesTable = (props) => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [orginData, setOrginData] = useState([]);
  const [isResolve, setIsResolve] = useState(true);
  const clientSize = useResize("ant-drawer-body");

  useEffect(() => {
    reloadData();
  }, []);

  const reloadData = async () => {
    setLoading(true);
    const requestData = props.network;
    const params = {
      cluster: props.params.cluster,
    };
    try {
      let res = await requestData(params);
      setIsResolve(true);
      setData(res);
      setOrginData(res);
    } catch {
      setIsResolve(false);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (val) => {
    let data = orginData.filter((item) => item?.index?.includes(val));
    setData(data);
  };

  return (
    <>
      <div>
        {isResolve ? (
          <DTable
            loading={loading}
            rowKey="index"
            dataSource={data}
            reloadData={reloadData}
            attrs={{
              scroll: { x: "max-content", y: clientSize.height - 220 },
            }}
            tableHeaderSearchInput={{
              submit: handleSubmit,
              style: { width: 220, paddingBottom: 10 },
              placeholder: "请输入关键字，支持index",
            }}
            columns={props.columns()}
            paginationProps={{
              showSizeChanger: true,
              pageSizeOptions: ["5", "10", "20", "50", "100", "200"],
              position: "bottomRight",
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条`,
            }}
          />
        ) : (
          <div className="error-container">
            <div className="error-gif">
              <ErrorSvg />
            </div>
            <div className="error-desc">
              <span>执行失败</span>
            </div>
          </div>
        )}
      </div>
    </>
  );
};
export const IndicesTables = IndicesTable;
