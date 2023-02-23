import React, { useState, useEffect } from "react";
import classNames from "classnames";
import { ConfigProviderProps } from "antd/es/config-provider";
import useAntdMediaQuery from "./use-media-antd-query";
import { Button, Input, Form, Row, Col, Select, ConfigProvider, DatePicker, TimePicker } from "knowdesign";
import { IconFont } from "@knowdesign/icons";

import { useContext } from "react";
import IntlContext from "./context";

const { RangePicker: DateRangePicker } = DatePicker;
const { RangePicker: TimeRangePicker } = TimePicker;

function useIntl(): any {
  const i18n = useContext(IntlContext);
  return i18n;
}

declare const ItemSizes: ["large", "default", "small", string];
export declare type ItemSize = typeof ItemSizes[number];

declare const ColumnTypes: ["select", "input", "datePicker", "dateRangePicker", "timePicker", "timeRangePicker", "custom", string];
export declare type ColumnType = typeof ColumnTypes[number];

declare const ModeTypes: ["full", "align", string];
export declare type ModeType = typeof ModeTypes[number];

declare const ColTypes: ["grid", "style", string];
export declare type ColType = typeof ColTypes[number];

const FormItem = Form.Item;
const { Option } = Select;

export interface IColumnsType {
  type: ColumnType;
  dataIndex: string;
  title: string | React.ReactNode;
  placeholder?: string | string[];
  valuePropName?: string;
  required?: boolean;
  colStyle?: React.CSSProperties;
  isInputPressEnterCallSearch?: boolean;
  size?: ItemSize;
  rules?: any[]; // 校验规则
  component?: React.ReactNode;
  componentProps?: any; // 需要传给组件的其他属性
  selectMode?: string; // 单选或者多选
  options?: {
    title: string;
    value: string | number;
  }[];
  formItemLayout?: any;
}
export interface FieldData {
  name?: string;
  level?: string;
  status?: string[];
  number?: Number;
}

export interface IQueryFormProps {
  layout?: "vertical" | "horizontal" | "inline";
  totalNumber?: number;
  onCollapse?: () => void | false;
  prefixCls?: string;
  className?: string;
  style?: React.CSSProperties | any;
  mode?: ModeType;
  colMode?: ColType;
  defaultColStyle?: React.CSSProperties;
  columnStyleHideNumber?: number;
  columns: IColumnsType[];
  searchText?: string | React.ReactNode;
  resetText?: string | React.ReactNode;
  showOptionBtns?: boolean;
  showCollapseButton?: boolean;
  onChange?: (data: any) => any;
  initialValues?: any;
  onSearch?: (data: any) => any;
  onReset?: (data: any) => any;
  getFormInstance?: (form: any) => any;
  isResetClearAll?: boolean;
  isTrimOnSearch?: boolean;
  antConfig?: ConfigProviderProps;
  defaultCollapse?: boolean;
  colConfig?:
    | {
        lg?: number;
        md?: number;
        xxl?: number;
        xl?: number;
        sm?: number;
        xs?: number;
      }
    | undefined;
}

const defaultColConfig = {
  xs: 6,
  sm: 6,
  md: 6,
  lg: 6,
  xl: 6,
  xxl: 6,
};

const defaultFormItemLayout = {
  labelCol: {
    xs: { span: 5 },
    sm: { span: 5 },
    md: { span: 7 },
    lg: { span: 7 },
    xl: { span: 8 },
    xxl: { span: 8 },
  },
  wrapperCol: {
    xs: { span: 19 },
    sm: { span: 19 },
    md: { span: 17 },
    lg: { span: 17 },
    xl: { span: 16 },
    xxl: { span: 16 },
  },
};

/**
 * 合并用户和默认的配置
 *
 * @param span
 * @param size
 */
const getSpanConfig = (span: number | typeof defaultColConfig, size: keyof typeof defaultColConfig): number => {
  if (typeof span === "number") {
    return span;
  }
  const config = {
    ...defaultColConfig,
    ...span,
  };
  return config[size];
};

/**
 * 获取最后一行的 offset，保证在最后一列
 *
 * @param length
 * @param span
 */
const getOffset = (length: number, span: number = 8) => {
  const cols = 24 / span;
  return (cols - (length % cols)) * span;
};

const getCollapseHideNum = (size: number) => {
  const maps = {
    6: 6,
    8: 6,
    12: 6,
    24: 6,
  } as { [key: number]: number };

  return maps[size] || 1;
};

const QueryForm = (props: IQueryFormProps) => {
  const prefixCls = `${props.prefixCls || "dantd"}-query-form`;
  const { t } = useIntl();
  const {
    layout = "vertical",
    onCollapse,
    className,
    style,
    colConfig,
    searchText = "查询",
    resetText = "清空",
    showOptionBtns = true,
    showCollapseButton = true,
    defaultCollapse = false,
    isResetClearAll = false,
    isTrimOnSearch = true,
    onChange,
    onSearch,
    onReset,
    getFormInstance,
    columns = [] as IColumnsType[],
    mode = "full",
    colMode = "grid",
    columnStyleHideNumber = 1,
    defaultColStyle = {
      width: "130px",
    },
    initialValues,
    totalNumber,
  } = props;
  const [form] = Form.useForm();
  const wrapperClassName = classNames(prefixCls, className);
  const formItemCls = classNames({
    [`${prefixCls}-formitem`]: true,
    [`${prefixCls}-formitem-full`]: mode === "full",
  });
  const windowSize = useAntdMediaQuery();
  const itemColConfig = { ...defaultColConfig, ...colConfig } || defaultColConfig;
  const [colSize, setColSize] = useState(getSpanConfig(itemColConfig || 8, windowSize));
  const { validateFields, getFieldsValue, resetFields, setFieldsValue } = form;

  const [collapsed, setCollapse] = useState(defaultCollapse);
  const [isShowCollapseButton, setIsShowCollapseButton] = useState(true);

  useEffect(() => {
    setColSize(getSpanConfig(itemColConfig || 8, windowSize));
    if (columns.length <= getCollapseHideNum(getSpanConfig(itemColConfig || 8, windowSize))) {
      setIsShowCollapseButton(false);
    } else {
      setIsShowCollapseButton(true);
    }
  }, [windowSize]);

  useEffect(() => {
    if (getFormInstance) {
      getFormInstance(form);
    }
  }, []);

  const collapseHideNum = getCollapseHideNum(getSpanConfig(itemColConfig || 8, windowSize));

  const handleSearch = () => {
    validateFields()
      .then((values) => {
        if (onSearch) {
          isTrimOnSearch ? handleTrimSearch(values) : onSearch(values);
        }
      })
      .catch(() => {
        //
      });
  };

  const handleTrimSearch = (values = {}) => {
    const data = {};
    Object.keys(values).forEach((key) => {
      if (typeof values[key] === "string") {
        data[key] = values[key].trim();
      } else {
        data[key] = values[key];
      }
    });
    onSearch(data);
  };

  const handleReset = () => {
    if (isResetClearAll) {
      const resetFieldsObj = columns.reduce((acc, cur: IColumnsType) => {
        return {
          ...acc,
          [cur.dataIndex]: undefined,
        };
      }, {});
      setFieldsValue(resetFieldsObj);
      (onChange as any)?.(initialValues);
    } else {
      resetFields();
      (onChange as any)?.({});
    }

    setTimeout(() => {
      if (onReset) {
        onReset(getFieldsValue());
      }
    });
  };

  const handlePressEnter = () => {
    handleSearch();
  };

  const renderInputItem = (colItem: any) => {
    const {
      dataIndex,
      title,
      required,
      componentProps = {},
      placeholder,
      isInputPressEnterCallSearch = true,
      formItemLayout,
      rules,
      size = "default",
    } = colItem;

    const itemPlaceholder = placeholder ? placeholder : t("form.placeholder.prefix");

    let itemRules: any[] = [];
    if (required) {
      itemRules = [
        {
          required: true,
          message: itemPlaceholder,
        },
      ];
    }

    const itemFormItemLayout = formItemLayout || mode === "align" ? defaultFormItemLayout : {};
    return (
      <FormItem
        shouldUpdate={true}
        key="input"
        name={dataIndex as string}
        rules={rules || itemRules}
        label={layout == "inline" ? "" : title}
        className={formItemCls}
        {...itemFormItemLayout}
      >
        <Input
          data-testid="field-input"
          size={size}
          placeholder={layout == "inline" ? title.split(":")[0] : itemPlaceholder}
          onPressEnter={isInputPressEnterCallSearch ? handlePressEnter : () => {}}
          {...componentProps}
        />
      </FormItem>
    );
  };

  const renderSelectItem = (colItem: any) => {
    const {
      dataIndex,
      title,
      required,
      placeholder,
      selectMode = "single",
      rules,
      formItemLayout,
      options = [],
      componentProps = {},
      size = "default",
      isSelectPressEnterCallSearch = true,
    } = colItem;
    const itemPlaceholder = placeholder ? (
      placeholder
    ) : (
      <>
        {t("form.selectplaceholder.prefix")}
        &nbsp;
        {title}
      </>
    );

    let itemRules: any[] = [];
    if (required) {
      itemRules = [
        {
          required: true,
          message: itemPlaceholder,
        },
      ];
    }

    const itemFormItemLayout = formItemLayout || mode === "align" ? defaultFormItemLayout : {};
    const handlePressEnter = (e) => {
      if (e.keyCode === 13) {
        handleSearch();
      }
    };
    return (
      <FormItem
        key="select"
        name={dataIndex as string}
        rules={rules || itemRules}
        label={layout == "inline" ? "" : title}
        className={formItemCls}
        {...itemFormItemLayout}
      >
        <Select
          suffixIcon={<IconFont style={{ pointerEvents: "none" }} type="icon-xia" />}
          data-testid="select"
          mode={selectMode}
          size={size}
          allowClear
          placeholder={layout == "inline" ? title.split(":")[0] : itemPlaceholder}
          showSearch={true}
          optionFilterProp="children"
          style={{ width: "100%" }}
          onInputKeyDown={isSelectPressEnterCallSearch ? handlePressEnter : () => {}}
          onSelect={handleSearch}
          onClear={handleSearch}
          filterOption={(val, option) => {
            return option.children.includes(val.trim());
          }}
          {...componentProps}
        >
          {options.map((option: any) => {
            return (
              <Option data-testid="select-option" value={option.value} key={option.value}>
                {option.title}
              </Option>
            );
          })}
        </Select>
      </FormItem>
    );
  };

  const renderDateItem = (colItem: any) => {
    const { dataIndex, title, required, componentProps = {}, placeholder, formItemLayout, rules, size = "default", type } = colItem;

    const itemPlaceholder = placeholder ? placeholder : t("form.placeholder.prefix");

    let itemRules: any[] = [];
    if (required) {
      itemRules = [
        {
          required: true,
          message: itemPlaceholder,
        },
      ];
    }

    const itemFormItemLayout = formItemLayout || mode === "align" ? defaultFormItemLayout : {};
    return (
      <FormItem
        shouldUpdate={true}
        key="date"
        name={dataIndex as string}
        rules={rules || itemRules}
        label={layout == "inline" ? "" : title}
        className={formItemCls}
        {...itemFormItemLayout}
      >
        {type === "dateRangePicker" ? (
          <DateRangePicker
            data-testid="dateRangePicker"
            size={size}
            allowClear
            showTime
            placeholder={layout == "inline" ? title.split(":")[0] : itemPlaceholder}
            // style={{ width: '100%' }}
            suffixIcon={<IconFont type="icon-riqi" style={{ color: "#74788D" }} />}
            {...componentProps}
          />
        ) : (
          <DatePicker
            data-testid="datePicker"
            size={size}
            allowClear
            placeholder={itemPlaceholder}
            style={{ width: "100%" }}
            {...componentProps}
          ></DatePicker>
        )}
      </FormItem>
    );
  };

  const renderTimeItem = (colItem: any) => {
    const { dataIndex, title, required, componentProps = {}, placeholder, formItemLayout, rules, size = "default", type } = colItem;

    const itemPlaceholder = placeholder ? placeholder : t("form.placeholder.prefix");

    let itemRules: any[] = [];
    if (required) {
      itemRules = [
        {
          required: true,
          message: itemPlaceholder,
        },
      ];
    }

    const itemFormItemLayout = formItemLayout || mode === "align" ? defaultFormItemLayout : {};
    return (
      <FormItem
        shouldUpdate={true}
        key="date"
        name={dataIndex as string}
        rules={rules || itemRules}
        label={layout == "inline" ? "" : title}
        className={formItemCls}
        {...itemFormItemLayout}
      >
        {type === "timeRangePicker" ? (
          <TimeRangePicker
            data-testid="timerRangePicker"
            size={size}
            allowClear
            showTime
            placeholder={layout == "inline" ? title.split(":")[0] : itemPlaceholder}
            style={{ width: "100%" }}
            {...componentProps}
          />
        ) : (
          <TimePicker
            data-testid="timePicker"
            size={size}
            allowClear
            placeholder={layout == "inline" ? title.split(":")[0] : itemPlaceholder}
            style={{ width: "100%" }}
            {...componentProps}
          ></TimePicker>
        )}
      </FormItem>
    );
  };

  const renderCustomItem = (colItem: any) => {
    const { formItemLayout, dataIndex, title, required, placeholder, rules, valuePropName = "value", component } = colItem;

    const itemPlaceholder = placeholder ? (
      placeholder
    ) : (
      <>
        {t("form.placeholder.prefix")}
        &nbsp;
        {title}
      </>
    );

    let itemRules: any[] = [];
    if (required) {
      itemRules = [
        {
          required: true,
          message: itemPlaceholder,
        },
      ];
    }

    const itemFormItemLayout = formItemLayout || mode === "align" ? defaultFormItemLayout : {};

    return (
      <FormItem
        key="custom"
        name={dataIndex as string}
        rules={rules || itemRules}
        label={layout == "inline" ? "" : title}
        valuePropName={valuePropName}
        className={formItemCls}
        {...itemFormItemLayout}
      >
        {component}
      </FormItem>
    );
  };

  const renderOptionBtns = () => {
    const offsetVal = collapsed
      ? columns.length <= collapseHideNum
        ? getOffset(columns.length, colSize)
        : getOffset(collapseHideNum, colSize)
      : getOffset(columns.length, colSize);
    let optionStyle = {};
    let isEmptyStyle = columns.length < 4 ? {} : { justifyContent: "flex-end" };

    if (colMode === "style") {
      optionStyle = defaultColStyle;
    }
    if (layout == "inline") {
      return null;
    }
    return (
      <Col
        // {...colConfig}
        offset={offsetVal}
        span={offsetVal}
        key="option"
        className={`${prefixCls}-option`}
        style={{
          display: "flex",
          alignItems: "flex-end",
          marginLeft: 0,
          flex: 1,
          ...isEmptyStyle,
          ...optionStyle,
        }}
      >
        <Form.Item key="option">
          <span>
            {columns.length >= 4 && <a onClick={handleReset}>{resetText || t("queryform.reset")}</a>}
            <Button ghost onClick={handleSearch} style={{ marginLeft: 10, width: 80 }} type="primary" htmlType="submit">
              {searchText || t("queryform.search")}
            </Button>
            {isShowCollapseButton && showCollapseButton && (
              <a
                style={{
                  marginLeft: 10,
                  display: "inline-block",
                }}
                onClick={() => {
                  onCollapse ? onCollapse() : setCollapse(!collapsed);
                }}
              >
                {collapsed ? "展开" : "收起"}
                {/* TODO  接入组件库后替换  */}
                <IconFont
                  type="icon-a-xialaIcon"
                  style={{
                    marginLeft: 8,
                    transform: collapsed ? "rotate(180deg)" : "none",
                    transition: "transform 0.3s",
                  }}
                />
              </a>
            )}
          </span>
        </Form.Item>
      </Col>
    );
  };

  return (
    <ConfigProvider {...props.antConfig}>
      <div className={wrapperClassName} style={style}>
        <Row gutter={10} justify="start">
          {columns.map((colItem, colIndex) => {
            // 操作记录有7个筛选
            let itemHide = collapsed && collapseHideNum < colIndex;
            let colItemStyle = {};
            let colConfig = colMode !== "style" ? itemColConfig : {};
            if (colMode === "style") {
              colItemStyle = colItem.colStyle || defaultColStyle;
              // if (collapsed && colIndex >= columnStyleHideNumber) {
              //   itemHide = true;
              // }
            }
            colItemStyle = {
              ...colItemStyle,
              display: itemHide ? "none" : "block",
            };
            return (
              <Col style={colItemStyle} key={`query-form-col-${colItem.dataIndex}-${colIndex}`} {...colConfig}>
                <Form
                  form={form}
                  onFieldsChange={(_changedFields, allFields) => {
                    (onChange as any)?.(allFields);
                  }}
                  initialValues={initialValues}
                  layout={layout}
                >
                  {colItem.type === "input" && renderInputItem(colItem)}
                  {colItem.type === "select" && renderSelectItem(colItem)}
                  {colItem.type === "datePicker" && renderDateItem(colItem)}
                  {colItem.type === "dateRangePicker" && renderDateItem(colItem)}
                  {colItem.type === "timePicker" && renderTimeItem(colItem)}
                  {colItem.type === "timeRangePicker" && renderTimeItem(colItem)}
                  {colItem.type === "custom" && renderCustomItem(colItem)}
                </Form>
              </Col>
            );
          })}
          {showOptionBtns && renderOptionBtns()}
        </Row>
        {layout == "inline" && totalNumber ? <div className={`${prefixCls}-result`}>共&nbsp;{totalNumber}&nbsp;条结果</div> : null}
      </div>
    </ConfigProvider>
  );
};

export default QueryForm;
