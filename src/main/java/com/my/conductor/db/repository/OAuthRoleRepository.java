package com.my.conductor.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.my.conductor.db.entity.OAuthRole;

public interface OAuthRoleRepository extends JpaRepository<OAuthRole, Integer> {
	
	OAuthRole findByName(String name);

}
