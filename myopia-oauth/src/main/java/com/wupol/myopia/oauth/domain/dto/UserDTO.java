package com.wupol.myopia.oauth.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.oauth.domain.model.Role;
import com.wupol.myopia.oauth.domain.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserDTO extends User {
    /**
     * 开始创建时间（不为空时，endCreateTime也不能为空，且不能比endCreateTime大）
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startCreateTime;
    /**
     * 结束创建时间（不为空时，startCreateTime也不能为空，且不能比startCreateTime小）
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endCreateTime;
    /**
     * 开始最后登录时间（不为空时，endLastLoginTime也不能为空，且不能比endLastLoginTime大）
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startLastLoginTime;
    /**
     * 结束最后登录时间（不为空时，startLastLoginTime也不能为空，且不能比startLastLoginTime小）
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endLastLoginTime;
    /**
     * 角色名
     **/
    private String roleName;
    /**
     * 创建人真实姓名
     **/
    private String createUserName;

    /**
     * 所属部门ID集
     **/
    private List<Integer> orgIds;

    /**
     * 当前页码
     **/
    private Integer current;

    /**
     * 每页条数
     **/
    private Integer size;

    /**
     * 角色ID集
     **/
    @TableField(exist = false)
    private List<Integer> roleIds;
    /** 用户拥有的所有角色 **/
    private List<Role> roles;

}
