import { regNonnegativeInteger, regNonnegativeNumber, regTwoNumber } from "constants/reg";

export const pagination = {
  position: "bottomRight",
  showQuickJumper: true,
  showSizeChanger: true,
  pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
  showTotal: (total) => `共 ${total} 条`,
  // hideOnSinglePage: true,
};

export const customPagination = {
  position: "bottomRight",
  showQuickJumper: true,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
};

export const cellStyle = {
  overflow: "hidden",
  whiteSpace: "nowrap",
  textOverflow: "ellipsis",
  cursor: "pointer",
  maxWidth: 150,
};

export const textAreaRuleProps = {
  validator: (rule: any, value: string) => {
    return value?.trim().length >= 5;
  },
};

export const staffRuleProps = {
  validator: (rule: any, value: string[]) => {
    if (value && typeof value === "string") {
      value = (value as string)?.split(",");
    }
    if (!value?.length) {
      return Promise.reject("请选择责任人");
    }
    if (value?.length < 1) {
      return Promise.reject("请输入至少一位负责人");
    }
    if (value?.length > 5) {
      return Promise.reject("最多五位负责人");
    }

    return Promise.resolve();
  },
};

export const nodeRuleProps = {
  validator: (rule: any, value: any) => {
    // if (!new RegExp(regNonnegativeInteger).test(value) || value <= 1) { return false; }
    return true;
  },
};

export const numberRuleProps = {
  validator: (rule: any, value: any) => {
    // if (!new RegExp(regNonnegativeInteger).test(value) || value <= 0) { return false; }
    return true;
  },
};

export const quotaRuleProps = {
  validator: (rule: any, value: any) => {
    if (!new RegExp(regNonnegativeNumber).test(value) || !new RegExp(regTwoNumber).test(value)) {
      return Promise.reject();
    }
    return Promise.resolve();
  },
};

export const searchProps = {
  showSearch: true,
  optionFilterProp: "children",
  filterOption: (input: any, option: any) => {
    if (typeof option.props.children === "object") {
      const { props } = option.props.children as any;
      return (props.children + "").toLowerCase().indexOf(input.toLowerCase()) >= 0;
    }
    return (option.props.children + "").toLowerCase().indexOf(input.toLowerCase()) >= 0;
  },
};

export const initPaginationProps = () => {
  return {
    position: "bottomRight",
    showQuickJumper: true,
    showSizeChanger: true,
    pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
    showTotal: (total) => `共 ${total} 条`,
    total: 0,
    current: 1,
    pageSize: 10,
  };
};
