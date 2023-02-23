import * as React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import Url from "lib/url-parser";
import { IUNSpecificInfo } from "typesPath/base-types";
import { message } from "antd";
import { Drawer, Table, Space, Divider, Modal, Button } from "knowdesign";
import { CloseCircleFilled } from "@ant-design/icons";
import { getClearInfo, clearIndex } from "api/cluster-index-api";
import { CONFIRM_BUTTON_TEXT } from "constants/common";

const { confirm } = Modal;

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const connects: any = connect;

@connects(mapStateToProps)
export class ClearModal extends React.Component<any, any> {
  public state = {
    searchKey: "",
    clearInfo: {} as IUNSpecificInfo,
    login: false,
    clearInfoSelectedRowKeys: [] as string[],
  };

  constructor(props: any) {
    super(props);
    const url = Url();
  }

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({ loading: true });
    getClearInfo(this.props.params)
      .then((value = {}) => {
        this.setState({
          clearInfo: value,
        });
      })
      .finally(() => {
        this.setState({ loading: false });
      });
  };

  public getData = (origin?: IUNSpecificInfo[]) => {
    origin = (origin || []).map((item) => {
      return {
        name: item,
      };
    });
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey ? origin.filter((d) => d.name?.toLowerCase().includes(searchKey as string)) : origin;
    return data;
  };

  public getColumns = () => {
    const cols = [
      {
        title: "索引列表",
        dataIndex: "index",
      },
      {
        title: "存储大小",
        dataIndex: "priStoreSize",
        width: "100px",
      },
    ];
    return cols;
  };

  public handleSubmit = () => {
    if (!this.state.clearInfoSelectedRowKeys?.length) {
      message.warning("请先选择需清理的索引");
      return;
    }
    confirm({
      ...CONFIRM_BUTTON_TEXT,
      title: `索引数据清理后数据无法恢复，确认要清理吗？`,
      icon: <CloseCircleFilled className="confirm-delete-icon" />,
      onOk: () => {
        clearIndex(this.props.params, this.state.clearInfoSelectedRowKeys).then(() => {
          message.success("清理成功");
          this.props.dispatch(actions.setDrawerId(""));
          this.props.cb && this.props.cb(); // 重新获取数据列表
        });
      },
    });
  };

  public handleCancel = () => {
    this.setState({
      relatedAppList: [],
    });
    this.props.dispatch(actions.setDrawerId(""));
  };

  public onSelectChange = (selectedRowKeys: string[]) => {
    this.setState({
      clearInfoSelectedRowKeys: selectedRowKeys,
    });
  };

  public renderTable = () => {
    const rowSelection = {
      selectedRowKeys: this.state.clearInfoSelectedRowKeys,
      onChange: this.onSelectChange,
    };
    return (
      <Table
        scroll={{ x: "max-content", y: 300 }}
        loading={this.state.login}
        rowKey="index"
        dataSource={this.state.clearInfo.indices}
        columns={this.getColumns()}
        pagination={false}
        rowSelection={rowSelection}
      />
    );
  };

  public render() {
    return (
      <>
        <Drawer
          title="清理"
          visible={true}
          width={480}
          closable={true}
          maskClosable={false}
          destroyOnClose={true}
          onClose={this.handleCancel}
          extra={
            <Space>
              <Button onClick={this.handleCancel}>取消</Button>
              <Button type="primary" onClick={this.handleSubmit}>
                清理
              </Button>
              <Divider type="vertical" />
            </Space>
          }
        >
          {this.renderTable()}
        </Drawer>
      </>
    );
  }
}
