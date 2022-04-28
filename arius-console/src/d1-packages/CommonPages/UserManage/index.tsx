import React, { useState, useEffect, useContext } from "react";
import { getTableCol, getFormCol, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryUserList, queryDeptTreeData, queryRoleListByName } from "./service";
import { TreeSelect } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import Detail from "./detail";
import { debounce } from "lodash";
const { TreeNode } = TreeSelect;
import Progress from '../../CommonComponents/ProgressBar'

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
        label: "分配角色",
        clickFunc: () => {
          handleAssignRole(row);
        },
      },
    ];
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderUserNameCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.username}
      </a>
    );
  };

  const renderRealNameCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.realName}
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

  const fetchUserList = () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      page: current,
      size: pageSize,
    };
    console.log(params, "params");
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
    fetchDeptList();
    fetchRoleList();
  }, []);

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
        formData[key] = "";
      }
    }
    setFormData(formData);
  };

  const onChangePagination = (current: any, pageSize: any) => {
    setPagination((value) => {
      return {
        ...value,
        current,
        pageSize,
      };
    });
  };

  const submitCb = () => {
    closeDetail();
    fetchUserList();
  };

  React.useEffect(() => {
    fetchUserList();
  }, [formData, pagination.current, pagination.pageSize]);

  return (
    <div className="user-manage">
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <QueryForm
          {...getFormText}
          defaultCollapse
          columns={getFormCol(renderDeptItem(), renderRoleItem())}
          onChange={() => null}
          onReset={handleSubmit}
          onSearch={handleSubmit}
          initialValues={formData}
          isResetClearAll
        />
      </div>
      <div className="table-content">
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={data}
          columns={getTableCol(renderIndex, renderUserNameCol, renderRealNameCol, renderOptCol)}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
        {detailVisible ? (
          <Detail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} userId={userId} submitCb={submitCb} />
        ) : null}
      </div>
    </div>
  );
};
