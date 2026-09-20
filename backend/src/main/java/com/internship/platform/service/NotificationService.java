package com.internship.platform.service;

import com.internship.platform.model.entity.Notification;
import com.internship.platform.model.entity.User;
import com.internship.platform.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** In-app notifications + optional email copy. */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository repo;
    private final JavaMailSender mailSender;
    private final String from;

    public NotificationService(NotificationRepository repo,
                               JavaMailSender mailSender,
                               @Value("${app.mail.from:noreply@internship-platform.local}") String from) {
        this.repo = repo;
        this.mailSender = mailSender;
        this.from = from;
    }

    /** Create notification and best-effort email copy. */
    @Transactional
    public Notification notify(User user, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        Notification saved = repo.save(n);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(user.getEmail());
            mail.setSubject(title);
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception ignored) {
            // email is best-effort in dev/test
        }
        return saved;
    }

    /** List notifications for a user (newest first). */
    public List<Notification> forUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
