package ee.tenman.automaks.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.UUID;

import static ee.tenman.automaks.config.TimeUtility.durationInSeconds;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String TRANSACTION_ID = "transactionId";

    private static void setTransactionId(UUID uuid) {
        String transactionId = uuid.toString();
        if (transactionId != null && !transactionId.isEmpty()) {
            MDC.put(TRANSACTION_ID, "[" + transactionId + "] ");
        } else {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private static void clearTransactionId() {
        MDC.remove(TRANSACTION_ID);
    }

    @Around("@annotation(ee.tenman.automaks.config.aspect.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.getReturnType().equals(Void.TYPE)) {
            log.error("Loggable annotation is used on a method with no return type: {}", method.getName());
            throw new IllegalStateException("Loggable annotation cannot be used on methods with no return type");
        }

        long startTime = System.nanoTime();
        setTransactionId(UUID.randomUUID());
        Object result = null;
        try {
            log.info("Entered method: {} with arguments: {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
            result = joinPoint.proceed();
            if (result instanceof Mono) {
                return handleMonoResult((Mono<?>) result, startTime);
            }
            log.info("Exited method: {} with result: {} in {} seconds", joinPoint.getSignature().toShortString(), result,
                    durationInSeconds(startTime).asString());
            return result;
        } catch (Throwable throwable) {
            log.error("Exception in method: {}", joinPoint.getSignature().toShortString(), throwable);
            throw throwable;
        } finally {
            if (!(result instanceof Mono)) {
                clearTransactionId();
            }
        }
    }

    private <T> Mono<T> handleMonoResult(Mono<T> mono, long startTime) {
        return mono.doOnNext(item -> log.info("Result of Mono: {} in {} seconds", item, durationInSeconds(startTime).asString()))
                .doOnError(error -> log.error("Error in Mono: {}", error.getMessage()))
                .doFinally(signalType -> clearTransactionId());
    }

}


