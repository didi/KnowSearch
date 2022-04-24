import React from "react";
import { dcdrInfo } from "./config";
import "./index.less";
import { BaseDetail } from "component/dantd/base-detail";
import Url from "lib/url-parser";
import { getDcdrInfo, createDcdr, deteleDcdr } from "api/dcdr-api";
import { Modal, Select, message, Button, Spin, Form, Tooltip, Empty } from "antd";
import { getClusterNameList } from "api/cluster-kanban";
import { getPhysicalTemplateIndexDetail } from "api/cluster-index-api";
import { getPhyClusterRacks } from "api/op-cluster-region-api";
import { isOpenUp } from "constants/common";

const Option = Select.Option;

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class DcdrInfo extends React.Component<any, any> {
  private id: number;
  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.state = {
      data: {},
      loading: false,
    };
  }

  public reloadData = () => {
    this.setState({ loading: true });
    getDcdrInfo(this.id).then((res) => {
      if (res !== null) {
        this.setState({
          data: res,
          loading: false,
        });
      }
    });
  };

  componentDidMount() {
    this.reloadData();
  }

  public render() {
    const mainInfo = [
      [
        {
          key: "masterClusterName",
          label: "未建立DCDR链路",
          render: (text) => {
            return text || "";
          },
        },
      ],
    ];
    return (
      <Spin spinning={this.state.loading}>
        <div className="tpl-dcdr">
          {isOpenUp ? (
            <div className="tpl-dcdr-btnbox">
              <Tooltip title="该功能仅面向商业版客户开放">
                <Button ghost type="primary" style={{ marginRight: 8 }} disabled={isOpenUp}>
                  删除DCDR
                </Button>
              </Tooltip>
              <Tooltip title="该功能仅面向商业版客户开放">
                <Button type="primary" disabled={isOpenUp}>
                  创建DCDR
                </Button>
              </Tooltip>
            </div>
          ) : (
            <div className="tpl-dcdr-btnbox">
              <DeleteDcdr templateId={this.id} reload={this.reloadData}>
                <Button ghost type="primary" style={{ marginRight: 8 }} disabled={!this.state.data?.dcdrFlag}>
                  删除DCDR
                </Button>
              </DeleteDcdr>
              <CreateDcdr templateId={this.id} reload={this.reloadData}>
                <Button type="primary" disabled={this.state.data?.dcdrFlag}>
                  创建DCDR
                </Button>
              </CreateDcdr>
            </div>
          )}
          {this.state.data?.dcdrFlag ? (
            <BaseDetail columns={dcdrInfo} baseDetail={this.state.data} />
          ) : (
            <div>
              <Empty style={{ margin: "20px 0px" }} image={Empty.PRESENTED_IMAGE_SIMPLE} />
            </div>
          )}
        </div>
      </Spin>
    );
  }
}

export const CreateDcdr: React.FC<any> = (props: any) => {
  const [list, setList] = React.useState([]);
  const [form] = Form.useForm();
  const [visible, setVisible] = React.useState(false);
  const [loading, setLoading] = React.useState(false);
  const [rackList, setRackList] = React.useState([]);
  const [regionList, setRegionList] = React.useState([]);
  // /v3/op/phy/cluster/region/phyClusterRacks?cluster=admin_test_1
  // /v3/op/template/physical/20037

  React.useEffect(() => {
    getClusterNameList().then((res) => {
      if (res && res?.length) {
        setList(res);
      }
    });
    getDefult();
  }, []);

  const getList = (cluster) => {
    getPhyClusterRacks(cluster).then((res) => {
      const arr = [];
      const value = [];
      if (res && res.length) {
        res.forEach((item) => {
          item.rack.split(",").forEach((item) => {
            if (!arr.includes(item)) {
              arr.push(item);
            }
            if (rackList.includes(item) && !value.includes(item)) {
              value.push(item);
            }
          });
        });
      }
      form.setFieldsValue({ rack: value });
      setRegionList(arr);
    });
  };

  const getDefult = () => {
    getPhysicalTemplateIndexDetail(Number(Url().search.id)).then((res) => {
      const arr = [];
      if (res && res.length) {
        res.forEach((item) => {
          item.rack.split(",").forEach((item) => {
            if (!arr.includes(item)) {
              arr.push(item);
            }
          });
        });
      }
      setRackList(arr);
    });
  };

  const onCancel = () => {
    setVisible(false);
  };

  const show = () => {
    setVisible(true);
  };

  const onOk = () => {
    form
      .validateFields()
      .then((values) => {
        if (values && values.value) {
          setLoading(true);
          createDcdr(props.templateId, values.value, values.rack).then((res) => {
            setLoading(false);
            setVisible(false);
            props.reload();
            message.success("操作成功");
          });
        }
      })
      .finally(() => {
        setLoading(false);
        setVisible(false);
      });
  };

  return (
    <>
      <span onClick={show}>{props.children}</span>
      <Modal title={"创建DCDR"} width={500} visible={visible} onCancel={onCancel} onOk={onOk} confirmLoading={loading}>
        <div style={{ padding: 20, minWidth: 400 }}>
          <Form form={form} layout="vertical">
            <Form.Item label="从集群" rules={[{ required: true, message: "请选择集群" }]} name="value">
              <Select style={{ minWidth: 200 }} placeholder="请选择集群" onChange={(e) => getList(e)} showSearch>
                {list?.map((item, index) => (
                  <Option value={item} key={`${item}@${index}`}>
                    {item}
                  </Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="rack" label="rack" rules={[{ required: true, message: "请选择rack" }]}>
              <Select
                mode="multiple"
                allowClear
                // style={{ width: 250}}
                placeholder="请选择rack"
              >
                {regionList.map((item) => (
                  <Select.Option value={item} key={item}>
                    {item}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  );
};

export const DeleteDcdr: React.FC<any> = (props) => {
  const [visible, setVisible] = React.useState(false);

  const onCancel = () => {
    setVisible(false);
  };

  const show = () => {
    setVisible(true);
  };

  const onOk = () => {
    deteleDcdr(props.templateId).then((res) => {
      setVisible(false);
      props.reload();
      message.success("操作成功");
    });
  };

  return (
    <>
      <span onClick={show}>{props.children}</span>
      <Modal title={"删除DCDR"} width={500} visible={visible} onCancel={onCancel} onOk={onOk}>
        确定删除DCDR链路吗？
      </Modal>
    </>
  );
};
