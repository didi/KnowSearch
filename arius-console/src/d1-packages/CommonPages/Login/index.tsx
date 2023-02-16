import React, { useState } from "react";
import { debounce } from "lodash";
import "./index.less";
import { LoginForm } from "./LoginForm";
import { RegisterForm } from "./RegisterForm";

export const LoginOrRegister: React.FC<any> = (props) => {
  const { type } = props;
  const defaultKey = window.location.hash.replace("#", "") || type || "login";
  const [currentKey, setCurrentKey] = useState(defaultKey);
  const [critical, setCritical] = useState(window.innerWidth < 1366);

  React.useEffect(() => {
    window.addEventListener("resize", handleResize); //监听窗口大小改变
    return () => window.removeEventListener("resize", debounce(handleResize, 500));
  }, []);

  const handleResize = (e) => {
    if (e.target.innerWidth < 1366) {
      setCritical(true);
    } else {
      setCritical(false);
    }
  };

  const renderContent = () => {
    return currentKey === "register" ? (
      <RegisterForm fn={handleMenuClick} />
    ) : (
      <LoginForm fn={(isAdminUser: boolean) => props.history.push(isAdminUser ? "/dashboard" : "/cluster/logic")} />
    );
  };

  const handleMenuClick = (e: string) => {
    setCurrentKey(e);
    window.location.hash = e;
  };

  /*
   *屏幕宽度大于等于1366
   *@ 图片宽度占页面宽度比三分之一，等比缩放
   *@ 表单区域占比三分之二，区域内水平居中
   *屏幕宽度小于1366
   *@ 不展示图片
   *@ 水平居中
   */

  const renderHeader = () => {
    return (
      <>
        {critical === true ? (
          <div className="login-box-form-center-header-left">
            <div className="login-box-form-center-header-logo"></div>
          </div>
        ) : isLogin ? (
          <div>
            <div className="login-box-form-center-header-logo"></div>
            <div className="login-box-form-center-header-desc">零侵入、零门槛 Elasticsearch GUI管控平台</div>
          </div>
        ) : (
          <div className="login-box-form-center-header-reg">
            <div className="login-box-form-center-header-reg-desc">账号注册</div>
            <div>
              已有账号，<a onClick={() => handleMenuClick("login")}>直接登录</a>
            </div>
          </div>
        )}
        {/* <div className='login-box-form-center-header-right'>
          <CopyOutlined />
          说明文档
        </div> */}
      </>
    );
  };

  const isLogin = currentKey === "login";

  return (
    <div className="login-box">
      <div className="login-box-left" style={{ display: critical ? "none" : null }}>
        <div className="login-box-left-img"></div>
      </div>
      <div className="login-box-form">
        <div className="login-box-form-center">
          <div className="login-box-form-center-header">{renderHeader()}</div>
          <div className="login-box-form-center-content">{renderContent()}</div>
          <div className="login-box-form-center-title" style={isLogin ? { marginTop: "5%" } : null}>
            <div className="login-box-form-center-title-right">
              {currentKey === "register" ? null : (
                <div>
                  {" "}
                  还没账号，<a onClick={() => handleMenuClick("register")}>立即注册</a>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
