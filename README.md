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

## 项目目录结构与说明
### 目录结构
- myopia-pac-backend  --- 根目录，聚合所有微服务
    - base-service    --- 服务公共模块
    - myopia-business --- 核心业务服务
        - bootstrap   --- 启动模块
        - common      --- 底层公共模块，各个端共用的业务功能
        - hospital    --- 医院端模块
        - management  --- 管理端模块
        - parent      --- 家长端模块
        - school      --- 学校端模块
        - screening   --- 筛查端模块
    - myopia-device   --- 采集设备数据服务
    - myopia-gateway  --- 网关服务
    - myopia-oauth    --- 授权中心服务
    
### 说明
- 核心业务服务myopia-business的配置文件全部放启动模块bootstrap的resource目录下
- dev环境要启动myopia-business，运行bootstrap模块下的启动类 MyopiaOauthApplication.java

## 自动生成代码

## Dev启动项目
### 准备
1. 下载并运行注册中心与配置中心 nacos
2. 下载并运行 Sentinel dashboard
### 启动
1. 同步maven依赖（根目录myopia-pac-backend下执行）
 ```bash
 mvn clean install -DskipTests
 ```
 2. 直接运行对应服务的启动类

## 部署项目
### 部署前准备
- 修改配置参数为对应环境的
- 启动依赖的治理服务

### 同步maven依赖 
 ```bash
 mvn clean install -DskipTests
 ```
 ### 打包
 ```bash
 mvn package -DskipTests
 ```
 or
 ```bash
 mvn clean validate install -DskipTests
 ```
 ### 运行
 ```bash
 java  -jar myopia.jar