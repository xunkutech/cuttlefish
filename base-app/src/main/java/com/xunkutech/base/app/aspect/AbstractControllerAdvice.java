package com.xunkutech.base.app.aspect;

import com.xunkutech.base.app.annotation.ServiceHandler;
import com.xunkutech.base.app.context.AppContextHolder;
import com.xunkutech.base.app.service.AppService;
import com.xunkutech.base.model.util.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractControllerAdvice {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    @Qualifier("async-service")
    private Executor executor;

    private <T, R> CompletableFuture<R> invokeService(AppService<? super T, ? extends R> service, T input, boolean async) {
        if (async) {
            return CompletableFuture.supplyAsync(() -> service.handle(input), executor);
        }
        return CompletableFuture.completedFuture(service.handle(input));
    }

    @Pointcut(" @annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping)")
    final protected void mappingPointcut() {
    }

    protected abstract void methodPointcut();

    @Around("mappingPointcut() && methodPointcut()")
    private Object processController(final ProceedingJoinPoint pjp) throws Throwable {
        logger.debug("Controller Advice start.");
        final HttpServletRequest request = AppContextHolder.currentAppContext().getRequest();
        final HttpServletResponse response = AppContextHolder.currentAppContext().getResponse();
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        logger.debug("Handle Request: {} {} by {}.{}", requestMethod, requestURI, method.getDeclaringClass().getName(), method.getName());
        CompletableFuture<?> result;
        Object[] args = pjp.getArgs();
        before(args, request, response);
        ServiceHandler targetService = method.getAnnotation(ServiceHandler.class);
        if (null != targetService) {
            AppService service = locateService(targetService);
            Objects.requireNonNull(service);
            Object input;
            if (null != args && args.length > 0) {
                input = args[0];
                logger.debug("App Input:\n {}", JsonUtils.printJson(input));
            } else {
                input = null;
            }
            result = targetService.async() ? invokeService(service, input, true) :
                    invokeService(service, input, false);
        } else {
            result = (CompletableFuture<?>) pjp.proceed();
        }
        result.whenComplete((o, t) -> {
            if (null != t) {
                logger.error("Error in request: \"{}\", caused by: {}", requestURI, t.getMessage());
            }
            after(o, request, response);
            if (null != o) logger.debug("App Output:\n {}", JsonUtils.printJson(o));
        });
        logger.debug("Controller handled");
        return result;
    }

    //@ServiceHandler("myServiceImpl.method")
    private AppService locateService(ServiceHandler targetService) {
        if (StringUtils.hasText(targetService.value())) {
            return (input) -> {
                final StandardEvaluationContext context = new StandardEvaluationContext();
                context.setBeanResolver(new ServiceBeanResolver(ctx));
                context.setVariable("input", input);
                final SpelExpressionParser parser = new SpelExpressionParser();
                final Expression exp = parser.parseExpression("@" + targetService.value() + "(#input)");
                return exp.getValue(context);
            };
        }
        return ctx.getBean(targetService.serviceClass());
    }

    protected void before(Object[] args, HttpServletRequest request, HttpServletResponse response) {

    }

    protected void after(Object output, HttpServletRequest request, HttpServletResponse response) {

    }

    static class ServiceBeanResolver implements BeanResolver {

        private ApplicationContext ctx;

        ServiceBeanResolver(ApplicationContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Object resolve(EvaluationContext context, String beanName) throws AccessException {
            return ctx.getBean(beanName);
        }
    }

}
