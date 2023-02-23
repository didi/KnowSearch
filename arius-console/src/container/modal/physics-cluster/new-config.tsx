import * as React from "react";
import { Modal, Form, Row, Col, Button, Select, Tooltip, Drawer } from "antd";
import "./index.less";
import { IFormItem, FormItemType, renderFormItem, handleFormItem } from "component/x-form";
import Url from "lib/url-parser";
import { connect } from "react-redux";
import { getClusterTemplateCentent } from "api/op-cluster-config-api";
import * as actions from "actions";
import { StepSelect } from "container/custom-form/step-select";
import { PlusOutlined } from "@ant-design/icons";
import { getPhysicClusterRoles } from "api/cluster-api";
import { clusterConfigAdd } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";

const mapStateToProps = (state) => ({
  phyClusterConfig: state.configInfo,
  app: state.app,
  user: state.user,
  params: state.modal.params,
  cb: state.modal.cb,
});
const connects: Function = connect;
@connects(mapStateToProps)
export class NewConfigModal extends React.Component<any> {
  public state = {
    addFormArr: [0],
    roleList: [] as string[],
    configRoleList: [],
    clusterOpRolesList: [],
    confirmLoading: false,
  };

  public physicsClusterId: number;
  public physicsCluster: string;
  public type: number;

  private $formRef: any[] = [];
  private $TwoFormRef: any = null;

  public componentDidMount() {
    const url = Url();
    this.physicsClusterId = Number(this.props.params.id);
    this.type = Number(url.search.type);
    this.physicsCluster = url.search.physicsCluster || this.props.params.cluster;
    if (this.physicsClusterId) {
      // 原节点角色列表接口，缺少判断字段，与下面接口数据冲突
      // getConfigRole(this.physicsClusterId).then((res) => {
      //   res = res?.map((item: string) => {
      //     return {
      //       label: item,
      //       value: item,
      //     };
      //   });
      //   this.setState({
      //     configRoleList: res,
      //   });
      // });
      getPhysicClusterRoles(this.physicsClusterId).then((res) => {
        res =
          res?.map((ele, index) => {
            return {
              ...ele,
              label: ele.roleClusterName,
              value: ele.roleClusterName,
              key: index,
            };
          }) || [];
        const configRoleList = res
          ?.map((ele, index) => ({
            ...ele,
            label: ele.role,
            value: ele.role,
          }))
          .filter((item) => item.podNumber);
        // 排序master>client>data
        let sortConfigRoleList = [];
        if (configRoleList.filter((item) => item.role === "masternode").length) {
          sortConfigRoleList.push(...configRoleList.filter((item) => item.role === "masternode"));
        }
        if (configRoleList.filter((item) => item.role === "clientnode").length) {
          sortConfigRoleList.push(...configRoleList.filter((item) => item.role === "clientnode"));
        }
        if (configRoleList.filter((item) => item.role === "datanode").length) {
          sortConfigRoleList.push(...configRoleList.filter((item) => item.role === "datanode"));
        }
        this.props.dispatch(actions.setPhyClusterConfigRoles(res));
        this.setState({
          clusterOpRolesList: res,
          configRoleList: sortConfigRoleList,
          //configRoleList: configRoleList,
        });
      });
    }
  }

  public handleCancel = () => {
    this.resetForm();
  };

  public handleSubmit = () => {
    this.setState({ confirmLoading: true });
    const esConfigs = [] as any[];
    this.$formRef.map((item) => {
      item
        .validateFields()
        .then(async (result) => {
          const esConfigItem = {
            clusterId: this.physicsClusterId,
            enginName: result.enginName,
            typeName: result.typeName,
            configData: result.configData,
            desc: result.desc,
          };
          esConfigs.push(esConfigItem);
          await this.twoFormRefHandleSubmit(esConfigs);
          this.setState({ confirmLoading: false });
        })
        .catch((errs) => {
          this.setState({ confirmLoading: false });
          return;
        });
    });
  };

  public twoFormRefHandleSubmit = async (esConfigs?: any) => {
    if (esConfigs) {
      await this.$TwoFormRef.validateFields().then(async (resultR) => {
        const roleOrder = [] as string[];
        const { phyClusterConfig, params } = this.props;
        phyClusterConfig?.clusterRolesList.forEach((item) => {
          if (item.roleClusterName.indexOf(params?.enginName) > -1) {
            roleOrder.push(item.roleClusterName);
          }
        });
        let expandData = {
          phyClusterId: this.physicsClusterId,
          phyClusterName: this.physicsCluster,
          roleOrder: params?.enginName ? roleOrder : resultR.roleOrder,
          type: this.type,
          actionType: params?.enginName ? 2 : 1,
          newEsConfigs: esConfigs,
          originalConfigs: params?.enginName ? [params] : [],
        } as any;
        expandData = JSON.stringify(expandData);
        let ret = await clusterConfigAdd({ expandData });
        this.props.dispatch(actions.setDrawerId(""));
        showSubmitTaskSuccessModal(ret, this.props.params?.history);
      });
    } else {
      await this.$TwoFormRef.validateFields().then(async (resultR) => {
        const roleOrder = [] as string[];
        const { phyClusterConfig, params } = this.props;
        phyClusterConfig?.clusterRolesList.forEach((item) => {
          if (item.roleClusterName.indexOf(params?.enginName) > -1) {
            roleOrder.push(item.roleClusterName);
          }
        });
        let expandData = {
          phyClusterId: this.physicsClusterId,
          phyClusterName: this.physicsCluster,
          roleOrder,
          type: this.type,
          actionType: 3,
          newEsConfigs: [params],
          originalConfigs: [params],
        } as any;
        expandData = JSON.stringify(expandData);
        let ret = await clusterConfigAdd({ expandData });
        this.props.dispatch(actions.setDrawerId(""));
        showSubmitTaskSuccessModal(ret, this.props.params?.history);
      });
    }
  };

  public resetForm = (resetFields?: string[]) => {
    this.$formRef.map((item) => {
      item.resetFields(resetFields || "");
    });
    this.props.dispatch(actions.setDrawerId(""));
  };

  public bindForm = (formRef: any, index: number) => {
    if (!formRef) return;

    if (this.$formRef.length === index) {
      this.$formRef.push(formRef);
    } else {
      this.$formRef[index] = formRef;
    }
  };

  public resetBindForm = (formRef: any) => {
    if (!formRef) return;

    this.$TwoFormRef = formRef;
  };

  public bindNodeRole = () => {
    const configArr = this.props.phyClusterConfig.typeNameList || [];
    let arr = [] as string[];
    configArr.map((itemP) => {
      this.state.clusterOpRolesList.map((item) => {
        if (item.roleClusterName.indexOf(itemP.role) > -1) {
          arr.push(item.roleClusterName);
        }
      });
    });
    arr = [...new Set(arr)];
    const setArr = Array.apply("", { length: arr.length });
    arr.map((item) => {
      if (item.indexOf("master") > -1) {
        setArr[0] = item;
      } else if (item.indexOf("clien") > -1) {
        if (arr.length === 1) setArr[0] = item;
        if (arr.length > 1) setArr[1] = item;
      } else if (item.indexOf("data") > -1) {
        setArr[arr.length - 1] = item;
      }
    });
    this.setSeptPhysicClusterRoles(setArr);
    this.$TwoFormRef.setFieldsValue({
      roleOrder: setArr,
    });
  };

  public setSeptPhysicClusterRoles(data: string[]) {
    const arr = this.props.phyClusterConfig?.clusterRolesList.map((item) => {
      item.label = item.value;
      return item;
    });
    const arrStr = arr.map((item) => {
      return item.value;
    });
    let num = null;
    data.forEach((item, index) => {
      num = arrStr.indexOf(item);
      if (num !== -1) {
        arr[num].label = arr[num].label + " " + `(步骤${index + 1})`;
      }
    });
    this.props.dispatch(actions.setPhyClusterConfigRoles(arr));
  }

  public computeTypeNameList(role: string, index: number) {
    const { typeNameList, configList } = this.props.phyClusterConfig;
    if (typeNameList[index]?.role === role) return;
    const allItem = [
      {
        label: "elasticsearch.yml",
        value: "elasticsearch.yml",
      },
      {
        label: "jvm.options",
        value: "jvm.options",
      },
      {
        label: "filebeat.yml",
        value: "filebeat.yml",
      },
    ];
    const arr = [] as string[];
    configList.forEach((i) => {
      if (i.enginName === role) {
        arr.push(i.typeName);
      }
    });

    typeNameList?.forEach((i) => {
      if (i.role === role) {
        arr.push(i.typeName);
      }
    });

    const arrCp = allItem.map((item) => {
      if (arr.join("").indexOf(item.label) > -1) {
        return { ...item, disabled: true };
      } else {
        return item;
      }
    });
    if (typeNameList.length === index) {
      typeNameList.push({
        role,
        typeName: "",
        typeList: arrCp,
      });
      this.props.dispatch(actions.setPhyClusterConfigType(typeNameList));
    } else {
      typeNameList[index] = {
        role,
        typeName: "",
        typeList: arrCp,
      };
      this.props.dispatch(actions.setPhyClusterConfigType(typeNameList));
    }
  }

  public setOptionConfigData(value: string, index: number) {
    const { typeNameList } = this.props.phyClusterConfig;
    if (typeNameList.length > index) {
      const initValue = typeNameList[index].typeName;
      typeNameList[index].typeName = value;
      typeNameList.map((item, i) => {
        if (typeNameList[index].role === item.role) {
          const arr = item.typeList.map((itemC) => {
            if (value.indexOf(itemC.label) > -1) {
              return { ...itemC, disabled: true };
            } else {
              if (initValue === itemC.label) {
                return { ...itemC, disabled: false };
              }
              return itemC;
            }
          });
          typeNameList[i].typeList = arr;
          this.props.dispatch(actions.setPhyClusterConfigType(typeNameList));
        }
      });
    }
  }

  public getFormMap = (index: number) => {
    return [
      [
        {
          key: "enginName",
          label: "节点角色",
          type: FormItemType.select,
          options: this.state.configRoleList,
          attrs: {
            disabled: this.props.params?.enginName ? true : false,
            placeholder: "请选择节点角色",
          },
          rules: [
            {
              required: true,
              message: "请选择节点角色",
              validator: (rule: any, value: string) => {
                this.computeTypeNameList(value, index);
                this.bindNodeRole();
                if (value) {
                  return Promise.resolve();
                } else {
                  return Promise.reject();
                }
              },
            },
          ],
        },
        {
          key: "typeName",
          label: "配置类别",
          type: FormItemType.custom,
          customFormItem: <ConfigNameSelect index={index} disabled={this.props.params?.enginName ? true : false} />,
          rules: [
            {
              required: true,
              message: "请选择配置类别",
              validator: async (rule: any, value: string) => {
                this.setOptionConfigData(value, index);
                if (value) {
                  return Promise.resolve();
                } else {
                  return Promise.reject();
                }
              },
            },
          ],
        },
      ],
      [
        {
          key: "desc",
          label: "描述信息",
          type: FormItemType.textArea,
          rules: [
            {
              required: false,
              validator: async (rule: any, value: string) => {
                if (value && value.length > 50) {
                  return Promise.reject("请输入0-50字符");
                }
                return Promise.resolve();
              },
            },
          ],
          attrs: {
            disabled: false,
            rows: 2,
            placeholder: "请输入描述",
          },
        },
      ],
      [
        {
          key: "configData",
          label: "配置内容",
          type: FormItemType.textArea,
          rules: [
            {
              required: true,
            },
          ],
          attrs: {
            disabled: false,
            rows: 3,
            placeholder: "请输入配置内容",
          },
        },
      ],
    ] as unknown as IFormItem[];
  };

  public getRolMap = () => {
    return [
      [
        {
          key: "roleOrder",
          label: "重启节点顺序",
          type: FormItemType.custom,
          customFormItem: <StepSelect disabled={this.props.params?.enginName ? true : false} />,
          rules: [
            {
              required: true,
              message: "请选择角色顺序",
              validator: (rule: any, value: any) => {
                if (value?.length > 0) {
                  return Promise.resolve();
                } else {
                  return Promise.reject();
                }
              },
            },
          ],
        },
      ],
    ];
  };

  public onDel = (index: number) => {
    if (index === 0) return;
    const { addFormArr } = this.state;
    addFormArr.splice(index, 1);
    // phyClusterConfig.delEsConfigs(index);
    this.setState({
      addFormArr,
    });
  };

  public addConfigItem = async () => {
    this.$formRef.map((item, index) => {
      if (this.$formRef.length - 1 === index) {
        item
          .validateFields()
          .then((result) => {
            const { addFormArr } = this.state;
            const index = addFormArr.length - 1;
            addFormArr.push(this.state.addFormArr.length);
            this.setState({
              addFormArr,
            });
          })
          .catch((errs) => {
            //
          });
      }
    });
  };

  public render() {
    const formDataRole = {
      roleOrder: this.props.params?.enginName,
    };
    return (
      <>
        <Drawer
          visible={true}
          closable={true}
          onClose={this.handleCancel}
          title={this.props.params?.enginName ? "编辑配置" : "新增配置"}
          maskClosable={false}
          destroyOnClose={true}
          width={728}
          footer={
            <div className="footer-btn">
              <Button style={{ marginRight: 10 }} loading={this.state.confirmLoading} type="primary" onClick={this.handleSubmit}>
                确定
              </Button>
              <Button onClick={this.handleCancel}>取消</Button>
            </div>
          }
        >
          {this.state.addFormArr.map((item, index) => {
            return (
              <div key={item} style={index > 0 ? { marginTop: 10 } : null} className="new-config-content">
                {item > 0 ? (
                  <div className="new-config-content-del">
                    <a>{null}</a>
                    <a onClick={() => this.onDel(index)}>删除</a>
                  </div>
                ) : null}
                <XFormComponent
                  key={item}
                  wrappedComponentRef={(form: any) => {
                    this.bindForm(form, index);
                  }}
                  formData={this.props.params || {}}
                  formMapList={this.getFormMap(index)}
                />
              </div>
            );
          })}

          {this.props.params?.enginName ? null : (
            <Button className="add-new-config" type="primary" onClick={this.addConfigItem} block icon={<PlusOutlined />}>
              添加新配置
            </Button>
          )}
          <XFormComponent wrappedComponentRef={this.resetBindForm} formData={formDataRole} formMapList={this.getRolMap()} />
        </Drawer>
      </>
    );
  }
}

interface IFormProps {
  wrappedComponentRef?: any;
  formData?: any;
  formMapList: any;
}

const XFormComponent = (props: IFormProps) => {
  const [form] = Form.useForm();

  const onValuesChange = (values) => {
    Object.keys(values).map((key) => {
      if (key === "typeName") {
        getClusterTemplateCentent(values.typeName).then((res) => {
          form.setFieldsValue({
            configData: res?.configData,
          });
        });
      }
      if (key === "enginName") {
        form.resetFields(["typeName", "configData"]);
      }
    });
  };

  const { formData, formMapList, wrappedComponentRef } = props;
  return (
    <Form ref={wrappedComponentRef} form={form} onValuesChange={onValuesChange} layout={"vertical"} className="base-info-form">
      {formMapList.map((row: IFormItem[], index: number) => {
        return (
          <Row key={index}>
            {row[0] && (
              <Col span={row.length === 1 ? 24 : 10} key={"col-1" + index} style={{ paddingRight: 20 }}>
                {!row[0].invisible ? (
                  <Form.Item
                    key={row[0].key}
                    label={row[0].label}
                    name={row[0].key}
                    rules={row[0].rules || [{ required: false, message: "" }]}
                    initialValue={handleFormItem(row[0], formData).initialValue}
                    valuePropName={handleFormItem(row[0], formData).valuePropName}
                    className="from-confing-item"
                  >
                    {renderFormItem(row[0])}
                  </Form.Item>
                ) : (
                  <Form.Item key={row[0].key}>-</Form.Item>
                )}
              </Col>
            )}
            {row[1] && (
              <Col span={10} key={"col-2" + index}>
                {!row[1].invisible ? (
                  <Form.Item
                    key={row[1].key}
                    label={row[1].label}
                    name={row[1].key}
                    rules={row[1].rules || [{ required: false, message: "" }]}
                    initialValue={handleFormItem(row[1], formData).initialValue}
                    valuePropName={handleFormItem(row[1], formData).valuePropName}
                    className="from-confing-item"
                  >
                    {renderFormItem(row[1])}
                  </Form.Item>
                ) : (
                  <Form.Item key={row[1].key}>-</Form.Item>
                )}
              </Col>
            )}
          </Row>
        );
      })}
    </Form>
  );
};

const ConfigNameSelect = connect(mapStateToProps)((props: any) => {
  const { disabled, value, index } = props;
  const opList = props.phyClusterConfig?.typeNameList[index]?.typeList || [];

  const handleChange = (params: any) => {
    const { onChange } = props;
    onChange && onChange(params);
  };

  const content = (val) => {
    if (val.disabled) {
      return (
        <Tooltip placement="right" title={"该配置已存在，不可重复添加"}>
          {val.label || val.value}
        </Tooltip>
      );
    }
    return val.label?.length > 35 || (val.value + "")?.length > 35 ? (
      <Tooltip placement="bottomLeft" title={val.label || val.value}>
        {val.label || val.value}
      </Tooltip>
    ) : (
      val.label || val.value
    );
  };
  return (
    <>
      <Select placeholder="请选择配置类别" showSearch={true} disabled={disabled} value={value} onChange={(e: any) => handleChange(e)}>
        {opList.map((v) => (
          <Select.Option key={v.value || v.label} value={v.value} disabled={v.disabled ? true : false}>
            {content(v)}
          </Select.Option>
        ))}
      </Select>
    </>
  );
});
