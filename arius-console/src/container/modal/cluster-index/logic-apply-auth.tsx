import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType } from "component/x-form";
import { IWorkOrder } from "@types/params-types";
import { submitWorkOrder } from "api/common-api";
import { appTemplateAuthEnum } from "constants/status-map";
import { RenderText } from "container/custom-form";
import { AppState, UserState } from "store/type";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
  app: state.app
});

export const LogicApplyAuth = connect(mapStateToProps)(
  (props: {
    dispatch: any;
    cb: Function;
    params: any;
    user: UserState;
    app: AppState
  }) => {
    const xFormModalConfig = {
      formMap: [
        {
          key: "name",
          label: "集群名称",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.params.name} />,
        },
        {
          key: "pro",
          label: "申请者所在项目",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.app.appInfo()?.name} />,
        },
        {
          key: "authCode",
          label: "权限类型",
          // defaultValue: props.params.authType,
          type: FormItemType.radioGroup,
          options:
            props.params.authType === 3
              ? appTemplateAuthEnum.slice(0, 1)
              : appTemplateAuthEnum,
          rules: [{ required: true, message: "请选择权限类型" }],
        },
        {
          key: "description",
          label: "申请原因",
          type: FormItemType.textArea,
          rules: [
            {
              required: true,
              message: "请输入申请原因1-100个字符",
              validator: (rule: any, value: string) => {
                if (value?.trim().length > 0 && value?.trim().length <= 100) {
                  return Promise.resolve();
                }
                return Promise.reject();
              },
            },
          ],
          attrs: {
            placeholder: "请输入申请原因1-100个字符",
          },
        },
      ],
      visible: true,
      title: "索引权限申请",
      formData: props.params,
      isWaitting: true,
      width: 660,
      onCancel: () => {
        props.dispatch(actions.setModalId(""));
      },
      onSubmit: async (result: any) => {
        const params: IWorkOrder = {
          contentObj: {
            id: props.params.id,
            name: props.params.name,
            authCode: result.authCode,
            memo: result.memo,
            responsible: props.params.responsible,
          },
          submitorAppid: props.app.appInfo()?.id,
          submitor: props.user.getName('domainAccount'),
          description: result.description || "",
          type: "templateAuth",
        };
        submitWorkOrder(params)
          .then((res) => {
            props.dispatch(actions.setModalId(""));
          })
          .finally(() => {
            props.cb && props.cb(); // 重新获取数据列表
          });
      },
    };

    return (
      <>
        <XFormWrapper visible={true} {...xFormModalConfig} />
      </>
    );
  }
);
