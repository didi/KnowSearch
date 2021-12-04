import React from "react";
import { RouteComponentProps } from "react-router-dom";
import "./Page403.less";

const Page403: React.FC<RouteComponentProps> = (props) => {
  const { history } = props;
  return (
    <div className="response-tpl-403">
      <div className="response-tpl-403-content">
        <div className="response-tpl-403-content-operate">
          <h3 className="response-tpl-403-content-operate-message-sorry">
            抱歉，您没有权限访问该页面
            <span
              onClick={() => {
                history.push({
                  pathname: "/",
                });
              }}
            >
              返回首页
            </span>
          </h3>
        </div>
      </div>
    </div>
  );
};

export default Page403;
