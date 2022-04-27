import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import './style.less';

const Page404: React.FC<RouteComponentProps> = (props) => {
  const { history } = props;
  return (
    <div className="response-tpl-page">
      <div className="response-tpl-page-content">
        <div className="response-tpl-page-content-operate">
          <h3 className="response-tpl-page-content-operate-message-sorry">很抱歉，页面走丢了～</h3>
          <div className="response-tpl-page-content-operate-message-tip">
            请检查页面地址是否正确，或刷新页面
            <span
              className="response-tpl-page-content-operate-message-btn"
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
