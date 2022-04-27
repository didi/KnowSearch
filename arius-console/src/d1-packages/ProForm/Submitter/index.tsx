import React from "react";
import type { FormInstance, ButtonProps } from "antd";
import { Button, Space } from "antd";
// import omit from "omit.js";

/** @name 用于配置操作栏 */
export type ButtonConfig = {
  /** @name 重置按钮的文本 */
  resetText?: React.ReactNode;
  /** @name 提交按钮的文本 */
  submitText?: React.ReactNode;
};

export type SubmitterProps<T = {}> = {
  /** @name  按钮文本的配置*/
  buttonConfig?: ButtonConfig;
  /** @name 提交按钮的 props */
  submitButtonProps?: false | (ButtonProps & { preventDefault?: boolean });
  /** @name 重置按钮的 props */
  resetButtonProps?: false | (ButtonProps & { preventDefault?: boolean });
  /** @name 自定义操作的渲染 */
  render?:
    | ((
        props: SubmitterProps &
          T & {
            submit: () => void;
            reset: () => void;
          },
        dom: JSX.Element[]
      ) => React.ReactNode[] | React.ReactNode | false)
    | false;
};

/**
 * FormFooter组件
  */

const Submitter: React.FC<SubmitterProps & { form: FormInstance }> = (
  props
) => {
  if (props.render === false) {
    return null;
  }

  const {
    form,
    render,
    buttonConfig = {},
    submitButtonProps,
    resetButtonProps = {},
  } = props;
  const submit = () => {
    form.submit();
  };

  const reset = () => {
    form.resetFields();
  };

  const { submitText = "提交", resetText = "重置" } = buttonConfig;
  /** 默认的操作的逻辑 */
  const dom = [];

  if (resetButtonProps !== false) {
    dom.push(
      <Button
        // {...omit(resetButtonProps, ["preventDefault"])}
        key="rest"
        onClick={(e) => {
          if (!resetButtonProps?.preventDefault) reset();
          resetButtonProps?.onClick?.(e);
        }}
      >
        {resetText}
      </Button>
    );
  }
  if (submitButtonProps !== false) {
    dom.push(
      <Button
        type="primary"
        // {...omit(submitButtonProps || {}, ["preventDefault"])}
        key="submit"
        onClick={(e) => {
          if (!submitButtonProps?.preventDefault) submit();
          submitButtonProps?.onClick?.(e);
        }}
      >
        {submitText}
      </Button>
    );
  }

  const renderDom = render ? render({ ...props, submit, reset }, dom) : dom;
  if (!renderDom) {
    return null;
  }
  if (Array.isArray(renderDom)) {
    if (renderDom?.length < 1) {
      return null;
    }
    if (renderDom?.length === 1) {
      return renderDom[0] as JSX.Element;
    }
    return <Space>{renderDom}</Space>;
  }
  return renderDom as JSX.Element;
};

export default Submitter;
