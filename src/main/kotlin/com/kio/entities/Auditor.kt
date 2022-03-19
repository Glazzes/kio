package com.kio.entities

import com.kio.shared.utils.SecurityUtil
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

@Component
class Auditor: AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        val authenticatedUser = SecurityUtil.getAuthenticatedUser()
        return Optional.ofNullable(authenticatedUser.id)
    }

}