### 简单图表组件 默认配置包含折线图和饼图

| 参数          | 说明                                                   | 类型                | 默认值 |
| ------------- | ------------------------------------------------------ | ------------------- | ------ |
| option        | 图表 option                                            | -                   | -      |
| wrapStyle     | 图表容器的 style                                       | React.CSSProperties | -      |
| wrapClassName | 图表容器的 class 名                                    | string              | -      |
| initOpts      | 图表初始化配置，可单独配置图表的高宽，不传取容器的宽高 | -                   | -      |
| onResize      | 窗口 resize 的回调, 不传使用图表默认处理               | function            | -      |
| resizeWait    | 窗口 resize 回调的节流时间                             | number              | 1000   |
