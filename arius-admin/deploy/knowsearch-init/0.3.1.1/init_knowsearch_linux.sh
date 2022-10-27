#!/bin/bash
#需要变更一些地址
url_prefix='http://127.0.0.1:8015/admin/api'
data="."
#元数据项目，默认不变
subject=2
#接入物理集群并创建逻辑集群的接口
put_cluster_url='/v3/thirdpart/common/cluster/join-with-logic'
header="-H  'X-SSO-USER: admin'  -H 'X-LOGI-SECURITY-PROJECT-ID: 1'  -H 'X-SSO-USER-ID: 1593'  -H 'Content-Type:application/json' "
header_subject="-H  'X-SSO-USER: admin'  -H 'X-LOGI-SECURITY-PROJECT-ID: $subject'  -H 'X-SSO-USER-ID: 1593'  -H 'Content-Type:application/json' "
#逻辑集群id
logic_cluster_id=0
run_join_cluster(){
 data_path=`dirname $0`
 url=$url_prefix$put_cluster_url
 data=`cat $data_path/cluster-phy-join`
 join_cluster="curl -s $header  -XPOST $url -d '$data'"
 response=`eval $join_cluster`
 echo "response $response"
 logic_cluster_id=`echo $response | awk -F 'data":' '{print $NF}'|awk -F "," '{print $1}'`
 echo "logic_cluster_id $logic_cluster_id" >> /vat/log/install_knowsearch.log
}



create_logic_template(){
  num_exec='echo "$logic_cluster_id" | sed -n "/^[0-9]\+$/p" '
  num=`eval $num_exec`
  if [ ! -n "$num" ]; then
        echo '逻辑集群id未获取到' >> /vat/log/install_knowsearch.log
        exit
  fi

  echo $logic_cluster_id
  data_path=`dirname $0`
  for i in `ls $data_path/template_in_arius/*`
  do
    create_logic_template_exec="cat $i| sed 's/#resourceId/$logic_cluster_id/g'"
    create_logic_template_data=`eval eval $create_logic_template_exec`
    url=$url_prefix/v3/template/logic
    create_logic_template_exec="curl -s  $header_subject  -XPOST $url -d '$create_logic_template_data'"
    result=`eval $create_logic_template_exec`
    echo "Create template results : $result ...." >> /vat/log/install_knowsearch.log
  done
}

perform_run_task(){
  task_url=$url_prefix/v1/logi-job/task/list
  data=`cat $data_path/task`
  echo $data >> /vat/log/install_knowsearch.log
	get_task_date="curl -s $header  -XPOST $task_url -d '$data'"
	response=`eval $get_task_date`
	taskCode=`echo $response |  grep  -E 'taskCode":"\d+"' -o| grep -E "\d+" -o`
	taskCode_url=$url_prefix/v1/logi-job/task/$taskCode/do
	echo $taskCode_url >> /vat/log/install_knowsearch.log
	taskCode_url_run="curl -s $header  -XPOST $taskCode_url -d '{}'"
	result=`eval $taskCode_url_run`
	echo $result >> /vat/log/install_knowsearch.log

}

url_status=$(curl -s -m 5 -IL $url_prefix/health  |grep 200)
if ["$url_status" == ""];then
	echo "服务异常，无法执行脚本" >> /vat/log/install_knowsearch.log
	exit
fi
#接入物理集群
run_join_cluster


#创建元数据模版
create_logic_template
#执行定时任务
perform_run_task
sleep 10

echo "请重启admin工程"