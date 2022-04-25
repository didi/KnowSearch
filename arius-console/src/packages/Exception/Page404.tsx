import React from "react";
import { RouteComponentProps } from "react-router-dom";
import "./Page404.less";

const Page404: React.FC<RouteComponentProps> = (props) => {
  const { history } = props;
  return (
    <div className="response-tpl-404">
      <div className="response-tpl-404-content">
        <div className="response-tpl-404-content-operate">
          <h3 className="response-tpl-404-content-operate-message-sorry">
            很抱歉，页面走丢了～
          </h3>
          <div className="response-tpl-404-content-operate-message-tip">
            请检查页面地址是否正确，重新刷新或者
            <span
              onClick={() => {
                history.push({
                  pathname: "/",
                });
              }}
            >
              返回首页
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Page404;
