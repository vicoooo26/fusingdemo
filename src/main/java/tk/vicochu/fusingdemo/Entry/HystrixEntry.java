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

    private String name;

    private static Random random = new Random();

    private static Callable<Boolean> retryCall = () -> {
        int num = random.nextInt(100);
        System.out.println(num);
        if (num < 40) {
            return true;
        } else {
            throw new RuntimeException();
        }
    };

    Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
            .retryIfExceptionOfType(RuntimeException.class)
            .withStopStrategy(StopStrategies.stopAfterAttempt(2))
            .build();


    public HystrixEntry(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HystrixEntry"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("Entry"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerEnabled(true)
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        //如果设置不当，在重试期间会超时
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
            return getFallback();
        }
        return null;
    }

    //降级逻辑
    @Override
    protected String getFallback() {
//        return "fallback";
        return new Executor().apply2().print(name);
    }

}
