import React, { useState, useEffect } from 'react';
import { Switch, Descriptions, Tooltip } from 'antd';
import { templateServiceWhenJoin } from "api/cluster-api";

const switchConfig = [
  {
    text: '预创建:',
    value: '1',
    tip: '对于分区创建的索引，支持预创建，减轻集群负担，提高稳定性',
  },
  {
    text: '过期删除:',
    value: '2',
    tip: '支持索引根据保存周期自动清理，避免磁盘过满',
  },
  {
    text: 'Mapping设置:',
    value: '4',
    tip: '提供修改索引的 mapping 的信息的功能',
  },
  {
    text: 'Setting设置:',
    value: '5',
    tip: '提供修改索引 Setting 的信息的功能',
  },
  {
    text: '冷热分离:',
    value: '8',
    tip: '提供SSD和HDD两种类型的磁盘来保存索引，从而降低成本',
  },
]

interface StepSwitchProps {
  templateSrvs: string[];
  formSwitch: any;
  param: any
}

const StepSwitch = (props: StepSwitchProps) => {
  const [templateSrvs, setTemplateSrvs] = useState<string[]>([]);

  const switchChange = (value: string) => {
    if (templateSrvs.includes(value)) {
      setTemplateSrvs(templateSrvs.filter(item => item !== value));
    } else {
      templateServiceWhenJoin(value, props.param).then(res => {
        if (res) {
          setTemplateSrvs([...templateSrvs, `${value}`]);
        }
      })
    }
  }

  useEffect(() => {
    if (props.templateSrvs && props.templateSrvs?.length && props.templateSrvs[0]) {
      setTemplateSrvs(props.templateSrvs)
    }
  }, [props.templateSrvs])

  useEffect(() => {
    props.formSwitch.current = templateSrvs;
  },[templateSrvs, props.formSwitch])

  return <>
    <Descriptions bordered column={2} labelStyle={{ width: 100, textAlign: 'right', padding: '12px 8px' }}>
      {switchConfig.map(item => {
        const tip = (
          <Tooltip
            placement="top"
            title={item.tip}
          >
            <span style={{ cursor: "pointer" }}>{item.text}</span>
          </Tooltip>
        )
        return <Descriptions.Item label={tip} key={item.value}>
          <Switch checked={templateSrvs.includes(item.value)} onChange={() => switchChange(item.value)} />
        </Descriptions.Item>
      })}
    </Descriptions>
  </>
};

export default StepSwitch;
