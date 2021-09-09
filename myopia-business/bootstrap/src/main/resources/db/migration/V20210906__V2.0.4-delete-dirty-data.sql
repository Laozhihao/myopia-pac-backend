DELETE
FROM m_vision_screening_result
WHERE plan_id in (select src_screening_notice_id
                  from m_screening_plan
                  where src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14));
DELETE
FROM m_screening_plan_school_student
WHERE screening_plan_id in (select src_screening_notice_id
                            from m_screening_plan
                            where src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14));
DELETE
FROM m_screening_plan_school
WHERE screening_plan_id in (select src_screening_notice_id
                            from m_screening_plan
                            where src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14));

DELETE
FROM m_screening_task_org
WHERE screening_task_id in (select task.id
                            from m_screening_task task
                            where screening_notice_id in (select src_screening_notice_id
                                                          from m_screening_plan plan
                                                          where plan.src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14)));

DELETE FROM m_screening_notice WHERE id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_district_big_screen_statistic WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_district_monitor_statistic WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_district_vision_statistic WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_school_monitor_statistic WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_school_vision_statistic WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_screening_notice_dept_org WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_screening_plan WHERE src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_stat_conclusion WHERE src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_screening_task WHERE screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);
DELETE FROM m_screening_plan WHERE src_screening_notice_id in (1, 2, 3, 4, 5, 10, 11, 13, 14);

delete from m_screening_plan_school where id in
      (select * from (select id from m_screening_plan_school where screening_plan_id not in (select plan.id from m_screening_plan plan) ) as temp);
