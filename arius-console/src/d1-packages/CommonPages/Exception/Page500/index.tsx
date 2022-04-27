import React from 'react';
import './style.less';

const Page500 = () => {
  return (
    <div className="response-tpl-page">
        <div className="response-tpl-page-content">
              <div className="response-tpl-page-content-operate">
                  <h3 className="response-tpl-page-content-operate-message-sorry">抱歉，服务器开小差儿了～</h3>
                  <div className="response-tpl-page-content-operate-message-tip">请刷新页面或稍后进行重试</div>
              </div>
        </div>
    </div>
  );
};

export default Page500;
