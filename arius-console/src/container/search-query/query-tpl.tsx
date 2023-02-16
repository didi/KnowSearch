import React, { useState, useCallback, useRef, useEffect } from "react";
import { Row, Col, Form, Input, Select, Button, Tooltip } from "antd";
import moment from "moment";
import { connect } from "react-redux";
import { getQueryTplColumns, filterColumnsList, customTimeOptions } from "./config";
import { getDslList, getCheckedList, setCheckedList } from "api/search-query";
import { DTable } from "component/dantd/dtable";
import DslDetail from "../drawer/dsl-detail";
import EditLimit from "./components/editLimit";
import FilterColumns from "component/filterColumns";
import { SearchTemplatePermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { AppState } from "store/type";
import { isSuperApp, filterOption, getCookie, getCurrentProject } from "lib/utils";
import { ProTable } from "knowdesign";
import { RenderTitle } from "component/render-title";
import { getQueryFormConfig } from "./config";
import "./index.less";
import store from "store";

const appInfo = {
  app: store.getState().app.appInfo,
  user: store.getState().user.getName,
};

const mapStateToProps = (state) => ({
  app: state.app,
});

export const QueryTpl = connect(mapStateToProps)((props: { app: AppState }) => {
  const department: string = localStorage.getItem("current-project");
  const defaultRangeKey = 0;
  const [loading, setloading] = useState(false);
  const [data, setData] = useState([] as any[]);
  const [queryForm, setQueryForm] = useState({ MD5: "", projectId: null, queryIndex: "" });
  const [startTime, setStartTime] = useState(moment(new Date().getTime() - customTimeOptions[defaultRangeKey]?.value) as any); // 默认近2小时
  const [endTime, setEndTime] = useState(moment() as any);
  const [visible, setVisible] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [records, setRecords] = useState([]);
  const [selectItem, setSelectItem] = useState([]);
  const [editVisible, setEditVisible] = useState(false);
  const [record, setRecord] = useState({});
  const [columns, setColumns] = useState([]);
  const [filterColumnsVisible, setFilterColumnsVisible] = useState(false);
  const buttonTime = useRef(null);
  const error = useRef(false);
  const superApp = isSuperApp();

  const [pagination, setPagination] = useState({
    position: "bottomRight",
    showQuickJumper: true,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total) => `共 ${total} 条`,
    current: 1,
    pageSize: 10,
  });
  const [page, setPage] = useState({
    page: 1,
    size: 10,
    sortInfo: "",
    orderByDesc: true,
  });

  useEffect(() => {
    reloadData();
  }, [department, page, queryForm]);

  const onSelectChange = (selectedRowKeys, records) => {
    setSelectItem(records);
    setSelectedRowKeys(selectedRowKeys);
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: onSelectChange,
  };

  const reloadData = () => {
    // 校验不通过时不发送请求
    if (error.current) {
      return;
    }
    setloading(true);
    const currentTime = new Date().getTime();
    const params = {
      ...page,
      startTime: startTime ? (buttonTime.current ? currentTime - (endTime?.valueOf() - startTime?.valueOf()) : startTime?.valueOf()) : "",
      endTime: endTime ? (buttonTime.current ? currentTime : endTime?.valueOf()) : "",
      projectId: superApp ? queryForm.projectId : null,
      dslTemplateMd5: queryForm.MD5,
      queryIndex: queryForm.queryIndex,
    };
    getDslList(params)
      .then((res: any) => {
        if (res) {
          res?.bizData.forEach((item, index) => {
            item.key = `${item.projectId}${index}`;
          });
          setData(res?.bizData);
          const { pageNo = 1, pageSize = 10 } = res.pagination;
          setPagination({
            ...pagination,
            total: res?.pagination?.total,
            current: pageNo,
            pageSize: pageSize,
          });
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const handleChange = (pagination, _, tableParams) => {
    setPage({
      page: pagination.current,
      size: pagination.pageSize,
      sortInfo: tableParams.order === "ascend" || tableParams.order === "descend" ? tableParams.field : null,
      orderByDesc: tableParams.order === "ascend" || tableParams.order === "descend" ? tableParams.order !== "ascend" : null,
    });
  };

  const onCancel = () => {
    setVisible(false);
  };

  const showDrawer = (record: any) => {
    setRecord(record);
    setVisible(true);
  };

  const onSearch = (result) => {
    const { MD5, projectId, queryIndex } = result;
    setQueryForm({ MD5, projectId, queryIndex });
    setPage({
      ...page,
      page: 1,
      size: page.size,
    });
  };

  const onReset = () => {
    setStartTime("");
    setEndTime("");
    setQueryForm({ MD5: "", projectId: null, queryIndex: "" });
  };

  const handleTimeChange = (times: number[], periodOrPicker: boolean) => {
    if (times) {
      setStartTime(moment(Number(times[0])));
      setEndTime(moment(Number(times[1])));
      buttonTime.current = periodOrPicker;
    }
  };

  const getCheckList = async () => {
    const checkList = await getCheckedList();
    return checkList;
  };

  const saveCheckFn = async (list: string[]) => {
    await setCheckedList(list);
  };

  const getOpBtns = useCallback(() => {
    return (
      <>
        {hasOpPermission(SearchTemplatePermissions.PAGE, SearchTemplatePermissions.BAT_MODIFY) &&
          (selectItem && selectItem.length >= 1 ? (
            <Tooltip title={selectItem && selectItem.length ? "" : "需要选中后批量修改"}>
              <Button
                onClick={() => showEditLimit()}
                type="primary"
                style={{ marginRight: 0 }}
                disabled={selectItem && selectItem.length ? false : true}
              >
                批量修改限流值
              </Button>
            </Tooltip>
          ) : (
            ""
          ))}
      </>
    );
  }, [selectItem]);

  const editCancel = () => {
    setEditVisible(false);
    setRecords([]);
  };

  const showEditLimit = (record?: any) => {
    if (record) {
      setRecords([record]);
    } else {
      setRecords(selectItem);
    }
    setEditVisible(true);
  };

  const renderTitleContent = () => {
    return {
      title: "查询模板",
      content: null,
    };
  };

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getQueryFormConfig(props.app.projectList, handleTimeChange, filterOption, superApp, error),
          onReset: onReset,
          onSearch: onSearch,
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "query_tpl_table",
          isCustomPg: false,
          loading,
          rowKey: "key",
          dataSource: data,
          columns: columns,
          reloadData,
          isDividerHide: selectItem.length > 0,
          getJsxElement: getOpBtns,
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          paginationProps: pagination,
          attrs: {
            onChange: handleChange,
            scroll: { x: "max-content" },
            rowSelection: rowSelection,
          },
        }}
      />
      <DslDetail
        history={(props as any).history}
        visible={visible}
        detailData={record}
        onCancel={onCancel}
        cb={reloadData}
        showEditLimit={showEditLimit}
      />
      <EditLimit
        history={(props as any).history}
        appInfo={appInfo}
        visible={editVisible}
        record={records}
        cancel={editCancel}
        cb={reloadData}
      />
      <FilterColumns
        columns={getQueryTplColumns(reloadData, showDrawer, showEditLimit, superApp, setFilterColumnsVisible, props)}
        setColumns={setColumns}
        checkArr={filterColumnsList}
        getCheckFn={getCheckList}
        saveCheckFn={saveCheckFn}
        filterColumnsVisible={filterColumnsVisible}
        setFilterColumnsVisible={setFilterColumnsVisible}
        sortObj={page}
      />
    </div>
  );
});
