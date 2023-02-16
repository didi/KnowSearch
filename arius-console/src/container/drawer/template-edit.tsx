import { message } from "antd";
import { Spin, Drawer, Space, Button, Divider } from "knowdesign";
import React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { AppState } from "store/type";
import { getIndexBaseInfo, updateIndexInfo, getTimeFormat } from "api/cluster-index-api";
import { LEVEL_MAP } from "constants/common";
import { KEEP_LIVE_LIST } from "constants/time";
import { CYCLICAL_ROLL_TYPE_LIST } from "container/index-tpl-management/create/constant";
import { LogicCluserSelect } from "container/custom-form/logic-cluser-select";
import { nounPartitionCreate } from "container/tooltip";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  params: state.modal.params,
  app: state.app,
  cb: state.modal.cb,
});

class EditTemplate extends React.Component<{
  app: AppState;
  cb: Function;
  setDrawerId: Function;
  params: any;
}> {
  state = {
    loading: false,
    visible: false,
    isCyclicalRoll: false,
    dataCenter: null,
    baseInfoData: {} as any,
    timeFormatList: [],
  };

  $formRef: any = React.createRef();
  id: number = null;

  constructor(props: any) {
    super(props);
    this.id = this.props.params.record?.id;
  }

  formMap = (): IFormItem[] => {
    const { isCyclicalRoll, timeFormatList } = this.state;
    return [
      {
        key: "name",
        label: "索引模板名称",
        attrs: {
          disabled: true,
          placeholder: "请填写索引模板名称，支持小写字母、数字、-、_4-128位字符",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "clusterInfo",
        label: "所属集群",
        type: FormItemType.custom,
        customFormItem: <LogicCluserSelect isModifyPage={true} $form={this.$formRef} onChange={this.onClusterInfoChange} />,
        rules: [
          {
            required: true,
            validator: async (rule: any, value: any) => {
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "level",
        label: "业务等级",
        type: FormItemType.select,
        rules: [
          {
            required: true,
            message: "请选择业务等级",
          },
        ],
        attrs: {
          disabled: true,
          placeholder: "请选择业务等级",
        },
        options: LEVEL_MAP,
      },
      {
        key: "cyclicalRoll",
        label: "是否分区",
        type: FormItemType.select,
        rules: [{ required: true, message: "请选择是否分区" }],
        attrs: {
          disabled: true,
          placeholder: "请选择是否分区",
          onChange: (value) => {
            this.setState({ isCyclicalRoll: value === "more" });
          },
        },
        options: CYCLICAL_ROLL_TYPE_LIST,
        extraElement: <>{nounPartitionCreate}</>,
      },
      {
        key: "dateField",
        label: "分区字段",
        invisible: !isCyclicalRoll,
        attrs: {
          disabled: true,
          placeholder: "请输入分区字段",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              return Promise.resolve();
            },
          },
        ],
      },
      [
        {
          key: "dateFieldFormat",
          label: "时间格式",
          invisible: !isCyclicalRoll,
          type: FormItemType.select,
          rules: [{ required: true, message: "请选择时间格式" }],
          options: timeFormatList.map((item) => ({
            label: item,
            value: item,
          })),
          attrs: {
            disabled: true,
            placeholder: "请选择时间格式",
          },
        },
        {
          key: "expireTime",
          label: "保存周期(天)",
          invisible: !isCyclicalRoll,
          type: FormItemType.select,
          defaultValue: KEEP_LIVE_LIST[0],
          rules: [{ required: true, message: "请选择保存周期" }],
          options: KEEP_LIVE_LIST.map((item) => {
            return {
              label: item === -1 ? "永不过期" : item,
              value: item,
            };
          }),
        },
      ],
      [
        {
          key: "diskSize",
          label: "索引模板数据大小(GB)",
          formAttrs: {
            dependencies: ["clusterInfo"],
          },
          defaultValue: 30,
          type: FormItemType.inputNumber,
          rules: [
            {
              required: true,
              validator: (rule: any, value: number) => {
                return Promise.resolve();
              },
            },
          ],
          attrs: {
            disabled: true,
            placeholder: "请输入",
            style: {
              width: "100%",
            },
          },
        },
        {
          key: "dataType",
          label: "业务类型",
          type: FormItemType.select,
          rules: [{ required: true, message: "请选择业务类型" }],
          options: this.props.params.dataTypeList,
          attrs: {
            placeholder: "请选择业务类型",
          },
        },
      ],
      {
        key: "desc",
        label: "模板描述",
        type: FormItemType.textArea,
        attrs: {
          placeholder: "请输入0-1000字模板描述",
        },
        rules: [
          {
            validator: (rule: any, value: string) => {
              value = value?.trim();
              if (!value) {
                return Promise.resolve();
              } else if (value.length > 1000) {
                return Promise.reject("请输入0-1000字模板描述");
              }
              return Promise.resolve();
            },
          },
        ],
      },
    ] as IFormItem[];
  };

  onClusterInfoChange = (value: any) => {
    this.$formRef?.current.setFieldsValue({ level: value?.level }); // 选择集群后给level初始值
    this.setState({
      dataCenter: value?.dataCenter,
    });
  };

  getBaseInfo = () => {
    this.setState({
      loading: true,
    });
    getIndexBaseInfo(this.id)
      .then((data = {}) => {
        data.cyclicalRoll = data.cyclicalRoll ? "more" : "one";
        data.clusterInfo = {
          cluster: data.cluster,
          clusterName: data.cluster,
          clusterType: data.clusterType,
        };
        data.type = data.clusterType;
        data.clusterName = data.cluster;
        this.setState({
          baseInfoData: data,
          isCyclicalRoll: data.cyclicalRoll === "more",
        });
        this.$formRef?.current.setFieldsValue(data);
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  getTimeFormatList = () => {
    getTimeFormat().then((res = []) => {
      this.setState({
        timeFormatList: res,
      });
    });
  };

  handleCancel = () => {
    this.props.setDrawerId("");
  };

  handleSubmit = () => {
    this.$formRef?.current.validateFields().then((result) => {
      const formData = {
        id: this.id,
        desc: result.desc,
        dataType: result.dataType,
        expireTime: result.expireTime,
      };
      updateIndexInfo(formData).then(() => {
        message.success("更新成功");
        this.props.setDrawerId("");
        this.props.cb();
      });
    });
  };

  componentDidMount() {
    this.getBaseInfo();
    this.getTimeFormatList();
  }

  componentWillUnmount() {
    // 这是一段比较恶心的代码 xform的值无法传下去 但是内部需要接收到
    (window as any).masternodeErr = false;
    delete (window as any).masternodeErr;
  }

  render() {
    const { loading, baseInfoData } = this.state;
    return (
      <Drawer
        visible={true}
        title="编辑模板"
        width={600}
        onClose={this.handleCancel}
        maskClosable={false}
        extra={
          <Space>
            <Button onClick={this.handleCancel}>取消</Button>
            <Button type="primary" onClick={this.handleSubmit}>
              确定
            </Button>
            <Divider type="vertical" />
          </Space>
        }
      >
        <Spin spinning={loading}>
          <XFormComponent layout="vertical" formData={baseInfoData} formMap={this.formMap()} wrappedComponentRef={this.$formRef} />
        </Spin>
      </Drawer>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(EditTemplate);
