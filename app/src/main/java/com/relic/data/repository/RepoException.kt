package com.relic.data.repository

import com.relic.domain.exception.RelicException

/**
 * Exceptions common to all repo classes
 */
sealed class RepoException(
    override val message: String?,
    override val cause: Throwable?
) : RelicException(message, cause) {

    class ClientException(
        message: String?,
        cause: Throwable?
    ) : RepoException("Failed to parse `$message` response", cause)

    abstract class RetrievalException(
        message: String?,
        cause: Throwable?
    ) : RepoException(message, cause)

    class UnknownException(
        cause: Throwable?
    ) : RepoException("Caught unknown exception in Repo class", cause)
}

class NetworkException(
    message: String?,
    cause: Throwable?
) : RepoException.RetrievalException(message, cause)

open class AuthException(
    message: String?,
    cause: Throwable?
) : RepoException.RetrievalException(message, cause)