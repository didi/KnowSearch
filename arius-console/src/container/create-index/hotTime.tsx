import { InputNumber, Tooltip } from 'antd';
import * as React from 'react';

interface IHotTimeProps {
  onChange?: (result: number) => any;
  value?: number;
  disabled?: boolean;
}

export class HotTime extends React.Component<IHotTimeProps> {
  constructor(props: IHotTimeProps) {
    super(props);
  }

  public render() {
    return (
      this.props.disabled ? 
        <Tooltip title="所选集群未开启冷热分离服务，无法设置热节点保存周期">
          <InputNumber
            onChange={this.handleChange}
            style={{
              width: '100%'
            }}
            placeholder="请输入热节点保存周期"
            value={this.props.value === -1 ? '' : this.props.value}
            disabled={this.props.disabled}
          />
        </Tooltip>
      :  <InputNumber
          onChange={this.handleChange}
          style={{
            width: '100%'
          }}
          placeholder="请输入热节点保存周期"
          disabled={this.props.disabled}
        />
    );
  }

  public handleChange = (params: number) => {
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(params);
  }

}
