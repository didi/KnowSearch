import React, { useState } from 'react';
import { Select } from 'antd';
import _ from 'lodash';
interface IProps {
  style?: object;
  value?: any;
  projectList?: IProject[];
  onProjectChange?: (newValue: any) => void;
}

export interface IProject {
  id?: number;
  ident?: string;
  name?: string;
}

export default function ProjectSelect(props: IProps) {
  const [data, setData] = useState<IProject[]>(props.projectList || []);
  React.useEffect(() => {
    setData(props.projectList || []);
  }, [props.projectList])
  
  const onChange = (value: number) => {
    props.onProjectChange && props.onProjectChange(data.filter(item => item.id === value)?.[0]);
  }

  return (
    <Select
      allowClear={true}
      placeholder="请选择"
      onChange={onChange}
      defaultValue={props.value || data?.[0]?.id}
    >
      {
        _.map(data, (item, index) => {
          return (
            <Select.Option key={item.ident + index} value={item.id}>
              {item.name}({item.id})
            </Select.Option>
          );
        })
      }
    </Select>
  )
}
