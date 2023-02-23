import React, { useEffect, useState } from "react";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { WRITE_TYPE, TRANSFER_TEMPLATE } from "constants/common";
import { Button, Tooltip } from "antd";
import { DoubleRightOutlined } from "@ant-design/icons";
import { regNonnegativeInteger } from "constants/reg";
import moment from "moment";
import "./index.less";

export default function OtherInfo(props) {
  const { dataType, otherInfoRef, targetCluster, address } = props;

  const [fold, setFold] = useState(false);

  const otherFormMap = () => {
    return [
      {
        key: "writeType",
        label: (
          <div className="writeType">
            <span className="title">写入方式</span>
            <Tooltip
              overlayClassName="write-type-tooltip"
              title={
                <div>
                  <div>ID冲突处理策略：不带ID写入，三种方式都是追加写，带ID写入有以下差别</div>
                  <div>index_with_id： 指定ID+版本号的写入方式，ID冲突时，以高版本文档为主，进行覆盖或者丢弃；</div>
                  <div>index：指定ID的写入方式，ID冲突时，进行覆盖写入；</div>
                  <div>create：指定ID的写入方式，ID冲突时，丢弃当前写入</div>
                </div>
              }
            >
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: WRITE_TYPE,
        attrs: { placeholder: "请选择写入方式" },
        rules: [{ required: true }],
      },
      {
        key: "targetIndexType",
        label: "type",
        invisible: dataType === 1,
        type: FormItemType.input,
        attrs: { placeholder: "请输入type", disabled: targetCluster?.targetCluster?.version?.[0] >= 7 },
        rules: [
          {
            required: false,
            validator: async (rule: any, value: string) => {
              if (value && value?.length > 128) {
                return Promise.reject("请输入128个字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "taskSubmitAddress",
        label: "任务提交地址",
        type: FormItemType.input,
        attrs: { placeholder: "请输入任务提交地址，支持128个字符" },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (value?.length > 128 || !value) {
                return Promise.reject("请输入任务提交地址，支持128个字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "transfer",
        label: "是否转让模板",
        type: FormItemType.radioGroup,
        invisible: dataType !== 1,
        defaultValue: "0",
        options: TRANSFER_TEMPLATE,
      },
      {
        key: "senior",
        className: "access-senior",
        type: FormItemType.custom,
        customFormItem: (
          <div className="access-cluster-senior">
            <Button type="link" onClick={() => setFold(!fold)}>
              高级
              <DoubleRightOutlined className={fold ? "up" : "down"} />
            </Button>
          </div>
        ),
      },
      {
        key: "taskReadRate",
        label: "任务读取速率",
        type: FormItemType.input,
        invisible: !fold,
        className: "task-read-rate",
        attrs: {
          placeholder: "请输入任务读取速率",
          suffix: "条/s",
        },
        rules: [
          {
            required: false,
            validator: (rule: any, value: string) => {
              if (value && !new RegExp(regNonnegativeInteger).test(value)) {
                return Promise.reject("请输入数字");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "taskStartTime",
        label: "任务开始时间",
        type: FormItemType.datePicker,
        invisible: !fold,
        className: "task-start-time",
        attrs: {
          placeholder: "请选择任务开始时间",
          format: "YYYY-MM-DD HH:mm",
          showTime: { defaultValue: moment("00:00:00", "HH:mm") },
          showNow: false,
          disabledDate: (current) => {
            return current && current < moment().startOf("day");
          },
          disabledTime: (data) => {
            const range = (start: number, end: number) => {
              const result = [];
              for (let i = start; i < end; i++) {
                result.push(i);
              }
              return result;
            };
            return {
              disabledHours: () => (moment(data).valueOf() < moment().valueOf() ? range(0, 24).splice(0, new Date().getHours()) : []),
              disabledMinutes: () =>
                moment(data).valueOf() < moment().valueOf() ? range(0, 60).splice(0, new Date().getMinutes() + 1) : [],
            };
          },
        },
      },
    ] as IFormItem[];
  };

  return (
    <XFormComponent
      formData={{
        writeType: 1,
        targetIndexType: targetCluster?.targetCluster?.version?.[0] > 8 ? "无" : "_doc",
        taskSubmitAddress: address,
      }}
      formMap={otherFormMap()}
      wrappedComponentRef={otherInfoRef}
      layout="vertical"
    />
  );
}
