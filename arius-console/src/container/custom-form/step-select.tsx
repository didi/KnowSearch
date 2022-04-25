import * as React from 'react';
import { Select, Tooltip } from 'antd';
import { connect } from "react-redux";
import * as actions from 'actions';


const mapStateToProps = (state) => ({
  phyClusterConfig: state.configInfo,
});
const connects: Function = connect

@connects(mapStateToProps)
export class StepSelect extends React.Component<any> {

  public render() {
    const  { disabled, value, phyClusterConfig } = this.props;

    return (
      <>
        <Select
            placeholder="请依次点击节点角色，设置操作的执行顺序。"
            showSearch={true}
            disabled={disabled}
            value={value}
            mode="multiple"
            onChange={(e: any) => this.handleChange(e)}
        >
            { phyClusterConfig?.clusterRolesList.map((v, index) => (
              <Select.Option
                key={v.value || v.key || index}
                value={v.value}
              >
                {(v.label?.length > 35 || (v.value + '')?.length > 35) ? <Tooltip placement="bottomLeft" title={v.label || v.value}>
                  {v.label || v.value}
                </Tooltip> : (v.label || v.value)}
              </Select.Option>
            ))}
        </Select>
      </>
    );
  }

  public setSeptPhysicClusterRoles(data: string[]) {
    const arr = this.props.phyClusterConfig.clusterRolesList.map(item => {
        item.label = item.value;
        return item;
    });
    const arrStr =  arr.map(item => {
      return item.value;
    });
    let num = null;
    data.forEach((item, index) => {
      num = arrStr.indexOf(item);
      if (num !== -1) {
        arr[num].label = arr[num].label + ' ' + `(步骤${index + 1})`;
      }
    });
    this.props.dispatch(actions.setPhyClusterConfigRoles(arr));
  }

  public handleChange(params: any) {
    const { onChange } = this.props;
    this.setSeptPhysicClusterRoles(params);
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(params);
  }
}
