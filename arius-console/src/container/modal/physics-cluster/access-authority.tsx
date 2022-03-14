import { Modal, Button, Steps, Switch, Form, Input, Select, Tooltip } from "antd";
import React, { useRef, useState } from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { AddRole, RenderText } from "container/custom-form";
import { StaffSelect } from "container/staff-select";
import "./index.less";
import { regClusterName } from "constants/reg";
import { clusterJoin } from "api/cluster-api";
import { INPUT_RULE_MAP, RESOURCE_TYPE_MAP, VERSION_MAINFEST_TYPE } from "constants/status-map";
import { AppState } from "store/type";
import { staffRuleProps } from "constants/table";
import { IAccessClusterRegion } from "typesPath/cluster/cluster-types";
import { QuestionCircleOutlined } from "@ant-design/icons";
import StepSwitch from "./stepSwitch";
import UserPassword from "./userPassword";

const { Step } = Steps;
const { Option } = Select;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});
class AccessCluster extends React.Component<{
  app: AppState;
  cb: Function;
  setModalId: Function;
}> {
  state = {
    loading: false,
    submitLoading: false,
    visible: false,
    current: 0,
    baseInfoData: {
      region: [],
    } as any,
    racks: [],
    param: {},
  };

  regionInfo: IAccessClusterRegion = null;
  regionItem = [] as any[];
  relationLogicClusterRef: any = React.createRef();
  formRef: any = React.createRef();
  formSwitch: any = React.createRef();
  isFullImportRule: any = React.createRef<boolean>();

  steps = [
    {
      title: "基础信息",
      content: "baseInfo",
    },
    {
      title: "创建关联逻辑集群",
      content: "relationLogicCluster",
    },
    {
      title: "设置索引模板服务",
      content: "stepSwitch",
    },
  ];

  showModal = () => {
    this.setState({
      visible: true,
    });
  };

  handleOk = () => {
    this.setState({ loading: true });
    setTimeout(() => {
      this.setState({ loading: false, visible: false });
    }, 3000);
  };

  handleCancel = () => {
    this.props.setModalId("");
  };

  prev = () => {
    const current = this.state.current - 1;
    this.setState({ current });
  };

  next = () => {
    const { current } = this.state;
    if (current === 0) {
      this.formRef
        ?.current!.validateFields()
        .then((values) => {
          const current = this.state.current + 1;
          if (this.state.baseInfoData.region !== values.region) {
            this.regionInfo = null;
          }
          this.setState({ current, baseInfoData: values });
        })
        .catch((errs) => {});
    } else if (current === 1) {
      this.getParams();
      this.setState({ current: this.state.current + 1 });
    } else if (current === 2) {
      this.onSubmit();
    }
  };

  getParams = async () => {
    const { baseInfoData, racks } = this.state;
    const values = await this.relationLogicClusterRef?.current?.validateFields();
    values.racks = racks.map((item) => item.racks);
    const roleClusterHosts = this.getRoleClusterHosts();
    const params = {
      type: 4,
      appId: this.props.app.appInfo()?.id,
      cluster: baseInfoData.name,
      divide: true,
      // esVersion: baseInfoData.esVersion || "",
      logicCluster: values.name,
      phyClusterDesc: baseInfoData.desc || "",
      regionRacks: values.racks,
      singular: baseInfoData.singular,
      roleClusterHosts,
      password: baseInfoData.password?.value || "",
    };
    this.setState({
      param: params,
    });
  };

  onSubmit = () => {
    const { baseInfoData, racks } = this.state;
    const roleClusterHosts = this.getRoleClusterHosts();
    const params = {
      type: 4,
      tags: JSON.stringify({
        resourceType: Number(baseInfoData.resourceType),
        createSource: 0, // 0接入1新建
      }),
      importRule: baseInfoData.importRule,
      appId: this.props.app.appInfo()?.id,
      cluster: baseInfoData.name,
      divide: true,
      // esVersion: baseInfoData.esVersion || "",
      logicCluster: (this.state.param as any)?.logicCluster,
      phyClusterDesc: baseInfoData.desc || "",
      regionRacks: (this.state.param as any)?.regionRacks,
      // responsible: Array.isArray(baseInfoData.responsible)
      //   ? baseInfoData.responsible.join(",")
      //   : baseInfoData.responsible,
      singular: baseInfoData.singular,
      roleClusterHosts,
      templateSrvs: this.formSwitch.current ? this.formSwitch.current?.join(",") : "",
      password: baseInfoData.password?.value || "",
    };
    this.setState({
      submitLoading: true,
    });
    clusterJoin(params)
      .then(() => {
        this.props.setModalId("");
        this.props.cb();
      })
      .finally(() => {
        this.setState({
          submitLoading: false,
        });
      });
  };

  getRoleClusterHosts = () => {
    const { baseInfoData, racks } = this.state;
    let arrRoleClusterHosts = [];
    if (baseInfoData.region?.length) {
      baseInfoData.region.forEach((element) => {
        const arr = element.value.split("\n").filter((ele: string) => ele !== "");
        let role = null;
        switch (element.type) {
          case "masternode":
            role = 3;
            break;
          case "clientnode":
            role = 2;
            break;
          case "datanode":
            role = 1;
            break;
          default:
            role = 3;
            break;
        }
        arr.forEach((item) => {
          const ipPostArr = item.split(":");
          const arrRoleClusterHost: any = {
            cluster: baseInfoData.name,
            // hostname: "hostname",
            id: 0,
            ip: ipPostArr[0], //节点ip 必填
            nodeSet: "",
            port: ipPostArr[1], // 端口号
            rack: role === 1 ? this.getRack(item) : "", // data
            role: role, //角色 1data   2client    3master 必填
            roleClusterId: 0,
            status: 0,
            beCold: element?.beCold || "false",
          };
          arrRoleClusterHosts.push(arrRoleClusterHost);
        });
      });
    }
    return arrRoleClusterHosts;
  };

  getRack = (ip) => {
    let flat = "";
    this.regionItem.map((item) => {
      if ((item.description + "").indexOf(ip) > -1) {
        flat = item.title;
      }
    });
    return flat;
  };

  renderBtn = () => {
    const { current } = this.state;
    return [
      <Button key="back1" onClick={this.handleCancel}>
        取消
      </Button>,
      current > 0 ? (
        <Button key="back2" onClick={this.prev}>
          上一步
        </Button>
      ) : (
        ""
      ),
      current < 2 ? (
        <Button key="submit3" type="primary" onClick={this.next}>
          下一步
        </Button>
      ) : (
        ""
      ),
      current === 2 ? (
        <Button loading={this.state.submitLoading} key="submit4" type="primary" onClick={this.next}>
          完成
        </Button>
      ) : (
        ""
      ),
    ];
  };

  renderContent = () => {
    const { current, baseInfoData } = this.state;
    if (this.steps[current].content === "baseInfo") {
      return <BaseInfo key={"baseInfo"} formRef={this.formRef} isFullImportRule={this.isFullImportRule} data={baseInfoData} app={this.props.app} />;
    } else if (this.steps[current].content === "relationLogicCluster") {
      return (
        <RelationLogicCluster
          formRef={this.relationLogicClusterRef}
          baseInfo={this.state.baseInfoData}
          racks={this.state.racks}
          param={this.state.param}
          key={"relationLogicCluster"}
        />
      );
    } else if (this.steps[current].content === "stepSwitch") {
      return <StepSwitch formSwitch={this.formSwitch} param={this.state.param} templateSrvs={this.formSwitch.current} />;
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
        <Modal
          visible={true}
          title="接入集群"
          width={728}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          footer={this.renderBtn()}
          maskClosable={false}
          okText={"确定"}
          cancelText={"取消"}
        >
          <Steps current={current}>
            {this.steps.map((item) => (
              <Step key={item.title} title={item.title} />
            ))}
          </Steps>
          <div className="steps-content">{this.renderContent()}</div>
        </Modal>
      </div>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AccessCluster);

const formMap = (app: AppState, ref, isFullImportRule) => {
  return [
    {
      key: "type",
      label: "集群类型",
      type: FormItemType.text,
      customFormItem: <RenderText text={VERSION_MAINFEST_TYPE[4]} />,
    },
    {
      key: "name",
      label: "集群名称",
      attrs: {
        placeholder: "请填写集群名称,集群命名建议使用：集群物理名称+版本号",
        style: { width: "60%" },
      },
      rules: [
        {
          required: true,
          message: "请填写集群名称，支持大、小写字母、数字、-、_，1-128位字符",
          validator: async (rule: any, value: string) => {
            const reg = /^[.a-zA-Z0-9_-]{1,}$/g;
            if (!value || value?.trim().length > 128) {
              return Promise.reject("请填写正确集群名称，支持大、小写字母、数字、-、_1-128位字符");
            }
            if (!reg.test(value)) {
              return Promise.reject("请填写正确集群名称，支持大、小写字母、数字、-、_1-128位字符");
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "project",
      label: "所属项目",
      type: FormItemType.text,
      customFormItem: <RenderText text={app.appInfo()?.name} />,
    },
    {
      key: "password",
      label: "",
      type: FormItemType.custom,
      customFormItem: <UserPassword />,
      rules: [
        {
          required: false,
          // message: "请按提示填写完整",
          validator: (rule: any, value: { value: string; check?: boolean }) => {
            if (value?.check) {
              return Promise.reject();
            }
            return Promise.resolve();
          },
        },
      ],
    },
    // {
    //   key: "esVersion",
    //   label: "集群版本",
    //   rules: [{
    //     required: true,
    //     message: "请输入正确版本号 x.x.x 或者 x.x.x.x，x 为数字且小于四位",
    //     validator: async(rule: any, value: string) => {
    //       const version = /^\d{1,4}(\.\d{1,4}){2,3}$/;
    //       if (!value || !version.test(value)) {
    //         return Promise.reject("请输入正确版本号 x.x.x 或者 x.x.x.x，x 为数字且小于四位");
    //       }
    //       return Promise.resolve();
    //     },
    //   }],
    //   attrs: {
    //     placeholder: "请输入",
    //     style: { width: "60%" },
    //   },
    // },
    {
      key: "resourceType",
      label: "所属资源类型",
      type: FormItemType.select,
      options: Object.keys(RESOURCE_TYPE_MAP).map((item) => {
        return {
          label: RESOURCE_TYPE_MAP[item],
          value: item,
        };
      }),
      rules: [
        {
          required: true,
        },
      ],
      attrs: {
        placeholder: "请选择",
        style: { width: "60%" },
      },
    },
    {
      key: "importRule",
      label: "录入规则",
      type: FormItemType.radioGroup,
      defaultValue: "0",
      attrs: {
        onChange: (e) => {
          if (e && e.target) {
            ref.current.setFullType(e.target.value == '1');
            if (isFullImportRule) {
              isFullImportRule.current = e.target.value === '1';
            }
          }
        }
      },
      options: Object.keys(INPUT_RULE_MAP).map((item) => {
        return {
          label: INPUT_RULE_MAP[item],
          value: item,
        };
      }),
      rules: [{ required: true, message: "请选择录入规则" }],
    },
    {
      key: "region",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <AddRole ref={ref} isFullType={isFullImportRule?.current}/>,
      rules: [
        {
          required: true,
          // message: "请按提示填写完整",
          validator: async (rule: any, value: { type: string; value: string; check?: boolean }[]) => {
            if (!value?.length) {
              (window as any).masternodeErr = true;
              return Promise.reject("");
            }
            const check = value?.filter((item) => item.check);
            if (check.length) {
              return Promise.reject("");
            }
            // 注释空判断
            // let flag = true;
            // value?.forEach((element) => {
            //   if (!element.value) {
            //     flag = false;git 
            //     return;
            //   }
            // });
            if (value?.length) {
              (window as any).masternodeErr = false;
              return Promise.resolve();
            }
            return Promise.resolve("");
          },
        },
      ],
    },
    {
      key: "desc",
      type: FormItemType.textArea,
      label: "集群描述",
      rules: [
        {
          required: false,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if (!value) {
              return Promise.resolve();
            }
            if (value?.trim().length >= 0 && value?.trim().length < 100) {
              return Promise.resolve();
            } else {
              return Promise.reject("请输入0-100字集群描述");
            }
          },
        },
      ],
      attrs: {
        placeholder: "请输入0-100字集群描述",
      },
    },
  ] as IFormItem[];
};

const BaseInfo = (props: { formRef: any; data: any; app: AppState, isFullImportRule: any }) => {
  const ref = useRef()
  return (
    <>
      <XFormComponent formData={props.data || {}} formMap={formMap(props.app, ref, props.isFullImportRule)} wrappedComponentRef={props.formRef} layout={"vertical"} />
    </>
  );
};

const RelationLogicCluster = React.forwardRef((props: { baseInfo: any; racks: any[]; formRef: any; param: any }) => {
  const [form] = Form.useForm();

  const [isShow, setIsShow] = useState(true);

  const handleChange = (e) => {
    console.log(Boolean);
    const flag = e === "true" ? true : false;
    setIsShow(flag);
  };

  return (
    <>
      <div>
        <Form
          ref={props.formRef}
          // className="relation-region-box-form"
          layout="vertical"
          form={form}
          name="control-hooks1"
        >
          <Form.Item
            name="createLoginName"
            label={
              <>
                是否创建关联逻辑集群
                <span style={{ marginLeft: 10 }}>
                  <Tooltip
                    placement="right"
                    overlayClassName="tooltip-container"
                    title={
                      <div className="tooltip-content">
                        <span>
                          选择“是”：会创建一个关联的逻辑集群，类型为独立集群
                          <br />
                          选择“否”：不会创建逻辑集群，用户可以前往集群管理-逻辑集群自定义绑定。
                        </span>
                      </div>
                    }
                  >
                    <QuestionCircleOutlined />
                  </Tooltip>
                </span>
              </>
            }
            rules={[{ required: true }]}
            className="relation-region-box-form-item"
            initialValue={"true"}
            style={{ marginBottom: 10 }}
          >
            <Select onChange={handleChange}>
              <Option value="true">是</Option>
              <Option value="false">否</Option>
            </Select>
          </Form.Item>
          {isShow ? (
            <Form.Item
              name="name"
              label="逻辑集群"
              rules={[
                {
                  required: true,
                  validator: async (rule: any, value: string) => {
                    if (!value || !new RegExp(regClusterName).test(value)) {
                      return Promise.reject("请填写逻辑集群名称，支持大、小写字母、数字、-、_");
                    }
                    if (value && value.length > 128) {
                      return Promise.reject("支持大、小写字母、数字、-、_，1-128位字符");
                    }
                    return Promise.resolve();
                  },
                },
              ]}
              className="relation-region-box-form-item"
              initialValue={props.param?.logicCluster || props.baseInfo.name}
            >
              <Input placeholder="请填写逻辑集群" />
            </Form.Item>
          ) : (
            ""
          )}
        </Form>
      </div>
    </>
  );
});
