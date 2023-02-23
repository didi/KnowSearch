import React from "react";
import { Button, Tooltip, Input, Form } from "antd";
import { MinusOutlined, PlusOutlined, QuestionCircleOutlined } from "@ant-design/icons";

export class RulePassword extends React.Component<any> {
  state = {
    isShow: false,
    addNodeTypeList: [
      {
        value: "user",
        text: "账户名",
        tip: "请输入账户",
        index: 0,
      },
      {
        value: "password",
        text: "密码",
        tip: "请输入密码",
        index: 1,
      },
    ],
    text: "",
    check: {
      user: {
        tipText: "",
        isTip: false,
      },
      password: {
        tipText: "",
        isTip: false,
      },
    } as { [key: string]: { tipText: string; isTip: boolean } },
  };

  addFrom = () => {
    this.setState(
      {
        isShow: !this.state.isShow,
      },
      () => {
        this.handleTextArea();
      }
    );
  };

  del() {
    this.setState(
      {
        isShow: !this.state.isShow,
        text: "",
        check: {
          user: {
            tipText: "",
            isTip: false,
          },
          password: {
            tipText: "",
            isTip: false,
          },
        },
      },
      () => this.handleChange("")
    );
  }

  handleTextArea(e?: string, type?) {
    const { check, text } = this.state;
    const textSplit = text.split(":");
    // 因为根据；号隔开
    e?.replace(":", "");
    if (type === "user") {
      if (!e || e?.length > 32 || e?.indexOf(":") !== -1) {
        check[type] = {
          tipText: "填写账户名，1-32位字符，不支持：号",
          isTip: true,
        };
      } else {
        check[type] = {
          tipText: "",
          isTip: false,
        };
      }
      textSplit[0] = e;
    } else if (type === "password") {
      if (!e || e?.length < 6 || e?.length > 32) {
        check[type] = {
          tipText: "请填写密码，6-32位字符，不支持:号",
          isTip: true,
        };
      } else {
        check[type] = {
          tipText: "",
          isTip: false,
        };
      }
      textSplit[1] = e;
    }
    if ((textSplit[0] && !textSplit[0]?.length) || !textSplit[0]) {
      check.user = {
        tipText: "填写账户名，1-32位字符，不支持：号",
        isTip: true,
      };
    }
    if ((textSplit[1] && !textSplit[1]?.length) || !textSplit[1]) {
      check.password = {
        tipText: "请填写密码，6-32位字符，不支持:号",
        isTip: true,
      };
    }
    this.setState({
      text: textSplit.join(":"),
      check,
    });
    this.handleChange(textSplit.join(":"));
  }

  handleChange(param) {
    const { onChange } = this.props;
    const { check } = this.state;
    let flag = false;
    for (const key in check) {
      if (check[key].isTip) {
        flag = true;
      }
    }
    onChange && onChange({ value: param, check: flag });
  }

  componentDidMount() {
    const { value } = this.props;
    if (value && value?.value) {
      this.setState({
        text: value.value,
        isShow: true,
      });
    }
  }

  renderItem = (item, key: number) => {
    const { text, check } = this.state;
    return (
      <div key={key}>
        <div className="add-role-header" style={{ marginTop: item.index == "1" ? 10 : 0 }}>
          <div>
            <span>{item.text}</span>
          </div>
        </div>
        <Input
          allowClear
          value={text.split(":")[item.index]}
          onChange={(e) => this.handleTextArea(e.target.value, item.value)}
          placeholder={item?.tip}
        />
        {check[item.value]?.isTip ? <p style={{ color: "#ff4d4f" }}>{check[item.value]?.tipText}</p> : null}
      </div>
    );
  };

  render() {
    let { addNodeTypeList, isShow } = this.state;
    return (
      <>
        <div style={{ marginTop: 10 }}>
          {!isShow ? (
            <Button type="primary" size="small" onClick={this.addFrom}>
              <PlusOutlined /> 添加账户密码
            </Button>
          ) : (
            <Button
              type="primary"
              size="small"
              onClick={() => {
                this.del();
              }}
              style={{ marginBottom: 10 }}
            >
              <MinusOutlined /> 删除账户密码
            </Button>
          )}
          <Tooltip title="集群具备账号和密码的用户请自定义添加，不具备请忽略，否则可能导致集群接入失败。">
            <QuestionCircleOutlined
              style={{
                fontSize: 14,
                // verticalAlign: 'middle',
                marginLeft: 10,
                color: "rgb(197, 197, 197)",
              }}
            />
          </Tooltip>
        </div>
        {isShow ? addNodeTypeList.map((item, index) => this.renderItem(item, index)) : null}
      </>
    );
  }
}

export default RulePassword;
