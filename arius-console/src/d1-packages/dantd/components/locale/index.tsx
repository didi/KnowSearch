import zh_CN from './zh_CN';
import en_US from './en_US';

export interface DLocale {
  back: string;
  // [key: string]: string,
  'table.search.placeholder': string;
  'table.sort.ascend': string;
  'table.sort.descend': string;
  'table.total.prefix': string;
  'table.total.suffix': string;
  'table.select.num': string;
  'table.filter.search.placeholder': string;
  'table.filter.search.btn.ok': string;
  'table.filter.search.btn.cancel': string;
  'table.filter.header.title': string;
  'table.filter.header.search': string;
  'table.filter.header.filter': string;
  'table.filter.header.btn.clear': string;
  'form.item.key.title': string;
  'form.item.value.title': string;
  'form.detail.nodata': string;
  'form.item.key.placeholder': string;
  'form.item.value.placeholder': string;
  'form.item.add': string;
  'form.placeholder.prefix': string;
  'form.selectplaceholder.prefix': string;
  'dqueryform.reset': string;
  'dqueryform.search': string;
  'dqueryform.collapsed': string;
  'dqueryform.expand': string;
  'color.placeholder': string;
}

export default {
  'zh-CN': zh_CN,
  'en-US': en_US,
};
