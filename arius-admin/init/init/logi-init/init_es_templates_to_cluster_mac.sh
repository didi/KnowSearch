#!/bin/bash

workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}

esHost=$1
esPort=$2
# 给模板分配的rack，如果有多个则逗号分隔
rack=$3
templateDir=$4
echo "esHost:${esHost} esPort:${esPort} rack:${rack} templateDir:${templateDir}"

if [ -z ${esHost} ]; then
    echo "error: esHost not specified."
    exit 1
fi

if [ -z ${esPort} ]; then
    echo "error: esPort not specified."
    exit 1
fi

if [ -z ${templateDir} ]; then
    echo "error: templateDir not specified."
    exit 1
fi

if [ -z ${rack} ]; then
    echo "error: rack not specified."
    exit 1
fi


for file in `ls ${templateDir}`
    do
        templateName=`echo ${file}`
        file="${templateDir}/${file}"

        # 替换rack
        sed -i '' "s/\"rack\": \"r1.*/\"rack\": \"${rack}\"/g" ${file}

        templateContent=`cat ${file}`
        echo -e "\ncreate admin template ${templateName}"
        response=`curl -X PUT "http://${esHost}:${esPort}/_template/${templateName}" -H 'content-type: application/json' -d "${templateContent}"`
        echo "${response}"
done

echo "finished."
