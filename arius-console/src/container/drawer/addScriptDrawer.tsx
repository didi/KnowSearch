import React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { UploadFile } from "container/custom-form/upload-file";
import { RenderText } from "container/custom-form";
import { Tooltip } from "antd";
import { IOpPackageParams } from "typesPath/params-types";
import { addScript, updateScript } from "api/software-admin";
import { UserState } from "store/type";
import { regNonnegativeInteger } from "constants/reg";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
});

const AddScriptDrawer = (props: { dispatch: any; cb: Function; user: UserState; params: any }) => {
  const { params } = props;

  const getFormMap = () => {
    let formMap = [
      {
        key: "uploadFile",
        label: "脚本内容",
        type: FormItemType.custom,
        className: "script-content",
        customFormItem: <UploadFile url={params?.contentUrl} accept=".sh" msg="单击或拖动文件上传，只能上传一个文件，且为.sh格式文件" />,
        rules: [
          {
            required: true,
            validator: async (rule: any, value: any) => {
              if (params?.contentUrl && value == null) {
                return Promise.resolve();
              }
              if (!value) return Promise.reject("仅支持上传单个文件且文件小于 500 MB");
              const { fileList } = value;
              const flag = fileList?.some((item) => item.size / 1024 / 1024 >= 500);
              if (!fileList || fileList.length !== 1 || flag) {
                return Promise.reject("仅支持上传单个文件且文件小于 500 MB");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "timeout",
        label: (
          <div className="cluster-label">
            模板超时时间
            <Tooltip title="任务执行时，若执行时间超过模板超时时间，则任务判定为超时">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        className: "template-timeout",
        rules: [
          {
            required: true,
            validator: (rule: any, value: string) => {
              if (!value) return Promise.reject(new Error("请输入模板超时时间"));
              if (value && !new RegExp(regNonnegativeInteger).test(value)) {
                return Promise.reject(new Error("请输入正确格式"));
              }
              return Promise.resolve();
            },
          },
        ],
        attrs: {
          placeholder: "请输入模板超时时间",
          suffix: "S",
        },
        isCustomStyle: true,
      },
      {
        key: "describe",
        label: "描述",
        type: FormItemType.textArea,
        rules: [
          {
            required: false,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (!value || value?.trim().length <= 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入0-100字描述");
              }
            },
          },
        ],
        className: "script-desc",
        attrs: {
          placeholder: "请输入0-100字描述",
        },
      },
    ] as IFormItem[];
    if (params?.addScript) {
      formMap.splice(0, 0, {
        key: "name",
        label: "脚本名称",
        attrs: {
          placeholder: "请填写脚本名称，支持中英文、数字、-、_、4-128位字符",
        },
        rules: [
          {
            required: true,
            message: "请请填写脚本名称，支持中英文、数字、-、_、4-128位字符",
            validator: async (rule: any, value: string) => {
              const reg = /^[a-zA-Z0-9\u4e00-\u9fa5_-]{4,128}$/g;
              if (!reg.test(value) || !value) {
                return Promise.reject("请填写脚本名称，支持中英文、数字、-、_、4-128位字符");
              }
              return Promise.resolve();
            },
          },
        ],
      } as any);
    } else {
      formMap.splice(0, 0, {
        key: "name",
        label: "脚本名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.name} />,
      } as any);
    }
    return formMap;
  };
  const xFormModalConfig = {
    formMap: getFormMap(),
    type: "drawer",
    className: "add-script",
    visible: true,
    title: params && !params.addScript ? "编辑脚本" : "新增脚本",
    formData: params || {},
    isWaitting: true,
    width: 600,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    actionAfterSubmit: () => {
      props.dispatch(actions.setDrawerId(""));
      props.cb && props.cb();
    },
    onSubmit: async (result: IOpPackageParams) => {
      if (result.uploadFile?.fileList) {
        const file = result.uploadFile?.fileList[0].originFileObj;
        result.uploadFile = file;
      } else {
        result.uploadFile = null;
      }
      // 判断是新增脚本还是编辑脚本
      if (!params.addScript) {
        result.id = params.id;
        return updateScript(result).catch(() => {
          throw new Error("编辑失败");
        });
      }
      return addScript(result).catch((req: any) => {
        throw new Error("上传失败");
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(AddScriptDrawer);
