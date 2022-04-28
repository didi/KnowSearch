# BaseTatail

## 何时使用

- 详情展示

## API

| 参数       | 说明 | 类型 | 默认值   |
| ---------- | ---- | ---- | -------- | ----------- |
| title      | 标题 | null | string   | JSX.Element |
| columns    | 配置 | []   | item[][] |
| baseDetail | 数据 | {}   | objet    |

## Demo

```js
const columns = [
  [
    {
      key: "rack",
      label: "所属rack",
    },
    {
      key: "role",
      label: "role",
      render: (value: number) => (
        <>
          <span>{value}</span>
        </>
      ),
    },
  ],
  [
    {
      key: "shard",
      label: "shard",
    },
    {
      key: "createTime",
      label: "创建时间",
      render: (value: string) => <>{moment(value).format(timeFormat)}</>,
    },
  ],
];
```
