//package com.robin.omegle.service;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.util.Map;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//@Service
//public class InMemorySessionService {
//
//    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
//    private final Queue<WebSocketSession> matchmakingQueue = new ConcurrentLinkedQueue<>();
//
//    // Add session to active sessions
//    public void addSession(String sessionId, WebSocketSession session) {
//    	
//    	System.out.println(" addSession sessionId :- "+sessionId+"   session :-"+session);
//        activeSessions.put(sessionId, session);
//    }
//
//    // Remove session from active sessions
//    public void removeSession(String sessionId) {
//    	System.out.println("sessionId :- "+sessionId);
//        activeSessions.remove(sessionId);
//    }
//
//    // Matchmaking logic to pair users
//    public void findMatch(WebSocketSession session) throws Exception {
//        WebSocketSession partner = matchmakingQueue.poll(); // Fetch waiting user
//        if (partner != null) {
//            // Notify both users that they are matched
//            notifyUsers(session, partner);
//        } else {
//            matchmakingQueue.add(session); // Add user to queue if no match found
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
//
//	public Map<String, WebSocketSession> getActiveSessions() {
//		return activeSessions;
//	}
//
//	public Queue<WebSocketSession> getMatchmakingQueue() {
//		return matchmakingQueue;
//	}
//    
//    
//    
//}
