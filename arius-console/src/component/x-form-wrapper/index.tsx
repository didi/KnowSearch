/* eslint-disable react/display-name */
import * as React from "react";
import { message } from "antd";
import { Drawer, Modal, Button, Form } from "knowdesign";
import { XForm as XFormComponent } from "component/x-form";
import { IXFormWrapper } from "interface/common";
import "./index.less";

export const XFormWrapper = React.forwardRef((props: IXFormWrapper, ref) => {
  const [confirmLoading, setConfirmLoading] = React.useState(false);
  const {
    type,
    visible,
    title,
    width,
    formLayout,
    cancelText,
    okText,
    layout,
    formData = {},
    formMap,
    onCancel,
    onChangeVisible,
    nofooter,
    customRenderElement,
    noform,
    onHandleValuesChange,
    needSuccessMessage = true,
    className,
  } = props;

  const [form] = Form.useForm();

  React.useEffect(() => {
    form.setFieldsValue(formData);
  }, [formData]);

  const resetForm = (resetFields?: any) => {
    form.resetFields(resetFields || "");
  };

  const closeModalWrapper = () => {
    onChangeVisible && onChangeVisible(false);
  };

  const handleCancel = () => {
    // tslint:disable-next-line:no-unused-expression
    onCancel && onCancel();
    resetForm();
    closeModalWrapper();
  };

  const handleSubmit = () => {
    form
      .validateFields()
      .then((result) => {
        const { onSubmit, isWaitting, needBtnLoading, actionAfterFailedSubmit, actionAfterSubmit } = props;
        if (typeof onSubmit === "function") {
          if (isWaitting) {
            setConfirmLoading(true);
            onSubmit(result)
              .then((res: any) => {
                setConfirmLoading(false);

                if (typeof actionAfterSubmit === "function") {
                  actionAfterSubmit(res);
                }
                needSuccessMessage && message.success("操作成功");
                resetForm();
                closeModalWrapper();
              })
              .catch((err) => {
                if (typeof actionAfterFailedSubmit === "function") {
                  actionAfterFailedSubmit();
                }
                setConfirmLoading(false);
              })
              .finally(() => {
                setConfirmLoading(false);
              });
            return;
          }
          if (needBtnLoading) {
            setConfirmLoading(true);
            onSubmit(result).finally(() => setConfirmLoading(false));
            return;
          }

          // tslint:disable-next-line:no-unused-expression
          onSubmit && onSubmit(result);
          closeModalWrapper();
        }
      })
      .catch((errs) => {
        //
      });
  };

  const renderDrawer = () => {
    return (
      <Drawer
        title={title}
        visible={visible}
        width={width}
        closable={true}
        className={`wrap-contain ${className ? className : ""}`}
        onClose={handleCancel}
        maskClosable={false}
        destroyOnClose={true}
        footer={
          !nofooter && (
            <div className="footer-btn">
              <Button className="confirm-button" loading={confirmLoading} type="primary" onClick={handleSubmit}>
                {okText || "确定"}
              </Button>
              <Button onClick={handleCancel}>{cancelText || "取消"}</Button>
            </div>
          )
        }
      >
        <>{customRenderElement}</>
        {!noform && (
          <XFormComponent
            layout={layout || "vertical"}
            form={form}
            formData={formData}
            formMap={formMap}
            formLayout={formLayout}
            onHandleValuesChange={onHandleValuesChange}
          />
        )}
      </Drawer>
    );
  };
  const renderModal = () => {
    return (
      <Modal
        width={width || 600}
        className={`wrap-contain ${className ? className : ""}`}
        title={title}
        visible={visible}
        confirmLoading={confirmLoading}
        maskClosable={false}
        onOk={handleSubmit}
        onCancel={handleCancel}
        okText={okText || "确定"}
        cancelText={cancelText || "取消"}
      >
        <XFormComponent
          form={form}
          formData={formData}
          formMap={formMap}
          formLayout={formLayout}
          layout={layout || "vertical"}
          onHandleValuesChange={onHandleValuesChange}
        />
      </Modal>
    );
  };

  React.useImperativeHandle(ref, () => ({
    form,
  }));

  return <>{type === "drawer" ? renderDrawer() : renderModal()}</>;
});
