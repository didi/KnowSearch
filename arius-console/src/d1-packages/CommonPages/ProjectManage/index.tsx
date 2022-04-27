import React, { useState, useEffect } from "react";
import { getProjectColumns, getProjectQueryXForm, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryProjectList, switchProjectStatus, deleteProject, queryProjectStatus, queryDeptTreeData } from "./service";
import { ProjectDetail } from "./detail";
import { message, TreeSelect, Modal } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import { ExclamationCircleOutlined } from "@ant-design/icons";
const { TreeNode } = TreeSelect;
export const ProjectList = () => {
  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [deptList, setDeptList] = useState([]);
  const [formColumn, setFormColumn] = useState([]);
  const [loading, setloading] = useState(false);
  const [formData, setFormData] = useState({
    // projectCode: "",
    // projectName: "",
    // deptId: "",
    // chargeUsername: "",
    // running: "",
  });
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    position: "bottomRight",
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total: number) => `共 ${total} 条`,
  });
  const [projectId, setProjectId] = useState();
  const [data, setData] = useState([]);
  const getOperationList = (row: any) => {
    return [
      {
        label: "编辑",
        clickFunc: () => {
          handleUpdate(row);
        },
      },
      {
        label: !row.running ? "启用" : "停用",
        clickFunc: () => {
          handleSwitchStatus(row);
        },
      },
      {
        label: "删除",
        clickFunc: () => {
          onDelete(row);
        },
      },
    ];
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderProjectCodeCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.projectCode}
      </a>
    );
  };

  const renderProjectNameCol = (value, row) => {
    return (
      <a
        type="javascript;"
        onClick={() => {
          handleDetail(row);
        }}
      >
        {row.projectName}
      </a>
    );
  };

  const renderOptsCol = (_value: any, row: any) => {
    const btns = getOperationList(row);
    return renderTableOpts(btns, row);
  };

  const fetchProjectList = async () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      page: current,
      size: pageSize,
    };
    setloading(true);
    queryProjectList(params)
      .then((res: any) => {
        setloading(false);
        if (res) {
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
        setloading(false);
      });
  };

  const fetchDeptList = async () => {
    const res: any = await queryDeptTreeData();
    setDeptList(res.childList);
  };

  const renderDeptTreeItem = (list) => {
    return list.map((item) => {
      return (
        <TreeNode key={item.id} value={item.id} title={item.deptName}>
          {item.childList && item.childList.length > 0 && renderDeptTreeItem(item.childList)}
        </TreeNode>
      );
    });
  };

  const renderDeptTree = () => {
    return (
      <TreeSelect
        treeNodeFilterProp="title"
        showSearch
        style={{ width: "100%" }}
        dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
        placeholder="请选择使用部门"
        allowClear
      >
        {renderDeptTreeItem(deptList)}
      </TreeSelect>
    );
  };

  const renderTitleContent = () => {
    return {
      title: "项目配置",
      content: null,
    };
  };

  const handleAdd = () => {
    setFlag("create");
    setDetailVisible(true);
    setProjectId(null);
  };

  const handleUpdate = (row: any) => {
    setFlag("update");
    setDetailVisible(true);
    setProjectId(row.id);
  };

  const handleDetail = (row) => {
    setFlag("detail");
    setDetailVisible(true);
    setProjectId(row.id);
  };

  const onDelete = (row: any) => {
    setloading(true);
    queryProjectStatus(row.id).then((res) => {
      setloading(false);
      const { serviceNameList, resourceNameList } = res;
      const tipNameStr = serviceNameList ? serviceNameList.join(",") : resourceNameList ? resourceNameList.join(",") : "";
      if (tipNameStr !== "") {
        const content = `项目${row.projectName}已被服务[${tipNameStr}]引用，请先解除引用关系再试。`;
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
          content: `要删除项目[${row.projectName}]吗？`,
          okText: "确认",
          cancelText: "取消",
          closable: true,
          onOk: async () => {
            await deleteProject(row.id);
            message.success("删除成功");
            fetchProjectList();
          },
        });
      }
    });
  };

  const handleSwitchStatus = async (row: { running: any; id: number }) => {
    const msg = row.running ? "停用成功" : "启用成功";
    await switchProjectStatus(row.id);
    message.success(msg);
    fetchProjectList();
  };

  const closeDetail = () => {
    setDetailVisible(false);
  };

  const handleSubmit = (formData) => {
    const data = {
      ...formData,
    };
    for (const k in data) {
      if (data[k] === "" || data[k] === undefined) {
        delete data[k];
      }
    }
    setFormData(data);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新建项目",
        className: "ant-btn-primary",
        clickFunc: () => handleAdd(),
      },
    ];
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

  const refreshList = () => {
    closeDetail();
    fetchProjectList();
  };

  useEffect(() => {
    fetchDeptList();
  }, []);

  useEffect(() => {
    const data = getProjectQueryXForm(renderDeptTree);
    setFormColumn(data);
  }, [deptList]);

  useEffect(() => {
    fetchProjectList();
  }, [formData, pagination.current, pagination.pageSize]);

  return (
    <>
      <div className="table-header">
        <RenderTitle {...renderTitleContent()} />
        <QueryForm
          {...getFormText}
          defaultCollapse
          columns={formColumn}
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
          columns={getProjectColumns(renderIndex, renderProjectCodeCol, renderProjectNameCol, renderOptsCol)}
          getOpBtns={getOpBtns}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
      </div>
      {detailVisible ? (
        <ProjectDetail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} id={projectId} refreshList={refreshList} />
      ) : null}
    </>
  );
};
