package com.gargotrust.gestion_achats_enligne.entity;

import com.gargotrust.gestion_achats_enligne.entity.Enum.Roles;
import com.gargotrust.gestion_achats_enligne.entity.Enum.Situation;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="Users")
@Data

public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String mail;

    private String password;
    private String telephone;

    @Lob
    private byte[] photo;

    @Enumerated(EnumType.STRING)
    private Roles role = Roles.CLIENT;

    @Enumerated(EnumType.STRING)
    private Situation situation;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public Roles getRole() {
        return role;
    }
}
