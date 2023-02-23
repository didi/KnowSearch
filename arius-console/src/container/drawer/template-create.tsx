import { message } from "antd";
import { Button, Steps, Drawer } from "knowdesign";
import React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { AppState } from "store/type";
import { SetMapping, SetSetting, Preview } from "../index-tpl-management/component";
import { createIndex, getNameCheck, getClusterCheck, getTimeFormat } from "api/cluster-index-api";
import { LEVEL_MAP } from "constants/common";
import { KEEP_LIVE_LIST } from "constants/time";
import { CYCLICAL_ROLL_TYPE_LIST } from "container/index-tpl-management/create/constant";
import { formatJsonStr, getFormatJsonStr } from "lib/utils";
import { LogicCluserSelect } from "container/custom-form/logic-cluser-select";
import { nounPartitionCreate } from "container/tooltip";

const { Step } = Steps;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  params: state.modal.params,
  app: state.app,
  cb: state.modal.cb,
});

class CreateTemplate extends React.Component<{
  app: AppState;
  cb: Function;
  setDrawerId: Function;
  params: any;
}> {
  state = {
    loading: false,
    visible: false,
    current: 0,
    isCyclicalRoll: false,
    dataCenter: null,
    baseInfoData: {} as any,
    mapping: "",
    setting: getFormatJsonStr({
      index: {
        "translog.durability": "async",
        "translog.sync_interval": "15s",
        refresh_interval: "1s",
        // codec: "default",
      },
    }),
    timeFormatList: [],
  };

  $formRef: any = React.createRef();
  $mappingRef: any = React.createRef();
  $settingRef: any = React.createRef();
  $previewRef: any = React.createRef();

  steps = [
    {
      title: "基础信息",
      content: "baseInfo",
    },
    {
      title: "Mapping设置",
      content: "Mapping",
    },
    {
      title: "Setting设置",
      content: "Setting",
    },
    {
      title: "设置完成",
      content: "Review",
    },
  ];

  formMap = (): IFormItem[] => {
    const { isCyclicalRoll, timeFormatList } = this.state;
    return [
      {
        key: "name",
        label: "索引模板名称",
        attrs: {
          placeholder: "请填写索引模板名称，支持小写字母、数字、-、_4-128位字符",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              value = value?.trim();
              const reg = /^[.a-z0-9_-]{1,}$/g;
              if (!value) {
                return Promise.reject("请填写索引模板名称");
              }
              if (!reg.test(value) || value.length > 128 || value.length < 4) {
                return Promise.reject("请正确填写索引模板名称，支持小写字母、数字、-、_4-128位字符");
              }
              if (value) {
                try {
                  const res = await getNameCheck(value);
                  if (res.code !== 0 && res.code !== 200) {
                    return Promise.reject(res.message);
                  }
                } catch (err) {
                  return Promise.reject("索引模板名称校验失败");
                }
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "clusterInfo",
        label: "所属集群",
        type: FormItemType.custom,
        customFormItem: <LogicCluserSelect isModifyPage={false} $form={this.$formRef} onChange={this.onClusterInfoChange} />,
        rules: [
          {
            required: true,
            message: "请填写完整",
            validator: async (rule: any, value: any) => {
              if (!value) return Promise.reject();
              if (value.cluster) {
                try {
                  const res = await getClusterCheck(value.cluster);
                  if (res.code !== 0 && res.code !== 200) {
                    return Promise.reject(res.message);
                  }
                } catch (err) {
                  return Promise.reject("所属集群校验失败");
                }
                return Promise.resolve();
              }
              return Promise.reject();
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
          placeholder: "请输入分区字段",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              value = value?.trim();
              if (!value || value.length > 20) {
                return Promise.reject("请输入分区字段，不超过20位字符");
              }
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
                if (typeof value !== "number") {
                  return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
                }
                if (value === Infinity || value < 0) {
                  return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
                }

                if (value < 3 || value > 3072) {
                  return Promise.reject("请输入数据大小(GB)，最小3G, 最大3072G");
                }
                return Promise.resolve();
              },
            },
          ],
          attrs: {
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

  prev = () => {
    const { current } = this.state;
    if (current === 1) {
      this.$mappingRef.handlePre();
    } else if (current === 2) {
      this.$settingRef.handlePre();
    } else if (current === 3) {
      this.$previewRef.handlePre();
    }
  };

  next = () => {
    const { current } = this.state;
    if (current === 0) {
      this.$formRef
        ?.current!.validateFields()
        .then((values) => {
          this.setState({
            current: 1,
            baseInfoData: values,
          });
        })
        .catch((errs) => {});
    } else if (current === 1) {
      this.$mappingRef.handleNext();
    } else if (current === 2) {
      this.$settingRef.handleNext();
    } else if (current === 3) {
      this.$previewRef.handleSubmit(this.onSubmit);
    }
  };

  onSubmit = () => {
    const { dataCenter, baseInfoData, mapping, setting } = this.state;
    const params = {
      dataCenter,
      name: `${baseInfoData.name}`,
      resourceId: baseInfoData.clusterInfo?.cluster,
      level: baseInfoData.level,
      cyclicalRoll: baseInfoData.cyclicalRoll === "more" ? 1 : 0,
      expireTime: baseInfoData.expireTime,
      dateField: baseInfoData.dateField,
      dateFieldFormat: baseInfoData.dateFieldFormat,
      diskSize: Number(baseInfoData.diskSize),
      dataType: baseInfoData.dataType,
      desc: baseInfoData.desc,
      mapping: formatJsonStr(mapping),
      setting: formatJsonStr(setting),
    };
    this.setState({
      loading: true,
    });
    createIndex(params)
      .then(() => {
        message.success("模板创建成功");
        this.props.setDrawerId("");
        this.props.cb();
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  renderBtn = () => {
    const { current } = this.state;
    return [
      current < 3 ? (
        <Button key="submit3" type="primary" style={{ marginRight: "10px" }} onClick={this.next}>
          下一步
        </Button>
      ) : (
        ""
      ),
      current === 3 ? (
        <Button loading={this.state.loading} key="submit4" type="primary" style={{ marginRight: "10px" }} onClick={this.next}>
          完成
        </Button>
      ) : (
        ""
      ),
      current > 0 ? (
        <Button key="back2" style={{ marginRight: "10px" }} onClick={this.prev}>
          上一步
        </Button>
      ) : (
        ""
      ),
      <Button key="back1" onClick={this.handleCancel}>
        取消
      </Button>,
    ];
  };

  updateState = (keyValue, cb?: any) => {
    this.setState(keyValue, cb && cb());
  };

  renderContent = () => {
    const { current, isCyclicalRoll, dataCenter, baseInfoData, mapping, setting } = this.state;
    if (this.steps[current].content === "baseInfo") {
      return <XFormComponent layout="vertical" formData={baseInfoData} formMap={this.formMap()} wrappedComponentRef={this.$formRef} />;
    } else if (this.steps[current].content === "Mapping") {
      return (
        <div style={{ marginTop: 20 }}>
          <SetMapping childEvevnt={(child) => (this.$mappingRef = child)} updateState={this.updateState} data={mapping} />
        </div>
      );
    } else if (this.steps[current].content === "Setting") {
      return (
        <div style={{ marginTop: 20 }}>
          <SetSetting childEvevnt={(child) => (this.$settingRef = child)} updateState={this.updateState} data={setting} />
        </div>
      );
    } else if (this.steps[current].content === "Review") {
      return (
        <Preview
          childEvevnt={(child) => (this.$previewRef = child)}
          updateState={this.updateState}
          data={{ isCyclicalRoll, dataCenter, baseInfoData, mapping, setting }}
        />
      );
    } else {
      return this.steps[current].content;
    }
  };

  componentDidMount() {
    this.getTimeFormatList();
  }

  componentWillUnmount() {
    // 这是一段比较恶心的代码 xform的值无法传下去 但是内部需要接收到
    (window as any).masternodeErr = false;
    delete (window as any).masternodeErr;
  }

  render() {
    const { current } = this.state;
    return (
      <div>
        <Drawer visible={true} title="新建模板" width={750} onClose={this.handleCancel} footer={this.renderBtn()} maskClosable={false}>
          <Steps current={current} style={{ margin: "0 0 30px 0", padding: "0 26px" }}>
            {this.steps.map((item) => (
              <Step key={item.title} title={item.title} />
            ))}
          </Steps>
          <div className="steps-content">{this.renderContent()}</div>
        </Drawer>
      </div>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(CreateTemplate);
