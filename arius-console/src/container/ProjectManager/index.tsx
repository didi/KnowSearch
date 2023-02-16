import React, { useState } from "react";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getProjectColumns, getProjectQueryXForm } from "./config";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { RenderTitle } from "component/render-title";
import QueryForm from "component/dantd/query-form";
import { queryFormText } from "constants/status-map";
import { getProjectList } from "api/app-api";
import { IPermission } from "store/type";
import { ProjectPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { getCookie } from "lib/utils";
import { ProTable } from "knowdesign";
const mapStateToProps = (state: any) => ({
  isAdminUser: state.user.isAdminUser,
  permissionTree: state.user.permissionTree,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: any) => dispatch(actions.setDrawerId(modalId, params, cb)),
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

export const ProjectList = connect(
  mapStateToProps,
  mapDispatchToProps
)((props: { permissionTree: IPermission[]; setDrawerId: any; setModalId: any; isAdminUser: boolean }) => {
  const [loading, setloading] = useState(false);
  const [queryFormObject, setqueryFormObject] = useState({} as any);
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  React.useEffect(() => {
    reloadData({ pageNo: 1 });
  }, [queryFormObject]);

  const reloadData = (param: { pageNo?: number; pageSize?: number } = {}) => {
    let { pageNo, pageSize } = param;
    setloading(true);

    pageNo = pageNo || pagination.current;
    pageSize = pageSize || pagination.pageSize;
    if (queryFormObject.searchType) {
      queryFormObject.searchType = +queryFormObject.searchType;
    }
    const params = {
      ...queryFormObject,
      page: pageNo,
      size: pageSize,
    };
    getProjectList(params)
      .then((res) => {
        if (res) {
          let isAdmin = getCookie("isAdminUser");
          const list = (res.bizData || []).map((item) => {
            const ownerIdList = [];
            const userIdList = [];
            item.ownersList = (item.ownerList || []).map((row) => {
              ownerIdList.push(row.id);
              return {
                label: row.userName,
                value: row.id,
              };
            });
            item.usersList = (isAdmin === "yes" ? item.userListWithBelongProjectAndAdminRole : item.userList || []).map((row) => {
              userIdList.push(row.id);
              return {
                label: row.userName,
                title: row.id,
                key: row.id,
              };
            });
            return {
              ...item,
              userIdList,
              ownerIdList,
            };
          });

          setData(list);
          setPagination({
            ...pagination,
            current: res.pagination.pageNo,
            pageSize: res.pagination.pageSize,
            total: res.pagination.total,
          });
        }
      })
      .finally(() => {
        setloading(false);
      });
  };

  const renderTitleContent = () => {
    return {
      title: "应用管理",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    for (const key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setqueryFormObject(result);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(ProjectPermissions.PAGE, ProjectPermissions.ADD) && {
        label: "新建应用",
        className: "ant-btn-primary",
        clickFunc: () => props.setDrawerId("addOrEditProjectModal", null, reloadData),
      },
    ].filter(Boolean);
  };

  const handleChange = (pagination, filters, sorter) => {
    reloadData({ pageNo: pagination.current, pageSize: pagination.pageSize });
  };

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getProjectQueryXForm(),
          // onChange={() => null}
          onReset: handleSubmit,
          onSearch: handleSubmit,
          isResetClearAll: true,
        }}
        tableProps={{
          tableId: "project_manager_table",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: data,
          columns: getProjectColumns({
            setDrawerId: props.setDrawerId,
            setModalId: props.setModalId,
            reloadData,
            isAdminUser: props.isAdminUser,
            pagination,
          }),
          reloadData,
          getOpBtns,
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          paginationProps: pagination,
          attrs: {
            onChange: handleChange,
          },
        }}
      />
    </div>
  );
});
