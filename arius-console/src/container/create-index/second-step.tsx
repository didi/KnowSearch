import * as React from 'react';
import { TableMappingForm } from './table-mapping-setting';
import { JSONMappingSetting } from './json-mapping-setting';
import { DATE_FORMAT_BY_ES, TEMP_FORM_MAP_KEY, MAPPING_TYPE } from './constant';
import { getFormatJsonStr } from 'lib/utils';
import Url from 'lib/url-parser';
import { getTableMappingData } from './config';
import { updateIndexMappingInfo, checkIndexMappingInfo, getIndexBaseInfo, getIndexMappingInfo } from 'api/cluster-index-api';
import { Tabs, Button, Modal, message } from 'antd';
import * as actions from 'actions';
import { connect } from "react-redux";
import { tuple } from 'antd/lib/_util/type';
import { CancelActionModal } from 'container/custom-component';

interface IMappingItem {
  type: string;
  enabled?: boolean;
  dynamic?: boolean;
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
  private activeKey: string = this.props.createIndex.temporaryFormMap?.get(TEMP_FORM_MAP_KEY.mappingType) || MAPPING_TYPE.table;
  private $tableFormRef: any = React.createRef();
  private $jsonFormRef: any =  React.createRef();
  private isCyclicalRoll: boolean = false;
  private isCreatePage: boolean = true;
  private isModifyPage: boolean = true;
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
  }

  public state = {
    btnLoading: false
  }

  public componentDidMount() {
    if (this.isModifyPage) {
      getIndexBaseInfo(this.indexId).then(info => {
        this.isCyclicalRoll = !!info?.cyclicalRoll;
        this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.isCyclicalRoll, !!info?.cyclicalRoll));
      });
    }
  }


  public onChangeTab = (key: string) => {
    this.activeKey = key;
  }

  public skip = () => {
    if (this.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
      const { getFieldValue } = this.$tableFormRef.current;
      const keys = getFieldValue('keys');

      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, keys));
    }
    if (this.activeKey === MAPPING_TYPE.json) {
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
        result.enabled = !!values[`search_${key}`]; // 检索
        result.dynamic = !!values[`dynamic_${key}`];
        result.doc_values = !!values[`sort_${key}`];
        break;
      case 'nested':
        result.index = !!values[`search_${key}`];
        result.doc_values = !!values[`sort_${key}`];
        break;
      case 'text':
        if (values[`analyzer_${key}`] && values[`analyzer_${key}`] !== 'none') {
          result.analyzer = values[`analyzer_${key}`];
        }
        break;
      case 'date':
      case 'date_range':
        result.doc_values = !!values[`sort_${key}`];
        result.index = !!values[`search_${key}`];
        result.format = DATE_FORMAT_BY_ES;
        break;
      default:
        result.index = !!values[`search_${key}`];
        result.doc_values = !!values[`sort_${key}`];
        break;
    }
    return result;
  }

  public onHandleNextStep = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.activeKey));
    if (this.activeKey === MAPPING_TYPE.table && this.$tableFormRef.current) {
      this.handleTableMappingSubmit();
    }
    if (this.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.handleSubmit();
    }
  }

  public onHandlePrevStep = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.activeKey));
    if (this.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.setFilledFormInfo();
      this.props.dispatch(actions.setCurrentStep(0));
    }
    if (this.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
      const { getFieldValue, getFieldsValue } = this.$tableFormRef.current;
      const keys = getFieldValue('keys');
      const allValues = getFieldsValue();

      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, allValues));
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingKeys, keys));

      this.props.dispatch(actions.setCurrentStep(0));
    }
  }

  public onSave = () => {
    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.mappingType, this.activeKey));
    if (this.activeKey === MAPPING_TYPE.table && this.$tableFormRef) {
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
              if (this.history.indexOf('index-tpl-management') !== -1 ) {
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
    if (this.activeKey === MAPPING_TYPE.json) {
      this.$jsonFormRef.handleSave(this.history);
    }
  }

  public updataBtnLoading = (b: boolean) => {
    this.setState({
      btnLoading: b
    })
  }

  public render() {
    const isDetailPage = window.location.pathname.includes('/index/detail');

    return (
      <>
        <div className={this.isModifyPage ? 'content-wrapper' : ''}>
          <Tabs
            type="card"
            className={isDetailPage || this.isModifyPage ? 'tab-content no-margin' : 'tab-content'}
            defaultActiveKey={this.activeKey}
            onChange={this.onChangeTab}
          >
            <Tabs.TabPane tab="表格格式" key={MAPPING_TYPE.table}>
              <TableMappingForm wrappedComponentRef={this.$tableFormRef} />
            </Tabs.TabPane>
            <Tabs.TabPane tab="JSON格式" key={MAPPING_TYPE.json}>
              <JSONMappingSetting childEvevnt={child => this.$jsonFormRef = child} upLoading={this.updataBtnLoading} isShowPlaceholder={this.props.isShowPlaceholder} />
            </Tabs.TabPane>
          </Tabs>
          {!isDetailPage && <div className="op-btn-group">
            {this.isCreatePage && <Button onClick={this.onHandlePrevStep}>上一步</Button>}
            {this.isCreatePage && <Button type="primary" onClick={this.onHandleNextStep}>下一步</Button>}
            {this.isModifyPage &&
              <Button loading={this.state.btnLoading}  type="primary" onClick={this.onSave}>保存</Button>}
            {this.isModifyPage && <CancelActionModal routeHref={this.history} history={this.props.history} />}
            {!this.isCyclicalRoll && this.isCreatePage && <Button onClick={() => this.skip()}>跳过</Button>}
          </div>}
        </div>
      </>

    );
  }
}
