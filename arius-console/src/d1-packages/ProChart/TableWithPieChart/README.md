### 图表和表格联动组件

| 参数               | 说明                                          | 类型                | 默认值     |
| ------------------ | --------------------------------------------- | ------------------- | ---------- |
| layout             | 布局方式， 支持横向纵向，默认横向             | string              | horizontal |
| tableProps         | 表格配置, 和 proTable 配置保持一致            | object              | -          |
| chartProps         | 图表配置 option                               | object              | -          |
| hightlightIndex    | 饼图默认高亮项                                | number              | -          |
| wrapClassName      | 组件容器的类名                                | string              | -          |
| tableClassName     | table 容器的类名                              | string              | -          |
| chartClassName     | 图表容器的类名                                | string              | -          |
| wrapStyle          | 组件容器的 style                              | React.CSSProperties | -          |
| tableStyle         | table 容器的 style                            | React.CSSProperties | -          |
| chartStyle         | 图表容器的 style                              | React.CSSProperties | -          |
| updateHighlighItem | 图表高亮项改变回调                            | function            | -          |
| onResize           | 窗口 resize 回调， 可不传采用组件默认处理方式 | function            | -          |
| resizeWait         | 窗口 resize 回调执行节流时间                  | number              | 1000       |
| chartData          | 图表的数据                                    | -                   | -          |
| initChartOpts      | 图表初始化高宽配置， 不传默认取容器的宽高     | object              | -          |
