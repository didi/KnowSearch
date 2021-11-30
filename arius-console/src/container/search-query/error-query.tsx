import React, { memo, useEffect, useState } from "react";
import _ from "lodash";
import { SearchQueryForm } from "./components";
import { DTable } from "component/dantd/dtable";
import { PERIOD_RADIO_MAP, errorQueryColumns as columns } from "./config";
import "./index.less";
import { getErrorQueryList as getQueryList } from "api/search-query";

const classPrefix = "error-query-container";

export const ErrorQuery = () => {
  const department: string = localStorage.getItem('current-project');
  const dates = PERIOD_RADIO_MAP.get("oneDay").dateRange;
  const [queryParams, setQueryParams] = useState({
    queryIndex: "",
    startTime: dates[0].valueOf(),
    endTime: dates[1].valueOf(),
  });
  const [dataSource, setDataSource] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [reload, setReload] = useState(false);

  const reloadPage = _.debounce(() => {
    setReload(!reload);
  }, 300);

  const changeQueryParams = (params) => {
    console.log(params)
    setQueryParams({ ...queryParams, ...params });
  };


  const getAsyncDataSource = async (queryParams) => {
    try {
      setIsLoading(true);
      const dataSource = await getQueryList(queryParams);
      if (!dataSource) {
        setDataSource([]);
        return;
      }
      dataSource.records = dataSource?.map(item => {
        return {
          ...item,
          id: Math.random(),
        }
      })
      setDataSource(dataSource?.records);
    } catch (error) {
      setDataSource([]);
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAsyncDataSource(queryParams);
  }, [queryParams, reload, department]);

  return (
    <div className={classPrefix}>
      <div>
        <SearchQueryForm setSearchQuery={changeQueryParams} />
      </div>
      <div className={`${classPrefix}-table`}>
        <DTable
          loading={isLoading}
          rowKey="key"
          dataSource={dataSource}
          attrs={{
            rowKey: 'id'
          }}
          paginationProps={{
            position: "bottomRight",
            showQuickJumper: true,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total) => `共 ${total} 条`,
          }}
          columns={columns}
          reloadData={reloadPage}
        />
      </div>
    </div>
  );
};
