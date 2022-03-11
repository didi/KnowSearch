import { Modal, message } from "antd";
import React, { useState, useEffect, useContext } from "react";
import { getFormCol, getTableCol, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryRoleList, queryRoleStatus, deleteRole } from "./service";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import Detail from "./detail";
import Progress from '../../CommonComponents/ProgressBar'

export const RoleManage = () => {
  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [loading, setloading] = useState(false);
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
  const [roleId, setRoleId] = useState();
  const [data, setData] = useState([]);

  const renderTitleContent = () => {
    return {
      title: "角色管理",
      content: null,
    };
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新建角色",
        className: "ant-btn-primary",
        clickFunc: () => handleAdd(),
      },
    ];
  };

  const getOperationList = (row: any) => {
    return [
      {
        label: "编辑",
        clickFunc: () => handleUpdate(row),
      },
      {
        label: "分配角色",
        clickFunc: () => handleAssignRole(row),
      },
      {
        label: "删除",
        clickFunc: () => onDelete(row),
      },
    ];
  };

  const handleAdd = () => {
    setFlag("create");
    setDetailVisible(true);
  };

  const handleUpdate = (row) => {
    setFlag("update");
    setRoleId(row.id);
    setDetailVisible(true);
  };

  const handleAssignRole = (row) => {
    setFlag("assign");
    setRoleId(row.id);
    setDetailVisible(true);
  };

  const onDelete = (row: any) => {
    setloading(true);
    queryRoleStatus(row.id).then((res) => {
      setloading(false);
      const { usernameList } = res;
      const tipNameStr = usernameList.length > 0 ? usernameList.join(",") : "";
      if (tipNameStr !== "") {
        const content = `角色[${row.roleName}]已被用户[${tipNameStr}]引用，请先解除引用关系再试。`;
        Modal.info({
          title: "删除提示",
          icon: <ExclamationCircleOutlined />,
          content,
          okText: "确认",
          closable: true,
        });
      } else {
        Modal.confirm({
          title: "删除提示",
          icon: <ExclamationCircleOutlined />,
          content: `要删除角色[${row.roleName}]吗？`,
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
    });
  };

  const handleDetail = (row) => {
    setFlag("detail");
    setRoleId(row.id);
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

  const fetchRoleList = () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      page: current,
      size: pageSize,
    };
    console.log(params, "params");
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
            total: res.pagination.total,
          };
        });
      })
      .finally(() => {
        Progress.done();
        setloading(false);
      });
  };

  const submitCb = () => {
    closeDetail();
    fetchRoleList();
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderRoleCodeCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.roleCode}
      </a>
    );
  };

  const renderRoleNameCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.roleName}
      </a>
    );
  };

  const renderOptCol = (value: any, row: any) => {
    const btns = getOperationList(row);
    return renderTableOpts(btns, row);
  };

  useEffect(() => {
    fetchRoleList();
  }, [formData, pagination.current, pagination.pageSize]);

  return (
    <div className="user-manage">
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <QueryForm
          {...getFormText}
          defaultCollapse
          columns={getFormCol()}
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
          getOpBtns={getOpBtns}
          columns={getTableCol(renderIndex, renderRoleCodeCol, renderRoleNameCol, renderOptCol)}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
        {detailVisible ? (
          <Detail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} roleId={roleId} submitCb={submitCb} />
        ) : null}
      </div>
    </div>
  );
};
