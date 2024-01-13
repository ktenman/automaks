package ee.tenman.automaks.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(ee.tenman.automaks.config.aspect.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String uuid = UUID.randomUUID().toString();
        MDC.put("transactionId", uuid);
        Object result = null;
        try {
            log.info("Entered method: {} with arguments: {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
            result = joinPoint.proceed();
            if (result instanceof Mono) {
                return handleMonoResult((Mono<?>) result);
            }
            log.info("Exited method: {} with result: {}", joinPoint.getSignature().toShortString(), result);
            return result;
        } catch (Throwable throwable) {
            log.error("Exception in method: {}", joinPoint.getSignature().toShortString(), throwable);
            throw throwable;
        } finally {
            if (!(result instanceof Mono)) {
                MDC.remove("transactionId");
            }
        }
    }

    private <T> Mono<T> handleMonoResult(Mono<T> mono) {
        return mono.doOnNext(item -> log.info("Result of Mono: {}", item))
                .doOnError(error -> log.error("Error in Mono: {}", error.getMessage()))
                .doFinally(signalType -> MDC.remove("transactionId"));
    }

}


