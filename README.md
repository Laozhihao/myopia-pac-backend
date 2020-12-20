# myopia-pac-backend

近视防控项目后端（myopia prevention and control backend）

## 技术栈

- 基础工程：Spring Boot + Maven
- 数据层：Mybatis Plus + MySQL
- 日志：Log4j2
- 缓存：Redis
- 消息队列：Kafka
- 注册中心与配置中心：Nacos
- 网关：Spring cloud gateway
- 服务熔断降级与限流：Sentinel
- 服务远程调用：Openfeign
- 服务负载均衡：Ribbon

## 系统架构

系统采取微服务架构，服务治理采用Spring Cloud Alibaba解决方案，详情查看ProcessOn：https://www.processon.com/diagraming/5fc852b96376895e9af1f275

## 项目目录结构说明

- myopia-pac-backend  --- 根目录，聚合所有微服务
    - myopia-business --- 核心业务服务
        - bootstrap   --- 启动模块
        - common      --- 公共模块
        - hospital    --- 医院端模块
        - management  --- 管理端模块
        - parent      --- 家长端模块
        - school      --- 学校端模块
        - screening   --- 筛查端模块
    - myopia-device   --- 采集设备数据服务
    - myopia-gateway  --- 网关服务
    - myopia-oauth    --- 授权中心服务
    
## 启动项目

## 自动生成代码

## 部署项目