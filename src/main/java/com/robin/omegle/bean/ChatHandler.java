package com.robin.omegle.bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatHandler extends TextWebSocketHandler {

	private static Map<String, WebSocketSession> activeSessions = new HashMap<>();
    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Store active session
        activeSessions.put(session.getId(), session);
        System.out.println("User connected: " + session.getId());
        findMatch(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("handleTextMessage payload :- " + payload);

        // Send message to the matched user
        for (WebSocketSession webSocketSession : activeSessions.values()) {
            if (!session.getId().equals(webSocketSession.getId()) && webSocketSession.isOpen()) {
                try {
                    synchronized (webSocketSession) {
                        webSocketSession.sendMessage(new TextMessage(payload));
                    }
                } catch (IOException e) {
                    System.out.println("Error sending message: " + e.getMessage());
                    webSocketSession.close(CloseStatus.SERVER_ERROR);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from the active sessions
        activeSessions.remove(session.getId());
        System.out.println("User disconnected: " + session.getId());

        // Remove the session from the matchmaking queue if it's still there
        matchmakingQueue.remove(session);

        // If there are still users in the queue, try to find a match for them
        if (!matchmakingQueue.isEmpty()) {
            WebSocketSession nextUser = matchmakingQueue.poll();
            findMatch(nextUser);
        }
    }

    private void findMatch(WebSocketSession session) throws Exception {
        // Attempt to find a match from the queue
        WebSocketSession partner = matchmakingQueue.poll(); 

        if (partner != null && partner.isOpen()) {
            // If a match is found, notify both users
            notifyUsers(session, partner);
        } else {
            // If no match is found, add the session to the queue
            matchmakingQueue.add(session);
            System.out.println("Added to matchmaking queue: " + session.getId());
        }
    }

    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
        String notification = "Matched!";
        System.out.println("Matched: " + session1.getId() + " and " + session2.getId());

        synchronized (session1) {
            session1.sendMessage(new TextMessage(notification));
        }
        synchronized (session2) {
            session2.sendMessage(new TextMessage(notification));
        }
    }
	   
 

	
//    private static Map<String, WebSocketSession> activeSessions = new HashMap<>();
//
//    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
//
//    
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Store active session
//        activeSessions.put(session.getId(), session);
//        findMatch(session);
//    	System.out.println(" afterConnectionEstablished activeSessions :- "+activeSessions+"   session :-"+session);
//
//    }
//
//    
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // Parse the message to extract offer/answer/ICE
//        String payload = message.getPayload();
//        System.out.println(" addSession payload :- "+payload);
//        // Broadcast message to other user
//        for (WebSocketSession webSocketSession : activeSessions.values()) {
//            if (!session.getId().equals(webSocketSession.getId())) {
//                webSocketSession.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // Remove session on close
//    	 System.out.println(" afterConnectionClosed activeSessions :- "+activeSessions);
//        activeSessions.remove(session.getId());
//        System.out.println(" afterConnectionClosed activeSessions after :- "+activeSessions);
//    }
//    
//    
//    
//    public void findMatch(WebSocketSession session) throws Exception {
//        WebSocketSession partner = matchmakingQueue.poll(); // Fetch waiting user
//        if (partner != null) {
//            // Notify both users that they are matched
//            notifyUsers(session, partner);
//        } else {
//            matchmakingQueue.add(session);
//            System.out.println("matchmakingQueue  :-"+matchmakingQueue.size());// Add user to queue if no match found
//        }
//    }
//
//    // Notify both users that they are matched
//    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
//        String notification = "Matched!";
//        System.out.println(" addSession  session1 notification :- "+notification+"   session2  :-"+notification);
//        session1.sendMessage(new TextMessage(notification));
//        session2.sendMessage(new TextMessage(notification));
//    }
 
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessionService.addSession(session.getId(), session);
//        sessionService.findMatch(session);  // Attempt to find a match
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // In-memory session can handle message broadcasting or signaling as necessary
//        for (WebSocketSession webSocketSession : sessionService.getActiveSessions().values()) {
//            if (webSocketSession.isOpen()) {
//                webSocketSession.sendMessage(message);
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessionService.removeSession(session.getId()); // Remove session from active sessions
//    }
}
