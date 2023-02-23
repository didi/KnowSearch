import React, { useEffect } from "react";
import _ from "lodash";
import { getAccessSettingColumns } from "./config";
import "./index.less";
import { DTable } from "component/dantd/dtable";
import url from "lib/url-parser";
import { getAppByProjectId } from "api";

export const AccessSetting = () => {
  const projectId = +url().search.projectId;
  const [list, setList] = React.useState([]);

  useEffect(() => {
    if (isNaN(projectId)) return;
    getAppByProjectId(projectId).then((res) => {
      setList(res);
    });
  }, []);

  return (
    <>
      <DTable rowKey="id" dataSource={list} columns={getAccessSettingColumns()} reloadData={null} />
    </>
  );
};
