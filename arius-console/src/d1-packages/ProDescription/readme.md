# ProDescription

## API
### BasisInfo
|参数|说明|类型|默认值|
|:---|:---|:---|:---|
|title|详情列表的标题| string \| ReactNode |-|
|titleStyle|列表标题行内样式|React.CSSProperties|-|
|dataSource|详情数据(必填)|any|-|
|config|展示项的配置(必填)|[optionItemType](#optionItemType)[]|-|
|labelWidth|lable的宽度|string \| number|80|
|labelStyle|lable行内样式|React.CSSProperties|-|
|needColon| 是否需要 ':' 符号|boolean|false|
|xl| 屏幕分24格，屏幕宽度小于1920的时候一行占的格数 |number|8|
|xxl| 屏幕分24格，屏幕宽度大于等于1920的时候一行占的格数 |number|6|
|getBasisInfoConfig| 获取详情列表展示的配置 |(data: any, config: [optionItemType](#optionItemType)[]) => [optionItemType](#optionItemType)[]|-|

----

### <h3 id='optionItemType'>optionItemType</h3>
|参数|说明|类型|默认值|
|:---|:---|:---|:---|
|label|必填项|string \| ReactNode|-|
|key|单条详情的key值|React.CSSProperties|-|
|copy| 是否展示复制图标 |boolean|false|
|span| 单条详情所占行数 |number|1|
|customType| 自定义配置显示内容，提供展示tag | [BASIS_TYPE](#BASIS_TYPE) | - |
|labelStyle|可自定义单个lable的行内样式|React.CSSProperties|-|
|render|可以自定义渲染的内容和逻辑|(ct: any) => any;(ct为当前详情的内容)|-|

---

### <h3 id='BASIS_TYPE'>BASIS_TYPE</h3>
```js
enum BASIS_TYPE {
  /* 展示标签类型 */
  tag = 'tag',
  /* 展示内容高亮 */
  highlight = 'highlight',
  /* 是否可编辑 */
  editable = 'editable',
}
```
----


### Demo
```js
import React,{useState} from 'react';
import BasisInfo from '../ProDescription';
import { optionItemType } from '../ProDescription/type';

const Demo:React.FC = ()=>{
  const [infoData,setInfoData]= useState(basisInfoData);
  // mock数据
  const basisInfoData = {
    name: 'myj-test-deployment',
    tag: ['app:myj-test-deployment', 'app:myj-test-deployment', 'test:deployment', 'app:myj-test-deployment'],
    create: 'root',
    cluster: 'cluster203',
    annotations: ['boos', 'myj-test-deployment', 'deployment', 'app:myj-test-deployment'],
    updateStrategy: '滚动更新',
    namespace: 'myj-test-deployment',
    description: '测试Nginx应用测试Nginx应用测试Nginx应用测试Nginx应用测试Nginx应用测试Nginx应用',
    selector: 'app:myj-test-deployment',
  };

  // 基础信息配置项
  const basisInfoConfig: optionItemType[] = [
    {
      label: '名称',
      key: 'name',
      copy: true,
      span: 3,
    },
    {
      label: '描述',
      key: 'description',
      copy: true
    },
    {
      label: '标签',
      key: 'tag',
      customType: BASIS_TYPE.tag,
    },
    {
      label: '创建人',
      key: 'create',
    },
    {
      label: '集群',
      key: 'cluster',
    },
    {
      label: '注解',
      key: 'annotations',
      customType: BASIS_TYPE.highlight,
    },
    {
      label: '更新策略',
      key: 'updateStrategy',
    },
    {
      label: '命名空间',
      key: 'namespace',
      render: (contnet: any) => {
        // 可自定义渲染及展示的逻辑
        return (
          <Tag key={contnet} color="green">
            {contnet}
          </Tag>
        );
      },
    },
    {
      label: '选择器',
      key: 'selector',
    },
  ];
  return (<BasisInfo
    title={<span>标题</span>}
    labelWidth={100}
    dataSource={infoData}
    config={basisInfoConfig}
  />)
}

```