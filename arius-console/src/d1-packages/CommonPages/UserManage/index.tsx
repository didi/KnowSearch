import React, { useState, useEffect } from "react";
import { getTableCol, getFormCol, getFormText } from "./config";
import { DTable } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryUserList, queryDeptTreeData, queryRoleListByName, deleteUser } from "./service";
import { Modal, TreeSelect, message } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import Detail from "./detail";
const { TreeNode } = TreeSelect;
import Progress from "../../CommonComponents/ProgressBar";
import { hasOpPermission } from "lib/permission";
import { UserPermissions } from "constants/permission";
import { ProTable } from "knowdesign";

export const UserManage = () => {
  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [loading, setloading] = useState(false);
  const [deptList, setDeptList] = useState([]);
  const [roleList, setRoleList] = useState([]);
  const [formData, setFormData] = useState({});

  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    position: "bottomRight",
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total: number) => `共 ${total} 条`,
  });

  const [userId, setUserId] = useState();
  const [data, setData] = useState([]);
  const getOperationList = (row: any) => {
    return [
      {
        invisible: !hasOpPermission(UserPermissions.PAGE, UserPermissions.ASSGIN),
        label: "分配角色",
        clickFunc: () => {
          handleAssignRole(row);
        },
      },
      {
        invisible: !hasOpPermission(UserPermissions.PAGE, UserPermissions.ASSGIN),
        label: "删除",
        clickFunc: () => {
          if (row.singleOwnerOfProjects?.length) {
            return Modal.info({
              title: "提示",
              content: (
                <>
                  <div>此用户是以下应用的唯一责任人，删除用户前需下线应用：</div>
                  <div>{row.singleOwnerOfProjects.join("、")}</div>
                </>
              ),
            });
          }
          Modal.confirm({
            title: "提示",
            content: row.ownProjects?.length ? (
              <>
                <div>用户是以下应用的责任人，是否确定删除？</div>
                <div>{row.ownProjects.join("、")}</div>
              </>
            ) : (
              `确认删除${row.userName || ""}`
            ),
            onOk: () => {
              deleteUser(row.id).then(() => {
                message.success("删除成功");
                fetchUserList();
              });
            },
          });
        },
      },
    ];
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderUserNameCol = (value, row) => {
    if (!value) return "-";
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {value}
      </a>
    );
  };

  const renderOptCol = (value: any, row: any) => {
    const btns = getOperationList(row);
    return renderTableOpts(btns, row);
  };

  const handleAssignRole = (row) => {
    setFlag("update");
    setDetailVisible(true);
    setUserId(row.id);
  };

  const fetchUserList = (customPagination: any = {}) => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      page: customPagination.current ?? current,
      size: customPagination.pageSize ?? pageSize,
    };
    Progress.start();
    setloading(true);
    queryUserList(params)
      .then((res: any) => {
        if (res) {
          Progress.done();
          setData(res.bizData);
          setPagination((origin) => {
            return {
              ...origin,
              current: res.pagination.pageNo,
              pageSize: res.pagination.pageSize,
              total: res.pagination.total,
            };
          });
        }
      })
      .finally(() => {
        Progress.done();
        setloading(false);
      });
  };

  const fetchDeptList = async (val?) => {
    const res = await queryDeptTreeData(val);
    setDeptList(res.childList);
  };

  const fetchRoleList = async (val?) => {
    const res: any = await queryRoleListByName(val);
    const roleList = res.map((item) => ({ value: item.id, title: item.roleName }));
    setRoleList(roleList);
  };

  const renderRoleItem = () => {
    const roleItem = {
      options: roleList,
      componentProps: {
        // onSearch: debounce(fetchRoleList, 1000),
      },
    };
    return roleItem;
  };

  const renderDeptTree = (list) => {
    return (
      list.length > 0 &&
      list.map((item: any) => (
        <TreeNode key={item.id} value={item.id} title={item.deptName}>
          {item.childList && item.childList.length > 0 && renderDeptTree(item.childList)}
        </TreeNode>
      ))
    );
  };

  const renderDeptItem = () => {
    const deptItem = {
      component: (
        <TreeSelect
          showSearch
          style={{ width: "100%" }}
          treeNodeFilterProp="title"
          dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
          placeholder="请选择使用部门"
          allowClear
        >
          {renderDeptTree(deptList)}
        </TreeSelect>
      ),
    };
    return deptItem;
  };

  useEffect(() => {
    renderRoleItem();
  }, [roleList]);

  useEffect(() => {
    renderDeptItem();
  }, [deptList]);

  const renderTitleContent = () => {
    return {
      title: "用户管理",
      content: null,
    };
  };

  const handleDetail = (row) => {
    setFlag("detail");
    setUserId(row.id);
    setDetailVisible(true);
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
        formData[key] = undefined;
      } else {
        formData[key] = formData[key].trim();
      }
    }
    setFormData(formData);
  };

  const onChangePagination = (current: any, pageSize: any) => {
    fetchUserList({
      current,
      pageSize,
    });
  };

  const submitCb = () => {
    closeDetail();
    fetchUserList();
  };

  React.useEffect(() => {
    fetchUserList({ current: 1, pageSize: pagination.pageSize });
  }, [formData]);

  return (
    <div className="table-layout-style">
      <ProTable
        showQueryForm={true}
        queryFormProps={{
          defaultCollapse: true,
          columns: getFormCol(renderDeptItem(), renderRoleItem()),
          // onChange={() => null}
          onReset: handleSubmit,
          onSearch: handleSubmit,
          isResetClearAll: true,
          initialValues: formData,
        }}
        tableProps={{
          tableId: "user_manager_table",
          isCustomPg: false,
          loading,
          rowKey: "id",
          dataSource: data,
          columns: getTableCol(renderIndex, renderUserNameCol, renderOptCol),
          paginationProps: { ...pagination, onChange: onChangePagination },
          customRenderSearch: () => <RenderTitle {...renderTitleContent()} />,
        }}
      />
      {detailVisible ? (
        <Detail
          flag={flag}
          detailVisible={detailVisible}
          closeDetail={closeDetail}
          setDetailVisible={setDetailVisible}
          userId={userId}
          submitCb={submitCb}
        />
      ) : null}
    </div>
  );
};
