<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${package.Mapper}.${table.mapperName}">

<#if enableCache>
    <!-- 开启二级缓存 -->
    <cache type="org.mybatis.caches.ehcache.LoggingEhcache"/>
</#if>
<#if baseResultMap>
    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="${package.Entity}.${entity}">
    </resultMap>

</#if>
    <!-- 通用查询条件 -->
    <sql id="Base_Where_Clause">
        <where>
            <trim prefixOverrides="and">
        <#list table.fields as field>
            <#if field.keyFlag><#--生成主键排在第一位-->
                <if test="@Ognl@isNotEmpty(${field.propertyName})">and `${field.name}` = ${r'#{' + field.propertyName + '}'}</if>
            </#if>
        </#list>
            <#list table.commonFields as field><#--生成公共字段 -->
                <if test="@Ognl@isNotEmpty(${field.propertyName})">and `${field.name}` = ${r'#{' + field.propertyName + '}'}</if>
            </#list>
        <#list table.fields as field>
            <#if !field.keyFlag><#--生成普通字段 -->
                <if test="@Ognl@isNotEmpty(${field.propertyName})">and `${field.name}` = ${r'#{' + field.propertyName + '}'}</if>
            </#if>
        </#list>
            </trim>
        </where>
    </sql>

<#if baseColumnList>
    <!-- 通用查询结果列 -->
    <sql id="Base_Column_List">
<#list table.commonFields as field>
        ${field.columnName},
</#list>
        ${table.fieldNames}
    </sql>

</#if>
</mapper>
