import React, { useState, useEffect } from "react";
import { Spin } from "antd";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";

export const HotTask = (props) => {
  const [data, setData] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    props
      .network(props.params.cluster)
      .then((res) => {
        setData(res || "");
      })
      .catch(() => {
        setData("网络请求错误");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  return (
    <>
      <div className="hot-task">
        <Spin spinning={loading} className="hot-task-spin">
          {!loading && <ACEJsonEditor options={{ useWorker: false }} className={"mapping-detail"} readOnly={true} data={data} />}
        </Spin>
      </div>
    </>
  );
};
