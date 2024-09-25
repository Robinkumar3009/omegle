
package com.robin.omegle.bean;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatHandler extends TextWebSocketHandler {
    private static Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
    private final Map<WebSocketSession, WebSocketSession> matchedUsers = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> disconnectedUsers = new CopyOnWriteArraySet<>();
    private final Map<WebSocketSession, WebSocketSession> previousMatches = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Store active session
        activeSessions.put(session.getId(), session);
        disconnectedUsers.remove(session);
        findMatch(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Send message only to the matched user
        WebSocketSession matchedSession = matchedUsers.get(session);

        if (matchedSession != null && matchedSession.isOpen()) {
            try {
                synchronized (matchedSession) {
                    matchedSession.sendMessage(new TextMessage(payload));
                }
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
                matchedSession.close(CloseStatus.SERVER_ERROR);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session from the active sessions
        activeSessions.remove(session.getId());

        // Remove the session from the matchmaking queue if it's still there
        matchmakingQueue.remove(session);

        // If the user was already matched, unpair the matched users
        WebSocketSession matchedSession = matchedUsers.remove(session);
        if (matchedSession != null) {
            matchedUsers.remove(matchedSession);
            // Store this pair in previousMatches to track disconnected pairs
            previousMatches.put(session, matchedSession);
            previousMatches.put(matchedSession, session);

            // Optionally, put the unmatched user back in the matchmaking queue
            if (matchedSession.isOpen() && !disconnectedUsers.contains(matchedSession)) {
                matchmakingQueue.add(matchedSession);
                findMatch(matchedSession);
            }
        }

        // Add the disconnected user to the set to prevent re-queueing
        disconnectedUsers.add(session);

        // Try to find matches for remaining users in the queue
        if (!matchmakingQueue.isEmpty()) {
            WebSocketSession nextUser = matchmakingQueue.poll();
            if (nextUser != null && nextUser.isOpen()) {
                findMatch(nextUser);
            }
        }
    }

    private void findMatch(WebSocketSession session) throws Exception {
        // Check if the user was previously matched with someone
        WebSocketSession previousMatch = previousMatches.get(session);

        // If the previous match is in the queue and both are open, reconnect them
        if (previousMatch != null && matchmakingQueue.contains(previousMatch)) {
            matchmakingQueue.remove(previousMatch);
            if (previousMatch.isOpen() && session.isOpen()) {
                notifyUsers(session, previousMatch);
                matchedUsers.put(session, previousMatch);
                matchedUsers.put(previousMatch, session);
                return;
            }
        }

        // Attempt to find a match from the queue
        WebSocketSession partner = matchmakingQueue.poll();

        if (partner != null && partner.isOpen() && session.isOpen()) {
            // If a match is found, notify both users and store their pair
            notifyUsers(session, partner);
            matchedUsers.put(session, partner);
            matchedUsers.put(partner, session);
        } else {
            // If no match is found or session is closed, add the session to the queue
            if (session.isOpen() && !disconnectedUsers.contains(session)) {
                matchmakingQueue.add(session);
            }
            System.out.println("User count: " + matchmakingQueue.size());
        }
    }

    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
        String notification = "Matched!";
        // Check if session1 is still open before sending a message
        if (session1.isOpen()) {
            synchronized (session1) {
                session1.sendMessage(new TextMessage(notification));
            }
        } else {
            System.out.println("Session1 is closed, cannot send notification.");
        }

        // Check if session2 is still open before sending a message
        if (session2.isOpen()) {
            synchronized (session2) {
                session2.sendMessage(new TextMessage(notification));
            }
        } else {
            System.out.println("Session2 is closed, cannot send notification.");
        }
    }
}


//package com.robin.omegle.bean;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Queue;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//public class ChatHandler extends TextWebSocketHandler {
//	private static Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
//    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
//    private final Map<WebSocketSession, WebSocketSession> matchedUsers = new ConcurrentHashMap<>();
//    private final Set<WebSocketSession> disconnectedUsers = new CopyOnWriteArraySet<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Store active session
//        activeSessions.put(session.getId(), session);
//        disconnectedUsers.remove(session);
//        findMatch(session);
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//
//        // Send message only to the matched user
//        WebSocketSession matchedSession = matchedUsers.get(session);
//
//        if (matchedSession != null && matchedSession.isOpen()) {
//            try {
//                synchronized (matchedSession) {
//                    matchedSession.sendMessage(new TextMessage(payload));
//                }
//            } catch (IOException e) {
//                System.out.println("Error sending message: " + e.getMessage());
//                matchedSession.close(CloseStatus.SERVER_ERROR);
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // Remove the session from the active sessions
//        activeSessions.remove(session.getId());
//
//        // Remove the session from the matchmaking queue if it's still there
//        matchmakingQueue.remove(session);
//
//        // If the user was already matched, unpair the matched users
//        WebSocketSession matchedSession = matchedUsers.remove(session);
//        if (matchedSession != null) {
//            matchedUsers.remove(matchedSession);
//            // Optionally, put the unmatched user back in the matchmaking queue
//            if (matchedSession.isOpen() && !disconnectedUsers.contains(matchedSession)) {
//                matchmakingQueue.add(matchedSession);
//                findMatch(matchedSession);
//            }
//        }
//
//        // Add the disconnected user to the set to prevent re-queueing
//        disconnectedUsers.add(session);
//
//        // Try to find matches for remaining users in the queue
//        if (!matchmakingQueue.isEmpty()) {
//            WebSocketSession nextUser = matchmakingQueue.poll();
//            if (nextUser != null && nextUser.isOpen()) {
//                findMatch(nextUser);
//            }
//        }
//    }
//
//    private void findMatch(WebSocketSession session) throws Exception {
//        // Attempt to find a match from the queue
//        WebSocketSession partner = matchmakingQueue.poll();
//
//        if (partner != null && partner.isOpen() && session.isOpen()) {
//            // If a match is found, notify both users and store their pair
//            notifyUsers(session, partner);
//            matchedUsers.put(session, partner);
//            matchedUsers.put(partner, session);
//        } else {
//            // If no match is found or session is closed, add the session to the queue
//            if (session.isOpen() && !disconnectedUsers.contains(session)) {
//                matchmakingQueue.add(session);
//            }
//            System.out.println("User count: " + matchmakingQueue.size());
//        }
//    }
//
//    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
//        String notification = "Matched!";
//        // Check if session1 is still open before sending a message
//        if (session1.isOpen()) {
//            synchronized (session1) {
//                session1.sendMessage(new TextMessage(notification));
//            }
//        } else {
//            System.out.println("Session1 is closed, cannot send notification.");
//        }
//
//        // Check if session2 is still open before sending a message
//        if (session2.isOpen()) {
//            synchronized (session2) {
//                session2.sendMessage(new TextMessage(notification));
//            }
//        } else {
//            System.out.println("Session2 is closed, cannot send notification.");
//        }
//    }
//}

//    private static Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
//    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
//    private final Map<WebSocketSession, WebSocketSession> matchedUsers = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Store active session
//        activeSessions.put(session.getId(), session);
//
//        // Try to find a match for the new user
//        findMatch(session);
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//
//        // Send message only to the matched user
//        WebSocketSession matchedSession = matchedUsers.get(session);
//
//        if (matchedSession != null && matchedSession.isOpen()) {
//            try {
//                synchronized (matchedSession) {
//                    matchedSession.sendMessage(new TextMessage(payload));
//                }
//            } catch (IOException e) {
//                System.out.println("Error sending message: " + e.getMessage());
//                matchedSession.close(CloseStatus.SERVER_ERROR);
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // Remove the session from the active sessions
//        activeSessions.remove(session.getId());
//
//        // Remove the session from the matchmaking queue if it's still there
//        matchmakingQueue.remove(session);
//
//        // If the user was already matched, unpair the matched users
//        WebSocketSession matchedSession = matchedUsers.remove(session);
//        if (matchedSession != null) {
//            matchedUsers.remove(matchedSession);
//            // Optionally, put the unmatched user back in the matchmaking queue
//            if (matchedSession.isOpen()) {
//                matchmakingQueue.add(matchedSession);
//                findMatch(matchedSession);
//            }
//        }
//
//        // Try to find matches for remaining users in the queue
//        if (!matchmakingQueue.isEmpty()) {
//            WebSocketSession nextUser = matchmakingQueue.poll();
//            if (nextUser != null && nextUser.isOpen()) {
//                findMatch(nextUser);
//            }
//        }
//    }
//
//    private void findMatch(WebSocketSession session) throws Exception {
//        // Attempt to find a match from the queue
//        WebSocketSession partner = matchmakingQueue.poll();
//
//        if (partner != null && partner.isOpen() && session.isOpen()) {
//            // If a match is found, notify both users and store their pair
//            notifyUsers(session, partner);
//            matchedUsers.put(session, partner);
//            matchedUsers.put(partner, session);
//        } else {
//            // If no match is found or session is closed, add the session to the queue
//            if (session.isOpen()) {
//                matchmakingQueue.add(session);
//            }
//            System.out.println("user count: " + matchmakingQueue.size());
//        }
//    }
//
//    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
//        String notification = "Matched!";
//        // Check if session1 is still open before sending a message
//        if (session1.isOpen()) {
//            synchronized (session1) {
//                session1.sendMessage(new TextMessage(notification));
//            }
//        } else {
//            System.out.println("Session1 is closed, cannot send notification.");
//        }
//
//        // Check if session2 is still open before sending a message
//        if (session2.isOpen()) {
//            synchronized (session2) {
//                session2.sendMessage(new TextMessage(notification));
//            }
//        } else {
//            System.out.println("Session2 is closed, cannot send notification.");
//        }
//    }
//}




//package com.robin.omegle.bean;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//public class ChatHandler extends TextWebSocketHandler {
//
//    private static Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
//    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
//    private final Map<WebSocketSession, WebSocketSession> matchedUsers = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // Store active session
//        activeSessions.put(session.getId(), session);
////        System.out.println("User connected: " + session.getId());
//
//        // Try to find a match for the new user
//        findMatch(session);
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
////        System.out.println("handleTextMessage payload :- " + payload);
//
//        // Send message only to the matched user
//        WebSocketSession matchedSession = matchedUsers.get(session);
//
//        if (matchedSession != null && matchedSession.isOpen()) {
//            try {
//                synchronized (matchedSession) {
//                    matchedSession.sendMessage(new TextMessage(payload));
//                }
//            } catch (IOException e) {
//                System.out.println("Error sending message: " + e.getMessage());
//                matchedSession.close(CloseStatus.SERVER_ERROR);
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // Remove the session from the active sessions
//        activeSessions.remove(session.getId());
////        System.out.println("User disconnected: " + session.getId());
//
//        // Remove the session from the matchmaking queue if it's still there
//        matchmakingQueue.remove(session);
//
//        // If the user was already matched, unpair the matched users
//        WebSocketSession matchedSession = matchedUsers.remove(session);
//        if (matchedSession != null) {
//            matchedUsers.remove(matchedSession);
//            // Optionally, put the unmatched user back in the matchmaking queue
//            matchmakingQueue.add(matchedSession);
//            findMatch(matchedSession);
//        }
//
//        // Try to find matches for remaining users in the queue
//        if (!matchmakingQueue.isEmpty()) {
//            WebSocketSession nextUser = matchmakingQueue.poll();
//            findMatch(nextUser);
//        }
//    }
//
//    private void findMatch(WebSocketSession session) throws Exception {
//        // Attempt to find a match from the queue
//        WebSocketSession partner = matchmakingQueue.poll(); 
//
//        if (partner != null && partner.isOpen()) {
//            // If a match is found, notify both users and store their pair
//            notifyUsers(session, partner);
//            matchedUsers.put(session, partner);
//            matchedUsers.put(partner, session);
//        } else {
//            // If no match is found, add the session to the queue
//            matchmakingQueue.add(session);
//            
//            System.out.println("user count: " + matchmakingQueue.size());
////            System.out.println("Added to matchmaking queue: " + session.getId());
//        }
//    }
//
//    private void notifyUsers(WebSocketSession session1, WebSocketSession session2) throws Exception {
//        String notification = "Matched!";
////        System.out.println("Matched: " + session1.getId() + " and " + session2.getId());
//
//        synchronized (session1) {
//            session1.sendMessage(new TextMessage(notification));
//        }
//        synchronized (session2) {
//            session2.sendMessage(new TextMessage(notification));
//        }
//    }
//}
//
//
//
//
//
