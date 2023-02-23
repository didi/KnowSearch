import React, { memo, useEffect, useRef, useState } from "react";
import _ from "lodash";
import { SearchQueryForm } from "./components";
import { Tooltip } from "antd";
import { PERIOD_RADIO_MAP, slowQueryColumns } from "./config";
import { getSlowQueryList as getQueryList } from "api/search-query";
import { isSuperApp } from "lib/utils";
import { ProTable } from "knowdesign";
import "./index.less";

const classPrefix = "slow-query-container";

export const SlowQuery = (props: any) => {
  const [queryParams, setQueryParams] = useState({
    queryIndex: undefined,
    startTime: undefined,
    endTime: undefined,
    totalCost: undefined,
  });
  const [dataSource, setDataSource] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState({
    page: 1,
    size: 10,
    sortTerm: "",
    orderByDesc: true,
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
      const res: any = await getQueryList(params as any);
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

  const handleChange = (pagination, _, tableParams) => {
    setPage({
      page: pagination.current,
      size: pagination.pageSize,
      sortTerm: tableParams.order === "ascend" || tableParams.order === "descend" ? tableParams.field : null,
      orderByDesc: tableParams.order === "ascend" || tableParams.order === "descend" ? tableParams.order !== "ascend" : null,
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
    props?.menu === "slow-query" && getAsyncDataSource();
  }, [props?.menu]);

  return (
    <div className={classPrefix}>
      <div className={`${classPrefix}-query`}>
        <SearchQueryForm setSearchQuery={changeQueryParams} isSlow value={"slow-query"} />
      </div>
      <div className={`${classPrefix}-table`}>
        <ProTable
          showQueryForm={false}
          // queryFormProps={{
          //   defaultCollapse: true,
          //   columns: slowQueryColumns(isSuperApp()),
          //   isResetClearAll: true,
          // }}
          tableProps={{
            tableId: "slow_query_table",
            isCustomPg: false,
            showHeader: false,
            loading: isLoading,
            rowKey: "key",
            dataSource: dataSource,
            columns: slowQueryColumns(isSuperApp()),
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
