import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { UploadFile } from "container/custom-form/upload-file";
import { addPlug } from "api/plug-api";
import { computeChecksumMd5 } from "lib/utils";
import { UserState } from "store/type";
import urlParser from "lib/url-parser";
import { pDefaultMap } from "container/cluster/logic-detail/config";


const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
});

const CustomPlugnModal = (props: {
  dispatch: any;
  cb: Function;
  user: UserState;
  params: any;
}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "uploadFile",
        label: "上传文件",
        type: FormItemType.custom,
        customFormItem: <UploadFile multiple={true} />,
        rules: [
          {
            required: true,
            message: "请上传的单个文件小于 100 MB",
            validator: async (rule: any, value: any) => {
              const { fileList } = value;
              const flag = fileList.some(item => item.size / 1024 /1024 >= 100);
              if(flag) {
                return  Promise.reject();
              }
              return Promise.resolve();
            },
          },
        ],
        attrs: {
          accept: ".gz",
        },
      },
      {
        key: "pDefault",
        label: "插件类型",
        type: FormItemType.select,
        options: Object.keys({
          1: 'ES能力',
          2: '平台能力',
        }).map((item) => {
          return {
            label: pDefaultMap[Number(item)],
            value: item,
          };
        }),
        rules: [
          {
            required: true,
            message: '请选择插件类型'
          },
        ],
        attrs: {
          placeholder: "请选择插件类型",
        },
      },
      {
        key: "desc",
        label: "描述",
        type: FormItemType.textArea,
        rules: [
          {
            required: false,
            validator: async (rule: any, value: any) => {
              if(value?.length > 100) {
                return  Promise.reject('请输入0-100个字符');
              }
              return Promise.resolve();
            },
          },
        ],
        attrs: {
          placeholder: "请输入备注",
        },
      },
      // {
      //   key: 'affirm',
      //   label: '我已阅读',
      //   type: FormItemType.checkBox,
      //   options: [{
      //     label: '插件安装需要重启集群，点击"确认"后，将自动提交申请工单，为保障集群稳定性，请务必保证插件本身的安全性和可用性。',
      //     value: '1',
      //   }],
      //   rules: [{ required: true, message: '请确认阅读' }],
      // },
    ] as IFormItem[],
    visible: true,
    title: "上传插件",
    formData: {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      const { physicsClusterId } = urlParser().search;

      const params = {
        creator: props.user.getName("domainAccount"),
        desc: result.desc,
        name: result.name,
        version: result.version,
        pDefault: result.pDefault,
        logicClusterId: props.params?.id || 0,
        physicsClusterId: physicsClusterId,
      };

      const files = await Promise.all(
        result.uploadFile.fileList.map(async (item) => ({
          ...params,
          fileName: item.name,
          uploadFile: item.originFileObj,
          md5: await computeChecksumMd5(item.originFileObj),
        }))
      );

      await Promise.all(files.map((item) => addPlug(item)))
        .then((res) => {
          props.cb && props.cb(); // 重新获取数据列表
        })
        .finally(() => {
          props.dispatch(actions.setModalId(""));
        });

      // result.file = result.uploadFile.fileList[0].originFileObj;
      // const md5 = await computeChecksumMd5(result.file);
      // const params = {
      //   creator: props.user.getName("domainAccount"),
      //   desc: result.desc,
      //   md5: md5 as string,
      //   fileName: result.uploadFile.fileList[0].name,
      //   name: result.name,
      //   version: result.version,
      //   uploadFile: result.file,
      //   pDefault: props.params?.pDefault ? true : false,
      //   logicClusterId: 0,
      //   physicsClusterId: physicsClusterId,
      // };
      // if (props.params?.id) params.logicClusterId = props.params.id;
      // addPlug(params)
      //   .then((res) => {
      //     props.dispatch(actions.setModalId(""));
      //     props.cb && props.cb(); // 重新获取数据列表
      //   })
      //   .finally(() => {
      //     props.dispatch(actions.setModalId(""));
      //   });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(CustomPlugnModal);
