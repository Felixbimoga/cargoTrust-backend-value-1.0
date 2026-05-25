package com.gargotrust.gestion_achats_enligne.service;

import com.gargotrust.gestion_achats_enligne.entity.Users;
import com.gargotrust.gestion_achats_enligne.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username);

        if (user == null){
            throw new UsernameNotFoundException("Utilisateur non trouvé avec le nom d'utilisateur :" +username);
        }

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("L'utilisateur n'a pas de rôle attribué");
        }

        if ("INACTIF".equals(user.getSituation())) {
            throw new DisabledException("Votre compte est désactivé. Contactez l'administrateur.");
        }

        System.out.println("loadUserByUsername");
        System.out.println(user.getRole().name());
        return new org.springframework.security.core.userdetails.
                User(user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))) ;
    }

    public List<Users> findAll() {
        return this.userRepository.findAll();
    }

    public byte[] getUserPhotoById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return user.getPhoto();
    }

    public Users getUsersById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + userId));
    }
}
