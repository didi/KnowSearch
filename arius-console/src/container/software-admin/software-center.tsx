import React, { useState, useEffect } from "react";
import { getSoftwareCenterXForm, getSoftwareColumns } from "./config";
import { RenderTitle } from "component/render-title";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";
import ProTable from "../../d1-packages/ProTable";
import { ITableBtn } from "component/dantd/dtable";
import { hasOpPermission } from "lib/permission";
import { SoftwareCenterPermissions } from "constants/permission";
import { getSoftwareList, getScriptNameList } from "api/software-admin";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

export const SoftwareCenter = connect(
  null,
  mapDispatchToProps
)((props: any) => {
  const department: string = localStorage.getItem("current-project");
  const [loading, setloading] = useState(false);
  const [data, setData] = useState([]);
  const [queryFormObject, setQueryFormObject] = useState({ page: 1, size: 10 } as any);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    reloadData();
  }, [department, queryFormObject]);

  const reloadData = async () => {
    setloading(true);
    let res = await getSoftwareList(queryFormObject);
    setData(res?.bizData);
    setTotal(res?.pagination?.total);
    setloading(false);
  };

  const renderTitleContent = () => {
    return {
      title: "软件中心",
      content: null,
    };
  };

  const handleSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, size: queryFormObject.size, page: 1 });
  };

  const resetSubmit = (result) => {
    for (var key in result) {
      if (result[key] === "" || result[key] === undefined) {
        delete result[key];
      }
    }
    setQueryFormObject({ ...result, page: 1, size: queryFormObject.size });
  };

  const handleChange = (pagination) => {
    setQueryFormObject({ ...queryFormObject, page: pagination.current, size: pagination.pageSize });
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(SoftwareCenterPermissions.PAGE, SoftwareCenterPermissions.ADD) && {
        label: "新建软件",
        className: "ant-btn-primary",
        clickFunc: async () => {
          let res = await getScriptNameList();
          let scriptList = (res || []).map((item) => ({ value: item?.id, label: item?.name }));
          props.setDrawerId("addSoftwareDrawer", { addSoftware: true, scriptList }, reloadData);
        },
      },
    ].filter(Boolean);
  };
  return (
    <>
      <div className="table-layout-style">
        <ProTable
          showQueryForm={true}
          queryFormProps={{
            defaultCollapse: true,
            columns: getSoftwareCenterXForm(),
            onReset: resetSubmit,
            onSearch: handleSubmit,
            isResetClearAll: true,
            showCollapseButton: false,
          }}
          tableProps={{
            isCustomPg: false,
            loading,
            rowKey: "id",
            dataSource: data,
            columns: getSoftwareColumns(props.setDrawerId, reloadData),
            reloadData,
            getOpBtns,
            isDividerHide: false,
            customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
            paginationProps: {
              position: "bottomRight",
              showQuickJumper: true,
              total: total,
              showSizeChanger: true,
              pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
              showTotal: (total) => `共 ${total} 条`,
              current: queryFormObject.page,
              pageSize: queryFormObject.size,
            },
            attrs: {
              onChange: handleChange,
              scroll: {
                x: "max-content",
              },
            },
          }}
        />
      </div>
    </>
  );
});
