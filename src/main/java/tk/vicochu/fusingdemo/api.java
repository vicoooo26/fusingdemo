package tk.vicochu.fusingdemo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.vicochu.fusingdemo.Entry.Executor;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class api {
    @Autowired
    private Executor executor;

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @RequestMapping(value = "/hystrix/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String doSomething(@PathVariable String value) {
        return executor.execute(value);
    }

    @RequestMapping(value = "/resilience", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String doAnother() {
        return executor.executeAnother();
    }

    @RequestMapping(value = "/hystrixAnother/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String doAnotherHystrix(@PathVariable String value) {
        return executor.executeHystrixAnother(value);
    }

    @RequestMapping(value = "/biz/{service}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String biz(@PathVariable String service) {
        return executor.biz(service);
    }

    @RequestMapping(value = "/monitor/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean getMonitorStatus(@PathVariable String value) {
        return executor.getMonitorStatus(value);
    }

    @RequestMapping(value = "/health/{service}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String serviceHealthStatus() {
        return String.valueOf(random.nextBoolean());
    }

    @RequestMapping(value = "/backlog/{service}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String serviceBacklog() {
        return String.valueOf(random.nextBoolean());
    }

}
