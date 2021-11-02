package com.kio.entities.oauth

import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "oauth_code")
class OAuth2Code(
        @Id @GeneratedValue
        var id: Long? = null,
        var code: String? = null,
        @Lob
        var authentication: Blob? = null
)