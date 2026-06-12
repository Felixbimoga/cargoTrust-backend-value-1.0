package com.gargotrust.gestion_achats_enligne.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "login_sessions", indexes = {
    @Index(name = "idx_session_account", columnList = "account_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)", length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private UUID accountId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 10)
    private DeviceType deviceType;

    @Column(name = "login_at", nullable = false)
    private Instant loginAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean successful = true;

    public enum DeviceType {
        MOBILE, WEB
    }
}
