import { MinusOutlined, PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { Button, Select, Tooltip, Input, Checkbox, Form } from 'antd';
import React from 'react';
import './index.less'
import { regIp } from 'constants/reg';

const { TextArea } = Input;

interface Item {
  value: string;
  disabled: boolean;
}

/*
*保存
*重现
*/
export class AddRole extends React.Component<any> {
  state = {
    itemArr: [
      {
        type: 'masternode',
        value: null,
        check: false,
        port: "",
      },
      {
        type: 'clientnode',
        value: null,
        port: "",
      },
      {
        type: 'datanode',
        value: null,
        port: ""
      }
    ],
    isShow: false,
    isFullType: this.props.isFullType || false,
    isCold: false,
    check: {
      masternode: {
        tipText: '',
        isTip: false
      },
      clientnode: {
        tipText: '',
        isTip: false
      },
      datanode: {
        tipText: '',
        isTip: false
      },
      'Cold属性节点': {
        tipText: '',
        isTip: false
      },
    } as { [key: string]: { tipText: string, isTip: boolean } },
    addNodeTypeList: [
      {
        value: 'clientnode',
        disabled: true
      },
      {
        value: 'datanode',
        disabled: true
      }
    ] as Item[]
  }

  setFullType = (isFullType: boolean) => {
    this.setState({
      isFullType,
    });

    // 非全量类型，且未展示全部时
    if (!isFullType && !this.state.isShow) {
      this.setState({
        itemArr: [
          this.state.itemArr[0],
          {
            type: 'clientnode',
            value: null,
            port: "",
          },
          {
            type: 'datanode',
            value: null,
            port: ""
          }]
      }, () => {
        this.handleTextArea();
      });
    }
  }


  addFrom = () => {
    this.setState({
      isShow: !this.state.isShow
    }, () => {
      this.handleTextArea('add');
    });
  }

  del() {
    this.setState({
      isShow: !this.state.isShow,
      itemArr: [
        this.state.itemArr[0],
        {
          type: 'clientnode',
          value: null,
          port: "",
        },
        {
          type: 'datanode',
          value: null,
          port: ""
        }]
    }, () => {
      this.handleTextArea();
    });
  }

  handleTextArea(e?, type?) {
    const arr = this.state.itemArr.map((item => {
      if (item.type === type) {
        item.value = e.target.value
        item.check = this.checkText(item.value, type);
      }
      if (e === 'add' && !type) {
        item.check = this.checkText(item.value, item.type);
      }
      return item
    }));
    if (this.state.isFullType) {
      return this.handleChange(arr);
    }
    if (!this.state.isShow) {
      this.handleChange(arr.splice(0, 1));
      return;
    }
    this.handleChange(arr);
  }

  handlePort(e?, type?) {
    const arr = this.state.itemArr.map((item => {
      if (item.type === type) {
        item.port = e.target.value
        item.check = this.checkText(item.value, type);
      }
      if (e === 'add' && !type) {
        item.check = this.checkText(item.value, item.type);
      }
      return item
    }));
    if (!this.state.isShow) {
      this.handleChange(arr.splice(0, 1));
      return;
    }
    this.handleChange(arr);
  }

  checkText(value, type) {
    const obj = this.state.check;
    // 非master不做空判断
    if (!value && type == 'masternode') {
      obj[type] = {
        tipText: this.props.isHost ? '请输入ip列：127.1.1.1，多个换行分割（回车键换行）。' : '请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。',
        isTip: true
      };
      return obj[type]?.isTip;
    } else if (!value) {
      obj[type] = {
        tipText: '',
        isTip: false
      };
      return obj[type]?.isTip;
    }
    const values = value?.split('\n').filter((ele: string) => ele !== '') || [];
    // 判断重复
    let flat_isHost = false
    if (this.props.isHost) {
      values.forEach(element => {
        if (!new RegExp(regIp).test(element)) {
          return flat_isHost = true
        };
      });
      if (flat_isHost) {
        obj[type] = {
          tipText: this.props.isHost ? '请输入ip列：127.1.1.1，多个换行分割（回车键换行）。' : '请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。',
          isTip: true
        }
      } else if (((new Set(values)).size != values.length) && this.props.isHost) {
        obj[type] = {
          tipText: 'ip存在重复！',
          isTip: true
        }
      } else if (obj[type]?.isTip) {
        obj[type].isTip = false
      }
      this.setState({
        check: obj
      })
      return obj[type].isTip;
    };
    let flat = false;
    let flatPost = false;
    let flatLength = false;
    let flatIp = false;
    const ipArr = [];

    values.forEach(element => {
      if (element.indexOf(':') === -1) {
        flat = true;
      } else {
        const isPostArr = element.split(':');
        ipArr.push(isPostArr[0]);
        if (!new RegExp(regIp).test(isPostArr[0])) {
          flatIp = true;
        }
        if (!isPostArr[1] && (flatIp === false)) {
          flatPost = true;
        }
        if (isPostArr[1] && (flatIp === false)) {
          if (isPostArr[1].length > 4) {
            flatLength = true;
          }
        }
      }
    });
    // 多个相同 ip
    if ((new Set(ipArr)).size != ipArr.length) {
      obj[type] = {
        tipText: '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割且 ip 不能相同',
        isTip: true
      }
    } else if (flat) {
      obj[type] = {
        // 格式错误，没有:号, 列：127.1.1.1:8888 => ip:端口
        tipText: '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割',
        isTip: true
      }
    } else if (flatIp) {
      // 格式错误，ip不符合规范, 列：127.1.1.1:8888 => ip:端口
      obj[type] = {
        tipText: '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割',
        isTip: true
      }
    } else if (flatPost) {
      // 格式错误，:号后面没有端口, 列：127.1.1.1:8888 => ip:端口
      obj[type] = {
        tipText: '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割',
        isTip: true
      }
    } else if (flatLength) {
      // 格式错误，:号端口格式不正确, 多个请换行。
      obj[type] = {
        tipText: '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割',
        isTip: true
      }
    }
    // 业务修改集群不判断个数
    // else if (values.length < 3) {
    //   obj[type] = {
    //     tipText: `${type}节点ip不少于三`,
    //     isTip: true
    //   }
    // }
    else if (obj[type]?.isTip) {
      obj[type].isTip = false
    }
    this.setState({
      check: obj
    });
    return obj[type]?.isTip;
  }

  handleChange(params) {
    const { onChange } = this.props;
    onChange && onChange(params);
  }

  renderItem = (item: Item, key: number) => {
    const { check, itemArr, isCold } = this.state;
    return (
      <div key={key}>
        <div className='add-role-header'>
          <div>
            <span>{item.value}</span>
            {
              this.props.cold && item.value === 'datanode' ?
                <span style={{ marginLeft: 60, display: 'inline-block' }}>
                  <Checkbox checked={isCold} onChange={(e) => {
                    let addNodeTypeList = this.state.addNodeTypeList;
                    let itemArr: any = this.state.itemArr;
                    // cold开关
                    if (e.target.checked) {
                      if (addNodeTypeList && addNodeTypeList.length === 2) {
                        addNodeTypeList.push({
                          value: 'Cold属性节点',
                          disabled: true
                        });
                      }
                      if (itemArr && itemArr.length === 3) {
                        itemArr.push({
                          type: 'Cold属性节点',
                          value: null,
                          port: "",
                          beCold: e.target.checked ? 'true' : 'false',
                        })
                      }
                    } else {
                      if (addNodeTypeList && addNodeTypeList.length === 3) {
                        addNodeTypeList.pop();
                      }
                      if (itemArr && itemArr.length === 4) {
                        itemArr.pop();
                      }
                    }
                    this.setState({
                      addNodeTypeList,
                      isCold: e.target.checked,
                      itemArr,
                    }, () => {
                      !e.target.checked ? this.handleTextArea() : this.handleTextArea('add');
                    });
                  }}>配置Cold节点</Checkbox>
                  <Tooltip title={'如需开启冷热分离服务，需要将部分data节点的rack属性值设为Cold。注：cold节点不能划分region，请谨慎设置'}><QuestionCircleOutlined /></Tooltip>
                </span> : null
            }
          </div>
          {/* {
            key === 0 ? <a href="#" onClick={() => this.del()} >删除</a> : null
          } */}
        </div>
        <TextArea value={itemArr[key + 1].value} style={check[item.value]?.isTip ? null : {  borderColor: '#d9d9d9', boxShadow: 'none' }} onChange={(value) => this.handleTextArea(value, item.value)} rows={2} placeholder={this.props.isHost ? '请输入ip列：127.1.1.1，多个换行分割（回车键换行）。' : '请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。'} />
        {
          check[item.value]?.isTip ?
            <p style={{ color: '#ff4d4f' }}>{check[item.value]?.tipText}</p>
            :
            null
        }
        {
          this.props.isHost ? <Form.Item
            label={`${item.value}Port`}
            name={`${item.value}Port`}
            style={{ marginTop: 10 }}
            rules={[
              {
                required: true,
                message: '请输入端口号！',
              },
            ]}>
            <Input value={itemArr[key + 1].port} onChange={(value) => this.handlePort(value, item.value)} placeholder={'请输入端口号'} />
          </Form.Item> : ""
        }
      </div>
    )
  }

  componentDidMount() {
    const { value } = this.props;
    if (value) {
      const arr: any = this.state.itemArr.map((item, index) => {
        if (item.type === value[index]?.type) {
          item.value = value[index]?.value
        }
        return item;
      });
      const addNodeTypeList = this.state.addNodeTypeList;
      let isCold = this.state.isCold
      // 兼容cold情况
      if (value && value.length === 4) {
        arr.push({
          type: 'Cold属性节点',
          value: value[3]?.value,
          port: "",
          beCold: value[3]?.beCold,
        })
        addNodeTypeList.push({
          value: 'Cold属性节点',
          disabled: true
        });
        if (value[3]?.beCold) {
          isCold = value[3]?.beCold === 'true';
        }
      }
      if (value.length > 1) {
        this.setState({
          itemArr: arr,
          isShow: true,
          addNodeTypeList,
          isCold
        })
      } else {
        this.setState({
          itemArr: arr,
          isShow: false,
          addNodeTypeList,
          isCold
        })
      }
    }
  }

  render() {
    let { addNodeTypeList, isShow, check, itemArr, isFullType } = this.state;
    return <>
      <TextArea value={itemArr[0].value} rows={2} onChange={(value) => this.handleTextArea(value, 'masternode')} placeholder={this.props.isHost ? '请输入ip列：127.1.1.1，多个换行分割（回车键换行）。' : '请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。'} />
      {
        check['masternode']?.isTip || (window as any)?.masternodeErr ?
          <p style={{ color: '#ff4d4f' }}>{check['masternode']?.tipText || '请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割'}</p>
          :
          null
      }
      {this.props.isHost ?
        <Form.Item
          label="MasternodePort"
          name="MasternodePort"
          style={{ marginTop: 10 }}
          rules={[
            {
              required: true,
              validator: (rule: any, value: string) => {
                const reg = /^[0-9]{4}$/;
                if (reg.test(value)) {
                  return Promise.resolve();
                } else {
                  return Promise.reject('请输入4位数字端口号！');
                }
              },
              message: '请输入4位数字端口号！',
            },

          ]}>
          <Input value={itemArr[0].port} onChange={(value) => this.handlePort(value, 'masternode')} placeholder={'请输入端口号'} />
        </Form.Item>
        : ""
      }
      {!isFullType ? <div style={{ marginTop: 10 }}>
        {
          !isShow ?
            <Button type="primary" size='small' onClick={this.addFrom}>
              <PlusOutlined /> 添加节点类型
            </Button>
            :
            <Button
              type="primary"
              size='small'
              onClick={() => {
                this.del()
              }}
              style={{ marginBottom: 10 }}>
              <MinusOutlined /> 删除节点类型
            </Button>
        }
      </div> : null}
      {
        isShow || isFullType ?
          addNodeTypeList.map((item, index) => this.renderItem(item, index))
          :
          null
      }
    </>;
  }
}