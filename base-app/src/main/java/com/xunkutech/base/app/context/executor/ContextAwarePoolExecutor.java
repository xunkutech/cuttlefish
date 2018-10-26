package com.xunkutech.base.app.context.executor;

import com.xunkutech.base.app.context.AppContextHolder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ContextAwarePoolExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(Runnable task) {
        super.execute(new ContextAwareRunnable(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()));
    }


    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(new ContextAwareRunnable(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(new ContextAwareRunnable(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new ContextAwareCallable<>(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return super.submitListenable(new ContextAwareRunnable(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(new ContextAwareCallable<>(task,
                RequestContextHolder.currentRequestAttributes(),
                LocaleContextHolder.getLocaleContext(),
                AppContextHolder.currentAppContext()));
    }

}