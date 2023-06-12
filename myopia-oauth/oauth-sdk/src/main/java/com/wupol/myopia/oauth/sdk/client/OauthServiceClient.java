package com.wupol.myopia.oauth.sdk.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.config.feign.BusinessServiceFeignConfig;
import com.wupol.myopia.base.domain.UserRequest;
import com.wupol.myopia.oauth.sdk.domain.request.PermissionDTO;
import com.wupol.myopia.oauth.sdk.domain.request.RoleDTO;
import com.wupol.myopia.oauth.sdk.domain.request.RolePermissionDTO;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/11
 **/
@FeignClient(name = "myopia-oauth", decode404 = true, fallbackFactory = OauthServiceFallbackFactory.class, configuration = BusinessServiceFeignConfig.class)
public interface OauthServiceClient {

    /**
     * 获取用户列表（分页）
     *
     * @param param 查询参数
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/page")
    Page<User> getUserListPage(@SpringQueryMap UserDTO param);

    /**
     * 获取用户列表
     *
     * @param param 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/list")
    List<User> getUserList(@SpringQueryMap UserDTO param);

    /**
     * 获取用户列表（仅支持用户名模糊查询）
     *
     * @param param 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/getByName")
    List<User> getUserListByName(@SpringQueryMap UserDTO param);

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/batch/id")
    List<User> getUserBatchByIds(@RequestParam("userIds") List<Integer> userIds);

    /**
     * 更新用户名称
     *
     * @param realName
     * @param byOrgId
     * @param bySystemCode
     * @param byUserType
     * @return
     */
    @PutMapping("/oauth/user/realname")
    Integer updateUserRealName(@RequestParam("realName") String realName, @RequestParam("byOrgId") Integer byOrgId,
                               @RequestParam("bySystemCode") Integer bySystemCode, @RequestParam("byUserType") Integer byUserType);

    /**
     * 根据手机号码批量获取用户
     *
     * @param phones     手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/batch/phone")
    List<User> getUserBatchByPhones(@RequestParam("phones") List<String> phones, @RequestParam("systemCode") Integer systemCode);

    /**
     * 根据手机号码批量获取用户
     *
     * @param idCards    身份证号码集
     * @param systemCode 系统编号
     * @param orgId      机构ID
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @GetMapping("/oauth/user/batch/idCard")
    List<User> getUserBatchByIdCards(@RequestParam("idCards") List<String> idCards, @RequestParam("systemCode") Integer systemCode, @RequestParam("orgId") Integer orgId);

    /**
     * 新增用户
     *
     * @param param 用户数据
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PostMapping("/oauth/user")
    User addUser(@RequestBody UserDTO param);

    /**
     * 管理端创建其他系统的用户(医院端、学校端、筛查端)
     *
     * @param param 用户数据
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PostMapping("/oauth/user/multi/system")
    User addMultiSystemUser(@RequestBody UserDTO param);

    /**
     * 批量新增筛查人员
     *
     * @param param 用户数据
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.User>
     **/
    @PostMapping("/oauth/user/screening/batch")
    List<User> addScreeningUserBatch(@RequestBody List<UserDTO> param);

    /**
     * 更新用户<br/>
     * 传手机号时必须有id, systemcode, usertype
     *
     * @param param 用户数据
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PutMapping("/oauth/user")
    User updateUser(@RequestBody UserDTO param);

    /**
     * 批量更新用户状态
     *
     * @param param 用户数据
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PutMapping("/oauth/user/status/batch")
    boolean updateUserStatusBatch(@RequestBody UserDTO param);

    /**
     * 重置管理端用户的密码【其他端用户的不适合】
     *
     * @param userId   用户ID
     * @param password 密码
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @PutMapping("/oauth/user/password/{userId}")
    User resetPwd(@PathVariable("userId") Integer userId, @RequestParam("password") String password);

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.oauth.sdk.domain.response.User
     **/
    @GetMapping("/oauth/user/{userId}")
    User getUserDetailByUserId(@PathVariable("userId") Integer userId);

    /**
     * 统计
     *
     * @param queryParam 查询条件
     * @return java.lang.Integer
     **/
    @GetMapping("/oauth/user/count")
    Integer count(@SpringQueryMap UserDTO queryParam);

    /**
     * 移除医院管理员关联的筛查机构管理员角色
     *
     * @param hospitalId              医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @DeleteMapping("/oauth/user/hospital/associated/role")
    void removeHospitalUserAssociatedScreeningOrgAdminRole(@RequestParam("hospitalId") Integer hospitalId, @RequestParam("associateScreeningOrgId") Integer associateScreeningOrgId);

    /**
     * 给医院管理员添加关联的筛查机构管理员角色
     *
     * @param hospitalId              医院ID
     * @param associateScreeningOrgId 关联筛查机构ID
     * @return void
     **/
    @PostMapping("/oauth/user/hospital/associated/role")
    void addHospitalUserAssociatedScreeningOrgAdminRole(@RequestParam("hospitalId") Integer hospitalId, @RequestParam("associateScreeningOrgId") Integer associateScreeningOrgId);

    /**
     * 更新医院相关用户的角色
     *
     * @param hospitalId  医院ID
     * @param serviceType 服务类型
     * @return void
     **/
    @PutMapping("/oauth/user/hospital/role")
    void updateHospitalRole(@RequestParam("hospitalId") Integer hospitalId, @RequestParam("serviceType") Integer serviceType);

    /**
     * 更新总览机构用户的角色
     *
     * @param overviewId 医院ID
     * @param configType 服务类型
     * @return void
     **/
    @PutMapping("/oauth/user/overview/role")
    void updateOverviewIdRole(@RequestParam("overviewId") Integer overviewId, @RequestParam("configType") Integer configType);

    /**
     * 获取角色列表
     *
     * @param param 查询参数
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.Role>
     **/
    @GetMapping("/oauth/role/list")
    List<Role> getRoleList(@SpringQueryMap RoleDTO param);

    /**
     * 获取角色列表 - 分页
     *
     * @param param 查询参数
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.wupol.myopia.oauth.sdk.domain.response.Role>
     **/
    @GetMapping("/oauth/role/page")
    Page<Role> getRoleListByPage(@SpringQueryMap RoleDTO param);

    @PostMapping("/oauth/role")
    Role addRole(@RequestBody RoleDTO param);

    @PutMapping("/oauth/role")
    Role updateRole(@RequestBody RoleDTO param);

    @PostMapping("/oauth/role/permission/{roleId}")
    List<RolePermission> assignRolePermission(@PathVariable("roleId") Integer roleId, @RequestBody List<Integer> permissionIds);

    @GetMapping("/oauth/role/{roleId}")
    Role getRoleById(@PathVariable("roleId") Integer roleId);

    /**
     * 获取指定行政区下的角色权限树
     *
     * @param roleId       角色ID
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.Permission>
     **/
    @GetMapping("/oauth/role/permission/structure/{roleId}/{templateType}")
    List<Permission> getRolePermissionTree(@PathVariable("roleId") Integer roleId, @PathVariable("templateType") Integer templateType);

    /**
     * 获取权限列表
     *
     * @param param 请求参数
     * @return java.util.List<com.wupol.myopia.oauth.sdk.domain.response.Permission>
     **/
    @GetMapping("/oauth/permission/list")
    List<Permission> getPermissionList(@SpringQueryMap PermissionDTO param);

    @PostMapping("/oauth/permission")
    Permission addPermission(@RequestBody PermissionDTO param);

    @PutMapping("/oauth/permission")
    Permission updatePermission(@RequestBody PermissionDTO param);

    @DeleteMapping("/oauth/permission/{permissionId}")
    boolean deletePermission(@PathVariable("permissionId") Integer permissionId);

    @GetMapping("/oauth/user/getByIds")
    List<User> getUserByIds(@SpringQueryMap UserRequest request);

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/oauth/districtPermission/{templateType}")
    List<Permission> getPermissionTemplate(@PathVariable("templateType") Integer templateType);

    /**
     * 根据模板类型获取模板权限的ID集
     *
     * @param templateType 模板类型
     * @return com.wupol.myopia.base.domain.java.util.List<java.lang.Integer>>
     **/
    @GetMapping("/oauth/districtPermission/list/{templateType}")
    List<Integer> getPermissionTemplateIdList(@PathVariable("templateType") Integer templateType);

    /**
     * 更新模板权限
     *
     * @param templateType      模板类型
     * @param rolePermissionDTO 角色权限
     * @return boolean
     **/
    @PutMapping("/oauth/districtPermission/{templateType}")
    Boolean updatePermissionTemplate(@PathVariable("templateType") Integer templateType, @RequestBody RolePermissionDTO rolePermissionDTO);

    /**
     * 根据手机号码批量获取用户
     *
     * @param systemCode 系统编号
     * @param orgIds     机构Ids
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/oauth/user/batch/orgIds")
    List<User> getUserBatchByOrgIds(@RequestParam("orgIds") List<Integer> orgIds, @RequestParam("systemCode") Integer systemCode, @RequestParam("userType") Integer userType);

    /**
     * 登录
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端秘钥
     * @param username     用户名
     * @param password     密码
     * @return com.wupol.myopia.base.domain.com.wupol.myopia.business.management.domain.dto.login.LoginInfoDTO>
     **/
    @PostMapping("/login")
    LoginInfo login(@RequestParam("client_id") Integer clientId, @RequestParam("client_secret") String clientSecret, @RequestParam("username") String username, @RequestParam("password") String password);

    /**
     * 通过templateType获取权限集合
     *
     * @param templateType 模板类型
     * @return 权限集合
     */
    @GetMapping("/oauth/districtPermission/permissionIds/{templateType}")
    List<Integer> getListByTemplateType(@PathVariable("templateType") Integer templateType);

    /**
     * 通过UserIds获取用户信息
     *
     * @param userIds 用户Ids
     * @return 用户列表
     */
    @GetMapping("/oauth/user/batch/userIds")
    List<User> getUserBatchByUserIds(@RequestParam("userIds") List<Integer> userIds);

    /**
     * 更新机构信息
     *
     * @param organization
     * @return
     */
    @PutMapping("/oauth/organization")
    Organization updateOrganization(@RequestBody Organization organization);

    /**
     * 增加机构信息
     *
     * @param organization
     * @return
     */
    @PostMapping("/oauth/organization")
    Organization addOrganization(@RequestBody Organization organization);

    /**
     * 通过机构类型获取权限
     *
     * @param configType 配置
     * @return List<String>
     */
    @GetMapping("/oauth/permission/organization/{configType}")
    List<String> getPermissionByConfigType(@PathVariable("configType") Integer configType);

}
