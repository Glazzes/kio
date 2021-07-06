package com.kio.entities

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity(name = "User")
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "users_uq_username", columnNames = ["username"]),
        UniqueConstraint(name = "users_uq_email", columnNames = ["email"])
    ]
)
class User(
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,

    @Column(name = "username", nullable = false)
    var username: String? = null,

    @Column(name = "password", nullable = false)
    var password: String? = null,

    @Column(name = "email", nullable = false)
    var email: String? = null,

    @Column(name = "nickname")
    var nickname: String? = username,

    var profilePicture: String? = null,
)