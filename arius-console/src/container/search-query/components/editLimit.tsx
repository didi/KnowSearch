import React, { useState, useEffect } from "react";
import { Modal, InputNumber, message, Tag } from "antd";
import { queryLimit } from "api/search-query";
import "./editLimit.less";
import { isSuperApp } from "lib/utils";
import { IWorkOrder } from "@types/params-types";
import { submitWorkOrder } from "api/common-api";

interface IProps {
  record: any;
  visible: boolean;
  cb: () => void;
  cancel: () => void;
  history?: any;
  appInfo: any;
}

const EditLimit: React.FC<IProps> = (props: IProps) => {
  const [value, setValue] = useState("" as any);

  const handleOk = async () => {
    if (value < 0) {
      message.warning("限流值不能小于0");
      return;
    }
    if (isSuperApp()) {
      const params = props?.record?.map((item) => ({
        dslTemplateMd5: item.dslTemplateMd5,
        queryLimit: value,
        projectId: item.projectId,
      }));
      return queryLimit(params).then(() => {
        props.cancel();
        props.cb();
        message.success("修改成功");
      });
    }
    const params: IWorkOrder = {
      contentObj: {
        dslQueryLimitDTOList: props?.record?.map((item) => ({
          dslTemplateMd5: item.dslTemplateMd5,
          queryLimit: value,
          queryLimitBefore: item.queryLimit,
          projectId: item.projectId,
        })),
        operator: props?.appInfo.user("userName") || "",
        projectId: props?.appInfo.app()?.id,
      },
      submitorProjectId: props?.appInfo.app()?.id,
      submitor: props?.appInfo.user("userName") || "",
      description: "",
      type: "dslTemplateQueryLimit",
    };
    await submitWorkOrder(
      params,
      props?.history,
      () => {
        props.cancel();
        props.cb();
      },
      500
    );
  };

  const renderTags = () => {
    const left = [];
    const right = [];
    props.record?.map((item, index) => {
      if (index % 2 !== 0) {
        right.push(
          <Tag key={item.dslTemplateMd5} className="dsl-editlimit-tag">
            {item.dslTemplateMd5}
          </Tag>
        );
      } else {
        left.push(
          <Tag key={item.dslTemplateMd5} className="dsl-editlimit-tag">
            {item.dslTemplateMd5}
          </Tag>
        );
      }
    });
    return (
      <>
        <div className="dsl-editlimit-tag-l">{left}</div>
        <div className="dsl-editlimit-tag-r">{right}</div>
      </>
    );
  };

  useEffect(() => {
    if (props?.record && props?.record?.length === 1) {
      setValue(props?.record[0]?.queryLimit);
    }
    return () => {
      setValue("");
    };
  }, [props?.record[0]?.queryLimit]);

  return (
    <Modal
      visible={props.visible}
      onOk={handleOk}
      onCancel={props.cancel}
      width={650}
      title={props.record?.length >= 2 ? "批量修改限流值" : "修改限流值"}
    >
      <div>
        <div className="dsl-editlimit-tags">{renderTags()}</div>
        <div>
          <label>查询限流值：</label>
          <InputNumber value={value} onChange={(e) => setValue(e)} className="dsl-editlimit-input" />
        </div>
      </div>
    </Modal>
  );
};

export default EditLimit;
