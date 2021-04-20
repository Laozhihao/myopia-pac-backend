package com.wupol.myopia.business.core.government.constant;

/**
 * 缓存相关常量
 * - 为了方便维护和便于Redis可视化工具中排查问题，采用冒号来分割风格
 * - 格式 = 类别:描述(或类别，下划线命名):唯一值描述_唯一值占位符
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public interface DistrictCacheKey {

    /**
     * 全部行政区域列表集合
     */
    String DISTRICT_ALL_LIST = "district:all:list";
    String DISTRICT_ALL_TREE = "district:all:tree";
    String DISTRICT_ALL_PROVINCE_TREE = "district:all:province_tree";

    /**
     * 指定的行政区域的路径明细（从省开始）
     */
    String DISTRICT_POSITION_DETAIL = "district:position_detail:code_%s";

    /**
     * 指定的行政区域为根节点的区域树
     */
    String DISTRICT_TREE = "district:tree:code_%s";

    /**
     * 指定的行政区域的下级行政区域
     */
    String DISTRICT_CHILD = "district:child:parent_code_%s";

    /**
     * 全部行政区域的ID、Name的Map
     */
    String DISTRICT_ID_NAME_MAP = "district:all:id_name_map";

    String DISTRICT_TOP_CN_NAME = "district:top_cn_name:code_%s";

    String DISTRICT_CN_NAME = "district:cn_name:code_%s";

    String DISTRICT_CODE = "district:name:code_%s";
}
