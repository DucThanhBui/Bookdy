package com.example.bookdy.reader

import com.example.bookdy.R
import org.readium.r2.shared.util.Error
import com.example.bookdy.utils.UserError

sealed class OpeningError(
    override val cause: Error?
) : Error {

    override val message: String =
        "Could not open publication"

    class PublicationError(
        override val cause: com.example.bookdy.domain.PublicationError
    ) : OpeningError(cause)

    class CannotRender(cause: Error) :
        OpeningError(cause)

    fun toUserError(): UserError =
        when (this) {
            is PublicationError ->
                cause.toUserError()
            is CannotRender ->
                UserError(R.string.opening_publication_cannot_render, cause = this)
        }
}
