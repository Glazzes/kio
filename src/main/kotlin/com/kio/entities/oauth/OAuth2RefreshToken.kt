package com.kio.entities.oauth

import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "oauth_refresh_token")
class OAuth2RefreshToken(
        @Id @GeneratedValue
        var id: Long? = null,
        @Column(name = "token_id")
        var tokenId: String? = null,
        @Lob
        var token: Blob? = null,
        @Lob
        var authentication: Blob? = null
)