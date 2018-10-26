package com.xunkutech.base.app.context;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Component
public class AppContextAccessor {

    public AppContext getAppContext() {
        return AppContextHolder.currentAppContext();
    }

    public HttpServletResponse getResponse() {
        return getAppContext().getResponse();
    }

    public HttpServletRequest getRequest() {
        return getAppContext().getRequest();
    }

    public Locale getLocale() {
        return getAppContext().getLocale();
    }

    public void put(Object obj) {
        getAppContext().put(obj);
    }

    public void put(Object key, Object obj) {
        getAppContext().put(key, obj);
    }

    public <T> T get(Class<T> clz) {
        return getAppContext().get(clz, clz);
    }

    public <T> T get(Object key, Class<T> clz) {
        return getAppContext().get(key, clz);
    }

    public AppContext.ClientInfo getClientInfo() {
        return getAppContext().getClientInfo();
    }

}
