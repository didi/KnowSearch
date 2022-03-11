import * as React from 'react';
import './index.less';
import Url from 'lib/url-parser';
import { IUNSpecificInfo } from 'typesPath/base-types';
import { SQLQuery1 } from './sql';
import { getApplyOnlineColumns } from './config';
import { IClearIndexParams } from 'typesPath/params-types';
import { Table, Radio, Tooltip, Checkbox, Button, message } from 'antd';
import { clearIndex, getClearInfo } from 'api/cluster-index-api';
import { QuestionCircleTwoTone } from '@ant-design/icons';

export class ClearIndex extends React.Component<any, any> {
  public state = {
    searchKey: '',
    radioType: 1,
    checked: false,
    clearInfo: {} as IUNSpecificInfo,
    login: false,
    clearInfoSelectedRowKeys: [] as string[]
  };
  private id: number;
  private clusterId: number;
  private sqlEditorRef: any;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.clusterId = Number(url.search.clusterId);
  }

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    if (!isNaN(this.id)) {
      this.setState({loading: true});
      getClearInfo(this.id).then((value) => {
        this.setState({
          clearInfo: value,
        });
      }).finally(() => {
        this.setState({loading: false});
      });
    }
  }

  public getData = (origin?: IUNSpecificInfo[]) => {
    origin = (origin || []).map(item => {
      return {
        name: item,
      };
    });
    let { searchKey } = this.state;
    searchKey = (searchKey + '').trim().toLowerCase();
    const data = searchKey ? origin.filter((d) => d.name?.toLowerCase().includes(searchKey as string),
    ) : origin;
    return data;
  }

  public getColumns = () => {
    const cols = [{
      title: '',
      dataIndex: 'name',
      key: 'name',
    }];
    return cols;
  }

  public onSubmit = () => {
    const value = this.sqlEditorRef?.getEditorValue();
    const params: IClearIndexParams = {
      logicId: this.id,
      delIndices: this.state.clearInfoSelectedRowKeys,
      delQueryDsl: value,
    };
    clearIndex(params).then(() => {
      message.success('清除成功');
      this.setState({
        checked: false,
      });
      this.setState({
        clearInfoSelectedRowKeys: []
      });
      this.reloadData();
    });
  }

  public onRadioChange = e => {
    this.setState({
      radioType: e.target.value,
    });
  }

  public onCheckChange = e => {
    this.setState({
      checked: e.target.checked,
    });
  }

  public onSelectChange = (selectedRowKeys: string[]) => {
    this.setState({
      clearInfoSelectedRowKeys: selectedRowKeys
    });
  }

  public renderTable = () => {
    const rowSelection = {
      selectedRowKeys: this.state.clearInfoSelectedRowKeys,
      onChange: this.onSelectChange,
    };
    return (
      <>
        <Table
          scroll={{ x: 450, y: 900 }}
          loading={this.state.login}
          rowKey="name"
          dataSource={this.getData(this.state.clearInfo.indices)}
          columns={this.getColumns()}
          pagination={false}
          rowSelection={rowSelection}
        />
      </>
    );
  }

  public render() {
    return (
      <>
        <div className="clear-info">
          <div className="table-wrapper no-padding">
            {this.renderTable()}
          </div>
          <div className="option-panel">
            <div className="op-btn">
              <Checkbox checked={this.state.checked} onChange={this.onCheckChange}>索引数据清理后数据无法恢复，请确认影响后继续</Checkbox>
              <Button
                type="primary"
                loading={this.state.login}
                onClick={this.onSubmit}
                disabled={!this.state.checked || this.state.login }
              >
                清理
              </Button>
              <Button
                type="primary"
                style={{ marginLeft: 15 }}
                onClick={() => { this.props?.history?.push('/index-tpl-management') }}
              >
                取消
              </Button>
            </div>
            {this.state.radioType === 2 && <SQLQuery1 ref={editorRef => this.sqlEditorRef = editorRef} className="sql-content" />}
            <div className={this.state.radioType === 2 ? 'tip' : 'tip more-padding'}>
              <span>您选择的物理模版文件最近7天有如下AppId在查询：</span>
              <Tooltip title="您选择的物理索引文件中的数据将被清除，逻辑模板依然保留">
                <QuestionCircleTwoTone />
              </Tooltip>
            </div>
            <Table
              rowKey="key"
              scroll={{ x: 450, y: 400 }}
              dataSource={this.state.clearInfo.accessApps}
              columns={getApplyOnlineColumns()}
              pagination={false}
              bordered={true}
            />
          </div>
        </div>
      </>
    );
  }
}
