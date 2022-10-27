#!/bin/bash
### 方法简要说明：
### 1. 是先查找一个字符串：带双引号的key。如果没找到，则直接返回defaultValue。
### 2. 查找最近的冒号，找到后认为值的部分开始了，直到在层数上等于0时找到这3个字符：,}]。
### 3. 如果有多个同名key，则依次全部打印（不论层级，只按出现顺序）
### @author lux feary
###
### 3 params: json, key, defaultValue
function getJsonValuesByAwk() {
    awk -v json="$1" -v key="$2" -v defaultValue="$3" 'BEGIN{
        foundKeyCount = 0
        while (length(json) > 0) {
            # pos = index(json, "\""key"\""); ## 这行更快一些，但是如果有value是字符串，且刚好与要查找的key相同，会被误认为是key而导致值获取错误
            pos = match(json, "\""key"\"[ \\t]*?:[ \\t]*");
            if (pos == 0) {if (foundKeyCount == 0) {print defaultValue;} exit 0;}

            ++foundKeyCount;
            start = 0; stop = 0; layer = 0;
            for (i = pos + length(key) + 1; i <= length(json); ++i) {
                lastChar = substr(json, i - 1, 1)
                currChar = substr(json, i, 1)

                if (start <= 0) {
                    if (lastChar == ":") {
                        start = currChar == " " ? i + 1: i;
                        if (currChar == "{" || currChar == "[") {
                            layer = 1;
                        }
                    }
                } else {
                    if (currChar == "{" || currChar == "[") {
                        ++layer;
                    }
                    if (currChar == "}" || currChar == "]") {
                        --layer;
                    }
                    if ((currChar == "," || currChar == "}" || currChar == "]") && layer <= 0) {
                        stop = currChar == "," ? i : i + 1 + layer;
                        break;
                    }
                }
            }

            if (start <= 0 || stop <= 0 || start > length(json) || stop > length(json) || start >= stop) {
                if (foundKeyCount == 0) {print defaultValue;} exit 0;
            } else {
                value=substr(json, start+1, stop - start - 2);
                gsub(/\n/,"\\n",value);
                print value;
            }

            json = substr(json, stop + 1, length(json) - stop)
        }
    }'
}

# shellcheck disable=SC2145
echo "脚本参数: ${@}"
knowSearch_url="http://172.23.161.28:8888/op/manager/api/v3/task/task-config"
type=$1
task_id=$2
group_name=$3

if [[ "$type" == "0" ]]; then
    echo "组件安装"
    response=$(curl "${knowSearch_url}?taskId=${task_id}&groupName=${group_name}&host=10.96.75.28")
    echo $response
    user=$(getJsonValuesByAwk "${response}" "user" "root")
    echo $user
    elasticsearch_yml=$(getJsonValuesByAwk "$response" "elasticsearch.yml" "root")
    echo $elasticsearch_yml
    install_director=$(getJsonValuesByAwk "$response" "installDirector" "/data")
    echo $install_director
    package_url=$(getJsonValuesByAwk "$response" "url" "/data")
    echo $package_url
    sudo su - ${user} -c 'mkdir -p /tmp/huafei/elasticsearch'
    sudo su - ${user} -c "curl  ${package_url} | tar -zxC /tmp/huafei/elasticsearch"
    sudo su - ${user} -c "echo -e '$elasticsearch_yml' > /tmp/huafei/elasticsearch/elasticsearch-6.8.2/config/elasticsearch.yml"
    sudo su - ${user} -c "/tmp/huafei/elasticsearch/elasticsearch-6.8.2/bin/elasticsearch -d"
fi






