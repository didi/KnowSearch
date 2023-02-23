import React, { useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { useSelector, shallowEqual, useDispatch } from "react-redux";
import * as actions from "actions";
import { XNotification } from "component/x-notification";
import { FormItemType, IFormItem } from "component/x-form";
import { editPlug } from "api/plug-api";

const EditPluginDesc = () => {
  const dispatch = useDispatch();
  const { params, cb } = useSelector((state) => ({ params: (state as any).modal.params, cb: (state as any).modal.cb }), shallowEqual);
  const { record } = params;

  const xFormModalConfig = {
    formMap: [
      {
        key: "desc",
        type: FormItemType.textArea,
        label: "插件描述",
        formAttrs: {
          style: { margin: 0 },
        },
        attrs: {
          placeholder: "请输入插件描述，0-100字",
        },
        rules: [
          {
            validator: (rule: any, value: string) => {
              if (record.desc === value) {
                return Promise.reject("请编辑描述，不可与原本一致。");
              }
              if (!value || value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入0-100字描述信息");
              }
            },
          },
        ],
      },
    ] as IFormItem[],
    visible: true,
    title: "编辑插件描述",
    formData:
      {
        desc: record.desc,
      } || {},
    isWaitting: true,
    width: 660,
    needSuccessMessage: false,
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const { desc } = result;
      return editPlug(record.id, desc)
        .then(() => {
          XNotification({ type: "success", message: "编辑成功" });
        })
        .finally(() => {
          cb && cb();
          dispatch(actions.setModalId(""));
        });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default EditPluginDesc;
