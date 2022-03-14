import React, { useState, useEffect, useContext } from "react";
import { getProjectColumns, getProjectQueryXForm, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryMonitorRules, switchMonitorRuleStatus, deleteMonitorRule, queryProjectStatus, queryDeptTreeData } from "./service";
import { AlarmStrategyDetail } from "./detail";
import { message, TreeSelect, Modal } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import { renderTableLabels } from "../../ProTable/RenderTableLabels";
import { CheckCircleFilled, MinusCircleFilled } from "@ant-design/icons";
import { formatDate } from "../../Utils/tools";
import GlobalState from "../../GlobalStore";
const { TreeNode } = TreeSelect;
export const AlarmStrategy = () => {
  const { setState, project } = useContext(GlobalState) as any;
  const [flag, setFlag] = useState(""); //create：新建，update，编辑，detail:查看
  const [detailVisible, setDetailVisible] = useState(false); //抽屉显示隐藏
  const [formColumn, setFormColumn] = useState(getProjectQueryXForm()); //获取头部筛选项相关配置
  const [loading, setloading] = useState(false); //LODING
  const [formData, setFormData] = useState({
    projectCode: "",
    projectName: "",
    deptId: "",
    chargeUsername: "",
    isRunning: "",
  });
  //筛选字段默认值

  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    position: "bottomRight",
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total: number) => `共 ${total} 条`,
  });
  //表格分页相关
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
        label: row.status === 0 ? "启用" : " 禁用",
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
  //表格相关
  const getTableCols = () => {
    return [
      {
        title: "序号",
        dataIndex: "index",
        key: "index",
      },
      {
        title: "告警级别",
        dataIndex: "priority",
        key: "priority",
        render: (_value: string | number, row: { priority: string }): any => {
          return (
            <a
              type="javascript;"
              onClick={() => {
                handleDetail(row);
              }}
            >
              {row.priority}
            </a>
          );
        },
      },
      {
        title: "告警名称",
        dataIndex: "name",
        key: "name	",
        render: (_value: string | number, row: { name: string }): any => {
          return (
            <a
              type="javascript;"
              onClick={() => {
                handleDetail(row);
              }}
            >
              {row.name}
            </a>
          );
        },
      },
      {
        title: "告警对象",
        dataIndex: "objectNames",
        key: "objectNames",
        render: (_value: any, _row: { objectNames: [] }) =>
          renderTableLabels({
            list: _row.objectNames,
            limit: 2,
          }),
      },
      {
        title: "最后修改人",
        dataIndex: "operator",
        key: "operator",
      },
      {
        title: "最近更新时间",
        dataIndex: "updateTime",
        key: "updateTime",
        // render: (_value: any) => {
        //   formatDate(_value, "YYYY-MM-DD HH:mm:ss");
        // },
      },
      {
        title: "状态",
        dataIndex: "status",
        key: "status",
        // eslint-disable-next-line react/display-name
        render: (_value: boolean) => {
          return (
            <span>
              {_value ? (
                <>
                  <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
                  <span>启用</span>
                </>
              ) : (
                <>
                  <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
                  <span>停用</span>
                </>
              )}
            </span>
          );
        },
      },
      {
        title: "操作",
        dataIndex: "operation",
        key: "operation",
        render: (_value: any, row: any) => {
          const btns = getOperationList(row);
          return renderTableOpts(btns, row);
        },
      },
    ];
  };
  //获取表格列表的数据
  const fetchMonitorRules = async () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      pageNo: current,
      pageSize,
    };
    setloading(true);
    queryMonitorRules(params)
      .then((res: any) => {
        if (res) {
          setData(res.bizData);
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
    const res = await queryDeptTreeData();
    setFormColumn((formColumn) => {
      return formColumn.map((item) => {
        return item.dataIndex === "deptId"
          ? {
              ...item,
              component: (
                <TreeSelect
                  showSearch
                  style={{ width: "100%" }}
                  dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
                  placeholder="请选择使用部门"
                  allowClear
                >
                  {renderDeptTree(res)}
                </TreeSelect>
              ),
            }
          : {
              ...item,
            };
      });
    });
  };

  const renderTitleContent = () => {
    return {
      title: "告警策略",
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
    const content = `要删除告警规则${row.name}吗？`;
    Modal.confirm({
      title: "删除提示",
      icon: null,
      content,
      okText: "确认",
      cancelText: "取消",
      closable: true,
      onOk: async () => {
        const result = await deleteMonitorRule(row.id);
        message.success("删除成功");
        fetchMonitorRules();
      },
    });
  };

  const handleSwitchStatus = (row: { isRunning: any; id: number; status: number }) => {
    const msg = row.isRunning ? "禁用成功" : "启用禁用";
    const params = {
      id: row.id,
      status: Number(!row.status),
    };
    switchMonitorRuleStatus(params).then((res: any) => {
      message.success(msg);
      fetchMonitorRules();
    });
  };

  const closeDetail = () => {
    setDetailVisible(false);
  };

  const handleSubmit = (formData) => {
    console.log(formData);
    setFormData(formData);
  };
  //页面标题名字
  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新建告警策略",
        className: "ant-btn-primary",
        clickFunc: () => handleAdd(),
      },
    ];
  };
  //切换分页
  const onChangePagination = (current: any, pageSize: any) => {
    setPagination((value) => {
      return {
        ...value,
        current,
        pageSize,
      };
    });
  };
  //提交信息，关闭弹窗
  const submitCb = () => {
    closeDetail();
    fetchMonitorRules();
  };

  useEffect(() => {
    fetchDeptList();
  }, []);

  useEffect(() => {
    fetchMonitorRules();
  }, [pagination, formData]);

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
          getOpBtns={getOpBtns}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
      </div>
      {detailVisible ? (
        <AlarmStrategyDetail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} id={projectId} submitCb={submitCb} />
      ) : null}
    </>
  );
};
