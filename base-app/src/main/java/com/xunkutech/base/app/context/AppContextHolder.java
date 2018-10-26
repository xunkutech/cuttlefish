package com.xunkutech.base.app.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AppContextHolder {
    private static final Logger logger = LoggerFactory.getLogger(AppContextHolder.class);

    private static final ThreadLocal<AppContext> appContextHolder =
            new NamedThreadLocal<>("App context");

    public static AppContext currentAppContext() {
        AppContext appContext = appContextHolder.get();
        if (null == appContext) {
//            throw new IllegalStateException("No thread-bound ServletRequestAttributes found: " +
//                    "Are you referring to request attributes outside of an actual web request, " +
//                    "or processing a request outside of the originally receiving thread? " +
//                    "If you are actually operating within a web request and still receive this message, " +
//                    "your code is probably running outside of DispatcherServlet/DispatcherPortlet: " +
//                    "In this case, use RequestContextListener or RequestContextFilter to expose the current request.");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (null == attributes) {
                throw new IllegalStateException("No thread-bound ServletRequestAttributes found: " +
                        "Are you referring to request attributes outside of an actual web request, " +
                        "or processing a request outside of the originally receiving thread? " +
                        "If you are actually operating within a web request and still receive this message, " +
                        "your code is probably running outside of DispatcherServlet/DispatcherPortlet: " +
                        "In this case, use RequestContextListener or RequestContextFilter to expose the current request.");
            }

            appContext = new AppContext(attributes.getRequest(), attributes.getResponse(), LocaleContextHolder.getLocale());
            appContextHolder.set(appContext);
            logger.debug("Created AppContext with locale: {}", appContext.getLocale());
        }
        return appContext;
    }

    public static void resetAppContext() {
        logger.debug("Reset current AppContext");
        appContextHolder.remove();
    }

    public static void setAppContext(AppContext appContext) {
        if (appContext == null) {
            resetAppContext();
            return;
        }
        appContextHolder.set(appContext);
    }

//
//    @Override
//    public void onApplicationEvent(RequestHandledEvent event) {
//        logger.debug("Reset app context on Request Handled Event");
//        AppContextHolder.resetAppContext();
//    }
}
