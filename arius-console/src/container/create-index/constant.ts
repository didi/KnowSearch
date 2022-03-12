export const CYCLICAL_ROLL_TYPE_LIST = [{
  label: '是',
  value: 'more',
}, {
  label: '否',
  value: 'one',
}];

export const timeFormatList = [
  'yyyy-MM-dd HH:mm:ss',
  'yyyy-MM-dd HH:mm:ss.SSS',
  'yyyy-MM-dd\'T\'HH:mm:ss',
  'yyyy-MM-dd\'T\'HH:mm:ss.SSS',
  'yyyy-MM-dd HH:mm:ss.SSS Z',
  'yyyy/MM/dd HH:mm:ss',
  'epoch_seconds',
  'epoch_millis',
];

// 二级类型
export const cascaderTypes = {
  date: timeFormatList,
  numeric: ['byte', 'double', 'float', 'half_float', 'integer', 'long', 'scaled_float', 'short'],
  range: ['date_range', 'double_range', 'float_range', 'integer_range', 'IP_range', 'long_range'],
} as {
  [key: string]: string[],
};

export const DATE_FORMAT_BY_ES = 'yyyy-MM-dd HH:mm:ss Z||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||epoch_millis';

// table mapping 类型列表
export const fieldTypes = [
  'binary',
  'text',
  'keyword',
  'boolean',
  'geo_point',
  'geo_shape',
  'ip',
  'flattened',
  'shape',
  'date',
  'numeric',
  'range',
  'object',
  'nested',
];

export const MAX_CHILD_NUM = 3;

export const complexType = [
  'object',
  'nested',
];

export const yesOrNoOptions = [{
  label: '是',
  value: '1',
}, {
  label: '否',
  value: '0',
}];

export const participleList = [{
  label: '标准分词器',
  value: 'standard',
}, {
  label: 'ik_smart',
  value: 'ik_smart',
}, {
  label: '不配置',
  value: 'none',
}];

export const TEMP_FORM_MAP_KEY = {
  firstStepFormData: 'first-step-form-data',
  isCyclicalRoll: 'isCyclicalRoll',
  tableMappingValues: 'table-mapping-values',
  tempTableMappingValues: 'temp-table-mapping-values',
  tableMappingKeys: 'table-mapping-keys',
  jsonMappingValue: 'json-mapping-value',
  thirdStepPreviewJson: 'third-step-preview-json',
  jsonMappingFormData: 'json-mapping-form-data',
  mappingType: 'mapping-type',
  mappingValue: 'mapping-value',
};

export const MAPPING_TYPE = {
  table: 'table',
  json: 'json',
};
