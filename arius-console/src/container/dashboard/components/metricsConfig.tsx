import React, { memo, useState, useEffect } from "react";
import { Button, Modal, Checkbox, Divider, Form, Row, Col, Popover } from "antd";
const CheckboxGroup = Checkbox.Group;
import { SettingOutlined } from '@ant-design/icons';
import "./../index.less";
import Tooltip from "antd/es/tooltip";

export const MetricsConfig = memo((props: any) => {
  const {
    title,
    optionList,
    defaultCheckedData,
    cb: callback,
  } = props;
  const [selectedData, setSelectedData] = useState(defaultCheckedData || []);
  const [disabled, setDisabled] = useState(false);
  const [visible, setVisible] = React.useState(false);

  const showPopconfirm = () => {
    setVisible(true);
  };

  useEffect(() => {
    setDisabled(selectedData.length >= 1 ? false : true)
  }, [selectedData, props.value])

  useEffect(() => {
    setSelectedData(props.value);
  }, [props.value])

  const handleCancel = () => {
    setSelectedData(props.value);
    setVisible(false);
  };
  const renderFooter = () => {
    return (
      <div style={{ overflow: 'hidden', marginTop: 20 }}>
        <Button
          type="primary"
          key="ok"
          onClick={() => {
            setVisible(false);
            callback(selectedData);
            setVisible(false);
          }}
          style={{ float: 'right', marginLeft: 8 }}
          disabled={disabled}
        >
          确认
        </Button>
        <Button key="cancel" onClick={() => handleCancel()} style={{ float: 'right' }}>
          取消
        </Button>
      </div>
    )
  }

  const handleChange = (val: string[]) => {
    setSelectedData(val)
  }

  const renderContent = () => {
    return (
      <div style={{ width: 420 }}>
        <div style={{ fontSize: 14, color: '#212529', marginBottom: 20 }}>{title}</div>
        <Checkbox.Group value={selectedData} style={{ width: '100%' }} onChange={handleChange}>
          <Row gutter={38}>
            {optionList.map(item => (
              item.fixed ? null :
                <Col span={12} key={item.value}>
                  <Checkbox value={item.value}>{item.name?.split('（')[0]}</Checkbox>
                </Col>
            ))}
          </Row>
        </Checkbox.Group>
        {renderFooter()}
      </div>

    )
  }
  return (
    <>
      <Tooltip title="指标配置">
        <Popover
          title={null}
          visible={visible}
          trigger={['click']}
          placement="bottomLeft"
          content={renderContent()}
          onVisibleChange={showPopconfirm}
        >
          <SettingOutlined className="dashboard-config-setting" />
        </Popover>
      </Tooltip>
    </>
  );
});
