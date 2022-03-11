---
category: 组件
cols: 1
type: hook
title: useDebounce
subtitle: 防抖
---

配合 `useEffect` ，监听 `input` 输入值的变化。

```jsx
import { useDebounce, Table, Input, Icon, Row, Col, Button } from 'antd-advanced';
const listUrl = '';
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    commonSearch: true,
  },
  {
    title: '缩略图',
    dataIndex: 'image',
    render: (text, record, index) => (
      <div>
        {record.thumbnail_pic_s ? (
          <img style={{ maxHeight: 40 }} src={record.thumbnail_pic_s} alt={record.title} />
        ) : (
          '暂无图片'
        )}
      </div>
    ),
  },
  {
    title: '分类',
    dataIndex: 'category',
    commonFilter: true,
  },
  {
    title: '作者',
    dataIndex: 'author_name',
    commonFilter: true,
  },
  {
    title: '发布日期',
    dataIndex: 'date',
    commonSorter: true,
  },
];

const BasicExample: React.FC = () => {
    const [dataSource, setDataSource] = React.useState([]);
    const [loading, setLoading] = React.useState(false);
    const [searchQuery, setSearchQuery] = React.useState();
    const debouncedSearchQuery = useDebounce(searchQuery, 500);

    React.useEffect(() => {
      fetchData();
    }, [])

    React.useEffect(() => {
        const fetchParams = {
            title: debouncedSearchQuery
        }

        fetchData(fetchParams);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [debouncedSearchQuery]);

    async function fetchData(fetchParams = {}) {
        let url = new URL(listUrl) as any;
        url.search = new URLSearchParams(fetchParams);
        setLoading(true);
        const res = await fetch(url);
        res
        .json()
        .then(res => {
            setDataSource(res.data);
            setLoading(false);
        })
        .catch(() => {
            setLoading(false);
        });
    }

    const handleSearchChange = e => {
        const query = e.target.value;
        setSearchQuery(query);
    };

    return (
        <div>
            <Input
                style={{width: '100%', marginBottom: 10}}
                allowClear={true}
                value={searchQuery}
                onChange={handleSearchChange}
                placeholder="模糊搜索表格内容(多个关键词请用空格分隔。如：key1 key2)"
            />
            <Table
                columns={columns}
                dataSource={dataSource}
            />
        </div>
    );
}

ReactDOM.render(
  <div>
    <BasicExample />
  </div>,
  mountNode,
);
```
