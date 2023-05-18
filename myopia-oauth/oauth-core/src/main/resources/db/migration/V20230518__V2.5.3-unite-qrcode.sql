delete from o_permission where id in (703,706);
delete from o_role_permission where permission_id in (703,706);
delete from o_district_permission where permission_id in (703,706);

UPDATE o_permission t SET t.name = '筛查二维码' WHERE t.id = 705;