### DrawerForm

DrawerForm 组合了Drawer和XForm 抽屉式的表单 内置了相关提交重置的方法

> 使用默认的submit或者调用form.submit()提交数据，需要传递onFinish方法

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| trigger | 用于触发 Modal 打开的 dom，一般是 button | `ReactNode` | - |
| visible | 是否打开 | `boolean` | - |
| onVisibleChange | visible 改变时触发 | `(visible:boolean)=>void` | - |
| drawerProps | Drawer 的 props，使用方式与 [antd](https://ant.design/components/drawer-cn/) 相同。注意：不支持 'visible'，请使用全局的 visible, 自定义footer, 请使用submitter进行配置| [props](https://ant.design/components/drawer-cn/#API) | - |
| title | 抽屉的标题 | `ReactNode` | - |
| width | 抽屉的宽度 | `Number` | `size['small']` |
| size | 抽屉的尺寸 包含 `small (595px)\| middle (728px) \| large (1080px)` | `String` | `small` |
| onFinish | 使用默认的submit或者调用form.submit()提交数据校验通过后触发，需要如果返回一个 true 才会关掉抽屉, | `async (values)=>boolean` | - |
| submitter | footer操作按钮相关配置, 设为false时不显示操作区域 | `Submitter \| false` | - |
| submitterPosition | footer操作按钮的位置, 支持居左和居右 | string | right |
| XFormProps | XForm的属性|  |  |


### Submitter

底部操作区域配置
| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| buttonConfig | 提交重置的按钮文字 | `{submitText, resetText}` | submitText: '确定' resetText: '取消'   |
| submitButtonProps | 提交按钮的 props，preventDefault为true为将不执行默认的form.submit  | `preventDefault` && [ButtonProps](https://ant.design/components/button-cn/) | - |
| resetButtonProps | 重置按钮的 props，preventDefault为true为将不执行默认的form.reset方法 | `preventDefault` &&  [ButtonProps](https://ant.design/components/button-cn/) | - |
| render | 自定义操作的渲染 | `false`\|`(props: { form, submit, reset }, dom:JSX[])=>ReactNode[]`   | - |

> resetButtonProps 默认会关闭modal或drawer, 定义了onClick时会覆盖上述行为

> render 第一个参数props包含form实例、 form的submit和reset方法， 第二个参数是默认的 dom 数组，第一个是重置按钮，第二个是提交按钮。