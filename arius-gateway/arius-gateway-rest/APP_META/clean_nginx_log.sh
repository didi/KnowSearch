log_dir="/home/xiaoju/nginx/logs"
date=`date +%Y%m%d`
date_rm=`date +%Y%m%d -d '3 days ago'`
mv ${log_dir}/access.log  ${log_dir}/${date}_access.log
/home/xiaoju/nginx/sbin/nginx -s reopen
rm -f ${log_dir}/${date_rm}_access.log