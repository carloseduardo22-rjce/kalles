package dev.kalles.security.service;

import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var matches = accountRepository.findAllByEmailIgnoreCase(username);
        if (matches.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        if (matches.size() > 1) {
            throw new UsernameNotFoundException("Tenant is required for duplicated email: " + username);
        }
        return matches.getFirst();
    }
}
