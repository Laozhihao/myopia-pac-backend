CREATE TABLE `m_overview`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '总览机构名称',
  `contact_person` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `illustrate` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '说明',
  `district_id` int(11) NOT NULL COMMENT '行政区域ID',
  `district_detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '行政区域json',
  `config_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '配置类型，0：配置筛查机构、1：配置医院、2：配置筛查机构+医院',
  `hospital_service_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '医院服务类型（配置），0：居民健康系统(默认)、1：0-6岁眼保健、2：0-6岁眼保健+居民健康系统',
  `hospital_limit_num` int(11) NOT NULL DEFAULT 0 COMMENT '医院限制数量',
  `screening_organization_config_type` tinyint(4) NOT NULL COMMENT '筛查机构配置 0-省级配置 1-单点配置 2-VS666 3-单点配置+VS666',
  `screening_organization_limit_num` int(11) NOT NULL DEFAULT 0 COMMENT '筛查机构限制数量',
  `cooperation_type` tinyint(1) NULL DEFAULT NULL COMMENT '合作类型，0-合作 1-试用',
  `cooperation_time_type` tinyint(1) NULL DEFAULT NULL COMMENT '合作期限类型，-1-自定义 0-30天 1-60天 2-180天 3-1年 4-2年 5-3年',
  `cooperation_start_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作开始时间',
  `cooperation_end_time` timestamp(3) NULL DEFAULT NULL COMMENT '合作结束时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-启用 1-禁止 2-删除',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '总览机构信息表' ROW_FORMAT = Dynamic;

CREATE TABLE `m_overview_admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `create_user_id` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
  `overview_id` int(11) NOT NULL COMMENT '学校ID',
  `user_id` int(11) NOT NULL COMMENT '用户表ID',
  `gov_dept_id` int(11) NOT NULL COMMENT '部门ID',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '总览机构管理员表' ROW_FORMAT = Dynamic;

CREATE TABLE `m_overview_hospital`  (
  `overview_id` int(11) NOT NULL COMMENT '总览机构id',
  `hospital_id` int(11) NOT NULL COMMENT '医院id',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`overview_id`, `hospital_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '总览机构医院关联表' ROW_FORMAT = Dynamic;

CREATE TABLE `m_overview_screening_organization`  (
  `overview_id` int(11) NOT NULL COMMENT '总览机构id',
  `screening_organization_id` int(11) NOT NULL COMMENT '筛查机构id',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`overview_id`, `screening_organization_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '总览机构筛查机构关联表' ROW_FORMAT = Dynamic;