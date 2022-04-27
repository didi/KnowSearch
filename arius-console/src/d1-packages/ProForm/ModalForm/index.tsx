import React, { useEffect } from 'react';
import type { ModalProps, FormInstance } from 'antd';
import { Modal } from 'antd';
import useMergedState from 'rc-util/lib/hooks/useMergedState';
import { XForm, renderFormContent, IXFormProps } from '../XForm';
import { SubmitterProps } from '../Submitter';

interface IModalProps {
  /**
   * 接受返回一个boolean，返回 true 会关掉这个Modal
   * @name 表单结束后调用
   */
  onFinish?: (formData) => Promise<boolean | void>;

  /** @name 用于触发Modal打开的 dom */
  trigger?: JSX.Element;

  /** @name 受控的打开关闭 */
  visible?: ModalProps['visible'];

  /** @name 打开关闭的事件 */
  onVisibleChange?: (visible: boolean) => void;

  /** @name Modal的标题 */
  title?: ModalProps['title'];

  /** @name Modal的宽度 */
  width?: ModalProps['width'];

  /** @name 底部操作区域 */
  submitter?: SubmitterProps<{ form?: FormInstance<any> }> | false;

    /** @name 底部操作区域位置 */
  submitterPosition?: 'left' | 'right';

  /**
   * 不支持 'visible'，请使用全局的 visible
   *
   * @name Modal的属性
   */
  modalProps?: Omit<ModalProps, 'visible' | 'footer'>;

  /**
   * @name 表单的配置
   */
  XFormProps: IXFormProps;
}

const ModalForm = ({
  trigger,
  title,
  width,
  modalProps,
  XFormProps,
  onFinish,
  onVisibleChange,
  ...rest
}: IModalProps) => {
  
  const [visible, setVisible] = useMergedState<boolean>(!!rest.visible, {
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
                  submitText: modalProps?.okText || '确认',
                  resetText: modalProps?.cancelText || '取消',
                  ...rest.submitter?.buttonConfig,
                },
                submitButtonProps: {
                  type: (modalProps?.okType as 'text') || 'primary',
                  ...rest.submitter?.submitButtonProps,
                },
                resetButtonProps: {
                  onClick: (e: React.MouseEvent<HTMLButtonElement>) => {
                    modalProps?.onCancel?.(e);
                    setVisible(false);
                  },
                  ...rest.submitter?.resetButtonProps,
                },
              }
        }
        contentRender={(submitter) => {
          return (
            <Modal
              title={title}
              width={width || 500}
              {...modalProps}
              afterClose={() => {
                XFormProps.form?.resetFields();
                modalProps?.afterClose?.();
              }}
              getContainer={false}
              visible={visible}
              onCancel={(e) => {
                setVisible(false);
                modalProps?.onCancel?.(e);
              }}
              footer={
                !!submitter && (
                  <div
                    style={{
                      display: 'flex',
                      justifyContent:
                        rest.submitterPosition === 'left'
                          ? 'flex-start'
                          : 'flex-end',
                    }}
                  >
                    {submitter}
                  </div>
                )
              }
            >
              {renderFormContent({ ...XFormProps })}
            </Modal>
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

export default ModalForm;
