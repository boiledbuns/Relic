package com.relic.data.repository

/**
 * Exceptions common to all repo classes
 */
class RepoError(
    override val message: String?,
    override val cause: Throwable?
) : Exception(message, cause)