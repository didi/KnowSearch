drop function if exists PW_encode;
create function PW_encode(pw varchar(2048)) returns varchar(2048)
begin
    declare SALT0_START varchar(2048) default '{@VjJ4ak{[#@'; declare SALT0_END varchar(2048) default '@#]}J6Rllh@}';
    declare SALT1 varchar(2048) default 'Mv{#cdRgJ45Lqx}3IubEW87!=='; declare i int default 1;
    declare pw_temp varchar(2048) default '';
    set pw_temp = pw;

    while (i < 4)
        do
            set pw_temp = concat(pw_temp, SALT0_START, i, SALT0_END); set pw_temp = REPLACE(pw_temp, '\n', '');
            set pw_temp = to_base64(pw_temp); set i = i + 1;
        end while;
    set pw_temp = concat(pw_temp, SALT1); set pw_temp = REPLACE(pw_temp, '\n', ''); return pw_temp;
end;


# 通过arius_user_info 生成 logi_security_user
insert into es_manager_test2.logi_security_user(id, user_name, pw, real_name, phone, email, dept_id, is_delete,
                                                app_name, salt)
select id,
       domain_account,
       if(length(password) > 5, PW_encode(password), ''),
       name,
       mobile,
       email,
       0             as dept_id,
       0             as is_delete,
       'know_search' as app_name,
       ''            as salt
from admin_zh.arius_user_info
where status = 1;

# 通过arius_user_info 生成 logi_security_user_role
insert into es_manager_test2.logi_security_user_role(user_id, role_id, is_delete, app_name)
select id, 2 as role_id, 0 as is_delete, 'know_search' as app_name
from admin_zh.arius_user_info
where status = 1;
# 修改admin为管理员
insert into es_manager_test2.logi_security_user_role(user_id, role_id, is_delete, app_name)
select id, 1 as role_id, 0 as is_delete, 'know_search' as app_name
from admin_zh.arius_user_info
where status = 1
  and domain_account = 'admin';