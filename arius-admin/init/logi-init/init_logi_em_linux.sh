#!/bin/bash

workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}

# arius地址
ariusHost=$1
ariusPort=$2

clusterMasterIp=$3
clusterMasterPort=$4

deleteExitMateClusterFlag=$5

# 模板负责人，admin
ariusResponsible="admin"
# ES管控项目appId
ariusManagerAppId=1

# 逻辑集群ID
logicClusterId=0
# 物理集群名
phyClusterName="logi-em-matedata-cluster"
# 给模板分配的rack，如果有多个则逗号分隔, 开源用户接入默认为*
rack=""

# 管理员用户cookie里的domainAccount
domainAccount="admin"

# 模板数据所在的目录
templateDirForArius="./template_in_arius"

metaDataClusterDir="./join_cluster_in_logi_em"

templateDirForCluster="./template_in_cluster"

codeForCheckClusterHealthResp="1"

getJsonValuesByAwk() {
    awk -v json="$1" -v key="$2" -v defaultValue="$3" 'BEGIN{
        foundKeyCount = 0
        while (length(json) > 0) {
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
                print substr(json, start, stop - start);
            }

            json = substr(json, stop + 1, length(json) - stop)
        }
    }'
}

if [ -z ${ariusHost} ]; then
    echo "error: ariusHost not specified."
    exit 1
fi

if [ -z ${ariusPort} ]; then
    echo "error: ariusPort not specified."
    exit 1
fi

if [ -z ${clusterMasterIp} ]; then
    echo "error: clusterMasterIp not specified."
    exit 1
fi

if [ -z ${clusterMasterPort} ]; then
    echo "error: clusterMasterPort not specified."
    exit 1
fi

curlCmdForCheckClusterIsExitResp=$(curl -X GET "http://${ariusHost}:${ariusPort}/admin/api/v3/white/phy/cluster/${phyClusterName}/isExit" -H "Cookie: domainAccount=${domainAccount}" -H "X-SSO-USER: ${ariusResponsible}" -H "X-ARIUS-APP-ID: ${ariusManagerAppId}" -H "content-type: application/json")
codeForCheckClusterIsExitResp=$(getJsonValuesByAwk "${curlCmdForCheckClusterIsExitResp}" code ""  | sed 's/\"//g')
if [ "x$codeForCheckClusterIsExitResp" == "x0" ]; then
    echo "join cluster is exit..."
    if [ "x$:deleteExitMateClusterFlag" == "x1" ]; then
    curlCmdForDelMetaDataClusterResp=$(curl -X DELETE "http://${ariusHost}:${ariusPort}/admin/api/v3/white/phy/cluster/${phyClusterName}/del" -H "Cookie: domainAccount=${domainAccount}" -H "X-SSO-USER: ${ariusResponsible}" -H "X-ARIUS-APP-ID: ${ariusManagerAppId}" -H "content-type: application/json")
    fi
fi

echo "starting join cluster..."
    for file in `ls ${metaDataClusterDir}`
        do
            joinClusterName=`echo ${file}`
            file="${metaDataClusterDir}/${file}"
            echo "handle file ${file}"
             # 替换数据
            # 元数据集群master节点Ip
            sed -i "s/\"ip\":.*/\"ip\": \"${clusterMasterIp}\",/g" ${file}

            # 元数据集群端master节点端口号
            sed -i "s/\"port\":.*/\"port\": ${clusterMasterPort},/g" ${file}

            # 指定接入集群master角色
            sed -i "s/\"role\":.*/\"role\": 3,/g" ${file}

            joinClusterContent=`cat ${file}`

            curlCmdForJoinResp=$(curl -X POST "http://${ariusHost}:${ariusPort}/admin/api/v3/op/phy/cluster/join" -H "Cookie: domainAccount=${domainAccount}" -H "X-SSO-USER: ${ariusResponsible}" -H "X-ARIUS-APP-ID: ${ariusManagerAppId}" -H "content-type: application/json" -d "${joinClusterContent}")

            code=$(getJsonValuesByAwk "${curlCmdForJoinResp}" code ""  | sed 's/\"//g')

            if [ "x$code" == "x0" ];
            then
                echo "Get the return value successfully and start field parsing"
                logicClusterId=$(getJsonValuesByAwk "${curlCmdForJoinResp}" v1 ""  | sed 's/\"//g')
                phyClusterName=$(getJsonValuesByAwk "${curlCmdForJoinResp}" v2 ""  | sed 's/\"//g')
            else
                echo "Failed to join meta data cluster"
                exit 1;
            fi

            echo "${curlCmdForJoinResp}"
            eval "${curlCmdForJoinResp}"
    done
echo "Successful join cluster!!!"

echo "start checking join cluster valid..."
for i in `seq 1 50`
do
    echo  "Loop checking metadata cluster status, number of times: ${i}"
    curlCmdForCheckClusterHealthResp=$(curl -X GET "http://${ariusHost}:${ariusPort}/admin/api/v3/white/phy/cluster/${phyClusterName}/checkHealth" -H "Cookie: domainAccount=${domainAccount}" -H "X-SSO-USER: ${ariusResponsible}" -H "X-ARIUS-APP-ID: ${ariusManagerAppId}" -H "content-type: application/json")
    codeForCheckClusterHealthResp=$(getJsonValuesByAwk "${curlCmdForCheckClusterHealthResp}" code ""  | sed 's/\"//g')
    if [ "x$codeForCheckClusterHealthResp" == "x0" ];
        then
            break
    fi
    sleep 1
done

if [ "x$codeForCheckClusterHealthResp" != "x0" ];
    then
        echo "join matedata cluster is invalid"
    else
        echo "joined cluster is Effective!!!"
fi

echo "${logicClusterId}"
echo "${phyClusterName}"
sleep 3

echo "starting created system template for arius..."
for file in `ls ${templateDirForArius}`
    do
        templateName=`echo ${file}`
        file="${templateDirForArius}/${file}"
        echo "handle file ${file}"
        # 替换模板数据
        # 负责人
        sed -i "s/\"responsible\":.*/\"responsible\": \"${ariusResponsible}\",/g" ${file}

        # 项目appId
        sed -i "s/\"appId\":.*/\"appId\": ${ariusManagerAppId},/g" ${file}

        # 逻辑集群ID
        sed -i "s/\"resourceId\":.*/\"resourceId\": ${logicClusterId},/g" ${file}

        # 物理集群名
        sed -i "s/\"cluster\":.*/\"cluster\": \"${phyClusterName}\",/g" ${file}

        # rack
        sed -i "s/\"rack\":.*/\"rack\": \"${rack}\",/g" ${file}

        # 物理模板角色
        sed -i "s/\"role\":.*/\"role\": 1,/g" ${file}

        # 数据类型-系统数据
        sed -i "s/\"dataType\":.*/\"dataType\": 0,/g" ${file}

        templateContentForArius=`cat ${file}`

        curlCmdForTemplate="curl -X PUT 'http://${ariusHost}:${ariusPort}/admin/api/v2/op/template/logic/add' -H 'Cookie: domainAccount=${domainAccount}' -H 'X-SSO-USER: ${ariusResponsible}' -H 'X-ARIUS-APP-ID: ${ariusManagerAppId}' -H 'content-type: application/json' -d '${templateContentForArius}'"
        echo "${curlCmdForTemplate}"
        sleep 1
        eval "${curlCmdForTemplate}"
    done

echo "starting created system template for mateDataCluster..."
for file in `ls ${templateDirForCluster}`
    do
        templateName=`echo ${file}`
        file="${templateDirForCluster}/${file}"

        # 替换rack
        sed -i "s/\"rack\": \"r1.*/\"rack\": \"${rack}\"/g" ${file}

        templateContentForCluster=`cat ${file}`
        echo -e "\ncreate admin template ${templateName}"
        response=`curl -X PUT "http://${clusterMasterIp}:${clusterMasterPort}/_template/${templateName}" -H 'content-type: application/json' -d "${templateContentForCluster}"`
        echo "${response}"
done
echo "System template created successfully!!!"

echo -e "\nfinished."