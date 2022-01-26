package com.kio.entities

import javax.persistence.*

@Entity
@Table(name = "profile_pictures")
class ProfilePicture(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "url", updatable = false, nullable = false)
    var url: String,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean,

    @ManyToOne
    val user: User
)