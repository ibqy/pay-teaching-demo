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

@Aspect
@Component
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);
    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

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
