package com.robin.omegle.service;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class MatchmakingService {
    private static Queue<WebSocketSession> waitingQueue = new ConcurrentLinkedQueue<>();

    public void findMatch(WebSocketSession session) throws Exception {
        WebSocketSession partner = waitingQueue.poll();
        if (partner != null) {
            // Notify both users they are matched
            notifyUsers(session, partner);
        } else {
            waitingQueue.add(session);
        }
    }

    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
        String notification = "Matched!";
        session1.sendMessage(new TextMessage(notification));
        session2.sendMessage(new TextMessage(notification));
    }
}
