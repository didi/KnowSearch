import { Modal, Spin, Tooltip, Progress, notification } from 'antd';
import React, { useState } from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import {
  FormItemType,
  IFormItem,
  XForm as XFormComponent,
} from "component/x-form";
import "./index.less";
import { IZoomInfo } from "@types/index-types";
import { KEEP_LIVE_LIST } from "constants/time";
import { quotaRuleProps } from "constants/table";
import { getIndexCapacity, getIndexQuotaCost } from "api/cluster-index-api";
import { getPercent } from "lib/utils";
import { IWorkOrder } from "@types/params-types";
import { AppState, UserState } from "store/type";
import { submitWorkOrder } from "api/common-api";
import { Descriptions } from 'antd'; // TODO: 

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

export const ExpandShrinkIndex = connect(mapStateToProps)(
  (props: {
    dispatch: any;
    app: AppState;
    user: UserState;
    params: any;
    cb: any;
  }) => {
    const [loading, setLoading] = useState(false);
    const [id, setId] = useState(props?.params);
    const [indexZoomInfo, setIndexZoomInfo] = useState({} as IZoomInfo);

    React.useEffect(() => {
      reloadData();
    }, []);

    const reloadData = async () => {
      setLoading(true);
      let info: IZoomInfo = null;
      try {
        info = await getIndexCapacity(id);
        dealUsageInfo(info);
        const data = await getIndexQuotaCost(info.quota);
        const totalPrice = data?.totalPrice?.toFixed(2);
        Object.assign(
          info,
          {},
          {
            totalPrice,
          }
        );
        setIndexZoomInfo(info);
        setLoading(false);
      } catch (err) {
        setLoading(false);
      }
    };

    const dealUsageInfo = (info: IZoomInfo) => {
      // 固定两位小数及百分比计算
      if (info?.topUsage) {
        info.topUsage.actualCpuCount = Number.parseFloat(
          info.topUsage.actualCpuCount?.toFixed(2)
        );
        info.topUsage.quotaDiskG = Number.parseFloat(
          info.topUsage.quotaDiskG?.toFixed(2)
        );
        info.topUsage.quotaCpuCount = Number.parseFloat(
          info.topUsage.quotaCpuCount?.toFixed(2)
        );
        info.topUsage.actualDiskG = Number.parseFloat(
          info.topUsage.actualDiskG?.toFixed(2)
        );
        info.topUsage.cpuPercent = getPercent(
          info.topUsage.actualCpuCount,
          info.topUsage.quotaCpuCount
        );
        info.topUsage.diskGPercent = getPercent(
          info.topUsage.actualDiskG,
          info.topUsage.quotaDiskG
        );
      }

      if (info?.currentUsage) {
        info.currentUsage.actualCpuCount = Number.parseFloat(
          info.currentUsage.actualCpuCount?.toFixed(2)
        );
        info.currentUsage.quotaDiskG = Number.parseFloat(
          info.currentUsage.quotaDiskG?.toFixed(2)
        );
        info.currentUsage.quotaCpuCount = Number.parseFloat(
          info.currentUsage.quotaCpuCount?.toFixed(2)
        );
        info.currentUsage.actualDiskG = Number.parseFloat(
          info.currentUsage.actualDiskG?.toFixed(2)
        );
        info.currentUsage.cpuPercent = getPercent(
          info.currentUsage.actualCpuCount,
          info.currentUsage.quotaCpuCount
        );
        info.currentUsage.diskGPercent = getPercent(
          info.currentUsage.actualDiskG,
          info.currentUsage.quotaDiskG
        );
      }
    };

    const formRef: any = React.createRef();

    const formMap = (info: IZoomInfo) => {
      return [
        {
          key: "expectExpireTime",
          label: "期望保存周期",
          type: FormItemType.select,
          invisible: !info?.cyclicalRoll,
          rules: [
            { required: info?.cyclicalRoll, message: "请选择期望保存周期" },
          ],
          options: KEEP_LIVE_LIST.map((item) => ({
            label: item,
            value: item,
          })),
        },
        {
          key: "expectQuota",
          label: "期望Quota",
          type: FormItemType.inputNumber,
          rules: [
            {
              required: true,
              message: "请填写期望Quota, 非负数,支持两位小数。",
              ...quotaRuleProps,
            },
          ],
        },
        {
          key: "description",
          label: "申请原因",
          type: FormItemType.textArea,
          rules: [
            {
              required: true,
              message: "请输入申请原因",
            },
          ],
        },
      ] as unknown as IFormItem[];
    };

    const handleSubmit = () => {
      formRef.current?.validateFields().then((result) => {
        const indexInfo = indexZoomInfo;
        const contentInfo = {
          id: indexInfo.id,
          name: indexInfo.name,
          expireTime: indexInfo.expireTime,
          quota: indexInfo.quota,
          actualDiskG: indexInfo.topUsage.actualDiskG,
          actualCpuCount: indexInfo.topUsage.actualCpuCount,
          quotaDiskG: indexInfo.topUsage.quotaDiskG,
          quotaCpuCount: indexInfo.topUsage.quotaCpuCount,
        };
        const params: IWorkOrder = {
          contentObj: {
            ...contentInfo,
            ...result,
          },
          submitorAppid: props.app.appInfo()?.id,
          submitor: props.user.getName('domainAccount'),
          description: result.description || "",
          type: "templateIndecrease",
        };
        submitWorkOrder(params)
          .then(() => {
            // notification.success({ message: '', duration: 1500 });
          })
          .finally(() => { });
      });
    };

    const getDescList = (info: IZoomInfo) => {
      let descs = [
        {
          label: "模板名称",
          key: "name",
          render: (value: string) => {
            return (
              <>
                <Tooltip placement="bottomLeft" title={value}>
                  {value?.length > 25 ? value?.substring(0, 20) + "..." : value}
                </Tooltip>
              </>
            );
          },
        },
        {
          label: "已有配额",
          key: "quota",
          render: (value: string) => (
            <>
              <span>{value}台</span>
              <a
                className="ml-5"
                target="_blank"
                href=""
              >
                Quota说明
              </a>
            </>
          ),
        },
        {
          label: "保存周期",
          key: "expireTime",
          unit: "/天",
          invisible: !info?.cyclicalRoll,
        },
        {
          label: "成本计价",
          key: "totalPrice",
          unit: "元/月",
        },
      ] as any[];

      const currentUsage = [
        {
          label: "磁盘利用率(实时)",
          key: "currentUsage",
          render: (item: {
            quotaDiskG: string;
            actualDiskG: string;
            diskGPercent: any;
          }) => {
            const tip =
              "磁盘配额: " +
              item?.quotaDiskG +
              "GB, " +
              "使用容量: " +
              item?.actualDiskG +
              "GB";
            return (
              <>
                <Tooltip placement="bottomLeft" title={tip}>
                  <Progress percent={item?.diskGPercent} size="small" />
                </Tooltip>
              </>
            );
          },
        },
        {
          label: "CPU利用率(实时)",
          key: "currentUsage",
          render: (item: {
            quotaCpuCount: string;
            actualCpuCount: string;
            cpuPercent: any;
          }) => {
            const tip =
              "CPU配额: " +
              item?.quotaCpuCount +
              "core, " +
              "使用CPU: " +
              item?.actualCpuCount +
              "core";
            return (
              <>
                <Tooltip placement="bottomLeft" title={tip}>
                  <Progress percent={item?.cpuPercent} size="small" />
                </Tooltip>
              </>
            );
          },
        },
      ];

      if (info?.currentUsage) {
        descs = descs.concat(...currentUsage);
      }

      return descs;
    };

    const formData = {
      expectExpireTime: indexZoomInfo?.expireTime || 3,
      expectQuota: indexZoomInfo?.quota || 0,
    };

    return (
      <>
        <Modal
          visible={true}
          title="索引扩缩容"
          width={700}
          onOk={handleSubmit}
          onCancel={() => props.dispatch(actions.setModalId(""))}
          maskClosable={false}
          okText={'确定'}
          cancelText={'取消'}
        >
          <Spin spinning={loading}>
            <Descriptions className="base-info" size="middle" column={2}>
              {getDescList(indexZoomInfo).map((item, index: number) =>
                item.invisible ? null : (
                  <Descriptions.Item key={index} label={item.label}>
                    {item.render
                      ? item.render(indexZoomInfo?.[item.key])
                      : `${indexZoomInfo?.[item.key] || ""}${item.unit || ""}`}
                  </Descriptions.Item>
                )
              )}
            </Descriptions>
            <div>
              <XFormComponent
                formData={formData}
                formMap={formMap(indexZoomInfo)}
                wrappedComponentRef={formRef}
                layout={"vertical"}
              />
            </div>
          </Spin>
        </Modal>
      </>
    );
  }
);
