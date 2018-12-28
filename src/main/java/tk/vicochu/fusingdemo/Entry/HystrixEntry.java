package tk.vicochu.fusingdemo.Entry;

import com.github.rholder.retry.*;
import com.netflix.hystrix.*;
import org.springframework.beans.factory.annotation.Autowired;
import tk.vicochu.fusingdemo.Service.AbstractService;
import tk.vicochu.fusingdemo.Service.ServiceA;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HystrixEntry extends HystrixCommand<String> {

    @Autowired
    private Executor executor;
    private String name;

    private static Random random = new Random();

    private static Callable<Boolean> retryCall = () -> {
        int num = random.nextInt(50);
        System.out.println(num);
        if (num < 20) {
            return true;
        } else {
            throw new RuntimeException();
        }
    };

    Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .withWaitStrategy(WaitStrategies.fixedWait(10, TimeUnit.SECONDS))
            .retryIfException()
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .build();


    public HystrixEntry(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HystrixEntry"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("Entry"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerEnabled(true)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(3000)
                        .withCircuitBreakerErrorThresholdPercentage(40)
                        .withCircuitBreakerRequestVolumeThreshold(5)
                        .withCircuitBreakerSleepWindowInMilliseconds(5000)
                        .withFallbackEnabled(true)
                )
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HystrixPool"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(2)
                        .withMaximumSize(5)
                        .withMaxQueueSize(2)
                )
        );

        this.name = name;
    }


    @Override
    protected String run() {
        try {
            retryer.call(retryCall);
            //模拟是否可用
            if ("true".equals(name)) {
                AbstractService service = new ServiceA();
                return service.print(name);
            } else {
                throw new RuntimeException();
            }
        } catch (ExecutionException e) {

        } catch (RetryException e) {
            System.out.println("retry failure");
        }
        return null;
    }

    //降级逻辑
    @Override
    protected String getFallback() {
        return executor.apply2().print(name);
    }

}
