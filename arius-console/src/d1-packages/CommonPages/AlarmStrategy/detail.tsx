import React, { useState, useEffect } from "react";
import {
  Button,
  message,
  Drawer,
  Form,
  Select,
  Input,
  Row,
  Col,
  Radio,
  TreeSelect,
  Transfer,
  Tabs,
  Checkbox,
  TimePicker,
  Space,
  InputNumber,
  Table,
  Tag,
} from "antd";
import { readableForm } from "./config";
import { debounce } from "lodash";
import {
  queryMetrics,
  queryProjectDetail,
  createMonitorRule,
  updateMonitorRule,
  queryAlarmSettingList,
  queryMetricsMonitorRule,
  queryObjectNamesList,
  queryStatstype,
  queryOperator,
} from "./service";
import { CheckCircleFilled, MinusCircleFilled } from "@ant-design/icons";
import { MinusCircleOutlined, PlusOutlined } from "@ant-design/icons";
import "./index.less";
import moment from "moment";
const { TabPane } = Tabs;
const basicClass = "tpl-form";
enum Eflag {
  detail = "详情",
  create = "新增",
  update = "编辑",
}

interface IinitialValues {
  chargeUserIdList: [];
  deptId: number;
  description: string;
  isRunning: boolean;
  projectName: string;
}
const objectNamesListUrl = {
  1: "/v3/op/logic/cluster/clusterNames",
  2: "/v3/op/phy/cluster/names",
  3: "/v3/op/template/logic/listNames",
  4: "/v3/op/template/physical/listNames",
};
const statisticals = [
  { label: "且", value: 0 },
  { label: "或", value: 1 },
];
const { Option } = Select;
const { TreeNode } = TreeSelect;
export const AlarmStrategyDetail = (props: any) => {
  const { detailVisible, flag, closeDetail, submitCb, id } = props;
  const initialValues = {
    chargeUserIdList: [],
    deptId: 11,
    description: "",
    isRunning: true,
    projectName: "",
  };
  const format = "HH:mm";
  const [form] = Form.useForm();
  const [visible, setVisible] = useState(detailVisible);
  const [formModel, setFormModel] = useState(initialValues);
  const [mockData, setMockData] = useState([]);
  const [targetKeys, setTargetKeys] = useState([]);
  const [alarmObjects, setAlarmObjects] = useState([]);
  const [notifyGroups, setNotifyGroups] = useState([]);
  const [objectNamesList, setObjectNamesList] = useState([]);
  const [statistical, setStatistical] = useState([]);
  const [measurement, setMeasurement] = useState([]);
  const fetchAlarmSettingList = () => {
    const params = {
      appId: 1,
      name: "",
      members: "",
      operator: "",
      pageNo: 1,
      pageSize: 10000,
    };
    queryAlarmSettingList(params)
      .then((res: any) => {
        if (res) {
          setNotifyGroups(res.bizData);
        }
      })
      .finally(() => {});
  };
  const fetchMetrics = () => {
    queryMetrics()
      .then((res: any) => {
        if (res) {
          // setTargetKeys(targetKeys);
          const mockData = [];
          res.forEach((item) => {
            mockData.push({
              key: item,
              title: item,
              description: item,
              chosen: false,
            });
          });
          setMockData(mockData);
          // setQueryMetricsData(res.bizData);
        }
      })
      .finally(() => {});
  };
  const fetchMetricsMonitorRule = () => {
    queryMetricsMonitorRule()
      .then((res: any) => {
        if (res) {
          setAlarmObjects(res);
        }
      })
      .finally(() => {});
  };
  const fetchStatstype = () => {
    queryStatstype()
      .then((res: any) => {
        if (res) {
          setStatistical(res);
        }
      })
      .finally(() => {});
  };
  const fetchOperator = () => {
    queryOperator()
      .then((res: any) => {
        if (res) {
          setMeasurement(res);
        }
      })
      .finally(() => {});
  };
  //获取日志库列表
  const fetchObjectNamesList = (url) => {
    queryObjectNamesList(url)
      .then((res: any) => {
        if (res) {
          setObjectNamesList(res);
        }
      })
      .finally(() => {});
  };
  //表单提交
  const onSubmit = () => {
    form
      .validateFields()
      .then((values) => {
        submitForm(values);
      })
      .catch((err) => {
        console.log(err);
      });
  };
  //编辑和新增的表单提交
  const submitForm = async (formData: any) => {
    formData.objectNames = formData.objectNames.join();
    formData.enableDaysOfWeek = formData.enableDaysOfWeek.join();
    formData.notifyGroups = formData.notifyGroups.join();
    formData.notifyChannels = formData.notifyChannels.join();
    formData.metrics = formData.metrics.join();
    formData.enableStime = formData.enableStime.format("HH:mm");
    formData.enableEtime = formData.enableEtime.format("HH:mm");
    formData.appId = 1;
    formData.id = null;
    formData.metrics = targetKeys.join("");
    formData.togetherOrAny = formData.triggerConditions[0].togetherOrAny;
    console.log(formData);
    const res = flag === "create" ? await createMonitorRule(formData) : await updateMonitorRule({ ...formData, id });
    if (res) {
      message.success("提交成功");
      if (flag === "create") {
        form.resetFields();
      } else {
        submitCb();
      }
    }
  };

  const onClose = () => {
    closeDetail();
  };
  //处理编辑回填数据
  const formatUpdateData = (data) => {
    const formData = data.monitorRule;
    formData.enableDaysOfWeek = formData.enableDaysOfWeek.split(" ");
    formData.metrics = formData.metrics.split(",");
    formData.notifyChannels = formData.notifyChannels.split(" ");
    formData.notifyGroups = formData.notifyGroups.split(" ");
    formData.objectNames = formData.objectNames.split(",");
    formData.enableEtime = moment(formData.enableEtime, "HH:mm");
    formData.enableStime = moment(formData.enableStime, "HH:mm");
    formData.triggerConditions.forEach((item) => {
      item.togetherOrAny = formData.togetherOrAny;
    });
    setTargetKeys(formData.metrics);
    form.setFieldsValue(formData);
  };
  //处理详情回填数据
  const formatDetailData = (data) => {
    const formData = data.monitorRule;
    formData.enableDaysOfWeek = formData.enableDaysOfWeek.split(" ");
    formData.metrics = formData.metrics.split(",");
    formData.notifyChannels = formData.notifyChannels.split(" ");
    formData.notifyGroups = formData.notifyGroups.split(" ");
    formData.objectNames = formData.objectNames.split(",");
    formData.enableEtime = moment(formData.enableEtime, "HH:mm");
    formData.enableStime = moment(formData.enableStime, "HH:mm");
    formData.triggerConditions.forEach((item) => {
      item.togetherOrAny = formData.togetherOrAny;
    });
    formData.appName = data.appName;
    formData.categoryName = data.categoryName;
    setTargetKeys(formData.metrics);
    form.setFieldsValue(formData);
    setFormModel(formData);
  };
  //获取详情列表
  const fetchDetail = async (id) => {
    const data = await queryProjectDetail(id);
    if (flag === "detail") {
      formatDetailData(data);
    } else {
      formatUpdateData(data);
    }
  };

  //穿梭框
  const handleChange = (targetKeys) => {
    setTargetKeys(targetKeys);
    // this.setState({ targetKeys });
  };

  const handleSearch = (dir, value) => {
    // console.log('search:', dir, value);
  };

  const filterOption = (inputValue, option) => option.description.indexOf(inputValue) > -1;

  const renderAlarmObjects = (list) => {
    return list.map((item) => {
      return (
        <Option key={item.value} value={item.value}>
          {item.name}
        </Option>
      );
    });
  };
  const renderIconItem = (item) => {
    return formModel[item.prop] ? (
      <>
        <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
        <span>启用</span>
      </>
    ) : (
      <>
        <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
        <span>禁用</span>
      </>
    );
  };
  //渲染表格
  const readTable = () => {
    return readableForm.map((item, i) => {
      return (
        <Col key={item.prop} span={24} className={i && `${basicClass}-readonlyText`}>
          <span className="read-lable">{item.label}：</span>
          <span className="read-content">{item.prop === "isRunning" ? renderIconItem(item) : formModel[item.prop]}</span>
        </Col>
      );
    });
  };
  function projectChange(value) {
    fetchObjectNamesList(objectNamesListUrl[value]);
  }
  //筛选逻辑变化方法，其中一个变化，其他的一起变化
  const statisticalsChange = (data: any) => {
    const formData = form.getFieldsValue(true);
    formData.triggerConditions.forEach((item) => {
      item.togetherOrAny = data;
    });
    form.setFieldsValue(formData);
  };
  //详情只读
  const renderReadCol = () => {
    return (
      <>
        {readTable()}
        <Tabs defaultActiveKey="1">
          <TabPane
            tab={
              <span>
                <span style={{ color: "#EF645C" }}>*</span>
                <span>监控对象</span>
              </span>
            }
            key="1"
          >
            <div>
              {targetKeys.length != 0
                ? targetKeys.map((item) => {
                    return (
                      <Tag key={item} color="processing">
                        {item}
                      </Tag>
                    );
                  })
                : null}
            </div>
          </TabPane>
          <TabPane
            tab={
              <span>
                <span style={{ color: "#EF645C" }}>*</span>
                <span>触发规则</span>
              </span>
            }
            key="2"
          >
            <Row>
              <Form.Item
                name="enableDaysOfWeek"
                style={{ display: "inline" }}
                label="触发时间"
                rules={[{ required: true, message: "请选择监控指标" }]}
              >
                <Checkbox.Group>
                  <span>每周</span>
                  <Checkbox disabled value="1" style={{ lineHeight: "32px" }}>
                    周一
                  </Checkbox>

                  <Checkbox disabled value="2" style={{ lineHeight: "32px" }}>
                    周二
                  </Checkbox>

                  <Checkbox disabled value="3" style={{ lineHeight: "32px" }}>
                    周三
                  </Checkbox>

                  <Checkbox disabled value="4" style={{ lineHeight: "32px" }}>
                    周四
                  </Checkbox>

                  <Checkbox disabled value="5" style={{ lineHeight: "32px" }}>
                    周五
                  </Checkbox>

                  <Checkbox disabled value="6" style={{ lineHeight: "32px" }}>
                    周六
                  </Checkbox>

                  <Checkbox disabled value="7" style={{ lineHeight: "32px" }}>
                    周日
                  </Checkbox>
                </Checkbox.Group>
                {/* <TimePicker style={{ width: "60px" }} defaultValue={moment("12", format)} format={format} />
                <span style={{ padding: "0 10px" }}>至</span>
                <TimePicker style={{ width: "60px" }} defaultValue={moment("12", format)} format={format} /> */}
              </Form.Item>
              <Form.Item
                name="enableStime"
                style={{ display: "inline", marginLeft: "20px" }}
                label="开始时间 "
                rules={[{ required: true, message: "请选择监控开始时间" }]}
              >
                <TimePicker disabled style={{ width: "100px" }} defaultValue={moment("00:00", format)} format={format} />
                {/* <span style={{ padding: "0 10px" }}>至</span> */}
              </Form.Item>
              <Form.Item
                name="enableEtime"
                style={{ display: "inline", marginLeft: "20px" }}
                label="结束时间"
                rules={[{ required: true, message: "请选择监控结束时间" }]}
              >
                <TimePicker disabled style={{ width: "100px" }} defaultValue={moment("00:00", format)} format={format} />
              </Form.Item>
            </Row>

            <p className="early-warning-title">
              <span className="text">满足条件，则触发报警</span>
            </p>

            <Form.List name="triggerConditions">
              {(fields, { add, remove }) => (
                <>
                  {fields.map((field) => (
                    <Space key={field.key} align="baseline">
                      <Form.Item
                        noStyle
                        shouldUpdate={(prevValues, curValues) =>
                          prevValues.area !== curValues.area || prevValues.sights !== curValues.sights
                        }
                      >
                        {() => (
                          <Form.Item
                            {...field}
                            label=""
                            name={[field.name, "func"]}
                            fieldKey={[field.fieldKey, "func"]}
                            rules={[{ required: true, message: "请选择统计方式" }]}
                          >
                            <Select disabled style={{ width: 120 }} placeholder="请选择统计方式">
                              {statistical.map((item) => (
                                <Option key={item.value} value={item.value}>
                                  {item.name}
                                </Option>
                              ))}
                            </Select>
                          </Form.Item>
                        )}
                      </Form.Item>
                      <Form.Item
                        {...field}
                        label=""
                        name={[field.name, "num"]}
                        fieldKey={[field.fieldKey, "num"]}
                        rules={[{ required: false, message: "请输入数值" }]}
                      >
                        在最近
                        <Input disabled style={{ width: 100 }} />
                        个周期内
                      </Form.Item>
                      <Form.Item
                        noStyle
                        shouldUpdate={(prevValues, curValues) =>
                          prevValues.area !== curValues.area || prevValues.sights !== curValues.sights
                        }
                      >
                        {() => (
                          <Form.Item
                            {...field}
                            label=""
                            name={[field.name, "optr"]}
                            fieldKey={[field.fieldKey, "optr"]}
                            rules={[{ required: true, message: "请选择度量方式" }]}
                          >
                            <Select disabled style={{ width: 120 }} placeholder="请选择度量方式">
                              {measurement.map((item) => (
                                <Option key={item.value} value={item.value}>
                                  {item.name}
                                </Option>
                              ))}
                            </Select>
                          </Form.Item>
                        )}
                      </Form.Item>
                      <Form.Item
                        {...field}
                        label=""
                        name={[field.name, "threshold"]}
                        fieldKey={[field.fieldKey, "threshold"]}
                        rules={[{ required: true, message: "请输入数值" }]}
                      >
                        <Input disabled placeholder="请输入数值" style={{ width: 100 }} />
                      </Form.Item>

                      <Form.Item
                        {...field}
                        label=""
                        name={[field.name, "togetherOrAny"]}
                        rules={[{ required: true, message: "请选择逻辑条件" }]}
                        fieldKey={[field.fieldKey, "togetherOrAny"]}
                      >
                        <Select disabled style={{ width: 60 }} onChange={statisticalsChange} placeholder="请选择逻辑条件">
                          {statisticals.map((item) => (
                            <Option key={item.value} value={item.value}>
                              {item.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                      <MinusCircleOutlined onClick={() => remove(field.name)} />
                    </Space>
                  ))}

                  {/* <Form.Item>
                    <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                      增加规则
                    </Button>
                  </Form.Item> */}
                </>
              )}
            </Form.List>
          </TabPane>
          <TabPane
            tab={
              <span>
                <span style={{ color: "#EF645C" }}>*</span>
                <span>告警规则</span>
              </span>
            }
            key="3"
          >
            <Form.Item name="priority" label="报警级别:">
              <Radio.Group disabled>
                <Radio value={1}>一级警告</Radio>
                <Radio value={2}>二级警告</Radio>
                <Radio value={3}>三级警告</Radio>
              </Radio.Group>
            </Form.Item>
            <Form.Item name="notifyChannels" label="通知方式:">
              <Checkbox.Group style={{ width: "100%" }}>
                <Row>
                  <Col span={3}>
                    <Checkbox disabled value="voice" style={{ lineHeight: "32px" }}>
                      电话
                    </Checkbox>
                  </Col>
                  <Col span={3}>
                    <Checkbox disabled value="email" style={{ lineHeight: "32px" }}>
                      邮件
                    </Checkbox>
                  </Col>
                  <Col span={3}>
                    <Checkbox disabled value="sms" style={{ lineHeight: "32px" }}>
                      短信
                    </Checkbox>
                  </Col>
                  <Col span={3}>
                    <Checkbox disabled value="dingtalk" style={{ lineHeight: "32px" }}>
                      钉钉
                    </Checkbox>
                  </Col>
                </Row>
              </Checkbox.Group>
            </Form.Item>
            <Form.Item name="notifyGroups" label="告警组:" rules={[{ required: true, message: "请选择告警组!" }]}>
              <Select disabled mode="multiple" allowClear placeholder="请选择告警组">
                {notifyGroups.map((item) => (
                  <Option key={item.id} value={item.id}>
                    {item.name}
                  </Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="notifyUsers" label="告警用户:" rules={[{ required: true, message: "请输入告警用户！" }]}>
              <Input disabled placeholder="请输入告警用户" />
            </Form.Item>
            <Form.Item label="回调地址:" name="price" rules={[{ message: "请输入回调地址" }]}>
              <Input disabled />
            </Form.Item>
          </TabPane>
        </Tabs>
      </>
    );
  };
  //策略编辑和新建
  const renderWriteCol = () => {
    return (
      <>
        <Col span={24}>
          <Form.Item label="告警名称" name="name" rules={[{ required: true, message: "请输入告警名称" }]}>
            <Input placeholder="请输入告警名称" maxLength={128} />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="告警对象" style={{ marginBottom: 0 }} rules={[{ required: true }]}>
            <Form.Item
              style={{ display: "inline-block", width: "20%" }}
              label=""
              name="category"
              rules={[{ required: true, message: "请选择项目" }]}
            >
              <Select allowClear onChange={projectChange} placeholder="请选择项目">
                {renderAlarmObjects(alarmObjects)}
              </Select>
            </Form.Item>
            <Form.Item
              style={{ display: "inline-block", width: "80%" }}
              label=""
              name="objectNames"
              rules={[{ required: true, message: "请选择日志库" }]}
            >
              <Select mode="multiple" allowClear placeholder="请选择日志库">
                {objectNamesList.map((item) => (
                  <Option key={item} value={item}>
                    {item}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Form.Item>
        </Col>

        <Col span={24}>
          <Form.Item label="监控指标" name="metrics">
            <Transfer
              dataSource={mockData}
              showSearch
              filterOption={filterOption}
              targetKeys={targetKeys}
              onChange={handleChange}
              onSearch={handleSearch}
              render={(item) => item.title}
            />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Tabs defaultActiveKey="1">
            <TabPane
              tab={
                <span>
                  <span style={{ color: "#EF645C" }}>*</span>
                  <span>触发规则</span>
                </span>
              }
              key="1"
            >
              <Row>
                <Form.Item
                  name="enableDaysOfWeek"
                  style={{ display: "inline" }}
                  label="触发时间"
                  rules={[{ required: true, message: "请选择监控指标" }]}
                >
                  <Checkbox.Group>
                    <span>每周</span>
                    <Checkbox value="1" style={{ lineHeight: "32px" }}>
                      周一
                    </Checkbox>

                    <Checkbox value="2" style={{ lineHeight: "32px" }}>
                      周二
                    </Checkbox>

                    <Checkbox value="3" style={{ lineHeight: "32px" }}>
                      周三
                    </Checkbox>

                    <Checkbox value="4" style={{ lineHeight: "32px" }}>
                      周四
                    </Checkbox>

                    <Checkbox value="5" style={{ lineHeight: "32px" }}>
                      周五
                    </Checkbox>

                    <Checkbox value="6" style={{ lineHeight: "32px" }}>
                      周六
                    </Checkbox>

                    <Checkbox value="7" style={{ lineHeight: "32px" }}>
                      周日
                    </Checkbox>
                  </Checkbox.Group>
                  {/* <TimePicker style={{ width: "60px" }} defaultValue={moment("12", format)} format={format} />
                <span style={{ padding: "0 10px" }}>至</span>
                <TimePicker style={{ width: "60px" }} defaultValue={moment("12", format)} format={format} /> */}
                </Form.Item>
                <Form.Item
                  name="enableStime"
                  style={{ display: "inline", marginLeft: "20px" }}
                  label="开始时间 "
                  rules={[{ required: true, message: "请选择监控开始时间" }]}
                >
                  <TimePicker style={{ width: "100px" }} defaultValue={moment("00:00", format)} format={format} />
                  {/* <span style={{ padding: "0 10px" }}>至</span> */}
                </Form.Item>
                <Form.Item
                  name="enableEtime"
                  style={{ display: "inline", marginLeft: "20px" }}
                  label="结束时间"
                  rules={[{ required: true, message: "请选择监控结束时间" }]}
                >
                  <TimePicker style={{ width: "100px" }} defaultValue={moment("00:00", format)} format={format} />
                </Form.Item>
              </Row>

              <p className="early-warning-title">
                <span className="text">满足条件，则触发报警</span>
              </p>

              <Form.List name="triggerConditions">
                {(fields, { add, remove }) => (
                  <>
                    {fields.map((field) => (
                      <Space key={field.key} align="baseline">
                        <Form.Item
                          noStyle
                          shouldUpdate={(prevValues, curValues) =>
                            prevValues.area !== curValues.area || prevValues.sights !== curValues.sights
                          }
                        >
                          {() => (
                            <Form.Item
                              {...field}
                              label=""
                              name={[field.name, "func"]}
                              fieldKey={[field.fieldKey, "func"]}
                              rules={[{ required: true, message: "请选择统计方式" }]}
                            >
                              <Select style={{ width: 120 }} placeholder="请选择统计方式">
                                {statistical.map((item) => (
                                  <Option key={item.value} value={item.value}>
                                    {item.name}
                                  </Option>
                                ))}
                              </Select>
                            </Form.Item>
                          )}
                        </Form.Item>
                        <Form.Item
                          {...field}
                          label=""
                          name={[field.name, "num"]}
                          fieldKey={[field.fieldKey, "num"]}
                          rules={[{ required: false, message: "请输入数值" }]}
                        >
                          在最近
                          <Input style={{ width: 100 }} />
                          个周期内
                        </Form.Item>
                        <Form.Item
                          noStyle
                          shouldUpdate={(prevValues, curValues) =>
                            prevValues.area !== curValues.area || prevValues.sights !== curValues.sights
                          }
                        >
                          {() => (
                            <Form.Item
                              {...field}
                              label=""
                              name={[field.name, "optr"]}
                              fieldKey={[field.fieldKey, "optr"]}
                              rules={[{ required: true, message: "请选择度量方式" }]}
                            >
                              <Select style={{ width: 120 }} placeholder="请选择度量方式">
                                {measurement.map((item) => (
                                  <Option key={item.value} value={item.value}>
                                    {item.name}
                                  </Option>
                                ))}
                              </Select>
                            </Form.Item>
                          )}
                        </Form.Item>
                        <Form.Item
                          {...field}
                          label=""
                          name={[field.name, "threshold"]}
                          fieldKey={[field.fieldKey, "threshold"]}
                          rules={[{ required: true, message: "请输入数值" }]}
                        >
                          <Input placeholder="请输入数值" style={{ width: 100 }} />
                        </Form.Item>

                        <Form.Item
                          {...field}
                          label=""
                          name={[field.name, "togetherOrAny"]}
                          rules={[{ required: true, message: "请选择逻辑条件" }]}
                          fieldKey={[field.fieldKey, "togetherOrAny"]}
                        >
                          <Select style={{ width: 60 }} onChange={statisticalsChange} placeholder="请选择逻辑条件">
                            {statisticals.map((item) => (
                              <Option key={item.value} value={item.value}>
                                {item.label}
                              </Option>
                            ))}
                          </Select>
                        </Form.Item>
                        <MinusCircleOutlined onClick={() => remove(field.name)} />
                      </Space>
                    ))}

                    <Form.Item>
                      <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                        增加规则
                      </Button>
                    </Form.Item>
                  </>
                )}
              </Form.List>
            </TabPane>
            <TabPane
              tab={
                <span>
                  <span style={{ color: "#EF645C" }}>*</span>
                  <span>告警规则</span>
                </span>
              }
              key="2"
            >
              <Form.Item name="priority" label="报警级别:">
                <Radio.Group>
                  <Radio value={1}>一级警告</Radio>
                  <Radio value={2}>二级警告</Radio>
                  <Radio value={3}>三级警告</Radio>
                </Radio.Group>
              </Form.Item>
              <Form.Item name="notifyChannels" label="通知方式:">
                <Checkbox.Group style={{ width: "100%" }}>
                  <Row>
                    <Col span={3}>
                      <Checkbox value="voice" style={{ lineHeight: "32px" }}>
                        电话
                      </Checkbox>
                    </Col>
                    <Col span={3}>
                      <Checkbox value="email" style={{ lineHeight: "32px" }}>
                        邮件
                      </Checkbox>
                    </Col>
                    <Col span={3}>
                      <Checkbox value="sms" style={{ lineHeight: "32px" }}>
                        短信
                      </Checkbox>
                    </Col>
                    <Col span={3}>
                      <Checkbox value="dingtalk" style={{ lineHeight: "32px" }}>
                        钉钉
                      </Checkbox>
                    </Col>
                  </Row>
                </Checkbox.Group>
              </Form.Item>
              <Form.Item name="notifyGroups" label="告警组:" rules={[{ required: true, message: "请选择告警组!" }]}>
                <Select mode="multiple" allowClear placeholder="请选择告警组">
                  {notifyGroups.map((item) => (
                    <Option key={item.id} value={item.id}>
                      {item.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item name="notifyUsers" label="告警用户:" rules={[{ required: true, message: "请输入告警用户！" }]}>
                <Input placeholder="请输入告警用户" />
              </Form.Item>
              <Form.Item label="回调地址:" name="price" rules={[{ message: "请输入回调地址" }]}>
                <Input />
              </Form.Item>
            </TabPane>
          </Tabs>
        </Col>
      </>
    );
  };
  //底部保存和关闭
  const renderFooter = () => {
    return (
      <div
        style={{
          textAlign: "left",
        }}
      >
        {flag === "detail" ? (
          <Button onClick={onClose} type="primary">
            关闭
          </Button>
        ) : (
          <Button onClick={onSubmit} type="primary">
            {flag === "create" ? "保存并新增" : "保存"}
          </Button>
        )}
      </div>
    );
  };

  useEffect(() => {
    setVisible(detailVisible);
  }, [detailVisible]);

  useEffect(() => {
    flag !== "create" ? fetchDetail(id) : ""; //新建不会用到ID，
    fetchAlarmSettingList();
    fetchMetrics();
    fetchMetricsMonitorRule();
    fetchStatstype();
    fetchOperator();
  }, []);

  return (
    <Drawer
      width="1000"
      title={Eflag[flag] || ""}
      onClose={onClose}
      visible={visible}
      bodyStyle={{ paddingBottom: 80 }}
      footer={renderFooter()}
    >
      <div>
        <Form layout="horizontal" initialValues={initialValues} form={form}>
          <Row>{flag === "detail" ? renderReadCol() : renderWriteCol()}</Row>
        </Form>
      </div>
    </Drawer>
  );
};
