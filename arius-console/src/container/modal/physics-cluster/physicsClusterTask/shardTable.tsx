import React, { useState, useEffect, useRef } from "react";
import { DTable } from "component/dantd/dtable";
import { ErrorSvg } from "./svg"
import { useResize } from "../../../../lib/utils";
const ShardTable = (props) => {
  const [loading, setLoading] = useState(false);
  const [queryFormObject, setqueryFormObject]: any = useState({
    current: 1,
    size: 10,
  });
  const [data, setData] = useState([]);
  const [total, setTotal] = useState(0);
  const [isResolve, setIsResolve] = useState(true);
  const saveData = useRef(null);
  const clientSize = useResize("ant-drawer-body");
  useEffect(() => {
    reloadData();
  }, []);

  // useEffect(() => {
  //   const start = queryFormObject.size * (queryFormObject.current - 1);
  //   const end = queryFormObject.current * queryFormObject.size;
  //   if (saveData.current) {
  //     const formatRes = saveData.current?.decisions.slice(start, end).map((item, index) => {
  //       return {
  //         ...saveData.current,
  //         ...item,
  //       };
  //     });
  //     setData(formatRes);
  //   }
  // }, [queryFormObject]);

  const reloadData = () => {
    setLoading(true);
    const requestData = props.network;
    requestData(props.params.cluster)
      .then((res) => {
        if (res) {
          setIsResolve(true);
          const formatRes = res?.decisions.map((item, index) => {
            return {
              ...res,
              ...item,
            };
          });
          saveData.current = res;
          setData(formatRes);
        }
      }).catch((rej) => {
        setIsResolve(false);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    if (sorter.columnKey && sorter.order) {
      // switch (sorter.columnKey) {
      //   case "diskInfo":
      //     sorterObject.sortTerm = "disk_usage_percent";
      //     sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
      //     break;
      //   case "activeShardNum":
      //     sorterObject.sortTerm = "active_shard_num";
      //     sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
      //     break;
      //   default:
      //     break;
      // }
    }
    setqueryFormObject((state) => {
      if (!sorter.order) {
        delete state.sortTerm;
        delete state.orderByDesc;
      }
      return {
        ...state,
        // ...sorterObject,
        current: pagination.current,
        size: pagination.pageSize,
      };
    });
  };

  return (
    <>
      <div>
        {isResolve ? <DTable
          loading={loading}
          rowKey="id"
          dataSource={data}
          attrs={{
            //pagination: false,
            onChange: handleChange,
            scroll: { x: "max-content", y: clientSize.height - 190 },
            //bordered: true
          }}
          columns={props.columns()}
        /> : (<div className="error-container">
          <div className="error-gif">
            <ErrorSvg />
          </div>
          <div className="error-desc">
            <span>执行失败</span>
          </div>
        </div>)
        }
      </div>
    </>
  );
};
export const ShardTables = ShardTable;
