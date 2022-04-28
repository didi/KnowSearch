import { Select } from 'antd';
import * as React from 'react';
import { getAllUser } from 'api/common-api';
import { IUser } from 'typesPath/user-types';
import { isArray } from 'lodash';

const Option = Select.Option;

interface IUserSelectProps {
  onChange?: (result: string[]) => any;
  value?: string[];
  disabled?: boolean;
  placeholder?: string;
  style?: any; 
}

export class StaffSelect extends React.Component<IUserSelectProps> {

  public state = {
    staffList: [] as IUser[],
  };


  constructor(props: IUserSelectProps) {
    super(props);
  }

  public componentDidMount() {
    this.getStaffList();
  }

  public getStaffList = () => {
    getAllUser().then((res) => {
      const list = (res || []).map(item => ({
        ...item,
        label: item.domainAccount,
        value: item.domainAccount,
      }));
      this.setState({
        staffList: list,
      });
    })
  }

  public render() {
    const { disabled, placeholder, style } = this.props;
    let { value } = this.props;
    if (value && typeof (value) === 'string') { value = (value as string)?.split(','); }
    // 打印出value为空时 值为 [''],导致编辑时出现空标签
    if (value && isArray(value)) {
      value = value?.filter(item => {
        return item !== '';
      })
    }
    return (
      <Select
        mode="multiple"
        showSearch={true}
        optionFilterProp="children"
        placeholder={placeholder ? placeholder : '请输入负责人查询'}
        defaultValue={value || []}
        value={value || []}
        onChange={(e: string[]) => this.handleChange(e)}
        disabled={disabled}
        tokenSeparators={[',']}
        style={style}
        className="ant-form-item-custom"
        filterOption={(input, option) =>
          JSON.stringify(option).toLowerCase().indexOf(input.toLowerCase()) >=
          0
        }
      >
        {this.state.staffList.map((d: IUser) => <Option value={d.domainAccount} key={d.domainAccount}>
          {d.domainAccount}
        </Option>)}
      </Select>
    );
  }

  public handleChange(params: string[]) {
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(params);
  }

}
