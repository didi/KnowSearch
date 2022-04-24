import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { notification } from 'antd';
import { UploadFile } from "container/custom-form/upload-file";
import { computeChecksumMd5 } from "lib/utils";
import { PHY_CLUSTER_TYPE } from "constants/status-map";
import { IOpPackageParams } from "typesPath/params-types";
import { addPackage, updatePackage } from "api/cluster-api";
import { UserState } from "store/type";


const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
});

const AddPackageModal = (props: {
  dispatch: any;
  cb: Function;
  user: UserState;
  params: any;
}) => {
  const { params } = props;
  const $ref: any = React.createRef();
  const xFormModalConfig = {
    formMap: [
      {
        key: "manifest",
        label: "版本类型",
        type: FormItemType.select,
        options: PHY_CLUSTER_TYPE,
        defaultValue: 3,
        rules: [{ required: true, message: "请选择" }],
        attrs: {
          disabled: !!params,
          placeholder: "请选择",
          onChange: (e: number) => {
            if (e === 3) {
              updateFormModal(1);
            } else {
              updateFormModal(2);
            }
          },
        },
      },
      {
        key: "url",
        label: "URL",
        rules: [{ required: true, message: "请输入" }],
        attrs: {
          placeholder: "请输入",
        },
      },
      {
        key: "esVersion",
        label: "版本名称",
        rules: [{ 
          required: true, 
          validator: (rule: any, value: string) => {
            const reg = /^([1-9]\d|[1-9])(.([1-9]\d|\d)){2}(.(\d|[1-9]\d|[1-9]\d\d|[1-9]\d\d\d))$/;
            if(!value || !reg.test(value)) {
              return Promise.reject('必须是4位，x.x.x.x的形式, 每位x的范围分别为1-99,0-99,0-99,0-9999');
            }
            return Promise.resolve();
          }, 
        }],
        attrs: {
          disabled: false,
          placeholder: "请输入4位数字组成的版本号，如：7.6.0.12",
        },
      },
      {
        key: "desc",
        label: "描述",
        type: FormItemType.textArea,
        rules: [{
          required: false,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if (!value || value?.trim().length <= 100) {
              return Promise.resolve();
            } else {
              return Promise.reject('请输入0-100字描述');
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入0-100字描述',
        },
      },
    ] as IFormItem[],
    visible: true,
    title: (params && !params.addPackage) ? "编辑版本" : "新增版本",
    formData: params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    actionAfterSubmit: () => {
      props.dispatch(actions.setModalId(""));
      props.cb && props.cb();
      // notification.success({ message: `${params ? "编辑" : "上传"}成功` });
    },
    onSubmit: async (result: IOpPackageParams) => {
      result.creator = props.user.getName('domainAccount') || "";
      if (result.manifest === 4 && result.uploadFile?.fileList) {
        const file = result.uploadFile?.fileList[0].originFileObj;
        result.fileName = result.uploadFile.fileList[0].name;
        const md5 = await computeChecksumMd5(file);
        result.md5 = md5 as string;
        result.uploadFile = file;
      } else {
        result.uploadFile = null;
      }
      // 判断是新增版本还是编辑版本
      if (params && !params.addPackage) {
        result.id = params.id;
        return updatePackage(result).catch(() => {
          notification.error({ message: "编辑失败" });
          throw new Error("编辑失败");
        });
      }
      return addPackage(result).catch((req: any) => {
        throw new Error("上传失败");
      });
    },
  };

  const updateFormModal = (type) => {
    const dockerForm = {
      key: "url",
      label: "URL",
      rules: [{ required: true, message: "请输入" }],
      attrs: {
        placeholder: "请输入",
      },
    } as IFormItem;
    const hostForm = {
      key: "uploadFile",
      label: "上传文件",
      type: FormItemType.custom,
      customFormItem: (
        <UploadFile
          url={params?.url}
          accept=".gz"
          msg="单击或拖动文件到此区域以上传, 只能上传1个文件，且为.gz格式文件"
        />
      ),
      rules: [
        {
          required: true,
          validator: async (rule: any, value: any) => {
            if(params?.url && value == null) {
              return Promise.resolve();
            }
            if(!value) {
              return Promise.reject("请上传文件,仅支持上传单个文件且文件小于 500 MB");
            }
            const { fileList } = value;
            const flag = fileList?.some(item => item.size / 1024 /1024 >= 500);
            if(!fileList || fileList.length !== 1 || flag) {
              return Promise.reject("请上传文件,仅支持上传单个文件且文件小于 500 MB");
            }
            return Promise.resolve();
          },
        },
      ],
    } as any;
    if (type === 1) {
      xFormModalConfig.formMap[1] = dockerForm;
    } else {
      xFormModalConfig.formMap[1] = hostForm;
    }
    $ref.current?.updateFormMap$(xFormModalConfig.formMap, {});
  };

  if (params?.manifest === 4) {
    updateFormModal(2);
  }

  return (
    <>
      <XFormWrapper ref={$ref} visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(AddPackageModal);
