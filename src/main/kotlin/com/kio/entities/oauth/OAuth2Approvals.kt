package com.kio.entities.oauth

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "oauth_approvals")
class OAuth2Approvals(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        val userId: String? = null,
        val clientId: String? = null,
        val scope: String? = null,
        val status: String? = null,
        val expiresAt: Instant? = null,
        val lastModifiedAt: Instant? = null
)