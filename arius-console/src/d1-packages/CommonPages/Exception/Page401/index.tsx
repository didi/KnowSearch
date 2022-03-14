import React from 'react';
import './style.less';

const Page401 = () => {
  return (
    <div className="response-tpl-page">
      <div className="response-tpl-page-content">
        <div className="response-tpl-page-content-operate">
          <h3 className="response-tpl-page-content-operate-message-sorry">很抱歉，您暂时没有权限访问该页面～</h3>
          <div className="response-tpl-page-content-operate-message-tip">请联系管理员开通权限进行访问</div>
        </div>
      </div>
    </div>
  );
};

export default Page401;
