import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { StaffSelect } from "container/staff-select";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";
import { VirtualScrollSelect } from "container/custom-form/virtual-scroll-select";
import { getConsoleAppList } from "api/app-api";
import { AppState, UserState } from "store/type";
import { staffRuleProps } from "constants/table";


const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
  app: state.app
});

const TransClusterIndex = (props: {
  dispatch: any;
  cb: Function;
  user: UserState;
  app: AppState;
  params: any;
}) => {
  const [consoleAppList, setConsoleAppList] = React.useState(null);

  const getConsoleAppListFn = () => {
    if (consoleAppList?.length) {
      return Promise.resolve(consoleAppList);
    }
    return getConsoleAppList().then((data: any[]) => {
      const AppList = (data || []).map((item) => ({
        ...item,
        label: `${item.name}(${item.id})`,
        value: item.id,
        disabled: props.app.appInfo()?.id === item.id
      }));
      setConsoleAppList(AppList);
      return AppList;
    });
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "模板名称",
        defaultValue: props.params.name,
        attrs: {
          disabled: true,
        },
      },
      {
        key: "app",
        label: "当前项目",
        defaultValue: props.app.appInfo()?.name,
        attrs: {
          disabled: true,
        },
      },
      {
        key: "tgtAppId",
        label: "目标项目",
        type: FormItemType.custom,
        customFormItem: (
          <VirtualScrollSelect getData={() => getConsoleAppListFn()} attrs={{ placeholder: "请选择目标项目" }} />
        ),
        defaultValue: [] as string[],
        rules: [
          {
            required: true,
            message: "请选择目标项目",
          },
        ],
        
      },
      {
        key: "tgtResponsible",
        label: "责任人",
        type: FormItemType.custom,
        customFormItem: <StaffSelect />,
        defaultValue: props.user ? [props.user.getName('domainAccount')] : [],
        isCustomStyle: true,
        rules: [
          {
            required: true,
            ...staffRuleProps,
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
    title: "索引模板转让",
    formData: {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      const params: IWorkOrder = {
        contentObj: {
          id: props.params.id,
          sourceAppId: props.params.appId,
          name: result.name,
          tgtAppId: result.tgtAppId,
          tgtResponsible: result.tgtResponsible?.join(","),
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description || "",
        type: "templateTransfer",
      };
      submitWorkOrder(params, () => {
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(TransClusterIndex);
