import * as React from "react";
import { XForm as XFormComponent } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { notification, Modal, Spin } from "antd";
import { physicalCopy } from "api/cluster-index-api";
import {
  getOpPhysicsClusterList,
  getCopyClusterPhyNames,
  getRack,
} from "api/cluster-api";
import { CodeSandboxCircleFilled, ConsoleSqlOutlined } from "@ant-design/icons";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const PhyCopyIndex = connect(mapStateToProps)(
  (props: { dispatch: any; cb: Function; params: number }) => {
    const [confirmLoading, setConfirmLoading] = React.useState(false);
    const [physicsClusterList, setPhysicsClusterList] = React.useState([]);
    const [rackList, setRackList] = React.useState([]);
    const [physicsCluster, setPhysicsCluster] = React.useState("");

    const $formRef: any = React.createRef();

    React.useEffect(() => {
      setConfirmLoading(true);
      getCopyClusterPhyNames(props.params)
        .then((data) => {
          setPhysicsClusterList(
            data.map((item) => {
              return {
                value: item,
                label: item,
              };
            })
          );
        })
        .finally(() => {
          setConfirmLoading(false);
        });
    }, []);

    React.useEffect(() => {
      if (!physicsCluster) {
        return;
      }
      setConfirmLoading(true);
      getRack(physicsCluster)
        .then((data) => {
          setRackList(
            data.map((item) => {
              return {
                value: item,
                label: item,
              };
            })
          );
        })
        .finally(() => {
          setConfirmLoading(false);
        });
    }, [physicsCluster]);

    const xFormModalConfig = {
      formMap: [
        {
          key: "cluster",
          label: "目标集群",
          type: FormItemType.select,
          options: physicsClusterList,
          attrs: {
            placeholder: "请选择复制到哪个集群",
            onChange: (val) => {
              setPhysicsCluster(val);
            },
          },
          rules: [{ required: true, message: "请填写模版名" }],
        },
        {
          key: "rack",
          label: "目标rack",
          type: FormItemType.select,
          options: rackList,
          attrs: {
            placeholder: "请选择复制到该集群的哪个rack",
          },
          rules: [{ required: true, message: "请填写模版rack" }],
        },
        {
          key: "shard",
          label: "shard个数",
          type: FormItemType.inputNumber,
          attrs: {
            placeholder: "请填写shard个数",
          },
          rules: [
            {
              required: true,
              message: "请输入大于等于0且小于等于100的数字",
              validator: (rule: any, value: number) => {
                if (typeof value != "number" || value < 0 || value > 100) {
                  return Promise.reject(false);
                }
                return Promise.resolve(true);
              },
            },
          ],
        },
        // {
        //   key: "upgrade",
        //   label: "模板版本",
        //   type: FormItemType.select,
        //   options: [
        //     {
        //       label: "升级",
        //       value: true,
        //     },
        //     {
        //       label: "不升级",
        //       value: false,
        //     },
        //   ],
        //   attrs: {
        //     placeholder: "请选择",
        //   },
        //   rules: [{ required: true, message: "请选择" }],
        // },
      ] as IFormItem[],
      visible: true,
      title: "复制",
      isWaitting: true,
      width: 600,
      onCancel: () => {
        props.dispatch(actions.setModalId(""));
      },
    };

    const handleSubmit = async () => {
      $formRef.current
        .validateFields()
        .then((result) => {
          result.physicalId = props.params;
          physicalCopy(result)
            .then(() => {
              notification.success({ message: `copy成功！` });
            })
            .finally(() => {
              props.dispatch(actions.setModalId(""));
            });
        })
        .catch((err) => {
          notification.error({ message: `copy 失败！请先验证格式` });
        });
    };

    return (
      <>
        <Modal
          width={xFormModalConfig.width}
          title={xFormModalConfig.title}
          visible={xFormModalConfig.visible}
          confirmLoading={confirmLoading}
          maskClosable={false}
          onOk={handleSubmit}
          onCancel={xFormModalConfig.onCancel}
          okText={"确定"}
          cancelText={"取消"}
        >
          <Spin spinning={confirmLoading}>
            {physicsClusterList.length ? (
              <XFormComponent
                wrappedComponentRef={$formRef}
                formData={{}}
                formMap={xFormModalConfig.formMap}
                layout={"vertical"}
              />
            ) : (
              "获取目标集群失败！"
            )}
          </Spin>
        </Modal>
      </>
    );
  }
);
