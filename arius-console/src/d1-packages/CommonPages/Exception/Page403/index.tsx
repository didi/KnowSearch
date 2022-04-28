import React from 'react';
import './style.less';
import { RouteComponentProps } from 'react-router-dom';
const Page403: React.FC<RouteComponentProps> = (props) => {
  return (
    <div className="response-tpl-page">
      <div className="response-tpl-page-content">
        <div className="response-tpl-page-content-operate">
          <h3 className="response-tpl-page-content-operate-message-sorry">
            抱歉，您没有权限访问该页面
          </h3>
          <span
            className="response-tpl-page-content-operate-message-btn"
            onClick={() => {
              props.history.push({
                pathname: "/",
              });
            }}
          >
            返回首页
          </span>
        </div>
      </div>
    </div>
  );
};

export default Page403;
