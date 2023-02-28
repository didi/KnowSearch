#!/bin/bash

echo "use zeus;" >> task_hosts.sql
echo "set names utf8;" >> task_hosts.sql
echo "" >> task_hosts.sql

for i in {0..99}; do
	echo "CREATE TABLE task_host_$i (" >> task_hosts.sql
	echo '  `ii` bigint unsigned NOT NULL AUTO_INCREMENT,' >> task_hosts.sql
	echo '  `id` bigint unsigned not null,' >> task_hosts.sql
	echo '  `hostname` varchar(128) not null,' >> task_hosts.sql
	echo '  `status` varchar(32) not null,' >> task_hosts.sql
	echo '  `stdout` text,' >> task_hosts.sql
	echo '  `stderr` text,' >> task_hosts.sql
	echo '  UNIQUE KEY (`id`, `hostname`),' >> task_hosts.sql
	echo '  PRIMARY KEY (`ii`)' >> task_hosts.sql
	echo ') ENGINE=InnoDB DEFAULT CHARSET=utf8;' >> task_hosts.sql
	echo '' >> task_hosts.sql
done
