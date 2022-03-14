集成测试说明：
1. 到 arius-admin-rest 下的 application.yml 确保以下配置信息正确：
   1. integrate.test.admin.ip
   2. integrate.test.admin.port
   3. integrate.test.es.ip
   4. integrate.test.es.port
   5. integrate.test.appid
   6. integrate.test.operator
2. 找到需要测试的接口类 xxxController，到 com.didichuxing.datachannel.arius.admin.method 下创建接口对应的 xxxMethod；
3. 创建 xxxTest 类，如果该接口测试需要用到物理集群，则该 xxxTest 继承 BasePhyClusterInfoTest，需要用到逻辑集群，则该 xxxTest 继承 BaseLogicClusterInfoTest，其他的参考现有的实现；
4. 一些 Dto 相关的类信息，初始化的时候可以选择在 CustomDataSource 类中进行初始化；
5. 可以参考已经编写好的用例，再进行编写集成测试代码。