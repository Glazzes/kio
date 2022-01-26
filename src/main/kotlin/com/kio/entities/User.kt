package com.kio.entities

import com.kio.events.user.UserEntityEventListener
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import javax.persistence.*

@Entity(name = "User")
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "users_uq_username", columnNames = ["username"]),
        UniqueConstraint(name = "users_uq_email", columnNames = ["email"])
    ]
)
@EntityListeners(UserEntityEventListener::class)
class User(

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDHexGenerator")
    var id: String? = null,

    @Column(name = "username", nullable = false, updatable = false)
    var username: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "nickname")
    var nickname: String,

    var spaceUsed: Long = 0,

    @OneToMany(mappedBy = "users")
    var profilePicture: MutableSet<ProfilePicture> = HashSet()

) : Serializable