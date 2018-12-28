package tk.vicochu.fusingdemo.Service;

import org.springframework.stereotype.Component;

@Component
public class ServiceB implements AbstractService {
    @Override
    public String print(String info) {
        System.out.println("Service B executed." + info);
        return "Service B executed." + info;
    }
}
