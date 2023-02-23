import React, { memo, useEffect, useRef, useState } from "react";
import _ from "lodash";
import { SearchQueryForm } from "./components";
import { Tooltip } from "antd";
import { PERIOD_RADIO_MAP, errorQueryColumns } from "./config";
import { getErrorQueryList as getQueryList } from "api/search-query";
import { isSuperApp } from "lib/utils";
import { ProTable } from "knowdesign";
import "./index.less";

const classPrefix = "error-query-container";

export const ErrorQuery = (props: any) => {
  const [queryParams, setQueryParams] = useState({
    queryIndex: undefined,
    startTime: undefined,
    endTime: undefined,
  });
  const [dataSource, setDataSource] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState({
    page: 1,
    size: 10,
  });
  const [total, setTotal] = useState(0);

  const isFirst = useRef(true);
  const totalLimit = 10000;

  const changeQueryParams = (params) => {
    setQueryParams({ ...params });
    page.page !== 1 && setPage({ ...page, page: 1 });
  };

  const getAsyncDataSource = async () => {
    try {
      setIsLoading(true);
      let params = {
        ...page,
        ...queryParams,
      };
      const res = await getQueryList(params as any);
      let dataSource = res?.bizData;
      if (!dataSource) {
        setDataSource([]);
        return;
      }
      dataSource?.forEach((item, index) => {
        item.key = index;
      });
      setDataSource(dataSource);
      setTotal(res?.pagination.total);
    } catch (error) {
      setDataSource([]);
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (pagination) => {
    setPage({
      page: pagination.current,
      size: pagination.pageSize,
    });
  };
  useEffect(() => {
    if (isFirst.current && !queryParams.startTime) {
      isFirst.current = false;
      return;
    }
    getAsyncDataSource();
  }, [queryParams, page]);

  useEffect(() => {
    props?.menu === "error-query" && getAsyncDataSource();
  }, [props?.menu]);

  return (
    <div className={classPrefix}>
      <div className={`${classPrefix}-query`}>
        <SearchQueryForm setSearchQuery={changeQueryParams} value={"error-query"} />
      </div>

      <div className={`${classPrefix}-table`}>
        <ProTable
          showQueryForm={false}
          tableProps={{
            tableId: "error_search_query_table",
            isCustomPg: false,
            showHeader: false,
            loading: isLoading,
            rowKey: "key",
            dataSource: dataSource,
            columns: errorQueryColumns(isSuperApp()),
            paginationProps: {
              total: total > totalLimit ? totalLimit : total,
              current: page.page,
              pageSize: page.size,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
              itemRender: (pagination, type: "page" | "prev" | "next", originalElement) => {
                const lastPage = totalLimit / page.size;
                if (type === "page") {
                  if (total > totalLimit && pagination === lastPage) {
                    return <Tooltip title={`考虑到性能问题，只展示${totalLimit}条数据`}>{pagination}</Tooltip>;
                  } else {
                    return pagination;
                  }
                } else {
                  return originalElement;
                }
              },
            },
            attrs: {
              scroll: { x: "max-content" },
              onChange: handleChange,
            },
          }}
        />
      </div>
    </div>
  );
};
