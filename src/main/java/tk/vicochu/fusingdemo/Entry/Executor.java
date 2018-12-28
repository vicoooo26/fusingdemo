package tk.vicochu.fusingdemo.Entry;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tk.vicochu.fusingdemo.Service.AbstractService;
import tk.vicochu.fusingdemo.SpringContext;

import java.util.LinkedList;
import java.util.List;

@Component
public class Executor {

    @Autowired
    private ResilienceEntry resilienceEntry;

    @Autowired
    private HytrixEntryAnother hytrixEntryAnother;


    private List<AbstractService> policyChain = new LinkedList<>();


    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        List<AbstractService> policies = new LinkedList<>();
        if (policyChain.isEmpty()) {
            synchronized (Executor.class) {
                if (policyChain.isEmpty()) {
                    Object[] policyObjs = SpringContext.getBeans(AbstractService.class);
                    if (null != policyObjs && policyObjs.length > 0) {
                        for (Object policyObj : policyObjs) {
                            AbstractService service = (AbstractService) policyObj;
                            policies.add(service);
                        }
                    }
                }
                policyChain.addAll(policies);
            }
        }
    }

    public AbstractService apply() {
        return policyChain.get(0);
    }

    public AbstractService apply2() {
        return policyChain.get(1);
    }

    public String execute(String value) {
        return new HystrixEntry(value).execute();
    }

    public String executeAnother() {
        return resilienceEntry.realDo();
    }

    public String executeHystrixAnother(String value) {
        return hytrixEntryAnother.run(value);
    }

}

