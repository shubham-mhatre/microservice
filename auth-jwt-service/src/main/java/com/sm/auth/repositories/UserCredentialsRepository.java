package com.sm.auth.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sm.auth.entity.UserCredentials;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Integer> {

	Optional<UserCredentials> findByName(String username);
}
