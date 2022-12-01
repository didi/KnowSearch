#!/bin/bash

pid=$1
cpu_use=$2

if [ $# -lt 1 ] ; then
  echo "USAGE: $0 pid [cpu_use]"
  eche " e.g.: $0 23979"
  exit 1;
fi

if test -z "$cpu_use" ; then
  cpu_use=20
fi

grep_jstack()
{
  is_start="false"
  while read line
  do
    if echo "nid=0x"${line}|grep -q $1 ; then
      is_start="true"
    elif [[ "$is_start" = "true" ]] && [ -z "${line}" ]; then
      break;
    fi

    if [ "$is_start" = "true" ]; then
        echo ${line}
    fi

    done < /tmp/s-$pid
}

grep_all()
{
  while read line
    do
    if [ -n "${line}" ]; then
      grep_jstack ${line}
      echo "-------------------------------"
    fi
    done </tmp/j-$pid
}

jstack $pid > /tmp/s-$pid
jstack_pid=$!

top -H -b -n 1 -p $pid | sed -n '8,$p'|awk -v val=$cpu_use '$9>val{printf("%x\n",
 $1);fflush()}' > /tmp/j-$pid

wait $jstack_pid
grep_all

echo "-------------------------THE END---------------------------------\n\n"
