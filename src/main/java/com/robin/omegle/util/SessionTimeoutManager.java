package com.robin.omegle.util;
import java.util.concurrent.*;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

public class SessionTimeoutManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long timeoutPeriod = 10; // Timeout period in minutes

    public void scheduleTimeout(WebSocketSession session) {
        scheduler.schedule(() -> {
            try {
                if (session.isOpen()) {
                	System.out.println("check schedular");
                    session.close(CloseStatus.NORMAL); // Close the session if still open
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, timeoutPeriod, TimeUnit.MINUTES);
    }
}
