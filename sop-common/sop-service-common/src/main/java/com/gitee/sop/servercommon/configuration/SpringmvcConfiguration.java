package com.gitee.sop.servercommon.configuration;

import com.gitee.sop.servercommon.bean.ServiceConfig;
import com.gitee.sop.servercommon.interceptor.ServiceContextInterceptor;
import com.gitee.sop.servercommon.message.ServiceErrorFactory;
import com.gitee.sop.servercommon.route.ServiceRouteController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author tanghc
 */
@Slf4j
public class SpringmvcConfiguration implements WebMvcConfigurer {

    public static final String METADATA_SERVER_CONTEXT_PATH = "server.servlet.context-path";

    public SpringmvcConfiguration() {
        ServiceConfig.getInstance().getI18nModules().add("i18n/isp/bizerror");
    }

    static {
        System.setProperty("eureka.instance.metadata-map.server.startup-time", String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 支持swagger-bootstrap-ui首页
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        // 支持默认swagger
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 解决controller返回字符串中文乱码问题
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter)converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(new ServiceContextInterceptor());
    }

    @Bean
    @ConditionalOnMissingBean
    GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceRouteController serviceRouteInfoHandler() {
        return new ServiceRouteController();
    }

    @PostConstruct
    public final void after() {
        log.info("-----spring容器加载完毕-----");
        initMessage();
        doAfter();
    }


    /**
     * spring容器加载完毕后执行
     */
    protected void doAfter() {

    }


    protected void initMessage() {
        ServiceErrorFactory.initMessageSource(ServiceConfig.getInstance().getI18nModules());
    }

}
