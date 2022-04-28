### 折线图联动饼图组件

| 参数                | 说明                      | 类型                  | 默认值 |
| ------------------- | ------------------------- | --------------------- | ------ |
| wrapClassName       | 组件容器类名              | string                | -      |
| wrapStyle           | 组件容器 style            | React.CSSProperties   | -      |
| lineClassName       | 折线图容器类名            | string                | -      |
| lineStyle           | 折线图容器类 style        | React.CSSProperties   | -      |
| pieClassName        | 饼图容器类名              | string                | -      |
| pieStyle            | 饼图容器 style            | React.CSSProperties   | -      |
| lineOption          | 折线图 option             | echarts.EChartsOption | -      |
| pieOption           | 饼图 option               | echarts.EChartsOption | -      |
| lineInitOpts        | 折线图初始化配置， 可不传 | -                     | -      |
| pieInitOpts         | 饼图初始化配置，可不传    | -                     | -      |
| chartData           | 图表数据                  | -                     | -      |
| onResize            | 窗口 resize 事件 可不传   | (params: any) => void | -      |
| onUpdateAxisPointer | 折线图滑动事件            | (params: any) => void | -      |
| resizeWait          | 窗口 resize 节流时间      | number                | 1000   |
