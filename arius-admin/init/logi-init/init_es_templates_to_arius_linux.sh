#!/bin/bash

workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}

# arius地址
ariusHost=$1
ariusPort=$2

# 模板负责人，如root
ariusResponsible=$3
# ES管控项目appId
ariusManagerAppId=$4

# ES管控项目所在租户ID，如1
departmentId=$5
# ES管控项目所在租户名，如内置租户
department=$6

# 逻辑集群ID
logicClusterId=$7
# 物理集群名
phyClusterName=$8
# 给模板分配的rack，如果有多个则逗号分隔
rack=$9

# 管理员用户cookie里的ecmc-user
ecmcUser=${10}

# 模板数据所在的目录
templateDir=${11}


if [ -z ${ariusHost} ]; then
    echo "error: ariusHost not specified."
    exit 1
fi

if [ -z ${ariusPort} ]; then
    echo "error: ariusPort not specified."
    exit 1
fi

if [ -z ${ariusResponsible} ]; then
    echo "error: ariusResponsible not specified."
    exit 1
fi

if [ -z ${ariusManagerAppId} ]; then
    echo "error: ariusManagerAppId not specified."
    exit 1
fi

if [ -z ${departmentId} ]; then
    echo "error: departmentId not specified."
    exit 1
fi

if [ -z ${department} ]; then
    echo "error: department not specified."
    exit 1
fi

if [ -z ${logicClusterId} ]; then
    echo "error: logicClusterId not specified."
    exit 1
fi

if [ -z ${phyClusterName} ]; then
    echo "error: phyClusterName not specified."
    exit 1
fi

if [ -z ${rack} ]; then
    echo "error: rack not specified."
    exit 1
fi

if [ -z ${templateDir} ]; then
    echo "error: templateDir not specified."
    exit 1
fi


for file in `ls ${templateDir}`
    do
        templateName=`echo ${file}`
        file="${templateDir}/${file}"
        echo "handle file ${file}"
        # 替换模板数据
        # 负责人
        sed -i "s/\"responsible\":.*/\"responsible\": \"${ariusResponsible}\",/g" ${file}

        # 项目appId
        sed -i "s/\"appId\":.*/\"appId\": ${ariusManagerAppId},/g" ${file}

        # 租户ID
        sed -i "s/\"libraDepartmentId\":.*/\"libraDepartmentId\": \"${departmentId}\",/g" ${file}

        # 租户名
        sed -i "s/\"libraDepartment\":.*/\"libraDepartment\": \"${department}\",/g" ${file}

        # 逻辑集群ID
        sed -i "s/\"resourceId\":.*/\"resourceId\": ${logicClusterId},/g" ${file}

        # 物理集群名
        sed -i "s/\"cluster\":.*/\"cluster\": \"${phyClusterName}\",/g" ${file}

        # rack
        sed -i "s/\"rack\":.*/\"rack\": \"${rack}\",/g" ${file}

        # 物理模板角色
        sed -i "s/\"role\":.*/\"role\": 1,/g" ${file}

        # 数据类型-用户上报数据
        sed -i "s/\"dataType\":.*/\"dataType\": 2,/g" ${file}

        templateContent=`cat ${file}`

        curlCmd="curl -i -X PUT 'http://${ariusHost}:${ariusPort}/api/es/admin/v2/op/template/logic/add' -H 'Cookie: ecmc-user=${ecmcUser}' -H 'X-SSO-USER: ${ariusResponsible}' -H 'X-ARIUS-APP-ID: ${ariusManagerAppId}' -H 'content-type: application/json' -d '${templateContent}'"
        echo "${curlCmd}"
        eval "${curlCmd}"

        # response=`curl -X PUT "http://${ariusHost}:${ariusPort}/api/es/admin/v2/op/template/logic/add" -H "X-SSO-USER: ${ariusResponsible}" -H "X-ARIUS-APP-ID: ${ariusManagerAppId}" -H 'content-type: application/json' -d "${templateContent}"`
done

echo -e "\nfinished."
