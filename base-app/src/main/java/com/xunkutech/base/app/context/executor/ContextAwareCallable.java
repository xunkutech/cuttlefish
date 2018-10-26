package com.xunkutech.base.app.context.executor;

import com.xunkutech.base.app.context.AppContext;
import com.xunkutech.base.app.context.AppContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;

public class ContextAwareCallable<T> implements Callable<T> {
    private static final Logger logger = LoggerFactory.getLogger(ContextAwareCallable.class);

    private Callable<T> task;
    private RequestAttributes requestContext;
    private LocaleContext localeContext;
    private AppContext appContext;

    public ContextAwareCallable(Callable<T> task, RequestAttributes requestContext, LocaleContext localeContext, AppContext appContext) {
        this.task = task;
        this.requestContext = requestContext;
        this.localeContext = localeContext;
        this.appContext = appContext;
    }

    @Override
    public T call() throws Exception {
        logger.debug("Setting RequestContextHolder, LocaleContextHolder and AppContextHolder after borrow thread from pool");
        if (requestContext != null) {
            RequestContextHolder.setRequestAttributes(requestContext);
        }

        if (localeContext != null) {
            LocaleContextHolder.setLocaleContext(localeContext);
        }

        if (appContext != null) {
            AppContextHolder.setAppContext(appContext);
        }

        try {
            return task.call();
        } finally {
            logger.debug("Reset RequestContextHolder, LocaleContextHolder and AppContextHolder before return thread to pool");
            RequestContextHolder.resetRequestAttributes();
            LocaleContextHolder.resetLocaleContext();
            AppContextHolder.resetAppContext();
        }
    }
}