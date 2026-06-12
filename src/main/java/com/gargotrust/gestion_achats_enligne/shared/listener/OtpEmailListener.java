package com.gargotrust.gestion_achats_enligne.shared.listener;

import com.gargotrust.gestion_achats_enligne.shared.events.OtpRequestedEvent;
import com.gargotrust.gestion_achats_enligne.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpEmailListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleOtpRequestedEvent(OtpRequestedEvent event) {
        log.info("Received OTP requested event for email: {}, type: {}", event.email(), event.type());
        
        try {
            emailService.sendOtpEmail(event.email(), event.otpCode(), event.type());
            log.info("OTP email sent successfully to: {}", event.email());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", event.email(), e);
        }
    }
}
