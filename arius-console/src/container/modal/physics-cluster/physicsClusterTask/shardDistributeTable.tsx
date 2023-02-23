import React, { useState, useEffect } from "react";
import { DTable } from "component/dantd/dtable";
import { ErrorSvg } from "./svg";
import { useResize, uuid } from "../../../../lib/utils";

const ShardDistributeTab = (props) => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [originData, setOriginData] = useState([]);
  const [isResolve, setIsResolve] = useState(true);

  const clientSize = useResize("ant-drawer-body");

  useEffect(() => {
    reloadData();
  }, []);

  const reloadData = () => {
    setLoading(true);
    const requestData = props.network;
    const params = {
      cluster: props.params.cluster,
    };
    requestData(params)
      .then((res) => {
        if (res) {
          let data = res.map((item) => ({ ...item, id: uuid() }));
          setIsResolve(true);
          setData(data);
          setOriginData(data);
        }
      })
      .catch(() => {
        setIsResolve(false);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const handleSubmit = (val) => {
    let data = originData.filter((item) => item?.index?.includes(val) || item?.ip?.includes(val) || item?.node?.includes(val));
    setData(data);
  };

  return (
    <>
      <div>
        {isResolve ? (
          <DTable
            loading={loading}
            rowKey="id"
            dataSource={data}
            reloadData={reloadData}
            attrs={{
              scroll: { y: clientSize.height - 220 },
            }}
            tableHeaderSearchInput={{
              submit: handleSubmit,
              style: { width: 300, paddingBottom: 10 },
              placeholder: "请输入关键字，支持index、ip、node",
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
export const ShardDistributeTabs = ShardDistributeTab;
