package tk.vicochu.fusingdemo.Entry;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.vicochu.fusingdemo.Service.ServiceA;
import tk.vicochu.fusingdemo.Service.ServiceB;

import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;

@Component
public class ResilienceEntry {
    @Autowired
    private Executor executor;

    public Supplier<String> realFun() {
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
        circuitBreaker.getEventPublisher().onSuccess((event) -> System.out.println("----" + event.getEventType().name() + "----"));

        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                //一个时间周期内允许的请求数
                .limitForPeriod(1)
                //时间窗滑动的间距
                .limitRefreshPeriod(Duration.ofDays(1))
                //等待请求的超时
                .timeoutDuration(Duration.ofMillis(1000))
                .build();
        
        RateLimiter rateLimiter = RateLimiter.of("ratelimiter-demo", rateLimiterConfig);

        RetryConfig retryConfig = RetryConfig
                .custom()
                //哪些Exception会触发retry
                .retryExceptions(Throwable.class)
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


        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, () -> run());

        decoratedSupplier = RateLimiter.decorateSupplier(rateLimiter, decoratedSupplier);


        decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);

        return decoratedSupplier;
    }


    private String run() {
        Random random = new Random();
        int num = random.nextInt(100);
        System.out.println("--executing--");
//        throw new RuntimeException();
        if (num < 50) {
            throw new RuntimeException();
        } else {
            return new ServiceA().print("success");
        }
    }

    private String fallback() {
        return executor.apply2().print("fallback");
    }

    //recover中调用降级逻辑
    public String realDo() {
        return Try.ofSupplier(realFun()).recover(throwable -> fallback()).get();
    }

}
