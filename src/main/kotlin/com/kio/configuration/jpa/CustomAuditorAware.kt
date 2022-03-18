package com.kio.configuration.jpa

import com.kio.configuration.security.UserToUserDetailsAdapter
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAuditorAware : AuditorAware<User> {

    override fun getCurrentAuditor(): Optional<User> {
        return Optional.ofNullable(SecurityContextHolder.getContext().authentication)
            .map { it.principal as UserToUserDetailsAdapter }
            .map { it.user }
    }

}