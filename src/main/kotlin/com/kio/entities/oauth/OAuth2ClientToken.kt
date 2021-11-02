package com.kio.entities.oauth

import com.kio.entities.User
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "oauth_client_token")
class OAuth2ClientToken(
        @Column(name = "token_id")
        var tokenId: String? = null,

        @Lob
        var token: ByteArray? = null,

        @Id
        @Column(name = "authentication_id")
        var authenticationId: String? = null,

        @Column(name = "user_name")
        var username: String? = null,

        @Column(name = "client_id")
        var clientId: String? = null,

        @Lob
        var authentication: ByteArray? = null,

        @Column(name = "refresh_token")
        var refreshToken: String? = null,
)