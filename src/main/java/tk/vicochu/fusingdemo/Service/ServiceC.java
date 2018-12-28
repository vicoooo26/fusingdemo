package tk.vicochu.fusingdemo.Service;

import org.springframework.stereotype.Component;

@Component
public class ServiceC implements AbstractService {
    @Override
    public String print(String info) {
        System.out.println("Service C executed." + info);
        return "Service C executed." + info;
    }
}
