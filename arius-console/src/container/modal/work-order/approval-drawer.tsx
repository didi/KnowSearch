import React, { useState, useEffect, useRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType } from "component/x-form";
import { approvalOrder } from "api/order-api";
import { IApprovalOrder, IOrderInfo } from "typesPath/cluster/order-types";
import { AppState, UserState } from "store/type";
import { XNotification } from "component/x-notification";
import { RenderText } from "container/custom-form";
import RegionTransfer from "container/custom-form/region-transfer";
import { getRegionNode, getOpPhysicsClusterList, IClusterList } from "api/cluster-api";
import { logicClusterRegionList } from "api/op-cluster-region-api";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

export const ShowApprovalDrawer = connect(mapStateToProps)(
  (props: { params: IOrderInfo; dispatch: Function; app: AppState; cb: Function; user: UserState }) => {
    const { id, type, detailInfo, outcome } = props.params;

    const [transferList, setTransferList] = useState([]);
    const [targetKeys, setTargetKeys] = useState([]); //数据源改变时要进行设置
    const [leftTargetKeys, setLeftTargetKeys] = useState([]);
    const [logicRegionList, setLogicRegionList] = useState([]);
    const [selectKeys, setSelectKeys] = useState([]);
    const [regionName, setRegionName] = useState("");
    const selectData = useRef([]);
    const allData = useRef([]);
    const $ref: any = useRef();

    useEffect(() => {
      if (type === "logicClusterIndecrease") {
        //通过逻辑集群id获取逻辑集群下的region列表
        logicClusterRegionList(detailInfo.logicClusterId)
          .then((res) => {
            if (res && res.length) {
              res = res.map((item) => {
                return {
                  ...item,
                  value: JSON.stringify(item),
                  label: item.name || item.id,
                };
              });
              setLogicRegionList(res);
              $ref?.current?.form.setFieldsValue({ RegionList: res[0].value });
              //通过region里面的物理集群名称获取对应物理集群信息
              const params: IClusterList = {
                page: 1,
                size: 10,
                cluster: res[0].clusterName,
              };
              return getOpPhysicsClusterList(params);
            }
          })
          .then((res) => {
            if (res && res.bizData.length) {
              //通过上个接口返回的物理集群id获取物理集群下的ip节点
              return getRegionNode(res?.bizData[0].id);
            }
          })
          .then((res) => {
            if (res && res.length) {
              let arr = [];
              res = res.map((item) => {
                return {
                  ...item,
                  key: item.id,
                };
              });
              allData.current = JSON.parse(JSON.stringify(res)); //保存穿梭框初始值数据源(静态)
              const currentSelectObj = $ref?.current?.form && JSON.parse($ref?.current?.form.getFieldsValue().RegionList);
              //transferList指代ip节点列表
              res.map((item) => {
                //过滤出相等的regionid和未分配的region
                if (item.regionId == currentSelectObj.id) {
                  arr.push({ ...item });
                  setRegionName(item.regionName);
                } else if (item.regionId === -1) {
                  arr.push({ ...item });
                }
              });
              getTransferData(arr);
            }
          });
      }
    }, []);

    const getTransferData = (arr) => {
      let copyArr = JSON.parse(JSON.stringify(arr));
      let targetTransKey = getTargetTransferList(copyArr);
      setTargetKeys(targetTransKey);
      if (detailInfo?.dataNodeNu > detailInfo?.oldDataNodeNu) {
        // 期望节点数大于原始节点数，扩容，不允许操作右侧原始节点，置灰
        copyArr = (copyArr || []).map((item) => {
          if (targetTransKey?.includes(item?.id)) {
            return {
              ...item,
              disabled: true,
            };
          }
          return item;
        });
      } else {
        // 期望节点数小于原始节点数，缩容，不允许操作左侧原始节点，置灰
        copyArr = (copyArr || []).map((item) => {
          if (!targetTransKey?.includes(item?.id)) {
            return {
              ...item,
              disabled: true,
            };
          }
          return item;
        });
      }
      setTransferList(copyArr); //change变化的时候需要设置一下数据源（动态）
      selectData.current = copyArr; //保存当前下拉选中时的数据
    };

    const getTargetTransferList = (data) => {
      //过滤出被分配过的ip节点
      let list = data.filter((item) => item.regionId > -1);
      let res = list.map((t) => t.key);
      return res;
    };

    const xFormModalConfigFun = () => {
      const formtTemplateIndecreaseMap = [
        {
          key: "clusterName",
          label: "集群名称",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.detailInfo.logicClusterName || "-"} />,
        },
        {
          key: "BelongApplication",
          label: "所属应用",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.applicantAppName} />,
        },
        {
          key: "oldDataNodeNu",
          label: "data节点数",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.detailInfo?.oldDataNodeNu || "-"} />,
        },
        {
          key: "dataNodeNu",
          label: "期望节点数",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.detailInfo?.dataNodeNu || "-"} />,
        },
        {
          key: "dataNodeSpec",
          label: "节点规格",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.detailInfo?.dataNodeSpec || "-"} />,
        },
        {
          key: "RegionList",
          label: "Region",
          type: FormItemType.select,
          options: logicRegionList,
          rules: [{ required: true, message: "请选择Region" }],
          attrs: {
            onChange: (e) => {
              const eventValueObj = JSON.parse(e);
              $ref?.current?.form.validateFields(["regionObj"]);
              //下拉框值变化时，对数据源进行处理，同时也要对targetKey进行处理
              ///根据逻辑集群rejon过滤出与之对应的节点
              let arr = [];
              //transferList指代ip节点列表
              if (transferList.length && allData.current.length) {
                allData.current.map((item) => {
                  //过滤出相等的regionid和未分配的region
                  if (item.regionId == eventValueObj.id) {
                    arr.push({ ...item });
                    setRegionName(item.regionName);
                  } else if (item.regionId === -1) {
                    arr.push({ ...item });
                  }
                });
                getTransferData(arr);
              }
            },
          },
        },
        {
          key: "regionObj",
          type: FormItemType.custom,
          label: "节点列表",
          customFormItem: (
            <RegionTransfer
              dataSource={transferList}
              selectKeys={(keys) => {
                //在进行穿梭框之前要对上面下拉框进行验证操作
                $ref?.current?.form.validateFields(["RegionList", "regionObj"]).then(
                  (res) => {},
                  (rej) => {
                    console.log(rej);
                  }
                );
                //本属性会在onchange里面调用
                //leftKey,未分配的节点
                let leftKey = [];
                if (!keys.length) {
                  selectData.current.map((i) => {
                    leftKey.push(i.id);
                  });
                } else if (keys.length == selectData.current.length) {
                  leftKey = [];
                } else {
                  let allKey = [];
                  selectData.current.length &&
                    selectData.current.map((item) => {
                      allKey.push(item.id);
                    });
                  const left = allKey.concat(keys).filter((v, i, arr) => {
                    return arr.indexOf(v) === arr.lastIndexOf(v);
                  });
                  leftKey.push(...left);
                }
                setLeftTargetKeys(leftKey); //左侧的key
                setSelectKeys(keys); //相当于targetKey
              }}
              targetKeys={targetKeys}
              isExpand={detailInfo?.dataNodeNu > detailInfo?.oldDataNodeNu}
            />
          ),
          rules: [
            {
              required: true,
              whitespace: true,
              validator: async (rule: any, value: any) => {
                if (!leftTargetKeys.length && !selectKeys.length) {
                  return Promise.reject("请选择region节点列表！");
                }
                return Promise.resolve();
              },
            },
          ],
        },
      ] as any;

      const xFormModalConfig = {
        formMap: [
          {
            key: "id",
            label: "工单ID",
            type: FormItemType.text,
            className: "approval-id",
            customFormItem: <RenderText text={id} />,
          },
          {
            key: "opinion",
            label: "审批意见",
            type: FormItemType.textArea,
            attrs: {
              placeholder: "请输入1-100字审核意见",
            },
            rules: [
              {
                required: true,
                whitespace: true,
                validator: async (rule: any, value: any) => {
                  if (!value) {
                    return Promise.reject("请输入1-100字审核意见!");
                  }
                  if (value && value.length > 100) {
                    return Promise.reject("请输入1-100字审核意见!");
                  }
                  return Promise.resolve();
                },
              },
            ],
          },
        ],
        width: 1080,
        okText: outcome === "agree" ? "通过" : "驳回",
        visible: true,
        title: "审批",
        type: "drawer",
        needBtnLoading: true,
        //formData: { RegionList: logicRegionList && logicRegionList[0] ? logicRegionList[0].value : '' },
        onCancel: () => {
          props.dispatch(actions.setDrawerId(""));
        },
        onSubmit: (value: any) => {
          let contentObj = {} as any;
          if (outcome === "agree") {
            if (type === "logicClusterIndecrease") {
              const regionValue = JSON.parse(value.RegionList);
              contentObj = {
                regionWithNodeInfo: [
                  {
                    id: regionValue.id || "",
                    name: regionName || "",
                    phyClusterName: regionValue.clusterName || "",
                    logicClusterIds: detailInfo.logicClusterId || "",
                    config: regionValue.config || "",
                    bindingNodeIds: selectKeys,
                    unBindingNodeIds: leftTargetKeys,
                  },
                ],
              };
            }
          }

          const orderParams = {
            assignee: props.user.getName("userName"),
            checkAuthority: false,
            comment: value.opinion ? value.opinion.trim() : "",
            orderId: id + "",
            outcome,
            assigneeProjectId: props.app.appInfo()?.id,
            contentObj,
          } as unknown as IApprovalOrder;
          return approvalOrder(orderParams).then((res) => {
            let msg = outcome === "agree" ? "通过成功" : "驳回成功";
            if (res?.message) {
              msg = res?.description;
            }
            XNotification({ type: "success", message: msg });
            props.dispatch(actions.setDrawerId(""));
            props.cb();
          });
        },
      };

      if (outcome === "agree") {
        if (type === "logicClusterIndecrease") {
          xFormModalConfig.formMap.splice(1, 0, ...formtTemplateIndecreaseMap);
        }
      }
      return xFormModalConfig;
    };

    return (
      <>
        <XFormWrapper ref={$ref} visible={true} {...xFormModalConfigFun()} />
      </>
    );
  }
);
