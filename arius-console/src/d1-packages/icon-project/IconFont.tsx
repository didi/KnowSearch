import React from 'react';
import { createFromIconfontCN } from "@ant-design/icons";
import './_iconfont/iconfont.css';

const IconFont = createFromIconfontCN({
  // scriptUrl: '//at.alicdn.com/t/font_3056939_l2x4wmtyeos.js'
  scriptUrl: [
    require('./_iconfont/iconfont'),
  ],
});


export default IconFont;

// export default (props: any) => {
//   const { type, icon_class, ...rest } = props;
//   const defaultType = IconFontList && IconFontList[0]
//   const filterType = IconFontList.filter(item => item.type === type);
//   return <IconFont type={filterType.length > 0 ? filterType[0].icon_type : defaultType.icon_type} {...rest} />
// }
