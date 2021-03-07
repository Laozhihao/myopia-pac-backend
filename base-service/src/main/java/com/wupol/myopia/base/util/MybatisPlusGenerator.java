package com.wupol.myopia.base.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * 代码生成器：
 * 1.执行main方法
 * 2.控制台输入模块名和数据表名，回车
 * 3.自动生成对应项目目录中
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
public class MybatisPlusGenerator {

    /** 服务名 */
    private static String serviceAliasName;
    /** 模块名 */
    private static String moduleName;
    /** 类基础包路径 */
    private static String classBasePackage;
    /** 资源基础包路径 */
    private static String resourcesBasePackage;

    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();
        //全局配置（自定义文件命名规则）
        mpg.setGlobalConfig(getGlobalConfig());
        //数据源配置
        mpg.setDataSource(getDataSourceConfig());
        //配置文件的存放路径
        // 1、通过该配置，指定生成文件的保存路径，路径=Parent+ModuleName+Entity/Mapper/Service/...
        // 2、优先以InjectionConfig自定义配置中指定的保存路径为准，若没有则以这里的为准
        // 3、如果设置了路径，即使TemplateConfig中配置了不生成该模块，依然会生成一个空文件夹
        mpg.setPackageInfo(getPackageConfig());
        //配置自定义模板的路径（如果没有指定，则读取自带的模板）、配置不需要生成的模块
        // 1、默认优先读取系统src/main/resources/templates目录下模板，没有再读取框架自带的模板
        // 2、把模板放置templates目录下即可，不需要配置模板路径
        // 3、如果模板的名称与源码的不一致或者存放非templates目录下，则需要明确指定模板路径
        // 4、如果把路径设置为 空 OR Null，将不生成该模块
        // 5、自定义模板可以参考源码中模板，地址：
        // https://gitee.com/baomidou/mybatis-plus/tree/3.0/mybatis-plus-generator/src/main/resources/templates
        // 6、自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        mpg.setTemplate(getTemplateConfig());
        //配置数据库表映射实体类时的名字转换规则、指定新类extends的base类
        // 1、如果controller、entity没有指定base类，则不继承任何类
        // 2、service、mapper、serviceImpl没有指定base类，则会继承自带的base类
        mpg.setStrategy(getStrategyConfig());
        //自定义配置(例如指定xml的模板和保存路径，这里暂时不需要，需要的时候再重新运行main即可，已有的不会覆盖)
        mpg.setCfg(getInjectionConfig(mpg.getPackageInfo()));
        //选择 freemarker 引擎，默认 Veloctiy
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        //执行生成
        mpg.execute();
        // 打印注入设置【可无】
//        System.err.println(mpg.getCfg().getMap().get("abc"));
    }

    private static GlobalConfig getGlobalConfig() {
        //获取根包名
        getBasePackagePath();
        //全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setAuthor("HaoHao")
                // 文件输出的根路径，和后面指定的包路径组成文件的最终保存路径 "F:/wupol/myopia-pac-backend/myopia-business/management/src/main/java"
                .setOutputDir(classBasePackage)
                // 是否覆盖同名文件，默认是false
                .setFileOverride(false)
                // 是否支持AR模式【即MP自带封装好的crud方法】，不需要ActiveRecord特性的请改为false
                .setActiveRecord(false)
                // XML 二级缓存
                .setEnableCache(false)
                // XML 生成基本的resultMap
                .setBaseResultMap(true)
                // XML 生成基本的SQL片段
                .setBaseColumnList(true)
                // 主键策略
                .setIdType(IdType.AUTO)
                // 生成文件后是否打开文件夹
                .setOpen(false)
                // 指定数据库表字段为date的时候的转换方式【ONLY_DATE表示始终转为date类型】
                .setDateType(DateType.ONLY_DATE)
                // 实体类的名称，以这里输入的为准，没有指定，则根据表名驼峰法表示
                .setEntityName(scanner("实体名（首字母大写）"))
                .setKotlin(false)
                .setSwagger2(false)
                /* 自定义文件命名 */
                .setMapperName(gc.getEntityName()+"Mapper")
                .setXmlName(gc.getEntityName()+"Mapper")
                .setServiceName(gc.getEntityName()+"Service")
                // 这里去掉service接口模块，只保留serviceImpl模板，故直接命名为xxxService
                .setServiceImplName(gc.getEntityName()+"Service")
                .setControllerName(gc.getEntityName()+"Controller");
        return gc;
    }

    private static DataSourceConfig getDataSourceConfig() {
        // 数据源配置【数据库地址】
        DataSourceConfig dsc = new DataSourceConfig();
//        dsc.setTypeConvert(new MySqlTypeConvert() {
//            // 自定义数据库表字段类型转换【可选】
//            @Override
//            public DbColumnType processTypeConvert(GlobalConfig globalConfig,String fieldType) {
//                System.out.println("转换类型：" + fieldType);
//                // 注意！！processTypeConvert 存在默认类型转换，如果不是你要的效果请自定义返回、非如下直接返回。
//                return (DbColumnType) super.processTypeConvert(globalConfig,fieldType);
//            }
//        });
        // Mysql
        dsc.setDbType(DbType.MYSQL)
                .setUrl("jdbc:mysql://localhost:3306/myopia?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8")
                .setDriverName("com.mysql.cj.jdbc.Driver")
                .setUsername("root")
                .setPassword("123456");
        return dsc;
    }

    private static PackageConfig getPackageConfig() {
        // 包名配置
        // 1、通过该配置，指定生成文件的保存路径，路径=Parent+ModuleName+Entity/Mapper/Service/...
        // 2、优先以InjectionConfig自定义配置中指定的保存路径为准，若没有则以这里的为准
        // 3、如果设置了路径，即使TemplateConfig中配置了不生成该模块，依然会生成一个空文件夹
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.wupol.myopia" + (StringUtils.isNotBlank(serviceAliasName) ? "." + serviceAliasName : ""))
                .setEntity("domain.model")
                .setMapper("domain.mapper")
                //.setService("service") //不生成service接口模块，不需要生成的模块则不需要设置包名
                .setServiceImpl("service")
                .setController("controller")
                //.setXml("mapping") //会在Parent+ModuleName+Xml的目录下生成，并不是在resources
                .setModuleName(moduleName);
                //.setPathInfo(null);
        return pc;
    }

    private static TemplateConfig getTemplateConfig() {
        //设置不生成的模块、或设置模板的路径覆盖默认值
        // 1、默认优先读取系统src/main/resources/templates目录下模板，没有再读取框架自带的模板
        // 2、把模板放置templates目录下即可，不需要配置模板路径
        // 3、如果模板的名称与源码的不一致或者存放非templates目录下，则需要明确指定模板路径
        // 4、如果把路径设置为 空 OR Null，将不生成该模块
        // 5、自定义模板可以参考源码中模板，地址：
        // https://gitee.com/baomidou/mybatis-plus/tree/3.0/mybatis-plus-generator/src/main/resources/templates
        // 6、自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        TemplateConfig tc = new TemplateConfig();
        tc.setXml(null).setService(null);
        // tc.setController("templates/wupol/controller.java");
        // tc.setEntity("templates/wupol/entity.java");
        // tc.setMapper("...");
        // tc.setXml("...");
        // tc.setService("...");
        // tc.setServiceImpl("...");
        return tc;
    }

    private static StrategyConfig getStrategyConfig() {
        //数据库表与实体类的映射策略配置【设置生成的类的父类、生成的表、表字段生成实体的命名规则】
        // 1、如果controller、entity没有设置SuperXXXClass，则不会extends任何父类
        // 2、如果service、mapper、ServiceImpl没有设置SuperXXXClass，则会extends自带的Base类，分别是
        // com.baomidou.mybatisplus.extension.service.IService
        // com.baomidou.mybatisplus.core.mapper.BaseMapper
        // com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
        StrategyConfig strategy = new StrategyConfig();
        // 全局大写命名 ，ORACLE注意
        strategy.setCapitalMode(true)
                // 数据库表映射到实体的命名策略，underline_to_camel表示下划线转驼峰法
                .setNaming(NamingStrategy.underline_to_camel)
                // 列名规则下划线转驼峰法
                .setColumnNaming(NamingStrategy.underline_to_camel)
                // 需要生成的表，多个英文逗号分割
                .setInclude(scanner("表名").split(","))
                // 此处可以修改为您的表前缀
                .setTablePrefix("o_")
                // 排除生成的表
                // strategy.setExclude(new String[]{"test"});
                // 自定义实体，公共字段
                // strategy.setSuperEntityColumns(new String[] { "test_id", "age" });
                // 自定义 serviceImpl类父类
                .setSuperServiceImplClass("com.wupol.myopia.base.service.BaseService")
                // 自定义 entity父类
//                .setSuperEntityClass("com.syb.senying.base.domain.BaseEntity")
                // 自定义 mapper父类
//                .setSuperMapperClass("com.syb.senying.base.domain.BaseMapper")
                // 自定义 service父类
//                .setSuperServiceClass("com.syb.senying.base.service.BaseService")
                // 自定义 controller父类
                .setSuperControllerClass("com.wupol.myopia.base.controller.BaseController")
                .setRestControllerStyle(true)
                //实体类是否使用Lombok注解，true则没有get/set方法
                .setEntityLombokModel(true)
                .setControllerMappingHyphenStyle(false);
            /*strategy.setSkipView(false);
            strategy.setFieldPrefix("");
            strategy.setEntityBooleanColumnRemoveIsPrefix(true);
            strategy.setVersionFieldName(null);
            strategy.setLogicDeleteFieldName(null);
            strategy.setTableFillList(null);*/
        // 【实体】是否生成字段常量（默认 false）
        // public static final String ID = "test_id";
        // strategy.setEntityColumnConstant(true);
        // 【实体】是否为构建者模型（默认 false），Lombok的@Data已经可以代替
        // public User setName(String name) {this.name = name; return this;}
        // strategy.setEntityBuilderModel(true);
        return strategy;
    }

    private static InjectionConfig getInjectionConfig(PackageConfig pc) {
        // 注入自定义配置，通过该配置，可注入自定义参数等操作以实现个性化操作
        // 这里自定义了mapper xml的模板和xml的输入路径
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return resourcesBasePackage + "/mapping/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);

//        InjectionConfig cfg = new InjectionConfig() {
//            @Override
//            public void initMap() {
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
//                this.setMap(map);
//            }
//        };
//        cfg.setMap(null);
//        cfg.setConfig(null);
//        cfg.setFileCreate(null);
//
//        // 自定义 xxList.jsp 生成
//        List<FileOutConfig> focList = new ArrayList<>();
//        focList.add(new FileOutConfig("/template/list.jsp.vm") {
//            @Override
//            public String outputFile(TableInfo tableInfo) {
//                // 自定义输入文件名称
//                return "D://my_" + tableInfo.getEntityName() + ".jsp";
//            }
//        });
//        cfg.setFileOutConfigList(focList);
//        mpg.setCfg(cfg);

//        // 调整 xml生成目录
//        focList.add(new FileOutConfig("/templates/mapper.xml.vm") {
//            @Override
//            public String outputFile(TableInfo tableInfo) {
//                return "/develop/code/xml/" + tableInfo.getEntityName() + ".xml";
//            }
//        });
//        cfg.setFileOutConfigList(focList);
//        mpg.setCfg(cfg);
        return cfg;
    }

    /**
     * 读取控制台内容
     */
    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入" + tip + "：");
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    /**
     * 获取基础包路径
     **/
    private static void getBasePackagePath() {
        String serviceAndModuleName = scanner("微服务名（为多module的服务则输入“服务名/模块名”）");
        String[] nameArray =  serviceAndModuleName.split("/");
        String[] array = nameArray[0].split("-");
        String name = array.length >= 2 ? array[1] : array[0];
        if (nameArray.length >= 2) {
            moduleName = nameArray[1];
            serviceAliasName = name;
        } else {
            moduleName = name;
            serviceAliasName = null;
        }
        // 根包名，如 F:/wupol/myopia-pac-backend/myopia-business/management/src/main/java  myopia-oauth
        classBasePackage = System.getProperty("user.dir")+ "/" + serviceAndModuleName + "/src/main/java";
        resourcesBasePackage = System.getProperty("user.dir")+ "/" + serviceAndModuleName + "/src/main/resources";
    }
}