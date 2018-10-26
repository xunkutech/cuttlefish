package com.xunkutech.base.app;

import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * An implementation of {@link WebMvcConfigurer} with empty methods allowing
 * subclasses to override only the methods they're interested in.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public interface WebMvcConfigurerDefault extends WebMvcConfigurer {

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configurePathMatch(PathMatchConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addFormatters(FormatterRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addInterceptors(InterceptorRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addCorsMappings(CorsRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addViewControllers(ViewControllerRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureViewResolvers(ViewResolverRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns {@code null}.
     */
    @Override
    default Validator getValidator() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns {@code null}.
     */
    @Override
    default MessageCodesResolver getMessageCodesResolver() {
        return null;
    }
}
