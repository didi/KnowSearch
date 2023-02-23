import React, { useState } from "react";
import { getLogicListColumns } from "./config";
import { DTable } from "component/dantd/dtable";

export const LogicList = (props: { logicList: []; reloadData: any; loading: boolean }) => {
  const [searchKey, setSearchKey] = useState("");

  const getData = (origin: { v1: { name: string; id: number; projectNameList: string[] }; v2: { name: string } }[]) => {
    let data = [];
    (origin || []).forEach((item) => {
      let listItem = {
        logicName: item?.v1?.name,
        logicClusterId: item?.v1?.id,
        region: item?.v2?.name,
        projectNameList: item?.v1?.projectNameList,
      };
      data.push(listItem);
    });
    if (searchKey) {
      data = data.filter((item) => item.logicName.includes(searchKey) || item.region.includes(searchKey));
    }
    return data;
  };

  const handleSubmit = (val: string) => setSearchKey(val);

  const { logicList, reloadData, loading } = props;

  return (
    <div>
      <DTable
        loading={loading}
        rowKey="logicClusterId"
        dataSource={getData(logicList)}
        columns={getLogicListColumns()}
        reloadData={reloadData}
        tableHeaderSearchInput={{ submit: handleSubmit }}
        attrs={{
          bordered: true,
        }}
      />
    </div>
  );
};
