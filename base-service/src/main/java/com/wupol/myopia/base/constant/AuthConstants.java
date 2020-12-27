package com.wupol.myopia.base.constant;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
public interface AuthConstants {
    /**
     * JWT存储权限前缀
     */
    String AUTHORITY_PREFIX = "ROLE_";

    /**
     * JWT存储权限属性
     */
    String AUTHORITY_CLAIM_NAME = "authorities";

    /**
     * 认证信息Http请求头
     */
    String JWT_TOKEN_HEADER = "Authorization";

    /**
     * JWT令牌前缀
     */
    String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT载体key
     */
    String JWT_PAYLOAD_KEY = "payload";

    /**
     * Redis缓存权限规则key
     */
    String RESOURCE_ROLES_KEY = "auth:resourceRoles";

    /**
     * 黑名单token前缀
     */
    String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    /**
     * client表查询SQL
     */
    String CLIENT_DETAILS_FIELDS = "client_id, CONCAT('{noop}',client_secret) as client_secret, resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";
    String BASE_CLIENT_DETAILS_SQL = "select " + CLIENT_DETAILS_FIELDS + " from oauth_client_details";
    String FIND_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " order by client_id";
    String SELECT_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " where client_id = ?";

    /**
     * 密码加密方式
     */
    String BCRYPT = "{bcrypt}";

    /**
     * JWT增强内容的键
     */
    String JWT_CLIENT_ID_KEY = "client_id";
    String JWT_USER_KEY = "user_base_info";

    /**
     * 是否为菜单权限
     */
    Integer IS_NOT_MENU_PERMISSION = 0;
    Integer IS_MENU_PERMISSION = 1;

    /**
     * 是否为页面权限
     */
    Integer IS_PAGE_PERMISSION = 1;
    Integer IS_API_PERMISSION = 0;
}
