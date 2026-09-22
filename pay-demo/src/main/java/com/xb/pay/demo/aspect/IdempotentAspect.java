package com.xb.pay.demo.aspect;

import com.xb.pay.common.annotation.Idempotent;
import com.xb.pay.common.exception.PayException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IdempotentAspect - 幂等性 AOP 切面，拦截 @Idempotent 注解标记的方法
 *
 * 通过 SpEL 解析方法参数生成幂等 key，用 ConcurrentHashMap 模拟 Redis 缓存。
 * 在 TTL 窗口期内重复请求直接抛出 PayException，防止支付接口被重复调用。
 *
 * @author ibqy
 */
@Aspect
@Component
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);
    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 环绕通知：解析 SpEL 生成幂等 key，在 TTL 内拦截重复请求
     * @param pjp 连接点
     * @param idempotent 幂等注解实例
     * @return 目标方法返回值
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String[] paramNames = nameDiscoverer.getParameterNames(sig.getMethod());
        Object[] args = pjp.getArgs();

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                ctx.setVariable(paramNames[i], args[i]);
            }
        }

        String key = parser.parseExpression(idempotent.key()).getValue(ctx, String.class);
        long now = System.currentTimeMillis();
        Long deadline = cache.get(key);

        if (deadline != null && deadline > now) {
            log.warn("幂等拦截: key={}, 剩余TTL={}ms", key, deadline - now);
            throw new PayException("IDEMPOTENT", "重复请求: " + key);
        }

        cache.put(key, now + idempotent.ttl() * 1000L);
        log.info("幂等通过: key={}, TTL={}s", key, idempotent.ttl());
        return pjp.proceed();
    }
}
