package  com.cst438.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService  {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findByEmail(username);
		UserBuilder builder = null;
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		if (user != null) {
			builder = org.springframework.security.core.userdetails.User.withUsername(username);
			builder.password(user.getPassword());
			builder.roles(user.getRole());
		} else {
			throw new UsernameNotFoundException("User not found.");
		}

		return builder.build();	    
	}
}
