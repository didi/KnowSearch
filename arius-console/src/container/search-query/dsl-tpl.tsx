import React from 'react';
import { RenderTitle } from 'component/render-title';
import { HashMenu } from 'component/hash-menu';
import { TAB_LIST, MENU_MAP } from './config';
import './index.less';

export const DslTpl = () => {
  return (
    <>
      <HashMenu TAB_LIST={TAB_LIST} MENU_MAP={MENU_MAP} defaultHash='query-tpl' />
    </>
  )
}
