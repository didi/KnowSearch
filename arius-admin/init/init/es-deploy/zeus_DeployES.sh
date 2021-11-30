#!/bin/sh

#set -x

#获取当前文件的文件名称
MYNAME="${0##*/}"
RET_OK=0
RET_FAIL=1

# 在这里做新的测试
g_DeployUser="xiaoju"
g_DeployGroup="xiaoju"
g_EsHeapSize=31
g_DeployPath=""

g_ClusterName=""
g_MasterNodes=""
g_CfgAddress=""
g_PackageUrl=""
g_Role=""
g_PidCount=""
g_Action=""
g_EsVersion=""
g_Port=""
g_Ip_Port=""
g_ConfigAction=""

# ===== shuo =====
plugin_operation_type="4"                                                            # 插件操作类型：3（安装）4（卸载）
plugin_file_name="analysis-ik"                                                       # 插件文件名
plugin_s3_url="https://s3-gzpu-inter.didistatic.com/logi-data-es/analysis-ik.tar.gz" # 插件安装需要的下载地址
zeus_log="/soft/zeus-log-5-27"                                                       # 注：zeus脚本执行过程中打印的信息重定向到该文件，用于调试
# ===== shuo =====
#ecm_cluster_url="172.23.141.109:8010/v3/white/phy/cluster/updateHttpAddress"
ecm_cluster_url="http://172.23.144.9:8010/admin/api/testPost"
open_jdk="java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4 java-1.8.0-openjdk-devel-1.8.0.161-0.b14.el7_4"

if [ -t 1 ]; then
    RED="$(echo -e "\e[31m")"
    HL_RED="$(echo -e "\e[31;1m")"
    HL_BLUE="$(echo -e "\e[34;1m")"

    NORMAL="$(echo -e "\e[0m")"
fi

_hl_red() { echo "$HL_RED""$@""$NORMAL"; }
_hl_blue() { echo "$HL_BLUE""$@""$NORMAL"; }

_trace() {
    echo $(_hl_blue '  ->') "$@" >&2
}

_print_fatal() {
    echo $(_hl_red '==>') "$@" >&2
}

_usage() {
    cat <<USAGE
Usage: bash ${MYNAME} [options]

Require:
    --cluster_name=arius-log15     Operation cluster name.
    --masters=xxx,yyy,zzz          Master nodes for cluster.
    --cfg_address=http://xxx.git   Config file for cluster.
    --package_url=http://xxxx      Package url address.
    --es_version=version1          Version of elasticsearch.
    --role=rolename                Node role in cluster(masternode/clientnode/datanode).
    --pid_count=num                Es pid count in single machine(1/2...).
    --action=action                Action for this operation(new/expand/deploy/restart).

Options:
    -h, --help                     Print this help infomation.

USAGE

    exit $RET_FAIL
}

#
# Parses command-line options.
#  usage: _parse_options "$@" || exit $?
#
# --cluster_name=arius-log15 --masters=xxx,yyy,zzz --cfg_address=https://git.xiaojukeji.com/bigdata-databus/elasticsearch-trib  \
#    --package_url=https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.0.0-linux-x86_64.tar.gz --es_version=version1 --role=masternode --pid_count=1 --action=restart
_parse_options() {
    declare -a argv

    # $#表示的是传入参数的个数 -gt 表示的是大于
    while [[ $# -gt 0 ]]; do
        case $1 in
        -h | --help)
            _usage
            exit
            ;;
        # 不断的匹配传入的参数来获取变量值
        # 字符串的|可以表示或的操作
        -c=* | --cluster_name=*)
            g_ClusterName="${1#*=}"
            #作为脚本输入参数的偏移 不设置的话默认是偏移一位输入参数,不断的减少参数
            shift # past argument=value
            ;;
        --masters=*)
            g_MasterNodes="${1#*=}"
            shift # past argument=value
            ;;
        --config_action=*)
            g_ConfigAction="${1#*=}"
            shift
            ;;
        --cfg_address=*)
            g_CfgAddress="${1#*=}"
            shift # past argument=value
            ;;
        -p=* | --package_url=*)
            g_PackageUrl="${1#*=}"
            shift # past argument=value
            ;;
        -e=* | --es_version=*)
            g_EsVersion="${1#*=}"
            shift # past argument=value
            ;;
        -r=* | --role=*)
            g_Role="${1#*=}"
            shift # past argument=value
            ;;
        --pid_count=*)
            g_PidCount="${1#*=}"
            shift # past argument=value
            ;;
        -a=* | --action=*)
            g_Action="${1#*=}"
            shift # past argument=value
            ;;
        -p=* | --port=*)
            g_Port="${1#*=}"
            shift # past argument=value
            ;;
        -*)
            _print_fatal "command line: unrecognized option $1" >&2
            return 1
            ;;
        *)
            argv=("${argv[@]}" "${1}")
            shift
            ;;
        esac
    done

    # case ${#argv[@]} in
    #     *)
    #         _usage 1>&2
    #         return 1
    # ;;
    # esac
}
# ===== shuo =====
# 检查执行是否成功，若失败，则打印错误信息，执行相应命令，并退出脚本
check_result() {
    ret=$1
    message=$2
    order=$3

    #每一个基本的命令行指令执行之后都会有一个返回的值的指令
    if [ "${ret}" -ne 0 ]; then
        _print_fatal "${message}"
        $($order)
        exit "${ret}"
    fi
}

#function check_result() {
#    ret=$1
#    message="$2"
#
#    if [ $ret -ne 0 ]; then
#        _print_fatal $message
#        exit $ret
#    fi
#}
# ===== shuo =====

function initKernelPatch() {
    sudo su - -c "yum-complete-transaction --cleanup-only"
    sudo su - -c "yum --enablerepo=didi_kernel install -y didios-kpatch-modules-5.0-1.x86_64"
}

# shellcheck disable=SC2112
function user_add() {
    id -u $1 &>/dev/null
    if [[ $? == 0 ]]; then
        # echo "$1 user has eixst"
        echo -n
        sudo chmod 755 /home/$1
    else
        # echo "$1 user init..."
        sudo groupadd $1
        sudo adduser -K UMASK=022 $1 -g $1
    fi

    sudo mkdir -p /home/$1/.ssh &>/dev/null
    sudo sh -c "echo 'ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAupQVnu0/ERr1WiR3ipKjbxW+NapmgRluTtb2t/S4D0jPS556gcsCwVZZr6cRuewt5B8ALNBsu3ZecGwpKE1jHkj/kgN3/wFzUiKJLcrdT861o+FKTPhXCc0K6/xiPFhTjSzqVoqMHasnM5EeKyotk5x+5qDcTKLnfvT2YV0zlPZ7ok+8hhvT8D3qUDogbkyMeGKsZqSt6A7ETxt3Ong9c3S0GcKGiMaDfp1Y1/Z4Q+xu1//B4f6WGSX305W93vf396QAOe4tiWdusPYlADobWNgKL/Utf4ULG/dMchbPQ83ab1+ZdbfE4y8K6lFPsBpiYKcqKLQuhvEhiEMFb7NYtQ== hadoop@bigdata-ops-control01.xg01' > /home/$1/.ssh/authorized_keys"
    sudo sh -c "echo 'hadoop          ALL=(ALL)       NOPASSWD: ALL' > /etc/sudoers.d/07_hadoop"

    sudo chmod -R 700 /home/$1/.ssh
    sudo chown -R $1:$1 /home/$1/.ssh

}

# shellcheck disable=SC2112
function gmondinstall() {
    if ! mysql -V &>/dev/null; then
        sudo yum install mysql -y &>/dev/null
    fi
    sudo yum --enablerepo=didi_hadoop install libganglia ganglia-gmond ganglia-gmond-python MySQL-python -y && sudo /usr/sbin/groupadd -r ganglia -f && sudo /usr/sbin/useradd -s /sbin/nologin -r -d /var/lib/ganglia -g ganglia ganglia

    if [ -f /tmp/gmond.conf ]; then
        sudo rm /tmp/gmond.conf
    fi

    wget -q http://10.88.129.68/data/ganglia/gmond.conf -O /tmp/gmond.conf
    if [ "$cloneIp" != "" ]; then
        ip=$cloneIp
    else
        ip=$(hostname -i)
    fi

    info=$(mysql -h10.83.99.4 -usre_read -pSRE+ysqtlmy@308 -Dcmdb_asset -e "select a.node, b.host, b.port from machine_odin as a, gmond_odin_master as b where a.ip = '$ip' and a.children_id = b.children_id " | grep -v '\-\-\-\-\-' | tail -n 1)
    clustername=$(echo $info | awk '{print $1}')
    host=$(echo $info | awk '{print $2}')
    port=$(echo $info | awk '{print $3}')
    location=$(echo $ip | awk -F"." '{print $1"."$2"."$3}')
    if [ "$host" == "" -o "$port" == "" ]; then
        echo $info
        echo "host: $host, port: $port"
        echo "error"
        exit
    fi
    sed -i -e "s/ClusterName/$clustername/g" -e "s/Location/$location/g" -e "s/HostName/$host/g" -e "s/HnPort/$port/g" /tmp/gmond.conf
    test -d /etc/ganglia || sudo mkdir /etc/ganglia
    sudo cp /etc/ganglia/gmond.conf /etc/ganglia/gmond.conf.$(date +%Y%m%d)
    sudo mv /tmp/gmond.conf /etc/ganglia/gmond.conf
    sudo service gmond restart

}

# shellcheck disable=SC2112
function exesudo() {
    #
    # I use underscores to remember it's been passed
    local _funcname_="$1"

    local params=("$@")              ## array containing all params passed here
    local tmpfile="/dev/shm/$RANDOM" ## temporary file
    local filecontent                ## content of the temporary file
    local regex                      ## regular expression
    local func                       ## function source

    #
    # Shift the first param (which is the name of the function)
    unset params[0] ## remove first element
    # params=( "${params[@]}" )     ## repack array

    #
    # WORKING ON THE TEMPORARY FILE:
    # ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    content="#!/bin/bash\n\n"

    #
    # Write the params array
    content="${content}params=(\n"
    regex="\s+"
    for param in "${params[@]}"; do
        if [[ "$param" =~ $regex ]]; then
            content="${content}\t\"${param}\"\n"
        else
            content="${content}\t${param}\n"
        fi
    done

    content="$content)\n"
    echo -e "$content" >"$tmpfile"

    #
    # Append the function source
    echo "#$(type "$_funcname_")" >>"$tmpfile"

    #
    # Append the call to the function
    echo -e "\n$_funcname_ \"\${params[@]}\"\n" >>"$tmpfile"

    #
    # DONE: EXECUTE THE TEMPORARY FILE WITH SUDO
    # ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    sudo bash "$tmpfile"
    rm "$tmpfile"
}

# modify ulimit
# shellcheck disable=SC2112
function modify_ulimit() {
    echo "Start modify ulimit."

    #################修改ulimit############
    sed -i 's/\(^\*.*$\)/#\1/' /etc/security/limits.d/[0-9][0-9]-nproc.conf

    test -f /etc/security/limits.conf && cp /etc/security/limits.conf /etc/security/limits_bak_$(date +%F_%H-%M-%S)
    cat >/etc/security/limits.conf <<EOF
# /etc/security/limits.conf
#
#Each line describes a limit for a user in the form:
#
#<domain>        <type>  <item>  <value>
#
#Where:
#<domain> can be:
#        - an user name
#        - a group name, with @group syntax
#        - the wildcard *, for default entry
#        - the wildcard %, can be also used with %group syntax,
#                 for maxlogin limit
#
#<type> can have the two values:
#        - "soft" for enforcing the soft limits
#        - "hard" for enforcing hard limits
#
#<item> can be one of the following:
#        - core - limits the core file size (KB)
#        - data - max data size (KB)
#        - fsize - maximum filesize (KB)
#        - memlock - max locked-in-memory address space (KB)
#        - nofile - max number of open file descriptors
#        - rss - max resident set size (KB)
#        - stack - max stack size (KB)
#        - cpu - max CPU time (MIN)
#        - nproc - max number of processes
#        - as - address space limit (KB)
#        - maxlogins - max number of logins for this user
#        - maxsyslogins - max number of logins on the system
#        - priority - the priority to run user process with
#        - locks - max number of file locks the user can hold
#        - sigpending - max number of pending signals
#        - msgqueue - max memory used by POSIX message queues (bytes)
#        - nice - max nice priority allowed to raise to values: [-20, 19]
#        - rtprio - max realtime priority
#
#<domain>      <type>  <item>         <value>
#
#*               soft    core            0
#*               hard    rss             10000
#@student        hard    nproc           20
#@faculty        soft    nproc           20
#@faculty        hard    nproc           50
#ftp             hard    nproc           0
#@student        -       maxlogins       4

# End of file
*          hard    nproc           100000
*          soft    nproc           100000
*          soft    nofile          100000
*          hard    nofile          100000
*          soft    stack           102400
*          hard    stack           102400
*          soft    sigpending      600000
*          hard    sigpending      600000
*          soft    core            100000
*          hard    core            100000
EOF
}

# modify sysctl
# shellcheck disable=SC2112
# 主要是内核当中的sysctl的网络连接部分的配置的修改
function modify_sysctl() {
    echo "Start modify sysctl."

    grep -q 'vm.swappiness=1' /etc/sysctl.conf || sudo su - root -c "echo 'vm.swappiness=1' >> /etc/sysctl.conf"
    grep -q 'vm.max_map_count=262144' /etc/sysctl.conf || sudo su - root -c "echo 'vm.max_map_count=262144' >> /etc/sysctl.conf"

    sudo su - root -c "/sbin/sysctl -p &>/dev/null"

    return 0
}

# shellcheck disable=SC2112
function test_java_version() {
    ret=$(find /usr/local -maxdepth 1 -type d -regextype posix-egrep -regex ".*(jdk2|jdk1.8|jdk1.9).*")
    jpath=$(echo $ret | awk '{print $1}')

    if [ -d "${jpath}" ]; then
        export JAVA_HOME="${jpath}"
        export PATH=$JAVA_HOME/bin:$PATH

        return 1
    fi

    command -v java >/dev/null 2>&1

    if [ $? -eq 0 ]; then
        ##检查java版本
        JVERSION=$(java -version 2>&1 | awk 'NR==1{gsub(/"/,"");print $3}')
        major=$(echo $JVERSION | awk -F. '{print $1}')
        mijor=$(echo $JVERSION | awk -F. '{print $2}')
        if [ $major -le 1 ] && [ $mijor -lt 8 ]; then
            return 0
        else
            return 1
        fi
    else
        return 0
    fi
}

# shellcheck disable=SC2112
function check_java_version() {
    ret=$(find /usr/local -maxdepth 1 -type d -regextype posix-egrep -regex ".*(jdk2|jdk1.8|jdk1.9).*")
    jpath=$(echo $ret | awk '{print $1}')

    if [ -d "${jpath}" ]; then
        export JAVA_HOME="${jpath}"
        export PATH=$JAVA_HOME/bin:$PATH
    fi

    command -v java >/dev/null 2>&1

    if [ $? -eq 0 ]; then
        ##检查java版本
        JVERSION=$(java -version 2>&1 | awk 'NR==1{gsub(/"/,"");print $3}')
        major=$(echo $JVERSION | awk -F. '{print $1}')
        mijor=$(echo $JVERSION | awk -F. '{print $2}')
        number=$(echo $JVERSION | awk -F_ '{print $2}')
        if [ $major -le 1 ] && [ $mijor -lt 8 ]; then
            return 0
        elif [ $number -lt 92 ] && [ $mijor -eq 8 ]; then

            return 0
        else
            return 1
        fi
    else
        return 0
    fi
}

# shellcheck disable=SC2112
function prepare_dir() {
    # 创建/home/xiaoju目录 如果不存在的话就创建 -p
    sudo su - root -c "mkdir -p /home/xiaoju"
    check_result $? "mkdir -p /home/xiaoju failed."
    sudo su - root -c "chmod 777 /home/xiaoju"

    sudo su - root -c "grep -qw ${g_DeployGroup} /etc/group || groupadd ${g_DeployGroup} -g 2000"
    sudo su - root -c "id ${g_DeployUser} || useradd ${g_DeployUser} -u 2000 -g 2000"
    # 增加这一行的阻断
    check_result $? "准备文件出错"

    ## check and prepare dir of (/data2 or /data3)
    if [ ! -d /data2 ] || [ ! -d /data3 ]; then
        data_dir=$(df | grep -v Used | sort -k 2 -n | tail -n 1 | awk '{print $NF}')
        if [ ! -d ${data_dir} ]; then
            echo "get max data dir: ${data_dir} failed."
            exit 1
        fi

        # 递归创建对应的目录
        sudo su - root -c "mkdir -p ${data_dir}/data2 && mkdir -p ${data_dir}/data3"
        check_result $? "mkdir ${data_dir}/{data2,data3} failed."

        sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployGroup} ${data_dir}/data2"
        sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployGroup} ${data_dir}/data3"

        if [ ! -d /data2 ]; then
            sudo su - root -c "ln -s ${data_dir}/data2 /data2"
        fi

        if [ ! -d /data3 ]; then
            sudo su - root -c "ln -s ${data_dir}/data3 /data3"
        fi

        if [ ! -d /data2 ] || [ ! -d /data3 ]; then
            echo "prepare /data2,/data3 failed."
            exit 1
        fi
    fi

    sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployGroup} /data2"
    sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployGroup} /data3"
}

## install jdk 8
# shellcheck disable=SC2112
function install_jdk() {
    echo "Start install jdk."

    test_java_version
    if [ "X0" == "X$?" ]; then
        if [ -f ${jdk_rpm} ]; then
            sudo su - -c "rpm -ivh ${jdk_rpm} -y"
            check_result $? "安装jdk失败"
        else
            sudo su - -c "yum install ${open_jdk} -y"
            check_result $? "安装jdk失败"
        fi
    else
        echo "已安装jdk1.8版本"
    fi
}

## install jdk 11
# shellcheck disable=SC2112
function install_jdk11() {
    echo "Start install jdk11."

    if [ "X${g_EsVersion}" == "Xelasticsearch-2.3.3" ]; then
        return
    fi

    if [ ! -d /usr/local/jdk-11.0.2 ]; then
        sudo su - root -c "curl -SL https://n9e-server.s3.didiyunapi.com/openjdk-11.0.2_linux-x64_bin.tar.gz | tar -zxC /home/xiaoju"
        check_result $? "install jdk-11.0.2 failed."
    else
        echo "已安装jdk1.11版本"
    fi
}

## init bashrc
# shellcheck disable=SC2112
function init_bashrc() {
    home_dir=/home/${g_DeployUser}
    es_heap_size=${g_EsHeapSize}

    cat ${home_dir}/.bashrc | grep ES_HEAP_SIZE || sudo su - ${g_DeployUser} -c "{
    echo \"export ES_HEAP_SIZE=${es_heap_size}g\" >> ${home_dir}/.bashrc
    echo \"alias el='cd /data2/elasticsearch/logs/'\" >> ${home_dir}/.bashrc
    echo \"alias es='cd /data2/elasticsearch/bin;./elasticsearch -d' \" >> ${home_dir}/.bashrc
    echo \"alias ep='ps -ef|grep elasticsearch|grep -v grep'\" >> ${home_dir}/.bashrc
    echo \"export ES_GC_LOG_FILE=/data2/elasticsearch/logs/gc.log\" >> ${home_dir}/.bashrc
}"

    if [ -d /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64 ]; then
        cat ${home_dir}/.bashrc | grep "java-1.8.0-openjdk" || sudo su - ${g_DeployUser} -c "{
     echo \"export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64\" >> ${home_dir}/.bashrc

 }"
    fi
    if [ -d /usr/local/jdk-11.0.2 ]; then
        sudo su - ${g_DeployUser} -c "sed -i '/JAVA_HOME/d' ${home_dir}/.bashrc"
        sudo su - ${g_DeployUser} -c "{
     echo \"export JAVA_HOME=/usr/local/jdk-11.0.2\" >> ${home_dir}/.bashrc

 }"

    fi

    #     if [ -d /usr/local/jdk-11.0.2 ]; then
    #     cat ${home_dir}/.bashrc | grep "jdk-11" || sudo su - ${g_DeployUser} -c "{
    #     echo \"export JAVA_HOME=/usr/local/jdk-11.0.2\" >> ${home_dir}/.bashrc
    # }"
    # fi

}

function join_by() {
    local IFS="$1"
    shift
    echo "$*"
}

######### prepare env for es
# shellcheck disable=SC2112
function prepare_env_es() {
    prepare_dir
    install_jdk
    install_jdk11
    exesudo modify_ulimit
    modify_sysctl
    init_bashrc
    initKernelPatch
    user_add hadoop
    #这个地点要注意：原来就被注释掉了gmondinstall
    #这一步执行出现了问题
    hadoopinstall
    #这一步没有问题
    filebeatinstall
}

# shellcheck disable=SC2112
function filebeatinstall() {
    if [ ! -d /data2/filebeat-7.6.2-linux-x86_64/ ]; then
        sudo su - ${g_DeployUser} -c "curl -SL http://didiyum.sys.xiaojukeji.com/didiyum/didi/didi_hadoop/7/x86_64/filebeat-7.6.2-linux-x86_64.tar.gz | tar -zxC /data2/ "
        check_result $? "install filebeat failed."
    fi
}

# shellcheck disable=SC2112
function hadoopinstall() {
    if [ "x$HOSTNAME" == "xus01" ]; then
        sudo su - -c "yum-complete-transaction --cleanup-only"
        sudo yum install -y --enablerepo=didi_hadoop didi-hive-us
    else
        sudo su - -c "yum-complete-transaction --cleanup-only"
        sudo yum install -y --enablerepo=didi_hadoop didi-hive-nmg
    fi
}

### TODO: 解决对机器名的依赖，这里根据主机名来计算rack信息；
# shellcheck disable=SC2112
function gen_rack() {
    hostname=$(hostname)
    number=$(echo ${hostname} | awk -F. '{print $1}' | grep -oP "\d+$")
    rack=0

    if [ "_" == "${number}_" ]; then
        echo "modify rack num failed."
        exit 1
    else
        rack=$(expr ${number} / 2 + 1)
    fi

    if [[ "$hostname" =~ .*ceph.* ]]; then
        echo "c${rack}"
        ret=$(sudo su - root -c "cat ${config_file} | grep 'node.attr.set'")
        if [ x$? != "x0" ]; then
            sudo su - -c "sed -i '\$a\node.attr.set: cold' ${config_file}"
        fi
    else
        echo "r${rack}"
    fi
}

# shellcheck disable=SC2112
function setup_rack() {
    rack=$(gen_rack)
    if [ ! -f ${config_file} ]; then
        echo "ERR: ${config_file} is not existed."
        exit 1
    fi

    # 不支持elasticsearch-2.3.3
    ret=$(sudo su - root -c "cat ${config_file} | grep 'node.attr.rack'")
    if [ x$? == "x0" ]; then
        sudo su - root -c "sed -i 's/node.attr.rack:.*/node.attr.rack: ${rack}/g' ${config_file}"
    else
        sudo su - root -c "sed -i '\$a\node.attr.rack: ${rack}' ${config_file}"
    fi
    check_result $? "setup_rack failed."
}

setup_elastic_config_v1() {
    if [ ! -f ${config_file} ]; then
        echo "${config_file} is not existed."
        exit 1
    fi

    if [ "x$g_ClusterName" != "x" ]; then
        sed -i "s/cluster.name:.*/cluster.name: $g_ClusterName/g" ${config_file}
    else
        echo "parameter g_ClusterName must not null."
        exit 1
    fi

    # node role init
    echo "setup config file for $g_Role"
    g_Hosts=""
    sed_data_dir="\\"$data_dir
    datanode_dir=""
    for i in ${data_dir}; do
        if [ "x$g_Role" == "xdatanode" ]; then
            datanode_dir=$datanode_dir"\\"$i"\/es"","
            if [ ! -d ${i}/es ]; then
                sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployUser} ${i}"
                sudo su - $g_DeployUser -c "mkdir -p ${i}/es"
            fi
        fi
    done
    ret=$(cat ${config_file} | grep 'MASTERNODES')
    if [ x$? == "x0" ]; then
        if [ "x${g_MasterNodes}" != "x" ]; then
            IFS=',' read -r -a array <<<"${g_MasterNodes}"
            for node in ${array[*]}; do
                g_Hosts=${g_Hosts}"\"${node}:9300\","
                if [[ "x$g_Action" == "xexpand" || "x$g_Action" == "xdeploy" ]]; then
                    checkport=$(nc -v -w5 ${node} 9300 </dev/null)
                    if [ x$? != "x0" ]; then
                        echo "MASTERNODE has not active."
                        exit 1
                    fi
                fi
            done
            ret=$(cat ${config_file} | grep 'discovery.seed_hosts')
            if [ x$? == "x0" ]; then
                sed -i "s/discovery.seed_hosts:.*/discovery.seed_hosts: [ ${g_Hosts%?} ]/g" ${config_file}
            else
                sed -i "s/discovery.zen.ping.unicast.hosts:.*/discovery.zen.ping.unicast.hosts: [ ${g_Hosts%?} ]/g" ${config_file}
            fi

        else
            echo "ENV MASTERNODES must not null."
            exit 1
        fi
    fi
    if [ "x$g_Role" == "xdatanode" ]; then
        sed -i "s/path.data:.*/path.data: ${datanode_dir%?} /g" ${config_file}
        setup_rack
    fi

}

# shellcheck disable=SC2112
function refreshconfig() {

    sudo su - root -c "touch $config_file_tmp"
    sudo su - root -c "touch $config_jvm_file_tmp"
    sudo su - root -c "touch $config_filebeats_tmp"

    DATE=$(date +%Y%m%d%H%M)
    # 主机名字带有ceph会有特殊操作
    if [[ "$HOSTNAME" =~ .*ceph.* ]]; then
        response=$(curl --write-out %{http_code} --silent --output $config_file_tmp "${ecm_url}?cluster_name=${g_ClusterName}&engin_name=datanodeCeph&type_name=elasticsearch.yml&config_action=${g_ConfigAction}" --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")
        response_jvm=$(curl --write-out %{http_code} --silent --output $config_jvm_file_tmp "${ecm_url}?cluster_name=${g_ClusterName}&engin_name=datanodeCeph&type_name=jvm.options&config_action=${g_ConfigAction}" --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")
    else
        # --write-out %{http_code} 把http请求状态码打印控制台，请求admin项目下载用户自定义的ES配置文件，下载到 config_file_tmp，要记得带上cookie
        response=$(curl --write-out %{http_code} --silent --output $config_file_tmp "${ecm_url}?cluster_name=${g_ClusterName}&engin_name=${g_Role}&type_name=elasticsearch.yml&config_action=${g_ConfigAction}" --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")
        # 请求admin项目下载用户自定义的JVM配置文件，下载到 config_jvm_file_tmp，要记得带上cookie
        response_jvm=$(curl --write-out %{http_code} --silent --output $config_jvm_file_tmp "${ecm_url}?cluster_name=${g_ClusterName}&engin_name=${g_Role}&type_name=jvm.options&config_action=${g_ConfigAction}" --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")
    fi
    # 心跳的配置文件
    response_filebeats=$(curl --write-out %{http_code} --silent --output $config_filebeats_tmp "${ecm_url}?cluster_name=${g_ClusterName}&engin_name=${g_Role}&type_name=filebeat.yml&config_action=${g_ConfigAction}" --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")

    echo "${response_filebeats}"
    echo "${response_jvm}"
    echo "${response}"

    if [ $response_filebeats -eq 200 ] && [ -s $config_filebeats_tmp ]; then
        # 保存历史配置文件
        mv $config_filebeats ${config_filebeats}${DATE}.bak
        # 用户自定义的配置文件
        mv $config_filebeats_tmp $config_filebeats
        sudo su - ${g_DeployUser} -c "cd /data2/filebeat-7.6.2-linux-x86_64/ && nohup ./filebeat -e -c ${config_filebeats} > filebeat.log 2>&1 &"
        check_result $? "setup elasticsearch config failed."
    fi

    if [ $response_jvm -eq 200 ] && [ -s $config_jvm_file_tmp ]; then
        # 保存历史配置文件
        mv $config_jvm_file ${config_jvm_file}${DATE}.bak
        # 用户自定义的配置文件
        mv $config_jvm_file_tmp $config_jvm_file
    fi

    # 判断配置文件中是否有必要信息而非乱码
    ret=$(sudo su - root -c "cat ${config_file} | grep 'cluster.name'")
    # 对于sed的文本编辑操作需要切换到root权限，否则会出现权限不足的问题:Permission denied
    if [ x$? == "x0" ] && [ $response -eq 200 ] && [ -s $config_file_tmp ]; then
        echo "使用用户自定义配置文件"
        # 保存历史配置文件
        mv $config_file ${config_file}${DATE}.bak
        # 用户自定义的配置文件
        mv $config_file_tmp $config_file
        setup_elastic_config_v1
        check_result $? "setup elasticsearch config failed."
    elif [ $response -ne 200 ] || [ ! -s $config_file_tmp ]; then
        echo "使用默认配置文件"
        # 如果，请求下载用户自定义配置文件失败 || 下载文件内容为空，就用默认配置
        setup_elastic_config
        check_result $? "setup elasticsearch config failed."
    fi

    setup_log4j

}

setup_elastic_config() {
    if [ ! -f ${config_file} ]; then
        echo "${config_file} is not existed."
        exit 1
    fi

    if [ "x$g_ClusterName" != "x" ]; then
        # 设置集群名字
        sudo su - -c "sed -i 's/cluster.name:.*/cluster.name: $g_ClusterName/g' ${config_file}"
    else
        echo "parameter g_ClusterName must not null."
        exit 1
    fi

    # 设置节点角色
    echo "setup config file for $g_Role"
    g_Hosts=""
    sed_data_dir="\\"$data_dir
    datanode_dir=""
    for i in ${data_dir}; do
        if [ "x$g_Role" == "xdatanode" ]; then
            datanode_dir=$datanode_dir"\\"$i"\/es"","
            if [ ! -d ${i}/es ]; then
                sudo su - root -c "chown -R ${g_DeployUser}:${g_DeployUser} ${i}"
                sudo su - $g_DeployUser -c "mkdir -p ${i}/es"
            fi
        fi
    done

    if [ "x${datanode_dir}" == "x" ]; then
        datanode_dir="/data3/es"
    fi

    if [ "x${g_MasterNodes}" != "x" ]; then
        IFS=',' read -r -a array <<<"${g_MasterNodes}"
        for node in ${array[*]}; do
            g_Hosts=${g_Hosts}"\"${node}:9300\","
        done
        # 检测配置项中是否存在discovery.seed_hosts
        ret=$(sudo su - root -c "cat ${config_file} | grep 'discovery.seed_hosts'")
        # 对于sed的文本编辑操作需要切换到root权限，否则会出现权限不足的问题:Permission denied
        if [ x$? == "x0" ]; then
            sudo su - -c "sed -i 's/discovery.seed_hosts:.*/discovery.seed_hosts: [ ${g_Hosts%?} ]/g' ${config_file}"
        else
            sudo su - -c "sed -i 's/discovery.zen.ping.unicast.hosts:.*/discovery.zen.ping.unicast.hosts: [ ${g_Hosts%?} ]/g' ${config_file}"
        fi

    else

        echo "ENV MASTERNODES must not null."
        exit 1
    fi

    if [ "x$g_Role" == "xmasternode" ]; then
        sudo su - -c "sed -i 's/node.name:.*/node.name: $(hostname)/g' ${config_file}"
        sudo su - -c "sed -i 's/node.master:.*/node.master: true/g' ${config_file}"
        sudo su - -c "sed -i 's/node.data:.*/node.data: false/g' ${config_file}"                      ## XXX
        sudo su - -c "sed -i 's/http.enabled:.*/http.enabled: false/g' ${config_file}"                   ## XXX
        # 数据和日志注意放同一个文件夹
        sudo su - -c "sed -i 's/path.data:.*/path.data: \/data3\/${g_Role} /g' ${config_file}"           ## XXX
        sudo su - -c "sed -i 's/transport.tcp.port:.*/transport.tcp.port: 9300/g' ${config_file}"        ## XXX

        sudo su - -c "sed -i 's/processors:.*/processors: 4/g' ${config_file}"                           ## XXX
        sudo su - -c "sed -i 's/thread_pool.search.size:.*/thread_pool.search.size: 4/g' ${config_file}" ## XXX
        sudo su - -c "sed -i 's/thread_pool.write.size:.*/thread_pool.write.size: 3/g' ${config_file}"   ## XXX
        echo ${g_Port}
        sudo su - -c "sed -i 's/http.port:.*/http.port: ${g_Port}/g' ${config_file}"
    elif [ "x$g_Role" == "xdatanode" ]; then
        sudo su - -c "sed -i 's/node.name:.*/node.name: $(hostname)/g' ${config_file}"
        sudo su - -c "sed -i 's/node.master:.*/node.master: false/g' ${config_file}"
        sudo su - -c "sed -i 's/node.data:.*/node.data: true/g' ${config_file}"
        sudo su - -c "sed -i 's/path.data:.*/path.data: ${datanode_dir%?} /g' ${config_file}"

        ret=$(sudo su - -c "cat ${config_file} | grep 'http.host'")
        if [ x$? == "x0" ]; then
            sudo su - -c "sed -i 's/http.enabled:.*/http.enabled: true/g' ${config_file}"
            sudo su - -c "sed -i 's/http.host:.*/http.host: 127.0.0.1/g' ${config_file}"
        else
            sudo su - -c "sed -i 's/http.enabled:.*/http.enabled: false/g' ${config_file}"
        fi

        sudo su - -c "sed -i "s/node.max_local_storage_nodes:.*/node.max_local_storage_nodes: ${g_PidCount}/g" ${config_file}" ## XXX
        sudo su - -c "sed -i "/transport.tcp.port:.*/d" ${config_file}"                                                        ## XXX  否则无法起多个实例

        sudo su - -c "sed -i 's/processors:.*/processors: 16/g' ${config_file}"                           ## XXX
        sudo su - -c "sed -i 's/thread_pool.search.size:.*/thread_pool.search.size: 16/g' ${config_file}" ## XXX
        sudo su - -c "sed -i 's/thread_pool.write.size:.*/thread_pool.write.size: 12/g' ${config_file}"   ## XXX
        sudo su - -c "sed -i 's/http.port:.*/http.port: ${g_Port}/g' ${config_file}"
    elif [ "x$g_Role" == "xclientnode" ]; then
        sudo su - -c "sed -i 's/node.name:.*/node.name: $(hostname)/g' ${config_file}"
        sudo su - -c "sed -i 's/node.master:.*/node.master: false/g' ${config_file}"
        sudo su - -c "sed -i 's/node.data:.*/node.data: false/g' ${config_file}"
        sudo su - -c "sed -i 's/http.enabled:.*/http.enabled: true/g' ${config_file}"
        # 数据和日志注意放同一个文件夹
        sudo su - -c "sed -i "s/path.data:.*/path.data: \/data3\/${g_Role} /g" ${config_file}"            ## XXX
        sudo su - -c "sed -i "s/transport.tcp.port:.*/transport.tcp.port: 9300/g" ${config_file}"         ## XXX

        sudo su - -c "sed -i 's/processors:.*/processors: 12/g' ${config_file}"                           ## XXX
        sudo su - -c "sed -i 's/thread_pool.search.size:.*/thread_pool.search.size: 12/g' ${config_file}" ## XXX
        sudo su - -c "sed -i 's/thread_pool.write.size:.*/thread_pool.write.size: 9/g' ${config_file}"    ## XXX
        sudo su - -c "sed -i 's/http.port:.*/http.port: ${g_Port}/g' ${config_file}"
    else
        echo "Bad Node role: $g_Role"
        exit 1
    fi

    if [ "x$g_Role" == "xdatanode" ]; then
        setup_rack
    fi

}

########## update log4j config for report es log to kafka.
# shellcheck disable=SC2112
# 这一块的逻辑暂时没有修改过
function setup_log4j() {
    hostname=$(hostname)
    dir=$(dirname ${config_file})
    file_path=$dir"/log4j2.properties"
    location=$(echo ${hostname} | awk -F. '{print $NF}')

    if [ -f ${file_path} ] && [ ${location} == "us01" ]; then
        sed -i 's/appender.kafka.topic.*/appender.kafka.topic = us01_elasticsearch_log_topic/g' $file_path
        sed -i 's/appender.kafka.property1.value.*/appender.kafka.property1.value = 10.14.128.13:30357/g' $file_path
        sed -i 's/appender.kafka.property4.value.*/appender.kafka.property4.value = org.apache.kafka.common.security.plain.PlainLoginModule required username="44.appId_000128" password="LzHybFh21kJU";/g' $file_path
        sed -i 's/appender.kafkaSlowlog.topic.*/appender.kafkaSlowlog.topic = us01_elasticsearch_slowlog_topic/g' $file_path
        sed -i 's/appender.kafkaSlowlog.property1.value.*/appender.kafkaSlowlog.property1.value = 10.14.128.13:30357/g' $file_path
        sed -i 's/appender.kafkaSlowlog.property4.value.*/appender.kafkaSlowlog.property4.value = org.apache.kafka.common.security.plain.PlainLoginModule required username="44.appId_000128" password="LzHybFh21kJU";/g' $file_path
    fi
}

######### install es package
# shellcheck disable=SC2112
function install_es_package() {
    if [[ "x$g_Action" == "xexpand" || "x$g_Action" == "xnew" ]]; then
        prepare_env_es
    fi

    modify_sysctl

    # g_DeployPath /data2/clientnode、/data2/masternode、/data2/
    sudo su - ${g_DeployUser} -c "mkdir -p ${g_DeployPath}"
    check_result $? "mdkir ${g_DeployPath} failed."

    # 如果是扩容，同时部署路径已经存在，跳过部署，不然会影响已经启动的服务；
    if [ "x$g_Action" == "xexpand" ]; then
        if [ -d "${g_DeployPath}/elasticsearch" ]; then
            return
        fi
    fi

    # 拼接下载路径，g_Role masternode、clientnode、kong，g_EsVersion ES版本号
    down_path="/home/xiaoju/${g_Role}/${g_EsVersion}"
    if [ X"${g_Role}" == "Xdatanode" ]; then
        down_path="/home/xiaoju/${g_EsVersion}"
    fi

    # 递归创建目录（如果目录存在，则不创建）
    sudo su - ${g_DeployUser} -c "mkdir -p ${down_path}/elasticsearch"
    check_result $? "mdkir ${down_path} failed."

    # 创建ES日志保存文件夹
    sudo su - ${g_DeployUser} -c "mkdir -p /data3/${g_Role}-logs"
    check_result $? "mdkir /data3/logs failed."

    echo ${g_PackageUrl}
    echo ${down_path}
    # 判断该版本的ES是否已经下载
    if [ ! -d "${down_path}/elasticsearch/config" ]; then
        sudo su - root -c "curl -SL '${g_PackageUrl}' | tar --strip-components 1 -zxC ${down_path}/elasticsearch"
        check_result $? "install es to ${down_path} from ${g_PackageUrl} failed.测试测试测试"
        sudo su - root -c "chmod -R 777 ${down_path}/elasticsearch"
    fi

    # 删除老的软连接，g_DeployPath 即为 /data2/masternode、/data2/clientnode、/data2
    if [ -d "${g_DeployPath}/elasticsearch" ]; then
        sudo su - ${g_DeployUser} -c "rm -rf ${g_DeployPath}/elasticsearch"
    fi

    # /data2/masternode、/data2/clientnode、/data2 创建软链接，指向ES下载的源文件路径，/data3创建软链接，指向ES下载的源文件日志路径（data3也保存数据）
    sudo su - root -c "ln -nfs ${down_path}/elasticsearch ${g_DeployPath}/elasticsearch && ln -nfs /data3/${g_Role}-logs ${down_path}/elasticsearch/logs"
    check_result $? "install es for move to ${g_DeployPath} failed."

    echo "环境部署已经完成，后续等待配置刷新操作"
    # 更新配置文件
    refreshconfig
    check_result $? "setup elasticsearch config failed."

    echo "配置已经刷新，等待后续日志文件启动操作"
    setup_log4j
    check_result $? "setup log4j config failed."
}

######### 集群节点启动成功之后向admin发送post请求来更新数据
function get_ip() {
  echo ${g_port}
  local_ip=$(ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v 172|grep -v inet6|awk '{print $2}'|tr -d "addr:")
  g_Ip_Port="${local_ip}:${g_Port}"
}

# 向admin发送请求更新ip地址，其中请求体中的内容需要包含ip:port 集群名称 角色名称
function send_ip() {
    # 调试点打印详细的信息
    echo "${g_ClusterName}"
    echo "${g_Role}"
    echo "${g_Ip_Port}"
    response=$(curl --write-out %{http_code} --silent -H "Content-Type:application/json" -d "{\"clusterPhyName\":\"${g_ClusterName}\",\"role\":\"${g_Role}\",\"httpAddress\":\"${g_Ip_Port}\"}" ${ecm_cluster_url} --cookie "domainAccount=admin; Authorization=MTphekFXaUpoeGtobzMzYWM=")
    if [ $response -ne 200 ]; then
       stop_es
       exit 1
    fi

}


######### start es process
# shellcheck disable=SC2112
function start_es() {

    deploy_count=${g_PidCount}
    curdir="${g_DeployPath}/elasticsearch"
    deploy_dir="${curdir}"

    sudo su - ${g_DeployUser} -c "mkdir -p ${deploy_dir}/logs"
    check_result $? "start es mkdir ${deploy_dir}/logs failed."
    # shellcheck disable=SC2039
    if [ "x$g_Role" == "xdatanode" ]; then
        sudo su - ${g_DeployUser} -c "rm /data*/es/nodes/*/_state/segments*"
        sudo su - ${g_DeployUser} -c "rm /data*/es/nodes/*/_state/manifest*"
    fi

    # if [[ -f ${deploy_dir}/config/jvm.options ]];then
    #     sed -i "s/-Xms.*/-Xms${g_EsHeapSize}g/g" ${deploy_dir}/config/jvm.options
    #     sed -i "s/-Xmx.*/-Xmx${g_EsHeapSize}g/g" ${deploy_dir}/config/jvm.options
    # fi

    count=0
    #### 如果是扩容，就不影响已经启动的混部实例
    if [ "x$g_Action" == "xexpand" ]; then
        count=$(ps aux | grep -i "bootstrap.Elasticsearch" | grep ${deploy_dir} | grep -v grep | wc -l)
    fi

    # 以同样的角色在一个机器上可以启动不同数目的节点实例
    for ((i = ${count}; i < ${deploy_count}; ++i)); do
        echo $i
        if [ -d "${deploy_dir}" ]; then
            sudo su - ${g_DeployUser} -c "cd ${deploy_dir} && sh control.sh start"
            check_result $? "start es failed."
            check_es_pid ${deploy_dir}
            # 向admin发送post请求发送集群的名称 机器节点的角色 ip地址和port端口
            # 获取本机的ip和端口地址
#            get_ip
#            echo "${g_Ip_Port}"
#            echo "开始向admin发送http_post请求"
#            send_ip
            sleep 5s
        fi
    done

}

#### 检查进程是否启动
# shellcheck disable=SC2112
function check_es_pid() {
    curdir=$1
    num_pid=$(ps aux | grep -i "bootstrap.Elasticsearch" | grep "${curdir}" | grep -v "arius-gateway" | grep -v elasticsearch-trib | grep -v grep | wc -l)
    if [ ${num_pid} -lt 1 ]; then
        echo "ES start error"
        exit 1
    fi
}

# shellcheck disable=SC2112
function init_jdk11() {
    if [ -d /usr/local/jdk-11.0.2 ]; then
        home_dir=/home/${g_DeployUser}
        sudo su - ${g_DeployUser} -c "sed -i '/JAVA_HOME/d' ${home_dir}/.bashrc"
        cat ${home_dir}/.bashrc | grep "jdk-11" || sudo su - ${g_DeployUser} -c "{
     echo \"export JAVA_HOME=/usr/local/jdk-11.0.2\" >> ${home_dir}/.bashrc
 }"
        check_result $? "set java_home failed"
    fi
}
######### stop es process
# shellcheck disable=SC2112
function stop_es() {
    echo "stop es"
    curdir="${g_DeployPath}/elasticsearch"
    check_java_version
    if [ "X0" == "X$?" ]; then
        install_jdk11
        init_jdk11
    else
        echo "已安装jdk1.8.0_92版本"
    fi

    sudo su - ${g_DeployUser} -c "cd ${curdir} && sh control.sh stop"

    pids=$(ps aux | grep -i "bootstrap.Elasticsearch" | grep "${curdir}" | grep -v "arius-gateway" | grep -v elasticsearch-trib | grep -v grep | awk '{print $2}')
    #    if [ "x${pids}" == "x" ]; then
    #        echo "es stop failed"
    #        exit 1
    #    fi

    for pid in $pids; do
        echo $pid
        sudo su - ${g_DeployUser} -c "kill $pid"

        local timeout=30
        local i=0
        # 循环stop服务, 直至60s超时
        for ((i = 0; i < ${timeout}; i++)); do
            # 检查服务是否停止,如果停止则直接返回
            if ps -p $pid >/dev/null; then
                echo "$pid is running"
                sleep 2s
            else
                echo "${app} is stopped"
                break
            fi

            # 停止该服务
            if [ $i -ge $((timeout - 3)) ]; then
                #                sudo su - ${g_DeployUser} -c "kill -9 ${pid} &>/dev/null"
                sudo su - ${g_DeployUser} -c "kill -9 ${pid} &>/dev/null"
            else
                #                sudo su - ${g_DeployUser} -c "kill ${pid} &>/dev/null"
                sudo su - ${g_DeployUser} -c "kill ${pid} &>/dev/null"
            fi
        done

        if [ $i -ge ${timeout} ]; then
            echo "stop $pid failed."
            exit 1
        fi

        sleep 5s
    done
}

######### restart process
function restart_es() {
    stop_es
    start_es
}

# ===== shuo =====
######### 更新集群插件
######### 目前定的是插件的更新是针对于整个集群的，所以不应该是单独的某个节点做插件的更新操作，这一步如何操作呢，暂时定的是单个节点伴随着重启任务更新插件列表
refresh_plugin() {
    # 从 admin 第三方接口获取插件操作的信息
    get_plugin_info_from_admin
    echo "${plugin_operation_type}" >>"${zeus_log}"
    echo "${plugin_file_name}" >>"${zeus_log}"
    echo "${plugin_s3_url}" >>"${zeus_log}"

    if [ "${plugin_operation_type}" = "3" ]; then
        # 安装插件
        echo "从[${plugin_s3_url}]安装es插件[${plugin_file_name}]开始..." >>"${zeus_log}"
        install_es_plugin
        echo "从[${plugin_s3_url}]安装es插件[${plugin_file_name}]完成" >>"${zeus_log}"
    elif [ "${plugin_operation_type}" = "4" ]; then
        # 卸载插件
        echo "卸载es插件[${plugin_file_name}]开始..." >>"${zeus_log}"
        uninstall_es_plugin
        echo "卸载es插件[${plugin_file_name}]完成" >>"${zeus_log}"
    elif [ "${plugin_operation_type}" = "" ]; then
        # 没有插件操作
        echo "当前没有插件操作任务" >>"${zeus_log}"
    else
        # 非法操作
        echo "插件操作不合法" >>"${zeus_log}"
    fi
}

######### 从 admin 的第三方接口获取插件操作的信息
# 需要注意的是集群的重启删除更新的操作都是走的工单的形式，走工单的话就会在处理工单的时候向宙斯发送工单的任务请求
# 所以在宙斯分发执行脚本的时候，脚本之中就会调用admin的接口来获取相关的信息从而获取插件的信息
get_plugin_info_from_admin() {
    # 在这里详细的介绍一下curl的指令
    # curl是一个命令行工具，通过指定的URL来上传或下载数据，并将数据展示出来。curl中的c表示client，而URL，就是URL
    # 暂时先注掉
    response=$(curl --silent "${admin_plugin_url}?clusterName=${g_ClusterName}")
    # sed 参数s：替换文本,替换命令用替换模式替换指定模式
    plugin_operation_type=$(getJsonValuesByAwk "${response}" operationType "" | sed 's/\"//g')
    plugin_file_name=$(getJsonValuesByAwk "${response}" pluginFileName "" | sed 's/\"//g')
    plugin_s3_url=$(getJsonValuesByAwk "${response}" s3url "" | sed 's/\"//g')
}

######### 安装es插件
install_es_plugin() {
    es_plugin_root="${g_DeployPath}/elasticsearch/plugins"

    # 从 S3 下载插件并解压到该文件夹中
    # 解压的指令在linux当中zip需要使用unzip tar tar.gz需要使用tar
    #curl -SL --silent "${plugin_s3_url}" | tar -xC "${es_plugin_root}/${plugin_file_name}/"
    curl -SL --silent "${plugin_s3_url}" | tar -zxC "${es_plugin_root}/"
    # 注：这里暂时用cp代替下载，物理机暂时连接不到s3
    #cp -R "${g_DeployPath}/elasticsearch/${plugin_file_name}" "${es_plugin_root}/"

    # 检查解压结果
    check_result $? "install es plugin [${plugin_file_name}] to ${es_plugin_root} from ${plugin_s3_url} failed." "rm -rf ${plugin_file_name}"
}

######### 卸载es插件
uninstall_es_plugin() {
    es_plugin_root="${g_DeployPath}/elasticsearch/plugins"
    cd "${es_plugin_root}" || check_result $? "enter es plugin directory failed"
    rm -rf "${plugin_file_name}"
}

# 解析 json字符串
# 网上的代码，可以使用的是Awk工具和字符串的正则匹配来处理json串的信息
### 方法简要说明：
### 1. 是先查找一个字符串：带双引号的key。如果没找到，则直接返回defaultValue。
### 2. 查找最近的冒号，找到后认为值的部分开始了，直到在层数上等于0时找到这3个字符：,}]。
### 3. 如果有多个同名key，则依次全部打印（不论层级，只按出现顺序）
###
### 3 params: json, key, defaultValue
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
# ===== shuo =====

################################## main route #################################
# shellcheck disable=SC2145
echo "脚本参数: ${@}"
_parse_options "${@}" || _usage

# Linux df（英文全拼：disk free） 命令用于显示目前在 Linux 系统上的文件系统磁盘使用情况统计。
# grep是做一个抓取的动作 grep -v则是做排除性的抓取
# sort是做一个排序的操作 -k 2表示的是按照文本中的第二个域进行排序比较，-n表示展示的是全部的行
# awk AWK 是一种处理文本文件的语言，是一个强大的文本分析工具。 NF	一条记录的字段的数目
# echo >> 属于输出重定向的过程，可以将当前的字符串打印到对应文件的结尾，echo > 不会进行重定向，会直接清空掉原来的文件再进行输出字符串，相同点是如果原文件不存在，会首先进行创建
data2_flag=$(df | grep -v Used | sort -k 2 -n | grep data2 | awk '{print $NF}')
#g_home="/home/xiaoju"
############
if [[ X"${g_Role}" == "Xclientnode" || X"${g_Role}" == "Xmasternode" ]]; then
    # 对于目录的确认，这里会根据传入的不同的角色设置不同的集群部署的路径
    if [ ! -d "/home/xiaoju/${g_Role}" ] && [ X"${data2_flag}" != "X" ]; then
        if [ -d /data2/${g_Role} ]; then
            # 文件链接的命令 ln nfs网络文件系统 这里是生成一个文件的链接方便查询 主要就是做一些文件的转移
            sudo su - ${g_DeployUser} -c "mv /data2/${g_Role} /home/xiaoju/ && ln -nfs /home/xiaoju/${g_Role} /data2/${g_Role} && mv /home/xiaoju/${g_Role}/elasticsearch/logs /data3/ && ln -nfs /data3/logs  /home/xiaoju/${g_Role}/elasticsearch/logs"
            #      else
            #        sudo su - ${g_DeployUser} -c "mkdir -p /home/xiaoju/${g_Role} && ln -nfs /home/xiaoju/${g_Role} /data2/${g_Role}"
        fi

    fi
    # 说明是 clientnode 或 masternode 角色的ES
    g_DeployPath="/data2/${g_Role}"
else
    if [ ! -d "/home/xiaoju/elasticsearch" ] && [ X"${data2_flag}" != "X" ]; then
        if [ -d /data2/elasticsearch ]; then
            sudo su - ${g_DeployUser} -c "mv /data2/elasticsearch /home/xiaoju/ && ln -nfs /home/xiaoju/elasticsearch /data2/elasticsearch && mv /home/xiaoju/elasticsearch/logs /data3/ && ln -nfs /data3/logs  /home/xiaoju/elasticsearch/logs"
            #      else
            #        sudo su - ${g_DeployUser} -c "mkdir -p /home/xiaoju/elasticsearch && ln -nfs /home/xiaoju/elasticsearch /data2/elasticsearch"
        fi

    fi
    # 说明是 datanode 角色的ES
    g_DeployPath="/data2"
fi

# 给予data2权限，否则没有权限启动data2下的control.sh
sudo su - root -c "chmod -R 777 /data2"
config_file="${g_DeployPath}/elasticsearch/config/elasticsearch.yml"
config_jvm_file="${g_DeployPath}/elasticsearch/config/jvm.options"

# ES的备用配置文件
config_file_tmp="${g_DeployPath}/elasticsearch/config/elasticsearch.yml.tmp"
# JVM的备用配置文件
config_jvm_file_tmp="${g_DeployPath}/elasticsearch/config/jvm.options.tmp"

# 预发环境的admin项目
ecm_url="http://10.190.14.101:8080/admin/api/v3/thirdpart/cluster/config/file"

# 本次测试只是做s3和宙斯脚本的初步融合
# admin第三方接口，用于查询插件操作信息
admin_plugin_url="10.161.97.44:8888/admin/api/v3/thirdpart/plugin/info"

config_filebeats_tmp="/data2/filebeat-7.6.2-linux-x86_64/filebeat.yml.tmp"
config_filebeats="/data2/filebeat-7.6.2-linux-x86_64/filebeat.yml"

data_dir=$(df | grep -v Used | sort -k 2 -n | grep data | awk '{print $NF}')
# awk是一个强大的文本分析工具，相对于grep的查找，sed的编辑，awk在其对数据分析并生成报告时，显得尤为强大。简单来说awk就是把文件逐行的读入，以空格为默认分隔符将每行切片，切开的部分再进行各种分析处理。

# hostname会显示主机名称
HOSTNAME=$(hostname | awk -F. '{print $NF}')
#echo "$g_ClusterName $g_PackageUrl $g_EsVersion $g_Role $g_PidCount $g_Action"

case $g_Action in
"new")
    # 发布更新elasticsearch
    echo $g_Action
    # 停服务
    stop_es
    # 删除旧服务 安装服务
    install_es_package
    # 启动服务
    start_es
    ;;
"deploy")
    # 发布更新elasticsearch
    echo $g_Action
    # 停服务
    stop_es
    # 删除旧服务 安装服务
    install_es_package
    # 启动服务
    start_es
    ;;
"restart")
    # 重启单机elasticsearch
    echo $g_Action
    # 重启服务

    # 注：shuo：刷新配置功能有问题，暂时注掉
    refreshconfig

    # ===== shuo =====
    #refresh_plugin
    # ===== shuo =====

    restart_es
    ;;
"updateconfig")
    # 发布更新elasticsearch
    echo $g_Action
    # 删除旧服务 安装服务
    refreshconfig

    restart_es
    ;;
"expand")
    # 发布更新elasticsearch
    # shellcheck disable=SC2086
    echo $g_Action
    # 删除旧服务 安装服务
    install_es_package
    # 启动服务
    start_es
    ;;
"shrink")
    # 重启单机elasticsearch
    echo $g_Action
    # 停服务
    stop_es
    ;;
*)
    echo "unknown command"
    exit 1
    ;;
esac