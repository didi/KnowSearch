import * as React from "react";
import {
  getStepTowFormItems,
  getFormItemKeyLevel,
  judgeHasTwoOrMoreItems,
} from "./config";
import { renderFormItem, handleFormItem, IFormItem, FormItemType } from "component/x-form";
import cloneDeep from "lodash/cloneDeep";
import { TEMP_FORM_MAP_KEY, cascaderTypes } from "./constant";
import Url from "lib/url-parser";
import { getIndexBaseInfo, getIndexMappingInfo } from "api/cluster-index-api";
import { Form, Button, Row, Col, Tree, Popconfirm, Empty, Spin } from 'antd';
import { MinusOutlined, PlusOutlined } from "@ant-design/icons";
import { IStringMap, IUNSpecificInfo } from "typesPath/base-types";
import * as actions from "actions";
import { connect } from "react-redux";
import "./index.less";


interface IFormProps {
  form?: any;
  wrappedComponentRef?: any;
  kIndex?: string;
  addCustomForm?: (level: number, l?: any) => void;
  formData?: any;
  createIndex?: any;
  dispatch?: any;
}

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});
const connects: Function = connect

@connects(mapStateToProps)
export class TableMappingForm extends React.Component<IFormProps> {
  private id: number =
    this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.tableMappingKeys
    )?.length || 1;
  private isModifyPage: boolean = false;
  private isDetailPage: boolean = false;
  private isCreatePage: boolean = false;
  private modifyFormData: IUNSpecificInfo = null;
  private indexId: number = null;

  constructor(props: IFormProps) {
    super(props);
    const url = Url();
    this.indexId = Number(url.search.id);
    this.isModifyPage = window.location.pathname.includes("modify/mapping");
    this.isDetailPage = window.location.pathname.includes("detail");
    this.isCreatePage = window.location.pathname.includes("create");

    if (
      this.isCreatePage &&
      !this.props.createIndex.secondChildMap.get("0_level-0-0")
    ) {
      this.props.dispatch(
        actions.setSecondChildMap("0_level-0-0", {
          index: 0,
          children: [],
        })
      );
    }
  }

  public state = {
    loading: false
  }

  public componentDidMount() {
    const form = this.props.wrappedComponentRef.current;

    if (this.isCreatePage) {
      return this.handleCreatePageInfo();
    }

    if (this.isModifyPage || this.isDetailPage) {
      getIndexBaseInfo(this.indexId).then((info) => {
        this.props.dispatch(
          actions.setTemporaryFormMap(
            TEMP_FORM_MAP_KEY.isCyclicalRoll,
            !!info?.cyclicalRoll
          )
        );
        this.setState({
          loading: true
        });
        getIndexMappingInfo(this.indexId).then((data) => {
          // TODO::
          data = data?.typeProperties?.[0];
          const properties = data?.properties;
          const renderKeys: string[] = [];
          this.transMappingInfoToForm(renderKeys, properties, 0, 0, 0);
          this.id = Object.keys(properties).length;

          this.handleMappingInfo(data);

          if (renderKeys.length) {
            form.setFieldsValue({
              keys: renderKeys,
            });
          };
          this.setState({
            loading: false
          });
        });
      });
    }
  }

  public handleCreatePageInfo = () => {
    const form = this.props.wrappedComponentRef.current;
    const tempKeys = this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.tableMappingKeys
    );
    const values = this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.tableMappingValues
    );
    // 异步的获取表单内的值储存在tempTableMappingValues中 解决data错误问题
    setTimeout(async () => {
      const allValue = await form.getFieldsValue(true);
      this.props.dispatch(
        actions.setTemporaryFormMap(
          TEMP_FORM_MAP_KEY.tempTableMappingValues,
          allValue
        )
      );
    }, 300);

    if (tempKeys && values) {
      form.setFieldsValue({
        keys: tempKeys,
      });
      Object.keys(values).map((key) => {
        if (key.includes("type")) {
          let fieldTypeMap = this.props.createIndex.fieldTypeMap;
          if (key === "clear-map") {
            fieldTypeMap = {};
          } else {
            fieldTypeMap[key] = values[key]?.[0];
          }
          this.props.dispatch(actions.setFieldTypeMap(fieldTypeMap)); // TODO: 需不需要cascader二级项数值
          // indexStore.setFieldTypeMap(key, values[key]?.[0]); // TODO: 需不需要cascader二级项数值
        }
      });
    }
  };

  /**
   * 处理主键、routeing字段
   * @param data
   */

  public handleMappingInfo = (data: IUNSpecificInfo) => {
    const properties = data?.properties;
    const { dateField, dateFieldFormat, idField, routingField } = data;
    this.props.dispatch(
      actions.setTemporaryFormMap(
        TEMP_FORM_MAP_KEY.jsonMappingValue,
        JSON.stringify(properties, null, 4)
      )
    );
    this.props.dispatch(
      actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.jsonMappingFormData, {
        primaryKey: idField,
        partition: dateField,
        routing: routingField,
        timeFormat: dateFieldFormat,
      })
    );

    Object.keys(this.modifyFormData || {}).map((key) => {
      if (
        key.includes("name") &&
        idField?.split(",")?.indexOf(this.modifyFormData[key]) > -1
      ) {
        this.modifyFormData[key.replace("name", "primaryKey")] = true;
      }
      if (
        key.includes("name") &&
        routingField?.split(",")?.indexOf(this.modifyFormData[key]) > -1
      ) {
        this.modifyFormData[key.replace("name", "routing")] = true;
      }
      if (
        key.includes("name") &&
        dateField?.includes(this.modifyFormData[key])
      ) {
        const dateFieldTmps = dateField.split(".");
        const { firstLevel } = getFormItemKeyLevel(key.replace("name_", ""));

        if (firstLevel === dateFieldTmps.length - 1) {
          this.modifyFormData[key.replace("name", "partition")] = true;
          this.modifyFormData[key.replace("name", "type")] = [
            "date",
            dateFieldFormat,
          ];
        }
      }
      if (key.includes("type")) {
        let fieldTypeMap = this.props.createIndex.fieldTypeMap;
        if (key === "clear-map") {
          fieldTypeMap = {};
        } else {
          fieldTypeMap[key] = this.modifyFormData[key]?.[0];
        }
        this.props.dispatch(actions.setFieldTypeMap(fieldTypeMap)); // TODO: 需不需要cascader二级项数值
        // indexStore.setFieldTypeMap(key, this.modifyFormData[key]?.[0]); // TODO: 需不需要cascader二级项数值
      }
    });
    this.props.dispatch(
      actions.setTemporaryFormMap(
        TEMP_FORM_MAP_KEY.tempTableMappingValues,
        this.modifyFormData
      )
    );
  };

  /**
   * 将数据填充表单
   * @param renderKeys -form keys
   * @param data -json数据
   * @param firstLevel 0_level-0-0 表示firstLevel为0，secondLevel为0
   * @param secondLevel
   * @param currentId 表示当前一级表单的个数
   * 第一级 0_level-0-0 1_level-0-0 2_level-0-0
   * 第二级 0_level-1-0 0_level-2-0 0_level-3-0
   * 第三级 0_level-1-1 0_level-1-2 0_level-2-1 0_level-2-2
   */

  public transMappingInfoToForm = (
    renderKeys: string[],
    data: IUNSpecificInfo,
    firstLevel: number,
    secondLevel: number,
    currentId: number
  ) => {
    const keys = Object.keys(data || {});

    for (let i = 0; i < keys.length; i++) {
      currentId = firstLevel === 0 && secondLevel === 0 ? i : currentId;
      const key =
        firstLevel === 0 && secondLevel === 0
          ? `${i}_level-0-0`
          : secondLevel === 0
            ? `${currentId}_level-${firstLevel + i}-0`
            : `${currentId}_level-${firstLevel}-${secondLevel + i}`;
      renderKeys.push(key);
      if (firstLevel !== 0 && secondLevel === 0) {
        const secondKey = `${currentId}_level-0-0`;
        const { children: secondChildren = [] } =
          this.props.createIndex.secondChildMap.get(secondKey) || {};

        secondChildren.push(key);
        const index = renderKeys.findIndex((row) => row === secondKey);
        this.props.dispatch(
          actions.setSecondChildMap(secondKey, {
            index,
            children: secondChildren,
          })
        );
      }

      if (firstLevel !== 0 && secondLevel !== 0) {
        const thirdKey = `${currentId}_level-${firstLevel}-0`;

        const { children: thirdChildren = [] } =
          this.props.createIndex.thirdChildMap.get(thirdKey) || {};
        thirdChildren.push(key);
        const index = renderKeys.findIndex((row) => row === thirdKey);
        this.props.dispatch(
          actions.setThirdChildMap(thirdKey, {
            index,
            children: thirdChildren,
          })
        );
      }
      const type = data[keys[i]]?.type;
      this.modifyFormData = this.modifyFormData || {};
      this.modifyFormData[`name_${key}`] = keys[i];
      this.modifyFormData[`type_${key}`] = type
        ? [type]
        : data[keys[i]]?.properties
          ? ["object"]
          : ["numeric", "integer"];

      if (cascaderTypes.numeric.includes(type)) {
        this.modifyFormData[`type_${key}`] = ["numeric", type];
      }

      if (cascaderTypes.range.includes(type)) {
        this.modifyFormData[`type_${key}`] = ["range", type];
      }

      this.handleMappingJson(data[keys[i]], this.modifyFormData, key);

      if (data[keys[i]]?.properties) {
        const nextFLevel =
          firstLevel === 0 && secondLevel === 0 ? firstLevel + 1 : firstLevel;
        const nextSLevel = firstLevel === 0 ? 0 : secondLevel + 1;
        this.transMappingInfoToForm(
          renderKeys,
          data[keys[i]]?.properties,
          nextFLevel,
          nextSLevel,
          currentId
        );
      }
    }
  };

  /**
   * 处理表单其他项
   * @param currentObj
   * @param formData
   * @param currentKey
   */
  public handleMappingJson = (
    currentObj: IUNSpecificInfo,
    formData: IUNSpecificInfo,
    currentKey: string
  ) => {
    const type = currentObj.type;

    const searchSortDefaultValue = (value) => {
      return value === undefined || value === true ? '1' : '0';
    }

    switch (type) {
      case "object":
        formData[`search_${currentKey}`] = searchSortDefaultValue(currentObj.enabled); // 检索
        formData[`dynamic_${currentKey}`] = searchSortDefaultValue(currentObj.dynamic);
        formData[`sort_${currentKey}`] = searchSortDefaultValue(currentObj.doc_values);
        break;
      case "nested":
        formData[`search_${currentKey}`] = searchSortDefaultValue(currentObj.index); // 检索
        formData[`sort_${currentKey}`] = searchSortDefaultValue(currentObj.doc_values);
        break;
      case "text":
        // 无排序
        formData[`analyzer_${currentKey}`] = currentObj.analyzer || "none";
        formData[`search_${currentKey}`] = searchSortDefaultValue(currentObj.index); // 检索
        formData[`sort_${currentKey}`] = '-1'; // 检索
        break;
      case "date":
        formData[`sort_${currentKey}`] = searchSortDefaultValue(currentObj.doc_values);
        formData[`search_${currentKey}`] = searchSortDefaultValue(currentObj.index);
        formData[`type_${currentKey}`] = ['date', currentObj.format]; // 行内兼容
        break;
      default:
        formData[`sort_${currentKey}`] = searchSortDefaultValue(currentObj.doc_values);
        formData[`search_${currentKey}`] = searchSortDefaultValue(currentObj.index);
        break;
    }
  };

  public add = (
    firstLevel: number = 0,
    secondLevel: number = 0,
    id?: number
  ) => {
    const form = this.props.wrappedComponentRef.current;
    const keys = form.getFieldValue("keys");

    const nextKeys = [].concat(keys);

    // if (this.isModifyPage) {
    //   indexStore.setSaveBtnStatus(true);
    // }

    if (firstLevel === -1) {
      // 新增第一级
      const key = `${this.id++}_level-0-0`;

      this.props.dispatch(
        actions.setSecondChildMap(key, {
          index: keys.length,
          children: [],
        })
      );

      nextKeys.unshift(key);
      return form.setFieldsValue({
        keys: nextKeys,
      });
    }

    if (firstLevel === 0 && secondLevel === 0) {
      // 点击第一级子项
      const key = `${id}_level-${firstLevel}-${secondLevel}`;
      const { children: secondChildren = [], index: firstLevelIndex = 0 } =
        this.props.createIndex.secondChildMap.get(key) || {};
      const nextKey = `${id}_level-${(secondChildren?.length || 0) + 1}-0`;

      const thirdChildrenData = this.props.createIndex.thirdChildMap.get(
        secondChildren?.[secondChildren.length - 1] || ""
      );
      const { children: thirdChildren = [], index: secondLevelIndex = 0 } =
        thirdChildrenData || {};

      const spliceIndex = thirdChildrenData
        ? secondLevelIndex + (thirdChildren?.length || 0) + 1
        : firstLevelIndex + 1;
      this.props.dispatch(
        actions.setSecondChildMap(key, {
          index: firstLevelIndex,
          children: [].concat(secondChildren, nextKey),
        })
      );
      this.props.dispatch(
        actions.setThirdChildMap(nextKey, {
          index: spliceIndex,
          children: [],
        })
      );
      nextKeys.splice(spliceIndex, 0, nextKey);
      return form.setFieldsValue({
        keys: nextKeys,
      });
    }

    if (secondLevel === 0) {
      // 点击第二级子项
      const key = `${id}_level-${firstLevel}-${secondLevel}`;
      const { children = [], index = 0 } =
        this.props.createIndex.thirdChildMap.get(key);
      const nextKey = `${id}_level-${firstLevel}-${(children?.length || 0) + 1
        }`;
      const spliceIndex = index || 0 + (children?.length || 0) + 1;
      this.props.dispatch(
        actions.setThirdChildMap(key, {
          index,
          children: [].concat(children, nextKey),
        })
      );
      nextKeys.splice(spliceIndex, 0, nextKey);

      return form.setFieldsValue({
        keys: nextKeys,
      });
    }
  };

  public getFormList = () => {
    if (!this.props.wrappedComponentRef.current) return [];
    const { setFieldsValue, getFieldValue } =
      this.props.wrappedComponentRef.current;

    if (this.isCreatePage && !getFieldValue("keys")?.length) {
      setFieldsValue({ keys: ["0_level-0-0"] });
    } else if (!getFieldValue("keys")?.length) {
      setFieldsValue({ keys: [] });
    }

    const keys = getFieldValue("keys");
    const formItems = [] as React.ReactNode[];

    if (!keys?.length) {
      return formItems;
    }
    for (let i = 0; i < keys.length; i++) {
      const { firstLevel } = getFormItemKeyLevel(keys[i]);

      if (firstLevel === 0) {
        const { children = [] } =
          this.props.createIndex.secondChildMap.get(keys[i]) || {};
        if (!children.length) {
          formItems.push(
            <CustomMappingForm
              key={keys[i]}
              form={this.props.wrappedComponentRef.current}
              formData={this.modifyFormData}
              kIndex={keys[i]}
              addCustomForm={this.add}
            />
          );
        } else {
          const collapseElemet = this.renderTree(i);
          formItems.push(collapseElemet);
        }
      }
    }

    return formItems;
  };

  public renderTree = (start: number) => {
    const { getFieldValue } = this.props.wrappedComponentRef.current;
    const { wrappedComponentRef } = this.props;
    const keys = getFieldValue("keys");
    let { children: secondChildren = [] } =
      this.props.createIndex.secondChildMap.get(keys[start]);
    secondChildren = [...new Set(secondChildren)];
    console.log(secondChildren)
    const item = (
      <CustomMappingForm
        form={wrappedComponentRef.current}
        kIndex={keys[start]}
        formData={this.modifyFormData}
        addCustomForm={this.add}
      />
    );

    const getThirdTreeNode = (thirdChildren: string[]) => {
      return thirdChildren.map((row: string, index) => (
        <Tree.TreeNode
          key={row}
          className="third-level"
          title={
            <CustomMappingForm
              form={wrappedComponentRef.current}
              kIndex={row}
              formData={this.modifyFormData}
              addCustomForm={this.add}
            />
          }
        />
      ));
    };

    return (
      <div key={start} className="custom-tree-node">
        <Tree defaultExpandAll={true} showLine={true} motion={null}>
          <Tree.TreeNode className="first-level" title={item} key={keys[start]}>
            {secondChildren.map((row: string, index: number) => {
              const { children } =
                this.props.createIndex.thirdChildMap.get(row) || {};
              return (
                <Tree.TreeNode
                  className="second-level"
                  title={
                    <CustomMappingForm
                      form={wrappedComponentRef.current}
                      kIndex={row}
                      formData={this.modifyFormData}
                      addCustomForm={this.add}
                    />
                  }
                  key={row}
                >
                  {children?.length ? getThirdTreeNode(children) : null}
                </Tree.TreeNode>
              );
            })}
          </Tree.TreeNode>
        </Tree>
      </div>
    );
  };

  public onHandleValuesChange = (
    values: IStringMap,
    allValues: IUNSpecificInfo
  ) => {
    Object.keys(values).map((key) => {
      if (key.includes("type")) {
        let fieldTypeMap = this.props.createIndex.fieldTypeMap;
        if (key === "clear-map") {
          fieldTypeMap = {};
        } else {
          fieldTypeMap[key] = values[key]?.[0];
        }
        this.props.dispatch(actions.setFieldTypeMap(fieldTypeMap));
        // indexStore.setFieldTypeMap(key, values[key]?.[0]); // TODO: 需不需要cascader二级项数值
      }
      if (key.includes("partition") && values[key]) {
        Object.keys(allValues).map((row) => {
          if (row !== key && row.includes("partition") && allValues[row]) {
            // 保证分区字段选型的唯一性
            allValues[row] = false;
            const typeKey = row.replace("partition", "type");
            allValues[typeKey] = [""]; //
            this.props.wrappedComponentRef.current.setFieldsValue({
              ...allValues,
            });
          }
        });
      }
    });
    this.props.dispatch(
      actions.setTemporaryFormMap(
        TEMP_FORM_MAP_KEY.tempTableMappingValues,
        allValues
      )
    );
  };

  public render() {
    const formItems = this.getFormList();
    const stepTowFormItems = getStepTowFormItems({});

    // if (indexStore.loadingMap['mapping-loading']) {
    //   return <LoadingBlock height={400} loading={indexStore.loadingMap['mapping-loading']} />;
    // }
    const { wrappedComponentRef } = this.props;
    return (
      <>
        <Spin spinning={this.state.loading}>
          <Form
            ref={wrappedComponentRef}
            onValuesChange={this.onHandleValuesChange}
            name="control-hooks"
            className={
              this.isDetailPage
                ? "table-mapping-form detail-page"
                : "table-mapping-form"
            }
          >
            <Row className="first-row">
              {stepTowFormItems.map((formItem, index) => {
                return (
                  <Col
                    span={formItem.colSpan}
                    key={index}
                    className={`form-col ${formItem.customClassName || ""}`}
                  >
                    <Form.Item key={`label-${formItem.key}_${index}`}>
                      {formItem.label}
                    </Form.Item>
                  </Col>
                );
              })}
              {!this.isDetailPage && (
                <Col span={2}>
                  <Form.Item>
                    {''}
                  </Form.Item>
                </Col>
              )}
            </Row>
            {formItems.length ? (
              formItems
            ) : (
              <>
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
              </>
            )}
          </Form>
        </Spin>
      </>
    );
  }
}

@connects(mapStateToProps)
export class CustomMappingForm extends React.Component<IFormProps> {
  public remove = (k: string, form: any) => {
    let keys = form.getFieldValue("keys");

    if (keys.length === 1) {
      return;
    }
    const { firstLevel, secondLevel, id } = getFormItemKeyLevel(k);
    if (firstLevel === 0 && secondLevel === 0) {
      const { children = [], index } =
        this.props.createIndex.secondChildMap.get(k) || {};
      for (const item of children) {
        keys = keys.filter((key: string) => key !== item);
        const { children: thirdChildren = [] } =
          this.props.createIndex.thirdChildMap.get(item);
        for (const row of thirdChildren) {
          keys = keys.filter((key: string) => key !== row);
        }
        this.props.dispatch(actions.setThirdChildMap(item));
      }
      this.props.dispatch(
        actions.setSecondChildMap(k, {
          index,
          children: [],
        })
      );
    } else if (secondLevel === 0) {
      const { children: thirdChildren = [] } =
        this.props.createIndex.thirdChildMap.get(k) || {};
      const parentKey = `${id}_level-0-0`;
      const { children = [], index } =
        this.props.createIndex.secondChildMap.get(parentKey);

      for (const row of thirdChildren) {
        keys = keys.filter((key: string) => key !== row);
      }
      this.props.dispatch(
        actions.setSecondChildMap(parentKey, {
          // 一级子项中去除该项
          index,
          children: children.filter((key: string) => key !== k),
        })
      );
      this.props.dispatch(actions.setThirdChildMap(k)); // 二级子项中去除该项
    } else {
      const parentKey = `${id}_level-${firstLevel}-0`;
      const { children = [], index } =
        this.props.createIndex.thirdChildMap.get(parentKey);

      this.props.dispatch(
        actions.setThirdChildMap(parentKey, {
          // 二级子项中去除该项
          index,
          children: children.filter((key: string) => key !== k),
        })
      );
    }

    form.setFieldsValue({
      keys: keys.filter((key: string) => key !== k),
    });
  };

  public getRenderItem = (
    formItem: IFormItem,
    kIndex: string,
    currLevel: number,
    type: string,
    formData: any,
    isDetailPage: boolean
  ) => {
    const formItemAlias = cloneDeep(formItem);
    formItemAlias.key = `${formItem.key}_${kIndex}`;
    const { initialValue = undefined, valuePropName } = handleFormItem(
      formItemAlias,
      formData
    );
    const noItem = (
      <>
        <Form.Item key={`${formItem.key}_${kIndex}`}> - </Form.Item>
      </>
    );
    let item = (
      <>
        <Form.Item
          key={`${formItem.key}_${kIndex}`}
          className={formItem.extraElement ? "extra-element" : ""}
        >
          <Form.Item
            name={`${formItem.key}_${kIndex}`}
            rules={formItem.rules || [{ required: false, message: "" }]}
            initialValue={typeof initialValue === 'number' ? `${initialValue}` : initialValue}
            valuePropName={valuePropName}
            {...formItem.formAttrs}
          >
            {renderFormItem(formItem)}
          </Form.Item>
          {formItem.extraElement ? formItem.extraElement : null}
        </Form.Item>
      </>
    );

    if (type && type !== "text" && formItem.key === "analyzer") {
      // text类型没有排序，该字段为空“-”
      item = noItem;
    }

    if (type === "text" && formItem.key === "sort") {
      // text类型没有排序，该字段为空“-”
      item = noItem;
    }

    // 仅支持text、keyword、numeric, 不支持object\nested及其子项
    if (
      (formItem.key === "routing" || formItem.key === "primaryKey") &&
      type &&
      (type === "object" ||
        type === "nested" ||
        type === "date" ||
        currLevel > 0)
    ) {
      // TODO:date类型是否支持？？
      item = noItem;
    }

    if (formItem.key === "partition" && type && type !== "date") {
      item = noItem;
    }

    if (formItem.key === "dynamic" && type && type !== "object") {
      item = noItem;
    }
    if (isDetailPage) {
      // if ((formItem.key === "routing" || formItem.key === "primaryKey" || formItem.key === "partition")) {
      //   item = !initialValue ? noItem : (
      //     <>
      //       <Form.Item key={`${formItem.key}_${kIndex}`}> ✔️ </Form.Item>
      //     </>
      //   );
      // }
      if (!formItem?.type) {
        item = <Form.Item key={`${formItem.key}_${kIndex}`}>{initialValue ? initialValue : '-'}</Form.Item>
      } else {
        switch (formItem.type) {
          case FormItemType.cascader:
            item = <Form.Item key={`${formItem.key}_${kIndex}`}>{initialValue.join('/')}</Form.Item>
            break;
          case FormItemType.select:
            item = <Form.Item key={`${formItem.key}_${kIndex}`}>{formItem.options?.filter(item => item.value === initialValue)[0]?.label || '-'}</Form.Item>
            break;
          default:
            break;
        }
      }
    }

    return item;
  };

  public render() {
    const { getFieldValue } = this.props.form;
    const isDetailPage = window.location.pathname.includes("/detail");
    const isModifyPage = window.location.pathname.includes("modify/mapping");

    const keys = getFieldValue("keys");
    const formDataFromStore = this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.tableMappingValues
    ); // 上一步下一步时候保存的值
    const { form, kIndex, addCustomForm } = this.props; // kIndex表示当前表单项的key值
    const formData = this.props.formData || formDataFromStore || {};
    const { firstLevel } = getFormItemKeyLevel(kIndex);

    let isShowLessBtn = keys.length > 1;
    const hasTwoOrMoreItem = judgeHasTwoOrMoreItems(keys);

    if (!hasTwoOrMoreItem && firstLevel === 0) {
      // 只有一项但包含子项时第一条不可删除
      isShowLessBtn = false;
    }
    const type = this.props.createIndex.fieldTypeMap[`type_${kIndex}`];
    const isCyclicalRoll = !!this.props.createIndex.temporaryFormMap.get(
      TEMP_FORM_MAP_KEY.isCyclicalRoll
    );
    const stepTowFormItems = getStepTowFormItems({
      kIndex,
      cb: addCustomForm,
      isCyclicalRoll,
      isDetailPage,
      isModifyPage,
    });



    return (
      <>
        <Row className="table-mapping-setting-row">
          {stepTowFormItems.map((formItem, index) => {
            return (
              <Col
                span={formItem.colSpan}
                key={index}
                className={`form-col ${formItem.customClassName || ""}`}
              >
                {!formItem.invisible ? (
                  this.getRenderItem(
                    formItem,
                    kIndex,
                    firstLevel,
                    type,
                    formData,
                    isDetailPage
                  )
                ) : (
                  <Form.Item key={`${formItem.key}_${kIndex}`}>-</Form.Item>
                )}
              </Col>
            );
          })}
          {!isDetailPage && (
            <Col span={2}>
              <Form.Item>
                {isShowLessBtn ? (
                  <Popconfirm
                    title="删除操作无法恢复，请谨慎操作"
                    onConfirm={() => this.remove(kIndex, form)}
                    okText="删除"
                    cancelText="取消"
                  >
                    <Button style={{ width: 30, height: 30, marginRight: 8 }} icon={<MinusOutlined />} />
                  </Popconfirm>
                ) : null}
                <Button style={{ width: 30, height: 30 }} onClick={() => addCustomForm(-1, -1)} icon={<PlusOutlined />} />
              </Form.Item>
            </Col>
          )}
        </Row>
      </>
    );
  }
}
