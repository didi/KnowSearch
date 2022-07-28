#  user-》logi_security_user_project
insert into es_manager_test2.logi_security_user_project (project_id, user_id, app_name, user_type)
select distinct *
from (select qa.id project_id, eds.id user_id, 'know_search' as app_name, 1 as user_type
      from admin_zh.query_app qa
               left join admin_zh.arius_user_info eds on find_in_set(eds.id, responsible)
      order by project_id) t1
where user_id is not null;
#query_app( department,department_id) ->logi_security_dept
insert into es_manager_test3.logi_security_dept(id, dept_name, description, leaf, level, parent_id, app_name)
SELECT id, department, department_id, false as leaf, 1 level, 0 parent_id, 'know_search' as app_name
from admin_zh.query_app
GROUP BY department
HAVING COUNT(department) > 1;
# query_app(department,department_id,id)->logi_security_dept
update es_manager_test2.logi_security_project
set dept_id=id
where id in (SELECT id as app_name from admin_zh.query_app GROUP BY department HAVING COUNT(department) > 1);