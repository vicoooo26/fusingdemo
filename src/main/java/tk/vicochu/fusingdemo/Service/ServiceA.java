package tk.vicochu.fusingdemo.Service;

import org.springframework.stereotype.Component;

@Component
public class ServiceA implements AbstractService {
    @Override
    public String print(String info) {
        System.out.println("Service A executed." + info);
        return "Service A executed." + info;
    }
}
