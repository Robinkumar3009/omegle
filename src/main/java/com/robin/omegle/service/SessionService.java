//package com.robin.omegle.service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.robin.omegle.bean.UserSession;
//import com.robin.omegle.repo.UserSessionRepository;
//
//@Service
//public class SessionService {
//
//    @Autowired
//    private UserSessionRepository repository;
//
//    public void createUserSession(String userId, String sessionId) {
//        UserSession session = new UserSession();
//        session.setUserId(userId);
//        session.setSessionId(sessionId);
//        session.setActive(true);
//        repository.save(session);
//    }
//
//    public void closeSession(String sessionId) {
//        UserSession session = repository.findBySessionId(sessionId);
//        if (session != null) {
//            session.setActive(false);
//            repository.save(session);
//        }
//    }
//    
//    
//}
