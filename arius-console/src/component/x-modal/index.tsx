import React from "react";
import { Modal } from "antd";
import "./index.less";

export const XModal = (props) => {
  const { type, title, content, width = 400, okText = "确认", cancelText = "取消", onOk, className, icon, attrs } = props;
  const iconNode = (type: string) => {
    switch (type) {
      case "error":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconerror-circle"></use>
          </svg>
        );
      case "delete":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconerror-circle"></use>
          </svg>
        );
      case "info":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconinfo-circle"></use>
          </svg>
        );
      case "success":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconsuccess-circle"></use>
          </svg>
        );
      case "warning":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconwarning-circle"></use>
          </svg>
        );
    }
  };
  return Modal.confirm({
    title,
    content,
    width,
    okText,
    cancelText,
    onOk,
    className: `x-modal ${className ? className : ""}`,
    icon: icon || iconNode(type),
    ...attrs,
  });
};
