import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType } from "component/x-form";
import { Tooltip } from "antd";
import { approvalOrder } from "api/order-api";
import { IApprovalOrder, IOrderInfo } from "typesPath/cluster/order-types";
import { AppState, UserState } from "store/type";
import { XNotification } from "component/x-notification";
import { RenderText, RelevanceRegion } from "container/custom-form";
import { getPhysicsClusterList } from "api/cluster-api";
import { LEVEL_MAP } from "constants/common";
import { useDispatch } from "react-redux";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ShowApprovalModal = (props: { params: IOrderInfo; dispatch: Function; app: AppState; cb: Function; user: UserState }) => {
  const {
    id,
    type,
    detailInfo: { clusterPhyNameList = [], clusterLogicName, diskQuota },
  } = props.params;
  const dispatch = useDispatch();
  //获取物理集群列表数据
  const getPhyClusterList = (type: number) => {
    getPhysicsClusterList(type).then((res) => {
      if (res) {
        res = res.map((item) => {
          return {
            value: item,
            label: item,
          };
        });

        dispatch(actions.setPhyClusterList(res, type + ""));
      }
    });
  };
  React.useEffect(() => {
    if (props.params.type === "logicClusterCreate") {
      getPhyClusterList(props.params.detailInfo.type);
    }
  }, []);

  const filterClusterType = (value: number) => {
    if (value) {
      const RESOURCE_TYPE_LIST = [
        { value: -1, label: "未知集群" },
        { value: 1, label: "共享集群" },
        { value: 2, label: "独立集群" },
        { value: 3, label: "独享集群" },
      ];
      for (var item of RESOURCE_TYPE_LIST) {
        if (value === item.value) {
          return item.label;
        }
      }
    }
  };
  const formtTemplateCreateMapFun = () => {
    const formtTemplateCreateMap = [
      {
        key: "clusterName",
        label: "集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.detailInfo.name || "-"} />,
      },
      {
        key: "BelongApplication",
        label: "所属应用",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.applicantAppName} />,
      },
      {
        key: "type",
        label: "集群类型",
        type: FormItemType.text,
        customFormItem: <RenderText text={filterClusterType(props.params.detailInfo.type) || "-"} />,
      },
      {
        key: "level",
        label: "业务等级",
        type: FormItemType.text,
        customFormItem: <RenderText text={LEVEL_MAP[Number(props.params.detailInfo?.level) - 1]?.label || "-"} />,
      },
      {
        key: "nodeSize",
        label: "节点规格",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.detailInfo?.dataNodeSpec || "-"} />,
      },
      {
        key: "dataNodeSize",
        label: "data节点数",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.detailInfo?.dataNodeNu || "-"} />,
      },
      {
        key: "regionObj",
        type: FormItemType.custom,
        label: (
          <div className="cluster-label">
            关联region
            <Tooltip title="所选集群需满足集群类型一致，Region需要满足节点规格严格一致，节点数可大于等于用户选择节点数">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        customFormItem: <RelevanceRegion dataInfo={props.params.detailInfo || {}} />,
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (rule: any, value: any) => {
              if (!value) {
                return Promise.reject("请关联region，并点击添加完成操作！");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "description",
        label: "集群描述",
        type: FormItemType.textArea,
        attrs: {
          placeholder: "请输入审批意见1-200字符",
          readOnly: true,
        },
      },
    ] as any;
    return formtTemplateCreateMap;
  };

  const dslTemplateStatusMap = () => {
    const { params } = props;
    const formMap = [
      {
        key: "BelongApplication",
        label: "所属应用",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.applicantAppName} />,
      },
      {
        key: "desc",
        label: "工单内容",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.title} />,
      },
    ] as any;
    return formMap;
  };
  const dslTemplateQueryLimitMap = () => {
    const { params } = props;
    const formMap = [
      {
        key: "BelongApplication",
        label: "所属应用",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.applicantAppName} />,
      },
      {
        key: "title",
        label: "查询模版MD5",
        type: FormItemType.text,
        customFormItem: (
          <>
            {(params.detailInfo?.dslQueryLimitDTOList || []).map((item) => {
              return <div>{item.dslTemplateMd5 || ""}</div>;
            })}
          </>
        ),
      },
      params.detailInfo?.dslQueryLimitDTOList?.length === 1 && {
        key: "queryLimitBefore",
        label: "原限流值",
        type: FormItemType.text,
        customFormItem: (
          <>
            {(params.detailInfo?.dslQueryLimitDTOList || []).map((item) => {
              return <div>{item.queryLimitBefore || ""}</div>;
            })}
          </>
        ),
      },
      {
        key: "queryLimit",
        label: "修改后限流值",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.detailInfo?.dslQueryLimitDTOList?.[0]?.queryLimit || "-"} />,
      },
    ].filter(Boolean) as any;
    return formMap;
  };

  const templateLogicBlockMap = (type) => {
    const { params } = props;
    const formMap = [
      {
        key: "BelongApplication",
        label: "所属应用",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.applicantAppName} />,
      },
      {
        key: "name",
        label: "索引模版",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.detailInfo?.name || "-"} />,
      },
      {
        key: "op",
        label: "操作",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.detailInfo?.status ? `禁用${type}` : `启用${type}`} />,
      },
    ] as any;
    return formMap;
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "id",
        label: "工单ID",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.id} />,
      },
      {
        key: "opinion",
        label: "审批意见",
        type: FormItemType.textArea,
        attrs: {
          placeholder: "请输入1-100字审批意见",
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
    width: 668,
    formData: { description: props.params.description },
    okText: props.params.outcome === "agree" ? "通过" : "驳回",
    visible: true,
    title: "审批",
    needBtnLoading: true,
    className: "approval-modal",
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (value: any) => {
      let contentObj = {} as any;
      if (props.params.outcome === "agree") {
        if (type === "logicClusterCreate") {
          contentObj = {
            clusterRegionDTOS: Array.isArray(value.regionObj)
              ? value.regionObj.map((item) => {
                  const regionJson = JSON.parse(item.region["0"]);
                  return {
                    id: regionJson.id,
                    name: regionJson.name,
                    phyClusterName: regionJson.phyClusterName,
                    logicClusterIds: regionJson.logicClusterIds,
                    config: regionJson.config,
                  };
                })
              : [],
          };
        }
      }

      const orderParams = {
        assignee: props.user.getName("userName"),
        checkAuthority: false,
        comment: value.opinion ? value.opinion.trim() : "",
        orderId: id + "",
        outcome: props.params.outcome,
        assigneeProjectId: props.app.appInfo()?.id,
        contentObj,
      } as unknown as IApprovalOrder;

      return approvalOrder(orderParams).then((res) => {
        let msg = props.params.outcome === "agree" ? "通过成功" : "驳回成功";
        if (res?.message) {
          msg = res?.description;
        }
        XNotification({ type: "success", message: msg });
        props.dispatch(actions.setModalId(""));
        props.cb();
      });
    },
  };

  if (props.params.outcome === "agree") {
    if (type === "logicClusterCreate") {
      const formtTemplateCreateMap = formtTemplateCreateMapFun();
      xFormModalConfig.formMap.splice(1, 0, ...formtTemplateCreateMap);
    }
    if (type === "templateLogicBlockRead" || type === "templateLogicBlockWrite") {
      const formtTemplateCreateMap = templateLogicBlockMap(type === "templateLogicBlockRead" ? "读" : "写");
      xFormModalConfig.formMap.splice(1, 0, ...formtTemplateCreateMap);
    }
    if (type === "dslTemplateStatusChange") {
      const formtTemplateCreateMap = dslTemplateStatusMap();
      xFormModalConfig.formMap.splice(1, 0, ...formtTemplateCreateMap);
    }
    if (type === "dslTemplateQueryLimit") {
      const formtTemplateCreateMap = dslTemplateQueryLimitMap();
      xFormModalConfig.formMap.splice(1, 0, ...formtTemplateCreateMap);
    }
  }
  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};
export default connect(mapStateToProps)(ShowApprovalModal);
