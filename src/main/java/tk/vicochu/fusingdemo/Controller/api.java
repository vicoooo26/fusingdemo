package tk.vicochu.fusingdemo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.vicochu.fusingdemo.Entry.Executor;

@RestController
public class api {
    @Autowired
    private Executor executor;

    @RequestMapping(value = "/hystrix/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String doSomething(@PathVariable String value) {
        return executor.execute(value);
    }

    @RequestMapping(value = "/resilience/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String doAnother() {
        return executor.executeAnother();
    }

    @RequestMapping(value = "/hystrixAnother/{value}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String doAnotherHystrix(@PathVariable String value) {
        return executor.executeHystrixAnother(value);
    }


}
