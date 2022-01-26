package com.kio.entities.oauth

import javax.persistence.*

@Entity
@Table(name = "oauth_refresh_token")
class OAuth2RefreshToken(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "token_id")
        var tokenId: String? = null,

        @Lob
        var token: ByteArray? = null,

        @Lob
        var authentication: ByteArray? = null
)