package ${package.Controller};

<#-- 新增：允许跨域注解 -->
import org.springframework.web.bind.annotation.CrossOrigin;
<#-- end -->
import org.springframework.web.bind.annotation.RequestMapping;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
<#-- 改动点：引入依赖jar-->
import com.wupol.myopia.base.handler.ResponseResultBody;
import ${package.Entity}.${entity};
import ${package.ServiceImpl}.${table.serviceImplName};
<#-- end -->

/**
 * @Author ${author}
 * @Date ${date}
 */
<#-- 新增：允许跨域注解 -->
@ResponseResultBody
@CrossOrigin
<#-- end -->
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
<#-- 改动点：添加了<${table.serviceImplName}, ${entity}> -->
public class ${table.controllerName} extends ${superControllerClass}<${table.serviceImplName}, ${entity}> {
<#else>
public class ${table.controllerName} {
</#if>

}
</#if>
