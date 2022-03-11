import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { StaffSelect } from 'container/staff-select';
import { RenderText } from 'container/custom-form';
import { IWorkOrder } from 'typesPath/params-types';
import { submitWorkOrder } from 'api/common-api';
import { AppState, UserState } from 'store/type';
import { getAppList } from 'api/cluster-api';
import { IAppDetail } from 'typesPath/user-types';
import { IOpLogicCluster } from 'typesPath/cluster/cluster-types';
import { staffRuleProps } from 'constants/table';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const TransferClusterModal = (props: { dispatch: any, cb: Function, app: AppState, user: UserState, params: IOpLogicCluster }) => {
  const [appList, setAppList] = React.useState([]);
  const [load, setLoad] = React.useState(false);

  React.useEffect(() => {
    setLoad(true);
    getAppList().then((res: IAppDetail[]) => {
      const appOptions = res.map(i => {
        if (i.id === props.params.appId) {
          return {label: i.name, value: i.id, disabled: true}
        }
        return {label: i.name, value: i.id}
      });
      setAppList(appOptions);
    }).finally(() => {
      setLoad(false);
    });
  }, []);
  
  const xFormModalConfig = {
    formMap: [
      {
        key: 'name',
        label: '集群名称',
        type: FormItemType.text,
        customFormItem: <RenderText  text={props.params.name}/>,
      }, {
        key: 'project',
        label: '所属项目',
        type: FormItemType.text,
        customFormItem: <RenderText  text={props.app.appInfo()?.name}/>,
      }, {
        key: 'appId',
        label: '受让项目',
        colSpan: 10,
        rules: [{
          required: true,
          message: '请选择接受转让的项目',
        }],
        type: FormItemType.select,
        options: appList,
        attrs: {
          placeholder: '请选择接受转让的项目',
        },
      }, {
        key: 'responsible',
        label: '负责人',
        colSpan: 10,
        rules: [
          {
            required: true,
            ...staffRuleProps,
          },
        ],
        isCustomStyle: true,
        type: FormItemType.custom,
        // isCustomStyle: true,
        customFormItem: <StaffSelect style={{width: '60%'}}/>,
      }, {
        key: 'description',
        type: FormItemType.textArea,
        label: '申请原因',
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if(!value || value?.trim().length > 100) {
              return Promise.reject('请输入1-100字申请原因');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入1-100字申请原因',
        },
      }
    ] as IFormItem[],
    visible: true,
    title: '转让集群',
    formData: {},
    isWaitting: true,
    width: 660,
    okText: '提交',
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (result: any) => {
      result.responsible = Array.isArray(result.responsible) ? result.responsible.join(',') : result.responsible;
      const params: IWorkOrder = {
        contentObj: {
          clusterLogicId: props.params.id,
          clusterLogicName: props.params.name,
          targetAppId: result.appId,
          sourceAppId: props.params.appId,
          targetResponsible: result.responsible,
          memo: result.memo,
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description,
        type: 'logicClusterTransfer',
      };
      submitWorkOrder(params, () => {
        props.dispatch(actions.setModalId(''));
      });
    }
  };

  return (
    <>
    {
      load ? null :
      <XFormWrapper
          visible={true}
          {...xFormModalConfig}
      />
    } 
      
    </>
  )
};

export default connect(mapStateToProps)(TransferClusterModal);

