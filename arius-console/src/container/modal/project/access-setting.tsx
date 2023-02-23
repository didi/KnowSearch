/* eslint-disable react/display-name */
import * as React from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Button, Drawer, message, Modal, Select, Table, Tooltip, Input, Form, Switch } from "antd";
import { SEARCH_TYPE_MAP } from "container/ProjectManager/config";
import { createAppByProjectId, deleteOneAppByProjectId, getAppByProjectId, updateAppByProjectId } from "api";
import { getEsUserPrimitiveList, getEsUserList, setDefaultDisplay } from "api/cluster-api";
import { renderOperationBtns } from "container/custom-component";
import { filterOption } from "lib/utils";
import { regNonnegativeInteger } from "constants/reg";
import { XModal } from "component/x-modal";
import { cellStyle } from "constants/table";
import { XNotification } from "component/x-notification";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const searchTypeOptions = [
  { value: 0, label: "集群模式" },
  { value: 1, label: "索引模式" },
  { value: 2, label: "原生模式" },
];

const AccessSetting = (props: { dispatch: any; cb: any; params: any }) => {
  const { params = {} } = props;
  const [list, setList] = React.useState([]);
  const [modalVisible, setModalVisible] = React.useState(false);
  const [primitiveList, setPrimitiveList] = React.useState([]);
  const [clusterList, setClusterList] = React.useState([]);
  const [searchType, setSearchType] = React.useState(1);
  const opType = React.useRef("add");
  const currentRecord = React.useRef({} as any);
  const [form] = Form.useForm();

  React.useEffect(() => {
    getData();
    getClusterList();
  }, []);

  const getData = () => {
    getAppByProjectId(params.id).then((res) => {
      setList(res);
    });
  };

  const getClusterList = async () => {
    let primitive = await getEsUserPrimitiveList(params.id);
    let cluster = await getEsUserList(params.id);
    let clusterList = (cluster || []).map((item) => ({ value: item, label: item }));
    let primitiveList = (primitive || []).map((item) => ({ value: item, label: item }));
    setClusterList(clusterList);
    setPrimitiveList(primitiveList);
  };

  const getColumns: any = (list) => {
    return [
      {
        title: "ES_User",
        dataIndex: "id",
        key: "id",
        width: 90,
      },
      {
        title: "检验码",
        dataIndex: "verifyCode",
        key: "verifyCode",
        width: 150,
      },
      {
        title: "访问模式",
        dataIndex: "searchType",
        key: "searchType",
        width: 90,
        render: (text: number) => {
          return <>{SEARCH_TYPE_MAP[text]}</>;
        },
      },
      {
        title: "访问集群",
        dataIndex: "cluster",
        key: "cluster",
        width: 150,
        onCell: () => ({
          style: { ...cellStyle, maxWidth: 150 },
        }),
        render: (val: string) => <Tooltip title={val || "-"}>{val || "-"}</Tooltip>,
      },
      {
        title: "应用默认的ES_User",
        dataIndex: "defaultDisplay",
        key: "defaultDisplay",
        width: 130,
        render: (val: boolean, record) => {
          return (
            <div className="project-default-display">
              <Switch
                size="small"
                checked={val}
                disabled={val}
                onClick={() =>
                  XModal({
                    type: "info",
                    title: `确认应用${record.id}为默认的ES_User？`,
                    onOk: async () => {
                      await setDefaultDisplay(params.id, record.id);
                      message.success("操作成功");
                      getData();
                    },
                  })
                }
              />
            </div>
          );
        },
      },
      {
        title: "操作",
        dataIndex: "operation",
        filterTitle: true,
        key: "operation",
        width: 120,
        render: (text: string, record: any) => {
          const btns = [
            {
              clickFunc: async () => {
                onClickAddOrEdit("edit", record);
              },
              label: "编辑",
            },
            {
              clickFunc: () => {
                if (list.length === 1) {
                  return;
                }
                Modal.confirm({
                  title: `确认删除ES_User ${record.id}？`,
                  content: <span style={{ color: "red" }}>请确认影响后再进行删除操作！</span>,
                  onOk: () => {
                    deleteOneAppByProjectId(params.id, record.id).then((res) => {
                      message.success("删除成功");
                      getData();
                    });
                  },
                });
              },
              label:
                list.length === 1 ? (
                  <Tooltip title="至少保留一条ES_User">
                    <span style={{ color: "gray", cursor: "not-allowed" }}>删除</span>
                  </Tooltip>
                ) : (
                  <span>删除</span>
                ),
            },
          ];
          return renderOperationBtns(btns, record);
        },
      },
    ];
  };

  const onCancel = () => {
    props.dispatch(actions.setDrawerId(""));
  };

  const onSearchTypeChange = (searchType: number) => {
    setSearchType(searchType);
    if (searchType === 0 && !clusterList.length) {
      form.setFieldsValue({ selectCluster: undefined });
      XNotification({ type: "error", message: `该应用下无可用集群，无法新增集群模式的ES_User` });
      return;
    } else if (searchType === 2 && !primitiveList.length) {
      form.setFieldsValue({ selectCluster: undefined });
      XNotification({ type: "error", message: `该应用下无独立类型集群，无法新增原生模式的ES_User` });
      return;
    }
    opType.current === "add"
      ? form.setFieldsValue({ selectCluster: searchType === 0 ? clusterList?.[0]?.value : primitiveList?.[0]?.value || undefined })
      : form.setFieldsValue({ selectCluster: currentRecord?.current?.cluster || undefined });
  };

  const onClickAddOrEdit = (type: string, record?: any) => {
    opType.current = type;
    currentRecord.current = record || {};
    if (type === "edit") {
      form.setFieldsValue({
        searchType: record?.searchType,
        selectCluster: record?.cluster || clusterList?.[0]?.value || undefined,
        queryThreshold: record?.queryThreshold || undefined,
      });
      setSearchType(record?.searchType);
    } else {
      form.setFieldsValue({
        searchType: 1,
        queryThreshold: "100",
      });
      setSearchType(1);
    }
    setModalVisible(!modalVisible);
  };

  const onModalSubmit = (result) => {
    if (opType.current === "edit") {
      updateAppByProjectId({
        projectId: params.id,
        id: currentRecord.current.id,
        searchType: result.searchType,
        queryThreshold: result.queryThreshold,
        cluster: searchType !== 1 ? result.selectCluster : undefined,
      }).then((res) => {
        getData();
      });
    } else {
      createAppByProjectId(params.id, {
        searchType: result.searchType,
        queryThreshold: result.queryThreshold,
        cluster: searchType !== 1 ? result.selectCluster : undefined,
      }).then(() => {
        getData();
      });
    }
    setModalVisible(!modalVisible);
  };

  return (
    <>
      <Drawer onClose={onCancel} width={800} visible={true} title="访问设置">
        <div className="add-esuser">
          <Button onClick={() => onClickAddOrEdit("add")} type="primary">
            新建ES_User
          </Button>
        </div>
        <Table rowKey="id" columns={getColumns(list)} dataSource={list} scroll={{ x: "max-content" }} />
      </Drawer>
      {modalVisible && (
        <Modal
          title={opType.current === "edit" ? "编辑ES_User" : "新建ES_User"}
          visible={modalVisible}
          className="esuser-modal"
          footer=""
          onCancel={() => setModalVisible(!modalVisible)}
          destroyOnClose
        >
          <Form labelCol={{ span: 5 }} wrapperCol={{ span: 15 }} form={form} onFinish={onModalSubmit}>
            <Form.Item name="searchType" label="访问模式">
              <Select
                showSearch
                filterOption={filterOption}
                placeholder="请选择"
                options={searchTypeOptions}
                onChange={onSearchTypeChange}
              ></Select>
            </Form.Item>
            {searchType !== 1 && (
              <Form.Item name="selectCluster" label="访问集群">
                <Select
                  showSearch
                  filterOption={filterOption}
                  placeholder="请选择"
                  options={searchType === 0 ? clusterList : primitiveList}
                ></Select>
              </Form.Item>
            )}
            <div className="query-threshold">
              <Form.Item
                name="queryThreshold"
                label="查询限流值"
                rules={[
                  { required: true, message: "请输入查询限流值，支持1-16个数字字符" },
                  {
                    validator: (rule: any, value: string) => {
                      if ((value && !new RegExp(regNonnegativeInteger).test(value)) || value?.length > 16) {
                        return Promise.reject(new Error("请输入查询限流值，支持1-16个数字字符"));
                      }
                      return Promise.resolve();
                    },
                  },
                ]}
              >
                <Input placeholder="请输入" />
              </Form.Item>
              <span className="unit">次/s</span>
            </div>
            <div className="footer">
              <Form.Item>
                <Button className="cancel" onClick={() => setModalVisible(!modalVisible)}>
                  取消
                </Button>
                <Button type="primary" htmlType="submit">
                  确定
                </Button>
              </Form.Item>
            </div>
          </Form>
        </Modal>
      )}
    </>
  );
};

export default connect(mapStateToProps)(AccessSetting);
