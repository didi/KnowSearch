import "./index.less";
import React, { useState } from "react";

interface ITitle {
  title: string;
  content: string;
}
export const RenderTitle = (header: ITitle) => {
  return (
    <div className="table-header-box">
      <span className="table-header-box-title">{header.title}</span>
      {header.content ? (
        <>
          <span> / </span>
          <span className="table-header-box-content">{header.content}</span>
        </>
      ) : null}
    </div>
  );
};
