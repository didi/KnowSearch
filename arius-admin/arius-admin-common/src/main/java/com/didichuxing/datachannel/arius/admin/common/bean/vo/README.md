vo中的bean是用于响应rest请求数据的bean
命名规则: xxxVO
必须继承基类:BaseVO


同一个业务实体区分不用的使用场景:
AppVO               用于运维控制台获取数据,包含全部字段属性
AppGatewayVO        用于gateway获取app信息,包含基本信息和授权模板
AppConsoleSampleVO  用于用户控制台获取app列表,只包含基础信息,且不包含密码
AppConsoleVO        用于用户控制台app责任人获取app列表,只包含基础信息
