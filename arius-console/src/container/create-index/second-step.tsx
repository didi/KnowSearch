import * as React from 'react';
import { TableMappingForm } from './table-mapping-setting';
import { JSONMappingSetting } from './json-mapping-setting';
import { DATE_FORMAT_BY_ES, TEMP_FORM_MAP_KEY, MAPPING_TYPE } from './constant';
import { getFormatJsonStr } from 'lib/utils';
import Url from 'lib/url-parser';
import { getTableMappingData, strNumberToBoolean } from './config';
import { updateIndexMappingInfo, checkIndexMappingInfo, getIndexBaseInfo, getIndexMappingInfo } from 'api/cluster-index-api';
import { Tabs, Button, Modal, message, Checkbox, Tooltip, PageHeader, Empty } from 'antd';
import * as actions from 'actions';
import { connect } from "react-redux";
import { tuple } from 'antd/lib/_util/type';
import { CancelActionModal } from 'container/custom-component';
import { getSetting } from 'api/cluster-api';
import './index.less';
import { InfoCircleFilled } from '@ant-design/icons';
import { InfoItem } from 'component/info-item';

interface IMappingItem {
  type: string;
  enabled?: boolean;
  doc_values?: boolean;
  store?: boolean;
  properties?: IMappingItem;
  [key: string]: any;
}

const mapStateToProps = state => ({
  createIndex: state.createIndex
});

const connects: any = connect
@connects(mapStateToProps)
export class SecondStep extends React.Component<any> {
  // private activeKey: string = this.props.createIndex.temporaryFormMap?.get(TEMP_FORM_MAP_KEY.mappingType) || MAPPING_TYPE.table;
  private $tableFormRef: any = React.createRef();
  private $jsonFormRef: any = React.createRef();
  private isCyclicalRoll: boolean = false;
  private isCreatePage: boolean = true;
  private isModifyPage: boolean = true;
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
    this.isModifyPage = window.location.pathname.includes('mapping');
    this.isDetailPage = window.location.pathname.includes('/detail');
  }

  public state = {
    btnLoading: false,
    activeKey: this.props.createIndex.temporaryFormMap?.get(TEMP_FORM_MAP_KEY.mappingType) || MAPPING_TYPE.table,
    indexBaseInfo: {} as any,
  }

  public componentDidMount() {
    if (this.isModifyPage) {
      getIndexBaseInfo(this.indexId).then(info => {
        this.isCyclicalRoll = !!info?.cyclicalRoll;
        this.setState({
          indexBaseInfo: info,
        })
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.isCyclicalRoll, !!info?.cyclicalRoll));
      });
    }
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
    cbox?.setAttribute('style', `padding-bottom: ${55 + 'px'}`)
    const div = document.querySelector('#mappingName');
    const btn = document.querySelector('.op-btns-group');
    const sider = document.querySelector('.d1-layout-sider-nav');
    div?.setAttribute('style', `min-height: ${mainbox.clientHeight + 20 + 'px;'}position: relative;`)
    btn?.setAttribute('style', `width: ${mainbox.clientWidth + 'px;'}`)
    setTimeout(() => {
      // 通过获取侧边栏增加resiz适配e
      btn?.setAttribute('style', `width: calc(100% - ${sider.clientWidth + 'px);'}`)
    }, 300);
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

  public onChangeTab = (key: string) => {
    // this.activeKey = key;
    this.setState({
      activeKey: key
    })
  }

  public skip = () => {
    if (this.state.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
      const { getFieldValue } = this.$tableFormRef.current;
      const keys = getFieldValue('keys');

      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, keys));
    }
    if (this.state.activeKey === MAPPING_TYPE.json) {
      const editor = this.props.createIndex.activeInstance;
      const value = editor ? editor.getValue() : '';
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.jsonMappingValue, value));
    }
    this.props.dispatch(actions.setCurrentStep(2));
  }

  public handleTableMappingSubmit = () => {
    this.$tableFormRef.current.validateFields().then((values: any) => {
      const keys = Object.keys(values);
      const { getFieldsValue, getFieldValue } = this.$tableFormRef.current;
      if (this.isCyclicalRoll && this.judgeIsNeedWarning(keys, values)) {
        return Modal.warning({
          title: '提示',
          content: '请添加分区字段，分区字段类型必须是date类型, 分区字段为true',
          okText: '确定',
        });
      }
      const previewJson = this.transTableFormDataToJson(values);
      const params = {
        logicTemplateId: this.indexId,
        mapping: previewJson,
      };
      checkIndexMappingInfo(params).then(() => {
        const renderKeys = getFieldValue('keys');
        const allValues = getFieldsValue();

        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, renderKeys));
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, allValues));
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.thirdStepPreviewJson, getFormatJsonStr(previewJson)));
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingValue, JSON.stringify(previewJson)));

        this.props.dispatch(actions.setCurrentStep(2));
      });
    });
  }

  public judgeIsNeedWarning = (keys: string[], values: any) => {
    let i = 0;
    while (i < keys.length) {
      if (keys[i].includes('partition') && values[keys[i]]) {
        break;
      } else {
        i++;
      }
    }
    return i === keys.length;
  }

  public transTableFormDataToJson = (values: any) => {
    const level0List = Object.keys(values).filter(key => key.includes('level-0-0'));
    const idMap = new Map();
    const result = {} as any;
    for (const item of level0List) {
      const id = item?.split('_')[1]; // 'type_0_level-0-0'

      if (idMap.has(id)) {
        idMap.set(id, idMap.get(id).concat(item));
      } else {
        idMap.set(id, [item]);
      }
    }

    for (const [key, value] of idMap) {
      const resultKey = values[`name_${key}_level-0-0`];
      const resultValue = this.getObjByType(`${key}_level-0-0`, values);
      const { children = [] } = this.props.createIndex.secondChildMap.get(`${key}_level-0-0`) || {};

      const secondLevelObj = {} as IMappingItem;
      const thirdLevelObj = {} as IMappingItem;
      for (const item of children) {
        const secondObjKey = values[`name_${item}`];
        const secondObjValue = this.getObjByType(item, values);
        const { children: thirdChildren = [] } = this.props.createIndex.secondChildMap.get(item) || {};

        for (const row of thirdChildren) {
          const objKey = values[`name_${row}`];
          const objValue = this.getObjByType(row, values);
          thirdLevelObj[objKey] = objValue;
        }
        if (thirdChildren.length) {
          secondObjValue.properties = thirdLevelObj;
        }
        secondLevelObj[secondObjKey] = secondObjValue;
      }
      if (children.length) {
        resultValue.properties = secondLevelObj;
      }
      result[resultKey] = resultValue;
    }
    return result;
  }

  public getObjByType = (key: string, values: any) => {
    const typeArray = values[`type_${key}`];
    const type = typeArray[0] === 'date' ? 'date' : typeArray?.[typeArray?.length - 1]; // date类型第二项是gateway需要的参数
    const result = {
      type,
    } as IMappingItem;
    // 检索: object类型enabled、其他类型index
    // 排序: text类型fielddata、其他类型doc_values

    switch (type) {
      case 'object':
        result.enabled = strNumberToBoolean(values[`search_${key}`]); // 检索
        result.dynamic = strNumberToBoolean(values[`dynamic_${key}`]);
        result.doc_values = strNumberToBoolean(values[`sort_${key}`]);
        break;
      case 'nested':
        result.index = strNumberToBoolean(values[`search_${key}`]);
        result.doc_values = strNumberToBoolean(values[`sort_${key}`]);
        break;
      case 'text':
        if (values[`analyzer_${key}`] && values[`analyzer_${key}`] !== 'none') {
          result.analyzer = values[`analyzer_${key}`];
        }
        result.index = strNumberToBoolean(values[`search_${key}`]);
        break;
      case 'date':
      case 'date_range':
        result.doc_values = strNumberToBoolean(values[`sort_${key}`]);
        result.index = strNumberToBoolean(values[`search_${key}`]);
        result.format = values[`type_${key}`]?.[1] || DATE_FORMAT_BY_ES;
        break;
      default:
        result.index = strNumberToBoolean(values[`search_${key}`]);
        result.doc_values = strNumberToBoolean(values[`sort_${key}`]);
        break;
    }
    return result;
  }

  public onHandleNextStep = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.state.activeKey));
    if (this.state.activeKey === MAPPING_TYPE.table && this.$tableFormRef.current) {
      this.handleTableMappingSubmit();
    }
    if (this.state.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.handleSubmit();
    }
  }

  public onHandlePrevStep = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.state.activeKey));
    if (this.state.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.setFilledFormInfo();
      this.props.dispatch(actions.setCurrentStep(0));
    }
    if (this.state.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
      const { getFieldValue, getFieldsValue } = this.$tableFormRef.current;
      const keys = getFieldValue('keys');
      const allValues = getFieldsValue();

      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, allValues));
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, keys));

      this.props.dispatch(actions.setCurrentStep(0));
    }
  }

  public onSave = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.state.activeKey));
    if (this.state.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
      this.$tableFormRef.current.validateFields().then((values: any) => {
        const { getFieldsValue, getFieldValue } = this.$tableFormRef.current;

        const keys = Object.keys(values);
        if (this.isCyclicalRoll && this.judgeIsNeedWarning(keys, values)) {
          return Modal.warning({
            title: '提示',
            content: '请添加分区字段，分区字段类型必须是date类型',
            okText: '确定',
          });
        }
        const previewJson = this.transTableFormDataToJson(values);
        const params = {
          logicId: this.indexId,
          typeProperties: [{
            properties: previewJson,
          }],
        };
        const allValues = getFieldsValue();
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, allValues));

        getTableMappingData(params);
        this.setState({
          btnLoading: true
        });
        updateIndexMappingInfo(params).then(() => {
          message.success('编辑成功');
          window.setTimeout(() => {
            //因不确定改动范围 加判断
            if (this.history.indexOf('index-tpl-management') !== -1) {
              this.props.history.push('/index-tpl-management')
            } else {
              window.location.href = this.history;
            }
          }, 1000);
        }).finally(() => {
          this.setState({
            btnLoading: false
          })
        });
      });
    }
    if (this.state.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.handleSave(this.history);
    }
  }

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
    if ((key == 'customerAnalysis') && check) {
      this.setState({
        activeKey: MAPPING_TYPE.json
      });
    }
    this.props.dispatch(actions.setCreateIndex({ [key]: check }))
  }

  public onClearAndNext = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, {}));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, {}));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.thirdStepPreviewJson, {}));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingValue, JSON.stringify({})));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.jsonMappingValue, ''));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.thirdStepPreviewJson, ''));
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingValue, ''));
    this.props.dispatch(actions.setCurrentStep(2));
  }

  public render() {
    const { isMapping } = this.props.createIndex;
    const isDetailPage = window.location.pathname.includes('/index/logic/detail');
    return (
      <div id="mappingName">
        {this.isModifyPage ? <PageHeader
          className="detail-header"
          backIcon={false}
        > {[{
          label: "模版名称",
          key: "name",
        }, {
          label: "所属集群",
          key: "cluster",
        }].map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              `${this.state.indexBaseInfo?.[row.key] || "-"}`
            }
            width={250}
          />
        ))}</PageHeader> : null}
        <div className={this.isModifyPage ? 'content-wrapper' : ''}>
          {
            isDetailPage ?
              <div className="detail-tabs">
                <div onClick={() => this.onChangeTab(MAPPING_TYPE.table)} className={this.state.activeKey == MAPPING_TYPE.table ? 'detail-tabs-item check' : 'detail-tabs-item'}>表格格式</div>
                <div onClick={() => this.onChangeTab(MAPPING_TYPE.json)} className={this.state.activeKey == MAPPING_TYPE.json ? 'detail-tabs-item-json check' : 'detail-tabs-item-json'}>JSON格式</div>
              </div>
              : null
          }
          <Tabs
            // type="card"
            className={isDetailPage || this.isModifyPage ? `tab-content no-margin ${isDetailPage ? 'no-nav' : ''}` : 'tab-content'}
            activeKey={this.state.activeKey}
            onChange={this.onChangeTab}
          >
            <Tabs.TabPane tab="表格格式" key={MAPPING_TYPE.table}>
              <TableMappingForm wrappedComponentRef={this.$tableFormRef} />
            </Tabs.TabPane>
            <Tabs.TabPane tab="JSON格式" key={MAPPING_TYPE.json}>
              <JSONMappingSetting childEvevnt={child => this.$jsonFormRef = child} upLoading={this.updataBtnLoading} isShowPlaceholder={this.props.isShowPlaceholder} />
            </Tabs.TabPane>
          </Tabs>
          {!isDetailPage && <div className="op-btns-group">
            {this.isCreatePage && <Button onClick={this.onHandlePrevStep}>上一步</Button>}
            {this.isCreatePage && <Button type="primary" onClick={this.onHandleNextStep}>下一步</Button>}
            {this.isModifyPage &&
              <Button loading={this.state.btnLoading} type="primary" onClick={this.onSave}>保存</Button>}
            {this.isModifyPage && <CancelActionModal routeHref={this.history} history={this.props.history} cb={this.clearStore} />}
            {!this.isCyclicalRoll && this.isCreatePage && <Button onClick={() => this.skip()}>跳过</Button>}
          </div>}
        </div>
      </div >

    );
  }
}
