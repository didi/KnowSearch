import { Select } from "antd";
import * as React from "react";
import { getAllUserList } from "api/common-api";
import { IUser, NewIUser } from "typesPath/user-types";
import { isArray } from "lodash";
import { filterOption } from "lib/utils";

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
    getAllUserList().then((res) => {
      const list = (res || []).map((item) => ({
        ...item,
        label: item.userName,
        value: item.userName,
      }));
      this.setState({
        staffList: list,
      });
    });
  };

  public render() {
    const { disabled, placeholder, style } = this.props;
    let { value } = this.props;
    if (value && typeof value === "string") {
      value = (value as string)?.split(",");
    }
    // 打印出value为空时 值为 [''],导致编辑时出现空标签
    if (value && isArray(value)) {
      value = value?.filter((item) => {
        return item !== "";
      });
    }
    return (
      <Select
        mode="multiple"
        showSearch={true}
        optionFilterProp="children"
        placeholder={placeholder ? placeholder : "请输入负责人查询"}
        defaultValue={value || []}
        value={value || []}
        onChange={(e: string[]) => this.handleChange(e)}
        disabled={disabled}
        tokenSeparators={[","]}
        style={style}
        className="ant-form-item-custom"
        filterOption={filterOption}
      >
        {this.state.staffList.map((d: NewIUser) => (
          <Option value={d.userName} key={d.userName}>
            {d.userName}
          </Option>
        ))}
      </Select>
    );
  }

  public handleChange(params: string[]) {
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(params);
  }
}
