# myopia-pac-backend

近视防控项目后端（myopia prevention and control backend）

## 技术栈

- 基础工程：Spring Boot 2.2.11.RELEASE + Maven + JDK 1.8
- 数据层：Mybatis Plus + MySQL 5.7
- 日志：Log4j2
- 缓存：Redis 3.2.1
- 消息队列：Kafka
- 注册中心与配置中心：Nacos 2.0.0-ALPHA.1
- 网关：Spring cloud gateway
- 服务熔断降级与限流：Sentinel
- 服务远程调用：Openfeign
- 服务负载均衡：Ribbon
- 依赖：spring-cloud Hoxton.SR9、spring-cloud-alibaba 2.2.1.RELEASE
- 对象存储服务：AWS S3

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
运行base-service其util目录下的MybatisPlusGenerator的main方法，按照提示输入服务名、表名、实体名，可自动生成代码。注意事项：

- 修改数据库地址和账号密码为自己的
```bash
dsc.setDbType(DbType.MYSQL)
    .setUrl("jdbc:mysql://localhost:3306/myopia_oauth?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8")
    .setDriverName("com.mysql.cj.jdbc.Driver")
    .setUsername("root")
    .setPassword("laozh0111");
```
- 修改表前缀，如授权中心的表前缀为“o_”，管理端的表前缀为“m_”
```bash
private static StrategyConfig getStrategyConfig() {
    strategy.setTablePrefix("o_")
}
```
- 提示输入“微服务名（为多module的服务则输入“服务名/模块名”）”时，若生成管理端的代码，则输入 myopia-business/management；若要生成授权中心的代码，则输入 myopia-oauth 即可

## Dev环境启动项目
### 准备
#### 安装依赖环境【可从OneDrive下载对应安装包：OneDrive>产品&研发>文档>近视防控项目>工程依赖安装包】
- 下载并运行 Nacos 【运行：直接双击安装包bin目录下的startup_standalone.cmd文件（仅限Windows环境）】
- 下载 Sentinel dashboard 的jar包，运行：
 ```bash
 java -jar sentinel-dashboard-1.7.1.jar
 ```
- 下载 Zipkin 的jar包，运行：
 ```bash
 java -jar zipkin.jar
 ```
- 其他：Redis、MySQL数据库、Maven、JDK1.8

#### 配置环境变量
1. AWS_REGION : S3的REGION
2. AWS_ACCESS_KEY_ID : S3的AccessKey
3. AWS_SECRET_ACCESS_KEY : S3的SecretKey

#### 修改配置文件
1. copy对应微服务sample/setting目录下的配置文件到resource目录下
2. 修改配置文件中数据库、Redis、Nacos等参数值为自己的

#### 初始化数据库
1. 执行对应服务resource/db/migration目录下的SQL
2. 如果开启flyway自动执行，则不用手动执行（建议开发环境首次自动执行初始化完后，关闭自动执行）
3. 注意，myopia-business服务的m_district表数据过多，其初始化SQL放在了“OneDrive>产品&研发>文档>近视防控项目>工程依赖安装包”目录下的m_district.sql

### 启动
1. 同步maven依赖（根目录myopia-pac-backend下执行）
 ```bash
 mvn clean install -DskipTests
 ```
2. 根据需要运行对应服务的启动类（myopia-gateway和myopia-oauth服务必需启动）

### 开发调试
- 登录等接口说明，请查看Eolinker的Myopia项目
- 新增接口需要添加对应的路径到o_permission表，并为角色分配对应权限，用户重新登录后，才可正常访问【也可以从前端界面的用户管理模块中录入并分配权限】
- 请求业务接口时，Header都要带上Authorization字段，其值 = Bearer字符串 + 1个空格 + 登录时返回的access_token值，如：“Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVC...”

## 生产环境部署项目
### 部署前准备
- 修改配置参数为对应环境的
- 安装并启动依赖的治理服务【如：Nacos、Zipkin等】

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
 ```
 
## 开发规范约定
### 遵守公司java统一开发规范
https://git.vistel.cn/web/web-toolkits/java-coding-guide
### 命名
- 政府部门：gov_dept、筛查机构：screening_org、两者统称：org
- Redis缓存key值命名，采用冒号来分类，为了方便维护和便于Redis可视化工具中排查问题。格式 = 类别:描述(或类别，下划线命名):唯一值描述_唯一值占位符
### Mybatis-plus 使用
- 避免在代码里拼接SQL，难以维护
- 避免使用QueryWrapper()拼接查询参数（除了在BaseService.java中封装底层方法），难以维护
- 建议尽量使用Mybatis-plus提供的基础api，或在xxxMapper.xml中编写统一维护SQL、字段
### myopia-business 的 common 模块内容
- 不包含controller层，通过service层对外提供服务
- 不依赖其他模块，作为公共底层模块
- 遵循自上而下调用原则，仅被业务层调用，common模块不能调用业务层
- 抽取服务(功能)放到common层时，前提条件为：该服务(功能)会被至少两个2业务模块使用到