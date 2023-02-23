import React, { useEffect, useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { UploadFile } from "container/custom-form/upload-file";
import { computeChecksumMd5, getCookie } from "lib/utils";
import { IOpPackageParams } from "typesPath/params-types";
import { getClusterVersion, addPackage, updatePackage } from "api/cluster-api";
import { UserState } from "store/type";
import { AutoComplete } from "antd";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
});

const AddPackageModal = (props: { dispatch: any; cb: Function; user: UserState; params: any }) => {
  const [versionlist, setVersionlist] = useState([]);

  useEffect(() => {
    _getClusterVersion();
  }, []);

  const _getClusterVersion = async () => {
    let res = await getClusterVersion();
    let list = res.map((item) => {
      return { value: item };
    });
    setVersionlist(list);
  };

  const { params } = props;
  const xFormModalConfig = {
    formMap: [
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
        type: FormItemType.custom,
        customFormItem: <AutoComplete options={versionlist} placeholder="请输入4位数字组成的版本号，如：7.6.0.12"></AutoComplete>,
        rules: [
          {
            required: true,
            validator: (rule: any, value: string) => {
              const reg = /^([1-9]\d|[1-9])(.([1-9]\d|\d)){2}(.(\d|[1-9]\d|[1-9]\d\d|[1-9]\d\d\d))$/;
              if (!value || !reg.test(value)) {
                return Promise.reject("必须是4位，x.x.x.x的形式, 每位x的范围分别为1-99,0-99,0-99,0-9999");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "desc",
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
        attrs: {
          placeholder: "请输入0-100字描述",
        },
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: params && !params.addPackage ? "编辑版本" : "新增版本",
    formData: params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    actionAfterSubmit: () => {
      props.dispatch(actions.setDrawerId(""));
      props.cb && props.cb();
    },
    onSubmit: async (result: IOpPackageParams) => {
      result.manifest = result.manifest || 4;
      result.creator = getCookie("userName") || "";
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
        result.url = result.uploadFile ? result.url : params?.url;
        return updatePackage(result).catch(() => {
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
      customFormItem: <UploadFile url={params?.url} accept=".gz" msg="单击或拖动文件到此区域以上传, 只能上传1个文件，且为.gz格式文件" />,
      rules: [
        {
          required: true,
          validator: async (rule: any, value: any) => {
            if (params?.url && value == null) {
              return Promise.resolve();
            }
            if (!value) {
              return Promise.reject("请上传文件,仅支持上传单个文件且文件小于 500 MB");
            }
            const { fileList } = value;
            const flag = fileList?.some((item) => item.size / 1024 / 1024 >= 500);
            if (!fileList || fileList.length !== 1 || flag) {
              return Promise.reject("请上传文件,仅支持上传单个文件且文件小于 500 MB");
            }
            return Promise.resolve();
          },
        },
      ],
    } as any;
    if (type === 1) {
      xFormModalConfig.formMap[0] = dockerForm;
    } else {
      xFormModalConfig.formMap[0] = hostForm;
    }
  };

  if (params?.manifest === 4) {
    updateFormModal(2);
  }

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(AddPackageModal);
