package tk.vicochu.fusingdemo;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.Random;

public class SupplierTest {

    public static void main(String[] args) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig
                .custom()
                //失败百分比触发断路
                .failureRateThreshold(10)
                //哪些异常不会触发失败
//                .ignoreExceptions(RuntimeException.class)
                //哪些异常会触发失败
//                .recordExceptions()
                //自定义失败的触发
//                .recordFailure()
                //断路器处于关闭时记录请求的缓冲区大小 - int 2 ,请求必须达到2个才会可能触发断路器，否则就算一共来1个请求都失败也不会触发
                .ringBufferSizeInClosedState(8)
                //断路器处于半开时记录请求的缓冲区大小，其余同上
                .ringBufferSizeInHalfOpenState(8)
                //从开状态切换到半开状态前的时间
                .waitDurationInOpenState(Duration.ofMillis(1000))
                //经过开到半开状态转换时间后，自动切换为半开
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("breaker-demo", circuitBreakerConfig);
        //断路器记录成功时触发对应事件的consumer
//        circuitBreaker.getEventPublisher().onSuccess((event) -> System.out.println("----" + event.getEventType().name() + "----"));

        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                //一个时间周期内允许的请求数
                .limitForPeriod(2)
                //一个时间周期单位对应的时间
                .limitRefreshPeriod(Duration.ofSeconds(2))
                //等待请求的超时
                .timeoutDuration(Duration.ofMillis(2000))
                .build();

        RateLimiter rateLimiter = RateLimiter.of("ratelimiter-demo", rateLimiterConfig);

        RetryConfig retryConfig = RetryConfig
                .custom()
                //哪些Exception会触发retry
                .retryExceptions(RuntimeException.class)
                //根据一个Predication的boolean返回值来确定哪些Exception会触发retry
//                .retryOnException()
                //哪些Exception不会触发retry
                .ignoreExceptions()
                //返回什么样的结果会触发retry
//                .retryOnResult()
                //获取重试间隔的function
//                .intervalFunction()
                //最大重试次数
                .maxAttempts(2)
                //重试间隔
                .waitDuration(Duration.ofMillis(200))
                .build();

        Retry retry = Retry.of("retry-demo", retryConfig);

//        Supplier<String> resultSupplier = () -> {
//            if (healthSupplier.get()) {
//                return "running";
//            } else {
//                return "stop";
//            }
//        };
        CheckedFunction0<Boolean> healthFunction = Retry.decorateCheckedSupplier(retry, () -> {
            Random random = new Random();
            System.out.println("executing health check");
            if (random.nextInt(50) < 25) {
                System.out.println("available");
                return true;
            } else {
                System.out.println("unavailable");
                throw new RuntimeException();
            }
        });

        CheckedFunction0<String> rateLimiterFunction = RateLimiter.decorateCheckedSupplier(rateLimiter, () -> {
            if (healthFunction.apply()) {
                return "success";
            } else {
                return "failure";
            }
        });

        CheckedFunction0<String> resultFunction = CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> {
            if (rateLimiterFunction.apply().equals("success")) {
                return "ok";
            } else
                return "bad";
        });

        for (int i = 0; i < 20; i++) {
            System.out.println(Try.of(resultFunction).recover(Throwable -> "fallback").get());
        }

    }
}
