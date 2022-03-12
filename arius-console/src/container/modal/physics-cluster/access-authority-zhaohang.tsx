// 开源代码和招行代码功能不同
import {
  Modal,
  Button,
  Steps,
  Radio,
  Transfer,
  Switch,
  Table,
  Popconfirm,
  Form,
  Input,
  Select,
  message,
} from "antd";
import React from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import {
  FormItemType,
  IFormItem,
  XForm as XFormComponent,
} from "component/x-form";
import { AddRole, RenderText } from "container/custom-form";
import { StaffSelect } from "container/staff-select";
import "./index.less";
import { regClusterName } from "constants/reg";
import { clusterJoin } from "api/cluster-api";
import { VERSION_MAINFEST_TYPE } from "constants/status-map";
import { AppState } from "store/type";
import { staffRuleProps } from "constants/table";
import { IAccessClusterRegion } from "@types/cluster/cluster-types";

const { Step } = Steps;
const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});

const connects: any = connect;
@connects(mapStateToProps, mapDispatchToProps)
export class AccessCluster extends React.Component<{
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
  };

  regionInfo: IAccessClusterRegion = null;
  regionItem = [] as any[];
  relationLogicClusterRef: any = React.createRef();
  formRef: any = React.createRef();

  steps = [
    {
      title: "基础信息",
      content: "baseInfo",
    },
    {
      title: "创建region",
      content: "createRegion",
    },
    {
      title: "创建关联逻辑集群",
      content: "relationLogicCluster",
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
      if (this.regionInfo.regionList.length < 1) {
        message.error("请至少划分一个region");
        return;
      }
      const current = this.state.current + 1;
      this.setState({ current, racks: this.regionInfo.regionList });
    } else if (current === 2) {
      this.onSubmit();
    }
  };

  onSubmit = () => {
    const { baseInfoData, racks } = this.state;
    this.relationLogicClusterRef?.current!.validateFields().then((values) => {
      values.racks = racks.map((item) => item.racks);
      const roleClusterHosts = this.getRoleClusterHosts();
      const params = {
        type: 4,
        appId: this.props.app.appInfo()?.id,
        cluster: baseInfoData.name,
        divide: true,
        esVersion: baseInfoData.esVersion || "",
        logicCluster: values.name,
        phyClusterDesc: baseInfoData.desc || "",
        regionRacks: values.racks,
        responsible: Array.isArray(baseInfoData.responsible)
          ? baseInfoData.responsible.join(",")
          : baseInfoData.responsible,
        singular: baseInfoData.singular,
        roleClusterHosts,
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
    });
  };

  getRoleClusterHosts = () => {
    const { baseInfoData, racks } = this.state;
    let arrRoleClusterHosts = [];
    // if (baseInfoData.region.length === 1) {
    //   const arr = baseInfoData.region[0].value
    //     .split("\n")
    //     .filter((ele: string) => ele !== "");
    //   arr.forEach((item) => {
    //     const ipPostArr = item.split(":");
    //     for (let i = 0; i < 3; i++) {
    //       arrRoleClusterHosts.push({
    //         cluster: baseInfoData.name,
    //         // hostname: 'hostname',
    //         id: 0,
    //         ip: ipPostArr[0], //节点ip 必填
    //         nodeSet: "",
    //         port: ipPostArr[1], // 端口号
    //         rack: i + 1 === 1 ? this.getRack(item) : "", // data
    //         role: i + 1, //角色 1data   2client    3master 必填
    //         roleClusterId: 0,
    //         status: 0,
    //       });
    //     }
    //   });
    // } else if (baseInfoData.region.length > 1) {
    //   baseInfoData.region.forEach((element) => {
    //     const arr = element.value
    //       .split("\n")
    //       .filter((ele: string) => ele !== "");
    //     let role = null;
    //     switch (element.type) {
    //       case "masternode":
    //         role = 3;
    //         break;
    //       case "clientnode":
    //         role = 2;
    //         break;
    //       case "datanode":
    //         role = 1;
    //         break;
    //       default:
    //         role = 3;
    //         break;
    //     }
    //     arr.forEach((item) => {
    //       const ipPostArr = item.split(":");
    //       arrRoleClusterHosts.push({
    //         cluster: baseInfoData.name,
    //         // hostname: "hostname",
    //         id: 0,
    //         ip: ipPostArr[0], //节点ip 必填
    //         nodeSet: "",
    //         port: ipPostArr[1], // 端口号
    //         rack: role === 1 ? this.getRack(item) : "", // data
    //         role: role, //角色 1data   2client    3master 必填
    //         roleClusterId: 0,
    //         status: 0,
    //       });
    //     });
    //   });
    // }

    if(baseInfoData.region?.length) {
      baseInfoData.region.forEach((element) => {
        const arr = element.value
          .split("\n")
          .filter((ele: string) => ele !== "");
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
          arrRoleClusterHosts.push({
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
          });
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
        <Button
          loading={this.state.submitLoading}
          key="submit4"
          type="primary"
          onClick={this.next}
        >
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
      return (
        <BaseInfo
          key={"baseInfo"}
          formRef={this.formRef}
          data={baseInfoData}
          app={this.props.app}
        />
      );
    } else if (this.steps[current].content === "createRegion") {
      return (
        <CreateRegion
          getRegion={(regionInfo, regionItem) => {
            this.regionInfo = regionInfo;
            if (regionItem) this.regionItem = regionItem;
          }}
          key={"createRegion"}
          regionInfo={this.regionInfo}
          regionList={this.state.baseInfoData?.region}
        />
      );
    } else if (this.steps[current].content === "relationLogicCluster") {
      return (
        <RelationLogicCluster
          formRef={this.relationLogicClusterRef}
          baseInfo={this.state.baseInfoData}
          racks={this.state.racks}
          key={"relationLogicCluster"}
        />
      );
    } else {
      return this.steps[current].content;
    }
  };

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

const formMap = (app: AppState) => {
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
        placeholder: "请填写集群名称",
        style: { width: "60%" },
      },
      rules: [
        {
          required: true,
          message: "请填写集群名称，支持大、小写字母、数字、-、_，1-128位字符",
          validator: async (rule: any, value: string) => {
            const reg = /^[.a-z0-9_-]{1,}$/g;
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
      key: "responsible",
      label: "责任人",
      colSpan: 10,
      rules: [
        {
          required: true,
          ...staffRuleProps,
        },
      ],
      type: FormItemType.custom,
      isCustomStyle: true,
      customFormItem: <StaffSelect style={{ width: "60%" }} />,
    },
    {
      key: "esVersion",
      label: "ES版本",
      rules: [{ 
        required: true, 
        message: "请输入正确版本号 x.x.x 或者 x.x.x.x，x 为数字且小于四位",
        validator: async(rule: any, value: string) => {
          const version = /^\d{1,4}(\.\d{1,4}){2,3}$/;
          if (!value || !version.test(value)) {
            return Promise.reject("请输入正确版本号 x.x.x 或者 x.x.x.x，x 为数字且小于四位");
          }
          return Promise.resolve();
        },
      }],
      attrs: {
        placeholder: "请输入",
        style: { width: "60%" },
      },
    },
    // {
    //   key: "singular",
    //   label: "单节点实例数",
    //   rules: [
    //     {
    //       required: true,
    //       validator: async (rule: any, value: number) => {
    //         if (value > 50) {
    //           return Promise.reject("上限50");
    //         }
    //         if (value < 1 || value === undefined) {
    //           return Promise.reject("单节点实例数需大于0");
    //         }
    //         Promise.resolve("大于0");
    //       },
    //     },
    //   ],
    //   type: FormItemType.inputNumber,
    //   attrs: {
    //     placeholder: "请输入",
    //     style: { width: "60%" },
    //   },
    // },
    {
      key: "region",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <AddRole />,
      rules: [
        {
          required: true,
          // message: "请按提示填写完整",
          validator: async (
            rule: any,
            value: { type: string; value: string; check?: boolean }[]
          ) => {
            if(!value?.length)
              return;
              
            const check = value.filter((item) => item.check);

            if (check.length) {
              return Promise.reject("");
            }

            let flag = true;
            
            value.forEach((element) => {
              if (!element.value) {
                flag = false;
              }
            });
            
            if (flag) {
              return Promise.resolve();
            }

            return Promise.reject("");
          },
        },
      ],
    },
    {
      key: "desc",
      type: FormItemType.textArea,
      label: "集群描述",
      rules: [{
        required: false,
        whitespace: true,
        validator: (rule: any, value: string) => {
          if (!value) {
            return Promise.resolve();
          }
          if (value?.trim().length >= 0 && value?.trim().length < 100) {
            return Promise.resolve();
          } else {
            return Promise.reject('请输入1-100字集群描述');
          }
        },
      }],
    },
  ] as IFormItem[];
};

const BaseInfo = (props: { formRef: any; data: any; app: AppState }) => {
  return (
    <>
      <XFormComponent
        formData={props.data || {}}
        formMap={formMap(props.app)}
        wrappedComponentRef={props.formRef}
        layout={"vertical"}
      />
    </>
  );
};

const CreateRegion = (props: {
  regionList: any;
  getRegion: (data: any, regionItem?: any) => any;
  regionInfo: IAccessClusterRegion;
}) => {
  const [value, setValue] = React.useState(props.regionInfo?.value || 1);
  const [data, setData] = React.useState(props.regionInfo?.regionList || []);
  const [defaultData, setDefaultData] = React.useState([]);
  const [selectedKeys, setSelectedKeys] = React.useState([]);
  const [targetKeys, setTargetKeys] = React.useState([]);
  const [regionData, setRegionData] = React.useState([]);

  React.useEffect(() => {
    const { regionList } = props;
    if (regionList && regionList.length === 1) {
      const regionListArr = regionList[0]?.value
        .split("\n")
        .filter((ele: string) => ele !== "");
      getRegionList(regionListArr);
    } else if (regionList.length > 1) {
      const regionListArr = regionList[2]?.value
        ?.split("\n")
        ?.filter((ele: string) => ele !== "");
      getRegionList(regionListArr);
    }
  }, []);

  const getData = (data, regionItem?: any) => {
    const { getRegion } = props;
    getRegion({ regionList: data, value }, regionItem);
  };

  const getRegionList = (regionListArr) => {
    let regionItem = [];
    let regionItemRacks = [];
    regionListArr?.forEach((element, index) => {
      regionItemRacks.push(element);
      index++;
      if (index % 2 === 0) {
        regionItem.push({
          key: index,
          title: `r${index / 2}`,
          description: regionItemRacks,
        });
        regionItemRacks = [];
      }
      if (regionListArr.length % 2 !== 0 && regionListArr.length === index) {
        regionItem.push({
          key: index,
          title: `r${(regionListArr.length + 1) / 2}`,
          description: regionItemRacks,
        });
        regionItemRacks = [];
      }
    });
    if (props.regionInfo?.regionList.length) {
      regionItem = regionItem.map((i) => {
        let flat = false;
        props.regionInfo.regionList.forEach((item) => {
          if (item.racks === i.title) {
            flat = true;
          }
        });
        if (flat) {
          i.disabled = true;
        }
        return i;
      });
    }
    setRegionData(regionItem);
    const racks = regionItem.map((item) => item.title);
    const region = {
      region: "region0",
      racks: racks.toString(),
      key: "region0",
    };
    setDefaultData([region]);
    getData([region], regionItem);
  };

  const onChange = (e) => {
    setValue(e.target.value);
  };

  const handleChange = (nextTargetKeys, direction, moveKeys) => {
    setTargetKeys(nextTargetKeys);
  };

  const handleSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
    setSelectedKeys([...sourceSelectedKeys, ...targetSelectedKeys]);
  };

  const handleDelete = (item, key) => {
    const copyData = [...data];
    copyData.splice(key, 1);
    setData(copyData);
    const regionDataCopy = regionData.map((el) => {
      if (item.racks.indexOf(el.title) > -1) {
        el.disabled = false;
      }
      return el;
    });
    setRegionData(regionDataCopy);
  };

  const computeSurplusRegion = (item) => {
    const copyData = [...data];
    copyData.push(item);
    setData(copyData);
  };

  const getDataSource = () => {
    if (value === 1) {
      // console.log(value, defaultData);
      getData(defaultData);
      return defaultData;
    }
    // console.log(value, data);
    getData(data);
    return data;
  };

  const handleButtom = () => {
    if (!targetKeys || targetKeys.length === 0) return;
    let racksData = [];
    const mapData = regionData.map((item) => {
      targetKeys?.forEach((itemKey) => {
        if (item.key === itemKey) {
          item.disabled = true;
          racksData.push(item);
        }
      });
      return item;
    });
    racksData = racksData.map((item) => item.title);
    setTargetKeys([]);
    setRegionData(mapData);
    computeSurplusRegion({
      region: "region" + data.length,
      racks: racksData.toString(),
      key: "region" + data.length,
    });
  };

  const getColumns = () => {
    const columns = [
      {
        title: "region名字",
        key: "region",
        dataIndex: "region",
      },
      {
        title: "racks",
        key: "racks",
        dataIndex: "racks",
      },
      {
        title: "操作",
        key: "operation",
        dataIndex: "operation",
        render: (_, record: { key: React.Key }, index) => (
          <Popconfirm
            title={record.key}
            onConfirm={() => handleDelete(record, index)}
          >
            <a>删除</a>
          </Popconfirm>
        ),
      },
    ];
    const column = value === 1 ? columns.splice(0, 2) : columns;
    // console.log(column);
    return column;
  };

  return (
    <>
      <h3>是否划分集群？</h3>
      <p>
        region是对物理集群的虚拟划分，方便更好的利用集群资源。如果不划分，会默认整个集群为1个region。
      </p>
      <Radio.Group onChange={onChange} value={value}>
        <Radio value={1}>否</Radio>
        <Radio value={2}>是</Radio>
      </Radio.Group>
      <div className="create-region-box">
        <div
          style={value === 1 ? { display: "none" } : { display: "block" }}
          className="create-region-box-transfer"
        >
          <span>racks：</span>
          <Transfer
            dataSource={regionData}
            titles={["Source", "Target"]}
            targetKeys={targetKeys}
            selectedKeys={selectedKeys}
            onChange={handleChange}
            onSelectChange={handleSelectChange}
            render={(item) => item.title}
            style={{ marginBottom: 16 }}
          />
          {data.length < 1 ? (
            <p style={{ color: "red" }}>请至少划分一个region</p>
          ) : null}
          <Button
            type="primary"
            disabled={targetKeys.length === 0}
            onClick={handleButtom}
          >
            创建region
          </Button>
        </div>
      </div>
      <div className="create-region-table-box">
        <Table
          dataSource={getDataSource()}
          key={"key"}
          columns={getColumns()}
          pagination={false}
        />
      </div>
    </>
  );
};

const RelationLogicCluster = React.forwardRef(
  (props: { baseInfo: any; racks: any[]; formRef: any }) => {
    const [form] = Form.useForm();

    const { Option } = Select;

    const children = [];
    for (let i = 0; i < props.racks.length; i++) {
      children.push(props.racks[i].region);
    }

    return (
      <>
        <div className="relation-region-box">
          <Form
            ref={props.formRef}
            className="relation-region-box-form"
            layout="vertical"
            form={form}
            name="control-hooks1"
          >
            <Form.Item
              name="name"
              label="逻辑集群"
              rules={[
                {
                  required: true,
                  validator: async (rule: any, value: string) => {
                    if (!value || !new RegExp(regClusterName).test(value)) {
                      return Promise.reject('请填写集群名称，支持大、小写字母、数字、-、_');
                    }
                    if (value && value.length > 128) {
                      return Promise.reject('最大限制128字符');
                    }
                    return Promise.resolve();
                  },
                },
              ]}
              className="relation-region-box-form-item"
              initialValue={props.baseInfo.name}
            >
              <Input placeholder="请填写逻辑集群" />
            </Form.Item>
            <Form.Item
              name="regionId"
              label="关联Region"
              className="relation-region-box-form-item"
            >
              <div>{children.length ? children.join(",") : "_"}</div>
            </Form.Item>
          </Form>
        </div>
      </>
    );
  }
);
