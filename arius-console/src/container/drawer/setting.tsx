import { Modal, Switch, Button, message, Spin } from 'antd';
import React from 'react';
import { connect } from "react-redux";
import * as actions from 'actions';
import { getSetting, setSetting } from 'api/cluster-api';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const List = [{key: 'cancelCopy', text: '取消副本'}, {key: 'asyncTranslog', text: '异步Translog'}]

class EditSetting extends React.Component<any> {
  //cancelCopy :是否取消副本数 布尔型
  //asyncTranslog:是否开启异步translog设置 布尔型
  public state = {
    loading: false,
    cancelCopy: false,
    asyncTranslog: false,
  }

  public componentDidMount() {
    this.setState({ loading: true });
    getSetting(this.props.params).then(res => {
      if (res) {
        this.setState({
          cancelCopy: res.cancelCopy,
          asyncTranslog: res.asyncTranslog,
          loading: false,
        });
      }
    })
  }

  public handleChange = (check, key) => {
    this.setState({
      [key]: check
    });
  }

  public handleSubmit = () => {
    const { asyncTranslog, cancelCopy } = this.state;
    setSetting({
      asyncTranslog,
      cancelCopy,
      logicId: this.props.params
    }).then(res => {
      if (res) {
        message.success('操作成功');
        this.props.dispatch(actions.setCreateIndex({
          settingCount: Math.random()
        }));
        this.props.dispatch(actions.setModalId(''));
      }
    })
  }

  public renderFooter = () => {
    return (
      <>
        <Button onClick={() => this.props.dispatch(actions.setModalId(''))} style={{ marginRight: 5 }}>取消</Button>
        <Button type="primary" onClick={() => this.handleSubmit()}>确定</Button>
      </>
    )
  }

  public render() {
    const { dispatch } = this.props
    return (
      <Modal
        title={'Setting设置'}
        visible={true}
        onCancel={() => {
          dispatch(actions.setModalId(''))
        }}
        width={400}
        footer={this.renderFooter()}
      >
        <Spin spinning={this.state.loading}>
          <div>
            {List.map((item, index) => (
              <div className='min-title' key={index} style={{  display: 'flex', paddingRight: 30, alignItems: 'center', height: 35, border: '1px solid #eee', borderBottom: index === 0 ? '0px' : '1px solid #eee' }}>
                <div style={{ background: 'rgb(245, 245, 245)', lineHeight: index === 0 ? '34px' : '33px', width: 100, textAlign: 'right', color: '#aaa', paddingRight: 5 }}>
                  {item.text}:
                </div>
                <div>
                  <Switch style={{ marginLeft: 4 }} key={item.key} size="small" checked={this.state[item.key]} onClick={(checked) => this.handleChange(checked, item.key)} />
                </div>
              </div>
            ))}
          </div>
        </Spin>
      </Modal>
    );
  }
}
export default connect(mapStateToProps)(EditSetting);
