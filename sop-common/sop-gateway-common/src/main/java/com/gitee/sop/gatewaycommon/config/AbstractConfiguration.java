package com.gitee.sop.gatewaycommon.config;

import com.gitee.sop.gatewaycommon.bean.ApiConfig;
import com.gitee.sop.gatewaycommon.bean.ApiContext;
import com.gitee.sop.gatewaycommon.bean.BeanInitializer;
import com.gitee.sop.gatewaycommon.bean.SpringContext;
import com.gitee.sop.gatewaycommon.gateway.loadbalancer.NacosServerIntrospector;
import com.gitee.sop.gatewaycommon.interceptor.RouteInterceptor;
import com.gitee.sop.gatewaycommon.limit.LimitManager;
import com.gitee.sop.gatewaycommon.manager.EnvGrayManager;
import com.gitee.sop.gatewaycommon.manager.EnvironmentContext;
import com.gitee.sop.gatewaycommon.manager.EnvironmentKeys;
import com.gitee.sop.gatewaycommon.manager.IPBlacklistManager;
import com.gitee.sop.gatewaycommon.manager.IsvRoutePermissionManager;
import com.gitee.sop.gatewaycommon.manager.LimitConfigManager;
import com.gitee.sop.gatewaycommon.manager.RouteConfigManager;
import com.gitee.sop.gatewaycommon.manager.RouteRepositoryContext;
import com.gitee.sop.gatewaycommon.message.ErrorFactory;
import com.gitee.sop.gatewaycommon.monitor.MonitorManager;
import com.gitee.sop.gatewaycommon.param.ParameterFormatter;
import com.gitee.sop.gatewaycommon.route.RegistryListener;
import com.gitee.sop.gatewaycommon.route.ServiceListener;
import com.gitee.sop.gatewaycommon.route.ServiceRouteListener;
import com.gitee.sop.gatewaycommon.secret.IsvManager;
import com.gitee.sop.gatewaycommon.sync.SopAsyncConfigurer;
import com.gitee.sop.gatewaycommon.util.RouteInterceptorUtil;
import com.gitee.sop.gatewaycommon.validate.SignConfig;
import com.gitee.sop.gatewaycommon.validate.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tanghc
 */
@Slf4j
public class AbstractConfiguration implements ApplicationContextAware, ApplicationRunner {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private volatile boolean isStartupCompleted;

    @Autowired
    protected Environment environment;

    @Autowired
    private RegistryListener registryListener;

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    /**
     * nacos事件监听
     *
     * @param heartbeatEvent
     */
    @EventListener(classes = HeartbeatEvent.class)
    public void listenEvent(ApplicationEvent heartbeatEvent) {
        // 没有启动完毕先等待
        if (!isStartupCompleted) {
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                log.error("condition.await() error", e);
            } finally {
                lock.unlock();
            }
        }
        registryListener.onEvent(heartbeatEvent);
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceListener serviceListener() {
        return new ServiceRouteListener();
    }

    @Bean
    @ConditionalOnMissingBean
    Validator validator() {
        return ApiConfig.getInstance().getValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    IsvManager isvManager() {
        return ApiConfig.getInstance().getIsvManager();
    }

    @Bean
    @ConditionalOnMissingBean
    IsvRoutePermissionManager isvRoutePermissionManager() {
        return ApiConfig.getInstance().getIsvRoutePermissionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    RouteConfigManager routeConfigManager() {
        return ApiConfig.getInstance().getRouteConfigManager();
    }

    @Bean
    @ConditionalOnMissingBean
    LimitConfigManager limitConfigManager() {
        return ApiConfig.getInstance().getLimitConfigManager();
    }

    @Bean
    @ConditionalOnMissingBean
    LimitManager limitManager() {
        return ApiConfig.getInstance().getLimitManager();
    }

    @Bean
    @ConditionalOnMissingBean
    IPBlacklistManager ipBlacklistManager() {
        return ApiConfig.getInstance().getIpBlacklistManager();
    }

    @Bean
    @ConditionalOnMissingBean
    EnvGrayManager envGrayManager() {
        return ApiConfig.getInstance().getUserKeyManager();
    }

    @Bean
    @ConditionalOnMissingBean
    ParameterFormatter parameterFormatter() {
        return ApiConfig.getInstance().getParameterFormatter();
    }

    @Bean
    public SopAsyncConfigurer sopAsyncConfigurer(@Value("${sop.monitor-route-interceptor.thread-pool-size:4}")
                                                                int threadPoolSize) {
        return new SopAsyncConfigurer("gatewayAsync", threadPoolSize);
    }

    @Bean
    @ConditionalOnMissingBean
    public MonitorManager monitorManager() {
        return new MonitorManager();
    }

    /**
     * 跨域过滤器，gateway采用react形式，需要使用reactive包下的UrlBasedCorsConfigurationSource
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("sop.gateway-index-path")
    public CorsWebFilter corsWebFilter() {
        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", createCorsConfiguration());
        return new CorsWebFilter(source);
    }

    private CorsConfiguration createCorsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    /**
     * 负责获取nacos实例的metadata
     * @return
     */
    @Bean
    @ConditionalOnProperty("spring.cloud.nacos.discovery.server-addr")
    ServerIntrospector nacosServerIntrospector() {
        return new NacosServerIntrospector();
    }



    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.isStartupCompleted = true;
        lock.lock();
        condition.signalAll();
        lock.unlock();
        after();
    }

    @PostConstruct
    private void post() {
        EnvironmentContext.setEnvironment(environment);
        SpringContext.setApplicationContext(applicationContext);
    }

    public final void after() {
        if (RouteRepositoryContext.getRouteRepository() == null) {
            throw new IllegalArgumentException("RouteRepositoryContext.setRouteRepository()方法未使用");
        }
        String serverName = EnvironmentKeys.SPRING_APPLICATION_NAME.getValue();
        if (!"sop-gateway".equals(serverName)) {
            throw new IllegalArgumentException("spring.application.name必须为sop-gateway");
        }
        String urlencode = EnvironmentKeys.SIGN_URLENCODE.getValue();
        if ("true".equals(urlencode)) {
            SignConfig.enableUrlencodeMode();
        }

        initMessage();
        initBeanInitializer();
        initRouteInterceptor();
        doAfter();
    }

    protected void initBeanInitializer() {
        Map<String, BeanInitializer> beanInitializerMap = applicationContext.getBeansOfType(BeanInitializer.class);
        beanInitializerMap.values().forEach(BeanInitializer::load);
    }

    protected void initRouteInterceptor() {
        Map<String, RouteInterceptor> routeInterceptorMap = applicationContext.getBeansOfType(RouteInterceptor.class);
        Collection<RouteInterceptor> routeInterceptors = new ArrayList<>(routeInterceptorMap.values());
        RouteInterceptorUtil.addInterceptors(routeInterceptors);
    }

    protected void doAfter() {

    }

    protected void initMessage() {
        ErrorFactory.initMessageSource(ApiContext.getApiConfig().getI18nModules());
    }
}
