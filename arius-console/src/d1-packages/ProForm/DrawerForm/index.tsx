import React, { useEffect, useState } from 'react';
import type { DrawerProps, FormInstance } from 'antd';
import { Drawer } from 'antd';
import useMergedState from 'rc-util/lib/hooks/useMergedState';
import { XForm, renderFormContent, IXFormProps } from '../XForm';
import { SubmitterProps } from '../Submitter';

interface IDrawerProps {
  /**
   * 接受返回一个boolean，返回 true 会关掉这个抽屉
   * @name 表单结束后调用
   */
  onFinish?: (formData) => Promise<boolean | void>;

  /** @name 用于触发抽屉打开的 dom */
  trigger?: JSX.Element;

  /** @name 受控的打开关闭 */
  visible?: DrawerProps['visible'];

  /** @name 打开关闭的事件 */
  onVisibleChange?: (visible: boolean) => void;

  /** @name 抽屉的标题 */
  title?: DrawerProps['title'];

  /** @name 抽屉的宽度 */
  width?: DrawerProps['width'];

  /** @name 抽屉的预设的几种size */
  size?: 'small' | 'middle' | 'large';

  /** @name 底部操作区域 */
  submitter?: SubmitterProps<{ form?: FormInstance<any> }> | false;

  /** @name 底部操作按钮的位置 */
  submitterPosition?: 'left' | 'right';

  /**
   * 不支持 'visible'，请使用全局的 visible
   *
   * @name 抽屉的属性
   */
  drawerProps?: Omit<DrawerProps, 'visible' | 'getContainer' | 'footer' | 'footerStyle'>;

  /**
   *
   * @name XForm 表单的配置
   */
  XFormProps: IXFormProps;
}

const DrawerForm = ({ trigger, title, width, size = 'middle', drawerProps, XFormProps, onFinish, onVisibleChange, ...rest }: IDrawerProps) => {
  const sizeMap = {
    small: 595,
    middle: 728
  }
  const [visible, setVisible] = useMergedState<boolean>(!!rest.visible,  {
    value: rest.visible,  
    onChange: onVisibleChange,
  });  
  useEffect(() => {
    if (visible && rest.visible) {
      onVisibleChange?.(true);
    }
  }, [visible]);

  const renderFormDom = () => {
    return (
      <XForm
        formData={XFormProps.formData}
        formMap={XFormProps.formMap}
        form={XFormProps.form}
        submitter={
          rest.submitter === false
            ? false
            : {
                ...rest.submitter,
                buttonConfig: {
                  submitText: '确认',
                  resetText: '取消',
                  ...rest.submitter?.buttonConfig,
                },
                resetButtonProps: {
                  onClick: (e: any) => {
                    setVisible(false);
                    drawerProps?.onClose?.(e);
                  },
                  ...rest.submitter?.resetButtonProps,
                },
              }
        }
        contentRender={(submitter) => {
          return (
            <Drawer
              title={title}
              width={width || sizeMap[size]}
              {...drawerProps}
              getContainer={false}
              visible={visible}
              onClose={(e) => {
                setVisible(false);
                XFormProps.form?.resetFields();
                drawerProps?.onClose?.(e);
              }}
              footer={
                !!submitter && (
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: rest.submitterPosition === 'left' ? 'flex-start' : 'flex-end',
                    }}
                  >
                    {submitter}
                  </div>
                )
              }
            >
              {renderFormContent({ ...XFormProps })}
            </Drawer>
          );
        }}
        onFinish={async (values) => {
          if (!onFinish) {
            return;
          }
          const success = await onFinish(values);
          if (success) {
            setVisible(false);
            XFormProps.form?.resetFields();
          }
        }}
      ></XForm>
    );
  };
  return (
    <>
      {renderFormDom()}
      {trigger &&
        React.cloneElement(trigger, {
          ...trigger.props,
          onClick: (e: any) => {
            setVisible(!visible);
            trigger.props?.onClick?.(e);
          },
        })}
    </>
  );
};

export default DrawerForm;
