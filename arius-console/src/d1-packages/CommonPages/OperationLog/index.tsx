import React, { useState, useEffect, useContext } from "react";
import { getProjectColumns, getProjectQueryXForm, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryProjectList, switchProjectStatus, deleteProject, queryProjectStatus, queryDeptTreeData } from "./service";
import { ProjectDetail } from "./detail";
import { message, TreeSelect, Modal } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import GlobalState from "../../GlobalStore";
const { TreeNode } = TreeSelect;
export const OperationLog = () => {
  const { setState, project } = useContext(GlobalState) as any;
  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [formColumn, setFormColumn] = useState(getProjectQueryXForm());
  const [loading, setloading] = useState(false);
  const [formData, setFormData] = useState({
    projectCode: "",
    projectName: "",
    deptId: "",
    chargeUsername: "",
    isRunning: "",
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
        label: row.isRunning ? "启用" : " 禁用",
        clickFunc: () => {
          handleSwitchStatus(row);
        },
      },
      {
        needConfirm: false,
        label: "删除",
        clickFunc: () => {
          handleDelete(row);
        },
        confirmText: `确认删除${row.projectName}吗`,
      },
    ];
  };

  const getTableCols = () => {
    return [
      {
        title: "IP",
        dataIndex: "operatorIp",
        key: "operatorIp",
      },
      {
        title: "账号",
        dataIndex: "operatorUsername",
        key: "operatorUsername	",
      },
      {
        title: "时间",
        dataIndex: "createTime",
        key: "createTime",
      },
      {
        title: "操作类型",
        dataIndex: "operateType",
        key: "operateType",
      },
      {
        title: "对象分类",
        dataIndex: "targetType",
        key: "targetType",
      },
      {
        title: "操作对象",
        dataIndex: "target",
        key: "target",
      },
      {
        title: "操作项",
        dataIndex: "operation",
        key: "operation",
        render: (_value: any, row: any) => {
          return (
            <a
              type="javascript;"
              onClick={() => {
                handleDetail(row);
              }}
            >
              详细日志
            </a>
          );
        },
      },
    ];
  };

  const fetchProjectList = async () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      pageNo: current,
      pageSize,
    };
    setloading(true);
    queryProjectList(params)
      .then((res: any) => {
        if (res) {
          setData(res.bizData);
          // processIsOk;
          setState((slot) => {
            slot.globalMessage.processIsOk = true;
          });
        }
      })
      .finally(() => {
        setloading(false);
        setState((slot) => {
          slot.globalMessage.processIsOk = true;
        });
      });
  };

  const renderDeptTree = (list) => {
    return list.map((item) => {
      return (
        <TreeNode key={item.id} value={item.id} title={item.deptName}>
          {item.childList.length > 0 && renderDeptTree(item.childList)}
        </TreeNode>
      );
    });
  };

  const fetchDeptList = async () => {
    // const res = await queryDeptTreeData();
    // setFormColumn((formColumn) => {
    //   return formColumn.map((item) => {
    //     return item.dataIndex === "deptId"
    //       ? {
    //           ...item,
    //           component: (
    //             <TreeSelect
    //               showSearch
    //               style={{ width: "100%" }}
    //               dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
    //               placeholder="请选择使用部门"
    //               allowClear
    //             >
    //               {renderDeptTree(res)}
    //             </TreeSelect>
    //           ),
    //         }
    //       : {
    //           ...item,
    //         };
    //   });
    // });
  };

  const renderTitleContent = () => {
    return {
      title: "操作日志",
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

  const handleDelete = (row: any) => {
    queryProjectStatus(row.id).then((res) => {
      if (res) {
        const content = `项目${row.projectName}已被服务${res.serverName}引用，请先解除引用关系再试`;
        Modal.confirm({
          title: "删除提示",
          icon: null,
          content,
          okText: "确认",
          cancelText: "取消",
          closable: true,
          onOk: async () => {
            const result = await deleteProject(row.id);
            if (result) {
              message.success("删除成功");
              fetchProjectList();
            }
          },
        });
      }
    });
  };

  const handleSwitchStatus = (row: { isRunning: any; id: number }) => {
    const msg = row.isRunning ? "禁用成功" : "启用禁用";
    switchProjectStatus(row.id).then((res: any) => {
      if (res) {
        message.success(msg);
        fetchProjectList();
      }
    });
  };

  const closeDetail = () => {
    setDetailVisible(false);
  };

  const handleSubmit = (formData) => {
    console.log(formData);
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
    fetchProjectList();
  };

  useEffect(() => {
    fetchDeptList();
  }, []);

  useEffect(() => {
    fetchProjectList();
  }, [pagination, formData, project]);

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
          columns={getTableCols()}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
      </div>
      {detailVisible ? (
        <ProjectDetail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} id={projectId} submitCb={submitCb} />
      ) : null}
    </>
  );
};
