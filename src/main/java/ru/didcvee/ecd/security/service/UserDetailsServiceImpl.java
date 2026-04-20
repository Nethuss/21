package ru.didcvee.ecd.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.didcvee.ecd.security.repository.JdbcUserRepository;
import ru.didcvee.ecd.security.repository.model.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcUserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(JdbcUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameWithRoles(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Пользователь с таким логином не найден: " + username));

        return UserDetailsImpl.build(user);
    }
}
