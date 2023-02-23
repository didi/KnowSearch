import { Button, Steps, Drawer, message } from "antd";
import React from "react";
import { connect } from "react-redux";
import * as actions from "../../actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { addIndexAdmin } from "api/index-admin";
import { AppState } from "store/type";
import { SetMapping, SetSetting } from "../index-admin/component";
import { getClusterLogicNames } from "api/cluster-api";
import { checkIndexName, getPhyClusterPerType } from "api/cluster-index-api";
import { RESOURCE_TYPE_LIST } from "constants/common";
import { formatJsonStr, isSuperApp, getFormatJsonStr } from "lib/utils";

const { Step } = Steps;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});

class CreateIndex extends React.Component<{
  app: AppState;
  cb: Function;
  setDrawerId: Function;
}> {
  state = {
    loading: false,
    visible: false,
    current: 0,
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
    clusterList: [],
  };

  $formRef: any = React.createRef();
  $mappingRef: any = React.createRef();
  $settingRef: any = React.createRef();

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
  ];

  formMap = () => {
    return [
      {
        key: "name",
        label: "索引名称",
        attrs: {
          placeholder: "请填写索引名称，支持小写字母、数字、-、_4-128位字符",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              value = value?.trim();
              const reg = /^[.a-z0-9_-]{1,}$/g;
              if (!value) {
                return Promise.reject("请填写索引名称");
              }
              if (!reg.test(value) || value.length > 128 || value.length < 4) {
                return Promise.reject("请正确填写索引名称，支持小写字母、数字、-、_4-128位字符");
              }
              const cluster = this.$formRef?.current.getFieldValue(["cluster"]);
              if (value && cluster) {
                try {
                  const res = await checkIndexName(cluster, value);
                  if (res?.data) {
                    return Promise.reject("索引名称已存在");
                  }
                  if (res.code !== 0 && res.code !== 200) {
                    return Promise.reject(res.message);
                  }
                } catch (err) {
                  return Promise.reject("索引名称校验失败");
                }
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "clusterType",
        label: "集群类型",
        type: FormItemType.select,
        options: RESOURCE_TYPE_LIST,
        attrs: {
          onChange: (value) => {
            // 超级应用展示物理集群，其他应用展示逻辑集群
            const superApp = isSuperApp();
            const clusterFn = superApp ? getPhyClusterPerType : getClusterLogicNames;
            clusterFn(value).then((res = []) => {
              this.$formRef?.current.setFieldsValue({ cluster: undefined });
              this.setState({ clusterList: superApp ? res : res.map((item) => item.name) });
            });
          },
        },
        rules: [
          {
            required: true,
            message: "请选择集群类型",
          },
        ],
      },
      {
        key: "cluster",
        label: "集群名称",
        type: FormItemType.select,
        options: this.state.clusterList.map((item) => ({
          label: item,
          value: item,
        })),
        attrs: {
          onChange: (value) => {
            const name = this.$formRef?.current.getFieldValue(["name"]);
            if (value && name) {
              this.$formRef?.current.validateFields(["name"]);
            }
          },
        },
        rules: [
          {
            required: true,
            message: "请选择集群名称",
          },
        ],
      },
    ] as IFormItem[];
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
      this.$mappingRef.handleSubmit();
    } else if (current === 2) {
      this.$settingRef.handleSubmit(this.onSubmit);
    }
  };

  onSubmit = (value) => {
    const { baseInfoData, mapping } = this.state;
    const params = {
      index: baseInfoData.name,
      cluster: baseInfoData.cluster,
      mapping: formatJsonStr(mapping),
      setting: formatJsonStr(value),
    };
    this.setState({
      loading: true,
    });
    addIndexAdmin(params)
      .then((res) => {
        message.success(res?.message || "索引创建成功");
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
      current < 2 ? (
        <Button key="submit3" type="primary" style={{ marginRight: "10px" }} onClick={this.next}>
          下一步
        </Button>
      ) : (
        ""
      ),
      current === 2 ? (
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
    const { current, baseInfoData, mapping, setting } = this.state;
    if (this.steps[current].content === "baseInfo") {
      return <XFormComponent formData={baseInfoData} formMap={this.formMap()} wrappedComponentRef={this.$formRef} />;
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
    } else {
      return this.steps[current].content;
    }
  };

  componentWillUnmount() {
    // 这是一段比较恶心的代码 xform的值无法传下去 但是内部需要接收到
    (window as any).masternodeErr = false;
    delete (window as any).masternodeErr;
  }

  render() {
    const { current } = this.state;
    return (
      <div>
        <Drawer visible={true} title="新建索引" width={728} onClose={this.handleCancel} footer={this.renderBtn()} maskClosable={false}>
          <Steps current={current} style={{ margin: "24px 0 24px 0" }}>
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

export default connect(mapStateToProps, mapDispatchToProps)(CreateIndex);
