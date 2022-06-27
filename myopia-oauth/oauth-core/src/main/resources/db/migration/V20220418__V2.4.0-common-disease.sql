delete
from o_permission
where id in (603, 781);

delete
from o_role_permission
where permission_id in (603, 781);

delete
from o_district_permission
where permission_id in (603, 781);

-- 学校-筛查数据导出
delete
from o_permission
where id in (221, 224);

delete
from o_role_permission
where permission_id in (221, 224);

delete
from o_district_permission
where permission_id in (221, 224);

-- 导出学生筛查数据
delete
from o_permission
where id in (184, 186, 277);

delete
from o_role_permission
where permission_id in (184, 186, 277);

delete
from o_district_permission
where permission_id in (184, 186, 277);