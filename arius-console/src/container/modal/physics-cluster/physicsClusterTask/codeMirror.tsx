import React, { useState, useEffect } from "react";
import { Spin } from "antd";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
import { ReloadOutlined } from "@ant-design/icons";

export const HotTask = (props) => {
  const [data, setData] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reloadData();
  }, []);

  const reloadData = async () => {
    setLoading(true);
    await props
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
  };

  return (
    <>
      <div className="hot-task">
        <ReloadOutlined className="reload-hot-task-spin" onClick={reloadData} />
        <Spin spinning={loading} className="hot-task-spin">
          {!loading && <ACEJsonEditor options={{ useWorker: false }} className={"mapping-detail"} readOnly={true} data={data} />}
        </Spin>
      </div>
    </>
  );
};
