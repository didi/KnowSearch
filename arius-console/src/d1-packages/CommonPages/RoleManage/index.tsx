import { Modal, message } from "antd";
import React, { useState, useEffect } from "react";
import { getFormCol, getTableCol, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryRoleList, queryRoleStatus, deleteRole } from "./service";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import Detail from "./detail";
import "./index.less";
import Progress from "../../CommonComponents/ProgressBar";
import { getCookie } from "lib/utils";
import { getUser } from "api/logi-security";
import { PERMISSION_TREE } from "constants/common";
import store from "../../../store";
import * as actions from "actions";
import { hasOpPermission } from "lib/permission";
import { RolePermissions } from "constants/permission";
import { ProTable } from "knowdesign";

export const RoleManage = () => {
  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [loading, setloading] = useState(false);
  const [formData, setFormData] = useState({} as any);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    position: "bottomRight",
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total: number) => `共 ${total} 条`,
  });
  const [roleId, setRoleId] = useState();
  const [data, setData] = useState([]);
  const [roleName, setRoleName] = useState("");

  const renderTitleContent = () => {
    return {
      title: "角色管理",
      content: null,
    };
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      hasOpPermission(RolePermissions.PAGE, RolePermissions.ADD) && {
        label: "新建角色",
        className: "ant-btn-primary",
        clickFunc: () => handleAdd(),
      },
    ].filter(Boolean);
  };

  const getOperationList = (row: any) => {
    return [
      {
        label: "编辑",
        invisible: row.isDefaultRole || !hasOpPermission(RolePermissions.PAGE, RolePermissions.EDIT),
        clickFunc: () => handleDetail(row, "update"),
      },
      {
        invisible: !hasOpPermission(RolePermissions.PAGE, RolePermissions.BIND),
        label: "绑定用户",
        clickFunc: () => handleDetail(row, "assign"),
      },
      {
        label: "删除",
        invisible: row.isDefaultRole || !hasOpPermission(RolePermissions.PAGE, RolePermissions.DELETE),
        clickFunc: () => onDelete(row),
      },
      {
        label: "回收用户",
        invisible: !hasOpPermission(RolePermissions.PAGE, RolePermissions.RECYCLE),
        clickFunc: () => handleDetail(row, "recycle"),
      },
    ];
  };

  const handleAdd = () => {
    setFlag("create");
    setDetailVisible(true);
  };

  const onDelete = (row: any) => {
    if (row.authedUserCnt) {
      const content = (
        <>
          <div className="mb-20">要删除角色{row.roleName}吗？</div>
          <div className="mb-20">角色{row.roleName}已分配给用户，不允许删除，请先解除分配的用户再试！</div>
        </>
      );
      Modal.info({
        title: "删除提示",
        icon: <ExclamationCircleOutlined />,
        content,
        okText: "确认",
        closable: true,
        width: 500,
      });
    } else {
      Modal.confirm({
        title: "删除提示",
        icon: <ExclamationCircleOutlined />,
        content: `要删除角色${row.roleName}吗？`,
        okText: "确认",
        cancelText: "取消",
        closable: true,
        onOk: async () => {
          await deleteRole(row.id);
          message.success("删除成功");
          fetchRoleList();
        },
      });
    }
  };

  const handleDetail = (row, type) => {
    setFlag(type || "detail");
    setRoleId(row.id);
    setDetailVisible(true);
    setRoleName(row.roleName);
  };

  const closeDetail = () => {
    setDetailVisible(false);
  };

  const handleSubmit = (data) => {
    const formData = {
      ...data,
    };
    for (const key in formData) {
      if (!formData[key]) {
        formData[key] = "";
      } else {
        formData[key] = formData[key].trim();
      }
    }
    setFormData(formData);
  };

  const onChangePagination = (current: any, pageSize: any) => {
    fetchRoleList({
      current,
      pageSize,
    });
  };

  const fetchRoleList = (customPagination: any = {}) => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      id: formData?.id ? Number(formData?.id) : undefined,
      page: customPagination.current ?? current,
      size: customPagination.pageSize ?? pageSize,
    };
    setloading(true);
    Progress.start();
    queryRoleList(params)
      .then((res: any) => {
        Progress.done();
        setloading(false);
        setData(res.bizData);
        setPagination((origin) => {
          return {
            ...origin,
            current: res.pagination.pageNo,
            pageSize: res.pagination.pageSize,
            total: res.pagination.total,
          };
        });
      })
      .finally(() => {
        Progress.done();
        setloading(false);
      });
  };

  const updateUserPermission = () => {
    if (flag !== "update") return;
    const userId = getCookie("userId");
    const userName = getCookie("userName");
    // 如果当前登录用户属于该角色，需要同步更新权限点

    if (data.find((item) => item.id === roleId)?.authedUsers?.includes(userName)) {
      getUser(+userId).then((res) => {
        window.localStorage.setItem(PERMISSION_TREE, JSON.stringify(res.permissionTreeVO?.childList || []));
        store.dispatch(actions.setUserPermissionTree(res.permissionTreeVO?.childList || []));
      });
    }
  };

  const submitCb = (needClose = true) => {
    if (needClose) {
      closeDetail();
    }
    fetchRoleList();
    updateUserPermission();
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderRoleDetail = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row, "detail");
        }}
      >
        {value}
      </a>
    );
  };

  const renderUserNum = (value, row) => {
    return (
      <div className="user-num">
        <span className="num">{value}</span>
      </div>
    );
  };

  const renderOptCol = (value: any, row: any) => {
    const btns = getOperationList(row);
    return renderTableOpts(btns, row);
  };

  useEffect(() => {
    fetchRoleList({ current: 1, pageSize: pagination.pageSize });
  }, [formData]);

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getFormCol(),
          // onChange={() => null}
          onReset: handleSubmit,
          onSearch: handleSubmit,
          isResetClearAll: true,
          initialValues: formData,
        }}
        tableProps={{
          tableId: "role_manager_table",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: data,
          getOpBtns: getOpBtns,
          columns: getTableCol(renderIndex, renderRoleDetail, renderUserNum, renderOptCol),
          paginationProps: { ...pagination, onChange: onChangePagination },
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
          reloadData: fetchRoleList,
        }}
      />
      {detailVisible ? (
        <Detail
          flag={flag}
          detailVisible={detailVisible}
          setDetailVisible={setDetailVisible}
          closeDetail={closeDetail}
          roleId={roleId}
          submitCb={submitCb}
          roleName={roleName}
        />
      ) : null}
    </div>
  );
};
