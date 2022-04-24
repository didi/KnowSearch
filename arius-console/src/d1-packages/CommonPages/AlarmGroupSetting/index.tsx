import React, { useState, useEffect, useContext } from "react";
import { getFormCol, getTableCol, getFormText } from "./config";
import { DTable, ITableBtn } from "../../dantd/DTable";
import { RenderTitle } from "../RenderTitle";
import QueryForm from "../../ProForm/QueryForm";
import { queryAlarmSettingList, switchAlarmSettingStatus, deleteAlarmSetting, queryAlarmSettingStatus } from "./service";
import Detail from "./detail";
import { message, Modal } from "antd";
import { renderTableOpts } from "../../ProTable/RenderTableOpts";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import GlobalStore from "../../GlobalStore";
import Progress from '../../CommonComponents/ProgressBar'
export const AlarmGroupSetting = () => {
  const { project } = useContext(GlobalStore) as any;

  const [flag, setFlag] = useState("");
  const [detailVisible, setDetailVisible] = useState(false);
  const [loading, setloading] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    members: "",
    operator: "",
    status: undefined,
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
  const [id, setAlarmSettingId] = useState();
  const [appId, setAppId] = useState();
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
        label: row.status === 0 ? "启用" : " 停用",
        clickFunc: () => {
          handleSwitchStatus(row);
        },
      },
      {
        label: "删除",
        clickFunc: () => {
          handleDelete(row);
        },
      },
    ];
  };

  const renderIndex = (value, row, index) => {
    return `${(pagination.current - 1) * pagination.pageSize + (index + 1)}`;
  };

  const renderNameCol = (value, row) => {
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

  const renderOptsCol = (value: any, row: any) => {
    const btns = getOperationList(row);
    return renderTableOpts(btns, row);
  };

  const fetchAlarmSettingList = () => {
    const { current, pageSize } = pagination;
    const params = {
      ...formData,
      appId,
      pageNo: current,
      pageSize,
    };
    setloading(true);
    Progress.start();
    queryAlarmSettingList(params)
      .then((res: any) => {
        Progress.done();
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

  const renderTitleContent = () => {
    return {
      title: "告警组配置",
      content: null,
    };
  };

  const handleAdd = () => {
    setFlag("create");
    setDetailVisible(true);
    setAlarmSettingId(null);
  };

  const handleUpdate = (row: any) => {
    setFlag("update");
    setDetailVisible(true);
    setAlarmSettingId(row.id);
  };

  const handleDetail = (row) => {
    setFlag("detail");
    setDetailVisible(true);
    setAlarmSettingId(row.id);
  };

  const handleDelete = (row: any) => {
    setloading(true);
    queryAlarmSettingStatus(row.id).then((res) => {
      setloading(false);
      const tipNameStr = res.legnth > 0 ? res.join(",") : "";
      if (tipNameStr) {
        Modal.info({
          title: "删除提示",
          icon: <ExclamationCircleOutlined />,
          content: `告警组[${row.name}]已被告警策略[${tipNameStr}]引用，请先解除引用关系再试。`,
          okText: "确认",
          closable: true,
        });
      } else {
        Modal.confirm({
          title: "删除提示",
          icon: <ExclamationCircleOutlined />,
          content: `要删除告警组[${row.name}]吗？`,
          closable: true,
          okText: "确认",
          cancelText: "取消",
          onOk: async () => {
            const result = await deleteAlarmSetting(row.id);
            if (result) {
              message.success("删除成功");
              fetchAlarmSettingList();
            }
          },
        });
      }
    });
  };

  const handleSwitchStatus = async (row: { status: number; id: number }) => {
    const msg = row.status === 1 ? "停用成功" : "启用成功";
    const status = row.status === 1 ? 0 : 1;
    await switchAlarmSettingStatus(row.id, status);
    message.success(msg);
    fetchAlarmSettingList();
  };

  const closeDetail = () => {
    setDetailVisible(false);
  };

  const handleSubmit = (formData) => {
    console.log(formData, "formData");

    setFormData(formData);
  };

  const getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新建告警组",
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

  const submitCb = () => {
    closeDetail();
    fetchAlarmSettingList();
  };

  useEffect(() => {
    fetchAlarmSettingList();
  }, [formData, pagination.current, pagination.pageSize]);

  useEffect(() => {
    setAppId(project.id);
  }, [project]);

  useEffect(() => {
    fetchAlarmSettingList();
  }, [appId]);

  return (
    <>
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
          columns={getTableCol(renderIndex, renderNameCol, renderOptsCol)}
          getOpBtns={getOpBtns}
          paginationProps={{ ...pagination, onChange: onChangePagination }}
        />
      </div>
      {detailVisible ? (
        <Detail flag={flag} detailVisible={detailVisible} closeDetail={closeDetail} id={id} appId={appId} submitCb={submitCb} />
      ) : null}
    </>
  );
};
