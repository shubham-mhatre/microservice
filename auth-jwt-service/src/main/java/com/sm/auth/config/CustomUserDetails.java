package com.sm.auth.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sm.auth.entity.UserCredentials;

public class CustomUserDetails implements UserDetails {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private final String role;
	private Integer userId;

	public CustomUserDetails(UserCredentials userCredential) {
		this.username = userCredential.getName();
		this.password = userCredential.getPassword();
		this.role = userCredential.getRole().name();
		this.userId = userCredential.getId();
	}
	
	public Integer getId() {
	    return userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
