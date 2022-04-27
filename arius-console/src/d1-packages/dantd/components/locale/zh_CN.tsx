
import antdZhCN from 'antd/lib/locale/zh_CN';
import { Locale } from 'antd/lib/locale-provider';
import { DLocale } from './index';

const localeValues: Locale & DLocale = {
  ...antdZhCN,
  'back': '返回',
  'table.search.placeholder': '模糊搜索表格内容(多个关键词请用空格分隔。如：key1 key2)',
  'table.sort.ascend': '升序',
  'table.sort.descend': '降序',
  'table.total.prefix': '共',
  'table.total.suffix': '条',
  'table.select.num': '已选择: ',
  'table.filter.search.placeholder': '请输入要搜索的内容',
  'table.filter.search.btn.ok': '搜索',
  'table.filter.search.btn.cancel': '清空',
  'table.filter.header.title': '过滤条件:',
  'table.filter.header.search': ':搜索',
  'table.filter.header.filter': ':过滤',
  'table.filter.header.btn.clear': '清空',
  'form.item.key.title': '自定义Key',
  'form.item.value.title': '自定义Value',
  'form.detail.nodata': '暂无数据',
  'form.item.key.placeholder': '请输入Key',
  'form.item.value.placeholder': '请输入Value',
  'form.item.add': '增加自定义参数',
  'form.placeholder.prefix': '请输入',
  'form.selectplaceholder.prefix': '请选择',
  'dqueryform.reset': '重置',
  'dqueryform.search': '查询',
  'dqueryform.collapsed': '收起',
  'dqueryform.expand': '展开',
  'color.placeholder': '请选择颜色',
};
export default localeValues;