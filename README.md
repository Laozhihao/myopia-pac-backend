# myopia-pac-backend

近视防控项目后端（myopia prevention and control backend）

## 技术栈

- 基础工程：Spring Boot 2.3.12.RELEASE + Maven + JDK 1.8
- 数据层：Mybatis Plus + MySQL 5.7
- 日志：Log4j2
- 缓存：Redis 3.2.1
- 消息队列：Kafka
- 注册中心与配置中心：Nacos 2.0.3
- 网关：Spring cloud gateway
- 服务熔断降级与限流：Sentinel
- 服务远程调用：Openfeign
- 服务负载均衡：Ribbon
- 依赖：spring-cloud Hoxton.SR12、spring-cloud-alibaba 2.2.7.RELEASE
- 对象存储服务：AWS S3

## 系统架构

系统采取微服务架构，服务治理采用Spring Cloud Alibaba解决方案，详情查看ProcessOn：https://www.processon.com/diagraming/5fc852b96376895e9af1f275

## 项目目录结构与说明
### 目录结构
- myopia-pac-backend  --- 根目录，聚合所有微服务
    - service-common  --- 服务公共模块
    - myopia-business --- 核心业务服务
        - bootstrap                 --- 启动模块
        - business-aggregation      --- 中间聚合层模块
        - business-api              --- 业务api层
            - common-api                --- 各个业务系统公用api
            - device-api                --- 设备数据采集api
            - hospital-app-api          --- 医院端（居民健康）APP api
            - management-api            --- 综合管理平台api
            - parent-api                --- 家长端api
            - preschool-app-api         --- 0~6岁APP api
            - school-management-api     --- 学校管理平台api
            - screening-app-api         --- 筛查APP api
        - business-core             --- 基础业务功能层
            - common-core               --- 公共基础功能模块
            - device-core               --- 设备相关模块
            - government-core           --- 政府相关模块
            - hospital-core             --- 医院相关模块
            - parent-core               --- 家长相关模块
            - school-core               --- 学校相关模块
            - school-management-core    --- 学校管理平台相关模块
            - screening-flow-core       --- 筛查相关模块
            - screening-organization-core   --- 筛查机构相关模块
            - stat-core                 --- 统计相关模块
            - system-core               --- 系统设置相关模块
        - common-utils              --- 公共工具模块
    - myopia-gateway  --- 网关服务
    - myopia-oauth    --- 授权中心服务
        - oauth-core                --- 核心功能模块
        - oauth-sdk                 --- SDK模块
    
### 说明
- 核心业务服务myopia-business的配置文件全部放启动模块bootstrap的resource目录下
- dev环境要启动myopia-business，运行bootstrap模块下的启动类 MyopiaOauthApplication.java

## 自动生成代码
运行base-service其util目录下的MybatisPlusGenerator的main方法，按照提示输入服务名、表名、实体名，可自动生成代码。注意事项：

- 修改数据库地址和账号密码为自己的
```
dsc.setDbType(DbType.MYSQL)
    .setUrl("jdbc:mysql://localhost:3306/myopia_oauth?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8")
    .setDriverName("com.mysql.cj.jdbc.Driver")
    .setUsername("root")
    .setPassword("laozh0111");
```
- 修改表前缀，如授权中心的表前缀为“o_”，管理端的表前缀为“m_”
```
private static StrategyConfig getStrategyConfig() {
    strategy.setTablePrefix("o_")
}
```
- 提示输入“微服务名（为多module的服务则输入“服务名/模块名”）”时，若生成管理端的代码，则输入 myopia-business/management；若要生成授权中心的代码，则输入 myopia-oauth 即可

## Dev环境启动项目
### 准备
#### 安装依赖环境【可从微盘下载对应安装包：广州团队共享 > 近视防控项目 > 部署启动系统工具包】
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

#### 配置MAVEN私库
在maven的settings.xml文件中配置私服的账号密码地址

#### 配置环境变量
1. AWS_REGION : S3的REGION
2. AWS_ACCESS_KEY_ID : S3的AccessKey
3. AWS_SECRET_ACCESS_KEY : S3的SecretKey

#### 安装并连接VPN
- 访问maven私服需要连接VPN
- 安装包可从微盘下载

#### 修改配置文件
1. copy对应微服务config/sample目录下的配置文件到resource目录下
2. 修改配置文件application.yml中数据库、Redis、Nacos等参数值

#### 初始化数据库
1. 执行对应服务resource/db/migration目录下的SQL
2. 如果开启flyway自动执行，则不用手动执行（建议开发环境首次自动执行初始化完后，关闭自动执行）
3. 注意：myopia-business服务的m_district表数据过多，没放在flyway的初始化SQL中，可从“微盘 > 广州团队共享 > 近视防控项目 > 部署启动系统工具包”下载 m_district.sql 初始 m_district 表

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

### 安装相关依赖服务
1. 包括：Sentinel、Zipkin、MySQL、Redis、Nginx
2. 安装前准备
- 是以配置文件方式启动 Redis，需要先在安装环境的/docker/redis/conf/目录下创建 redis.conf，redis.conf 内容示例详情查看：doc/depend/redis.conf
- 创建 Nginx 挂载目录，并创建 Nginx 配置文件
```bash
mkdir -p /var/nginx_home
chmod 777 /var/nginx_home
vim /var/nginx_home/nginx.conf
```
3. docker-compose 编排部署
- yaml文件示例详情查看：doc/depend/docker-compose-depend.yml
```bash
docker-compose -f docker-compose-depend.yml up -d
```

### 安装 Nacos 集群
1. 初始化数据库
- 在MySQL中创建数据库 nacos_config
- 在数据库 nacos_config 中，执行 Nacos 建表脚本
- 脚本下载地址：https://github.com/alibaba/nacos/blob/master/distribution/conf/nacos-mysql.sql
2. 安装前准备
- 创建 docker-compose 部署脚本，内容示例查看：doc/nacos/docker-compose-nacos-cluster.yml
- 创建 nacos 环境变量文件，内容示例查看：doc/nacos/nacos-ip.env
3. 编排部署
```bash
docker-compose -f docker-compose-nacos-cluster.yml up -d
```
4. 通过 nginx 对 nacos 集群进行负载均衡
- 修改 nginx 的 nginx.conf 配置文件，增加 nacos 负载均衡配置信息，示例详情查看：doc/nacos/nginx-nacos.conf

### 修改项目配置参数
1. 修改每个微服务 setting/env 目录下的配置文件的参数值为对应环境的参数值

### 通过 Jenkins 部署各微服务
1. 安装 Jenkins
2. 配置 Jenkins
3. Jenkins 部署脚本
- 业务微服务 shell 部署脚本，示例请查看：doc/jenkins-shell/myopia-business.sh
- 网关微服务 shell 部署脚本，示例请查看：doc/jenkins-shell/myopia-gateway.sh
- 授权中心微服务 shell 部署脚本，示例请查看：doc/jenkins-shell/myopia-oauth.sh
4. Jenkins 需要创建6个部署任务
- 后端
    - 网关服务，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pac-backend
    - 授权服务，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pac-backend
    - 业务服务，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pac-backend
- 前端
    - 家长端，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pac-parent-frontend
    - 管理端，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pac-management-frontend
    - 筛查报告，工程地址：https://git.vistel.cn/web/myopia-pac/myopia-pdf-report

### 修改 Nginx 配置
 1. 由于管理端、家长端、筛查PDF报告的属于不同的前端工程，若都在同台主机上，则需要不同端口来监听进而区别开
 2. nginx.conf 中增加应用服务配置内容，示例详情查看：doc/nginx.conf

### 初始化数据
- 需要到管理平台的“权限集合包设置”菜单完成筛查端管理员角色权限初始化
- 执行district.sql初始化表d_district，由于district.sql文件较大，放在了OneDrive

## 开发规范约定
### 团队Java代码规范
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
