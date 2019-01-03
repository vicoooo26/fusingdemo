package tk.vicochu.fusingdemo.Monitor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.Map;

public class Monitor {
    private final static Map<String, String> ENGINE_STATUS = new HashMap<>();


    @EventListener(ApplicationReadyEvent.class)

    public void start() {
        Thread thread = new Thread(() -> {
            
        });
        thread.start();
    }
}
