import * as React from 'react';
import { TEMP_FORM_MAP_KEY, CHECK_GROUP } from './constant';
import Url from 'lib/url-parser';
import { Button, Empty, Tooltip, Checkbox } from 'antd';
import * as actions from 'actions';
import { connect } from "react-redux";
import { getSetting } from 'api/cluster-api';
import './index.less';
// 引入codemirror
import { UnControlled as CodeMirror } from 'react-codemirror2';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';
import { InfoCircleFilled } from '@ant-design/icons';

const mapStateToProps = state => ({
  createIndex: state.createIndex
});

const connects: any = connect
@connects(mapStateToProps)
export class Setting extends React.Component<any> {
  private isCyclicalRoll: boolean = false;
  private isCreatePage: boolean = true;
  private isDetailPage: boolean = true;
  private indexId: number = null;
  private history: string = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.indexId = Number(url.search.id);
    this.history = unescape(url.search.history);
    this.isCyclicalRoll = !!(props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.isCyclicalRoll));
    this.isCreatePage = window.location.pathname.includes('create');
    this.isDetailPage = window.location.pathname.includes('/detail');
  }

  public state = {
    btnLoading: false,
  }

  public componentDidMount() {
    if (this.isDetailPage) {
      getSetting(this.indexId).then(res => {
        if (res) {
          this.props.dispatch(actions.setCreateIndex({
            asyncTranslog: res.asyncTranslog,
            cancelCopy: res.cancelCopy,
            customerAnalysisValue: res.analysis ? JSON.stringify(res.analysis, null, 4) : '',
            customerAnalysis: res.analysis ? true : false,
          }))
        }
      })
    }
    const mainbox = document.querySelector('#d1-layout-main');
    const cbox = document.querySelector('.content-wrapper');
    mainbox?.setAttribute('style', `padding-bottom: ${0 + 'px'}`)
    cbox?.setAttribute('style', `padding-bottom: ${10 + 'px'}`)
    const div = document.querySelector('#mappingName');
    const btn = document.querySelector('.op-btns-group');
    div?.setAttribute('style', `margin-top: ${this.isDetailPage ? -4 : 20}px; min-height: ${mainbox.clientHeight + 20 + 'px;'}position: relative; padding-bottom: 55px;`)
    btn?.setAttribute('style', `width: ${mainbox.clientWidth + 'px;'}`)
  }

  public componentDidUpdate(preProps) {
    if (preProps?.createIndex?.settingCount !== this.props?.createIndex?.settingCount) {
      getSetting(this.indexId).then(res => {
        if (res) {
          this.props.dispatch(actions.setCreateIndex({
            asyncTranslog: res.asyncTranslog,
            cancelCopy: res.cancelCopy,
            customerAnalysisValue: res.analysis ? JSON.stringify(res.analysis, null, 4) : '',
            customerAnalysis: res.analysis ? true : false,
          }))
        }
      })
    }
  }

  public skip = () => {
    // 获取 customerAnalysisJson: null
    if (this.props.createIndex.isSetting) {
      const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
      const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : '';
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }))
      this.props.dispatch(actions.setCurrentStep(3));
    } else {
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue: '' }))
      this.props.dispatch(actions.setCurrentStep(3));
    }
  }

  public onHandleNextStep = () => {
    // if (this.state.activeKey === MAPPING_TYPE.table && this.$tableFormRef.current) {
    //   this.handleTableMappingSubmit();
    // }
    // if (this.state.activeKey === MAPPING_TYPE.json) {
    //   this.$jsonFormRef.handleSubmit();
    // }
    if (this.props.createIndex.isSetting) {
      const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
      const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : '';
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }))
      this.props.dispatch(actions.setCurrentStep(3));
    } else {
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue: '' }))
      this.props.dispatch(actions.setCurrentStep(3));
    }
  }

  public onHandlePrevStep = () => {
    if (this.props.createIndex.isSetting) {
      const customerAnalysisJsonEditor = this.props.createIndex.customerAnalysisJson;
      const customerAnalysisValue = customerAnalysisJsonEditor ? customerAnalysisJsonEditor.getValue() : '';
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue }))
      this.props.dispatch(actions.setCurrentStep(1));
    } else {
      this.props.dispatch(actions.setCreateIndex({ customerAnalysisValue: '' }))
      this.props.dispatch(actions.setCurrentStep(1));
    }
  }

  // public onSave = () => {
  //   this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.state.activeKey));
  //    if (this.state.activeKey === MAPPING_TYPE.json) {
  //      this.$jsonFormRef.handleSave(this.history);
  //    }
  // }

  public updataBtnLoading = (b: boolean) => {
    this.setState({
      btnLoading: b
    })
  }

  // 关闭页面跳转
  public clearStore = (str) => {
    this.props?.setRemovePaths([str]);
  }

  public handleCheckGroup = (key, check) => {
    this.props.dispatch(actions.setCreateIndex({ [key]: check }))
  }

  public handleCheckAllGroup = (check: boolean) => {
    this.props.dispatch(actions.setCreateIndex({ asyncTranslog: check, cancelCopy: check, customerAnalysis: check }))
  }

  public render() {
    const isDetailPage = window.location.pathname.includes('/detail');
    const { customerAnalysis, customerAnalysisValue } = this.props.createIndex
    return (
      <div id="mappingName" className={'tab-content'}>
        <div>
          {
            this.isDetailPage ? <>
              {
                CHECK_GROUP.map(item => {
                  return <Checkbox disabled={this.isDetailPage} className={'setingcheck-disabled'} style={{ marginRight: 100 }} checked={this.props.createIndex[item.value]} key={item.value} onChange={(e) => {
                    this.handleCheckGroup(item.value, e.target.checked);
                  }}>{item.text}</Checkbox>
                })
              }
              <Tooltip title="打开自定义分词器显示相应编辑器"><InfoCircleFilled style={{ width: 14, height: 14, color: '#495057', position: 'absolute', left: 500, top: 10 }} /></Tooltip>
            </>
              : <div className="setting-check">
                <div className="left-box">
                  <Checkbox indeterminate={!(this.props.createIndex['asyncTranslog'] && this.props.createIndex['cancelCopy'] && this.props.createIndex['customerAnalysis']) && (this.props.createIndex['asyncTranslog'] || this.props.createIndex['cancelCopy'] || this.props.createIndex['customerAnalysis'])} checked={this.props.createIndex['asyncTranslog'] && this.props.createIndex['cancelCopy'] && this.props.createIndex['customerAnalysis']} onChange={(e) => this.handleCheckAllGroup(e.target.checked)}>全选</Checkbox>
                </div>
                <div className="right-box">
                  {CHECK_GROUP.map(item => {
                    return <Checkbox disabled={this.isDetailPage} style={{ marginRight: 10 }} checked={this.props.createIndex[item.value]} key={item.value} onChange={(e) => {
                      this.handleCheckGroup(item.value, e.target.checked);
                    }}>{item.text}</Checkbox>
                  })}
                  <Tooltip title="打开自定义分词器显示相应编辑器"><span><InfoCircleFilled style={{ width: 14, height: 14, color: '#495057', position: 'absolute', right: 0, top: 24 }} /></span></Tooltip>
                </div>
              </div>
          }
          {
            customerAnalysis ?
              <div className={'json-editor-wrapper'}>
                <div className="json-content-title">
                  自定义分词编辑器
                </div>
                <CodeMirror
                  options={{
                    mode: 'application/json',
                    lineNumbers: true,
                    // 自动缩进
                    smartIndent: true,
                    //start-设置支持代码折叠
                    lineWrapping: true,
                    foldGutter: true,
                    gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'], //end
                    indentUnit: 4, // 缩进配置（默认为2）
                    readOnly: isDetailPage,
                  }}
                  editorDidMount={(editor) => {
                    editor.setValue(customerAnalysisValue);
                    this.props.dispatch(actions.setCreateIndex({ customerAnalysisJson: editor }));
                  }}
                />
              </div>
              : <Empty description="您还没有自定义分词器~" style={{ marginBottom: 100, marginTop: 100 }} />
          }
          {!isDetailPage && <div className="op-btns-group">
            {this.isCreatePage && <Button onClick={this.onHandlePrevStep}>上一步</Button>}
            {this.isCreatePage && <Button type="primary" onClick={this.onHandleNextStep}>下一步</Button>}
            {/* {this.isModifyPage &&
              <Button loading={this.state.btnLoading}  type="primary" onClick={this.onSave}>保存</Button>}
            {this.isModifyPage && <CancelActionModal routeHref={this.history} history={this.props.history} cb={this.clearStore} />} */}
            {!this.isCyclicalRoll && this.isCreatePage && <Button onClick={() => this.skip()}>跳过</Button>}
          </div>}
        </div>
      </div>

    );
  }
}
