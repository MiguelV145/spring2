package ec.edu.ups.icc.fundamentos01.security.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    
        private final UserRepository userRepository;

        public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{

        UserEntity user= userRepository.findByEmail(email)
        .orElseThrow(()-> new UsernameNotFoundException("Usario no econtrado con email: "+ email));

        return UserDetailsImpl.build(user);
    }
}
