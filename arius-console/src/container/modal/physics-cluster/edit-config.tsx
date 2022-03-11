import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { AppState, UserState } from 'store/type';
import { IPhyConfig } from 'typesPath/cluster/physics-type';
import { IWorkOrder } from 'typesPath/params-types';
import Url from "lib/url-parser";
import { submitWorkOrder } from 'api/common-api';
import { getPhysicClusterRoles } from 'api/cluster-api';


const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const EditConfig = (props: { dispatch: any, cb: Function, app: AppState, user: UserState, params: IPhyConfig}) => {
  const [clusterRolesList, setClusterRolesList] = React.useState([]);

  React.useEffect(() => {
    getPhysicClusterRoles(Number(Url().search.physicsClusterId)).then((res) => {
      res =
        res?.map((ele, index) => {
          return {
            ...ele,
            label: ele.roleClusterName,
            value: ele.roleClusterName,
            key: index,
          };
        }) || [];
      setClusterRolesList(res);
    });
  }, [])

  const xFormModalConfig = {
    formMap: [
      {
          key: 'configData',
          type: FormItemType.textArea,
          label: '配置内容',
          rules: [{
            required: true,
            validator: (rule: any, value: string) => {
              if (props.params?.configData === value) {
                return Promise.reject('不可与原来一致。');
              }
              return Promise.resolve();
            }, 
          }]
      }, {
        key: 'description',
        type: FormItemType.textArea,
        label: '申请原因',
        rules: [{
          required: true,
          validator: (rule: any, value: string) => {
            if (!value || value?.trim().length > 100) {
              return Promise.reject('请输入1-100字申请原因');
            } else {
              return Promise.resolve();
            }
          },
        }],
        attrs: {
          placeholder: '请输入1-100字申请原因',
        },
      }] as IFormItem[],
    visible: true,
    title: '编辑配置内容',
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (result: any) => {
      const url = Url();
      const esConfigs = {...props.params};
      esConfigs.configData = result.configData;
      const roleOrder = [];
      clusterRolesList.forEach((item) => {
        if (item?.roleClusterName.indexOf(props.params?.enginName) > -1) {
          roleOrder.push(item.roleClusterName);
        }
      });
      const workOrderParams: IWorkOrder = {
        contentObj: {
          phyClusterId: Number(url.search.physicsClusterId),
          phyClusterName: url.search.physicsCluster,
          roleOrder: roleOrder,
          type: Number(url.search.type),
          actionType: props.params?.enginName ? 2 : 1,
          newEsConfigs: [esConfigs],
          originalConfigs: props.params.enginName ? [props.params] : [],
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description || "",
        type: "clusterOpConfigRestart",
      };
      return submitWorkOrder(workOrderParams).finally(() => {
        props.dispatch(actions.setModalId(""));
      });
    },
  };

  return (
    <>
      <XFormWrapper
        visible={true}
        {...xFormModalConfig}
      />
    </>
  )
};

export default connect(mapStateToProps)(EditConfig);

