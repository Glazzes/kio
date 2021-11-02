package com.kio.repositories

import com.kio.entities.oauth.OAuth2ClientDetails
import org.springframework.data.jpa.repository.JpaRepository

interface OAuth2ClientRepository : JpaRepository<OAuth2ClientDetails, String>