#!/bin/bash

workspace=$(cd $(dirname $0) && pwd -P)
cd ${workspace}

esHost=$1
esPort=$2
templateDir=$4
echo "esHost:${esHost} esPort:${esPort}  templateDir:${templateDir}"

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

for file in `ls ${templateDir}`
    do
        templateName=`echo ${file}`
        file="${templateDir}/${file}"

        templateContent=`cat ${file}`
        echo -e "\ncreate admin template ${templateName}"
        response=`curl -X PUT "http://${esHost}:${esPort}/_template/${templateName}" -H 'content-type: application/json' -d "${templateContent}"`
        echo "${response}"
done

echo "finished."
