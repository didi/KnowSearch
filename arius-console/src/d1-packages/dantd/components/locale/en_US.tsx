import antdEnUS from 'antd/lib/locale/en_US';
import { Locale } from 'antd/lib/locale-provider';
import { DLocale } from './index';

const localeValues: Locale & DLocale = {
  ...antdEnUS,
  back: 'Back',
  'table.search.placeholder':
    'Fuzzy search table content (multiple keywords separated by Spaces. Such as: key1 key2)',
  'table.sort.ascend': 'Ascend',
  'table.sort.descend': 'Descend',
  'table.total.prefix': 'Total',
  'table.total.suffix': 'Items',
  'table.select.num': 'Selected: ',
  'table.filter.search.placeholder': 'Please enter keywords',
  'table.filter.search.btn.ok': 'Search',
  'table.filter.search.btn.cancel': 'Clear',
  'table.filter.header.title': 'Filtration Conditions:',
  'table.filter.header.search': ': Search',
  'table.filter.header.filter': ': Filter',
  'table.filter.header.btn.clear': 'Clear',
  'form.item.key.title': 'Custom Key',
  'form.item.value.title': 'Custom Value',
  'form.detail.nodata': 'No Data',
  'form.item.key.placeholder': 'Please enter key',
  'form.item.value.placeholder': 'Please enter value',
  'form.item.add': 'Add Custom Parameters',
  'form.placeholder.prefix': 'Please enter',
  'form.selectplaceholder.prefix': 'Please select',
  'dqueryform.reset': 'Clear',
  'dqueryform.search': 'Search',
  'dqueryform.collapsed': 'Collapsed',
  'dqueryform.expand': 'Expand',
  'color.placeholder': 'Please select color',
};

export default localeValues;