package com.relic.data.repository

import com.relic.exception.RelicException

/**
 * Exceptions common to all repo classes
 */
class RepoException(
    override val message: String?,
    override val cause: Throwable?
) : RelicException(message, cause)