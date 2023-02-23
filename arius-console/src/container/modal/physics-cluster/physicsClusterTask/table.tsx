import React, { useState, useEffect } from "react";
import { DTable } from "component/dantd/dtable";
import { ErrorSvg } from "./svg";
import { useResize, uuid } from "../../../../lib/utils";
// import { ProTable, DTable } from "knowdesign";

const PhysicsClusterTask = (props) => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [orginData, setOrginData] = useState([]);
  const [isResolve, setIsResolve] = useState(true);
  const clientSize = useResize("ant-drawer-body");

  const { network, params, hasSearch, placeholder, columns, keyword, type } = props;

  useEffect(() => {
    reloadData();
  }, []);

  const reloadData = () => {
    setLoading(true);
    network(params.cluster)
      .then((res) => {
        if (res) {
          let data = (res || []).map((item) => {
            return {
              ...item,
              id: uuid(),
            };
          });
          setIsResolve(true);
          if (type === "task") {
            data = data.sort((aa, bb) => bb?.runningTimeInNanos - aa?.runningTimeInNanos);
          }
          setData(data);
          setOrginData(data);
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
    let data = (orginData || []).filter((item) => item[keyword].includes(val));
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
              scroll: { x: "max-content", y: clientSize.height - 190 },
            }}
            tableHeaderSearchInput={
              hasSearch
                ? {
                    submit: handleSubmit,
                    style: { width: 250, paddingBottom: 10 },
                    placeholder,
                  }
                : null
            }
            columns={columns()}
          />
        ) : (
          // <ProTable
          //   tableProps={{
          //     tableId: "physics_cluster_task_table",
          //     loading,
          //     rowKey: "key",
          //     dataSource: data,
          //     columns: columns(),
          //     attrs: {
          //       scroll: { x: "max-content", y: clientSize.height - 190 },
          //     },
          //   }}
          // />
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
export const PhysicsClusterTable = PhysicsClusterTask;
