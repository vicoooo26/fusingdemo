package tk.vicochu.fusingdemo.Entry;

import com.github.rholder.retry.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tk.vicochu.fusingdemo.Service.AbstractService;
import tk.vicochu.fusingdemo.Service.ServiceA;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class HytrixEntryAnother {

    Random random = new Random();

    @HystrixCommand(ignoreExceptions = RetryException.class, commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "1000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "40"),

    }, fallbackMethod = "getFallback")
    public String run(@PathVariable("value") String name) {
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
            return "retry failure";
        }
        return null;
    }

    private Callable<Boolean> retryCall = () -> {
        int num = random.nextInt(100);
        System.out.println(num);
        if (num < 50) {
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

    protected String getFallback(String value) {
//        return "fallback";
        return new Executor().apply2().print(value);
    }
}
