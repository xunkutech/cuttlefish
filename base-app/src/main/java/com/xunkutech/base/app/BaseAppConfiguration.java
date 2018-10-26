package com.xunkutech.base.app;

import com.google.gson.Gson;
import com.xunkutech.base.app.context.executor.ContextAwarePoolExecutor;
import com.xunkutech.base.app.filter.ContextAwareFilter;
import com.xunkutech.base.model.util.JsonUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.Executor;

@Configuration
public class BaseAppConfiguration implements WebMvcConfigurerDefault {

    public static String HOSTNAME;
    public static String PID;

    static {
        int idx = ManagementFactory.getRuntimeMXBean().getName().indexOf('@');

        if (idx < 0) {
            HOSTNAME = "unknown";
            PID = "0";
        } else {
            PID = ManagementFactory.getRuntimeMXBean().getName().substring(0, idx);
            HOSTNAME = ManagementFactory.getRuntimeMXBean().getName().substring(idx + 1);
        }
    }

    @Bean
    public Gson gson() {
        return JsonUtils.GSON;
    }

    @Bean(name = "async-service")
    public Executor workerExecutor() {
        ContextAwarePoolExecutor executor = new ContextAwarePoolExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix(new StringJoiner("-").add(HOSTNAME.replace('-', '_')).add(PID).add("service-").toString());
        return executor;
    }

    @Bean
    public LocaleContextResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        resolver.setCookieName("ctx-locale");
        resolver.setCookieMaxAge(4800);
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setBasename("classpath:locale/messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("locale");
        registry.addInterceptor(interceptor);
    }

    @Bean
    public FilterRegistrationBean contextAwareFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setFilter(new ContextAwareFilter());
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);

        return filterRegistrationBean;
    }



    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

}
