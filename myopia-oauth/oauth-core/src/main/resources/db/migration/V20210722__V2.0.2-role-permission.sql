-- 更新角色权限
update o_role set role_type = 3 where role_type = 0 and id != 1;