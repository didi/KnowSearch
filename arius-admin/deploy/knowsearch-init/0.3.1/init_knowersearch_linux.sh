#!/bin/bash
#需要变更一些地址
url_prefix='http://localhost:8010/admin/api'
#元数据项目，默认不变
subject=2
#接入物理集群并创建逻辑集群的接口
put_cluster_url='/v3/thirdpart/common/join-cluster-phy-and-create-logic-cluster'
header="-H  'X-SSO-USER: admin'  -H 'X-LOGI-SECURITY-PROJECT-ID: 1'  -H 'X-SSO-USER-ID: 1593'  -H 'Content-Type:application/json' "
header_subject="-H  'X-SSO-USER: admin'  -H 'X-LOGI-SECURITY-PROJECT-ID: $subject'  -H 'X-SSO-USER-ID: 1593'  -H 'Content-Type:application/json' "
#逻辑集群id
logic_cluster_id=0
join_cluster(){
 url=$url_prefix$put_cluster_url
 data=`cat ./cluster-phy-join`
 join_cluster="curl $header  -X POST $url -d'$data'"
 response=`eval $join_cluster`
 echo "response $response"
 logic_cluster_id=`echo $response | awk -F'data":' '{print $NF}'|awk -F "," '{print $1}'`
 echo "logic_cluster_id $logic_cluster_id"
}



create_logic_template(){
  echo $logic_cluster_id
  for i in `ls ./template_in_arius/*`
  do
    create_logic_template_exec="cat $i| sed 's/#resourceId/$logic_cluster_id/g'"
    create_logic_template_data=`eval eval $create_logic_template_exec`
    url=$url_prefix/v3/template/logic
    create_logic_template_exec="curl $header_subject  -X POST $url -d '$create_logic_template_data'"
    result=`eval $create_logic_template_exec`
    echo "创建模版结果$result ...."
  done
}
#接入物理集群
join_cluster

#创建元数据模版
create_logic_template