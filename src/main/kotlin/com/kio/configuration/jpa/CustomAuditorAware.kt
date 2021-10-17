package com.kio.configuration.jpa

import com.kio.configuration.security.SecurityUserAdapter
import com.kio.entities.User
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAuditorAware : AuditorAware<User> {

    override fun getCurrentAuditor(): Optional<User> {
        return Optional.ofNullable(SecurityContextHolder.getContext().authentication)
            .map { it.principal as SecurityUserAdapter }
            .map { it.user }
    }

}