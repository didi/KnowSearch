import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import '../style.less';

const Page403 = (props) => {
  return (
    <div className="response-tpl-page">
      <div className="response-tpl-page-content p-403">
        <div className="response-tpl-page-content-operate">
          <h3 className="response-tpl-page-content-operate-message-sorry">
            抱歉，您没有权限访问该页面
          </h3>
          {/* <div
            className="response-tpl-page-content-operate-message-btn"
            onClick={() => {
              props.history.push({
                pathname: "/",
              });
            }}
          >
            返回首页
          </div> */}
        </div>
      </div>
    </div>
  );
};

export default Page403;
