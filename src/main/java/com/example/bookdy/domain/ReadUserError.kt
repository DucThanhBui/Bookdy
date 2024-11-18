package com.example.bookdy.domain

import com.example.bookdy.R
import org.readium.r2.shared.util.content.ContentResolverError
import org.readium.r2.shared.util.data.ReadError
import org.readium.r2.shared.util.file.FileSystemError
import com.example.bookdy.utils.UserError

fun ReadError.toUserError(): UserError = when (this) {
    is ReadError.Access ->
        when (val cause = this.cause) {
            is FileSystemError -> cause.toUserError()
            is ContentResolverError -> cause.toUserError()
            else -> UserError(R.string.error_unexpected, cause = this)
        }

    is ReadError.Decoding -> UserError(R.string.publication_error_invalid_publication, cause = this)
    is ReadError.OutOfMemory -> UserError(R.string.publication_error_out_of_memory, cause = this)
    is ReadError.UnsupportedOperation -> UserError(
        R.string.publication_error_unexpected,
        cause = this
    )
}

fun FileSystemError.toUserError(): UserError = when (this) {
    is FileSystemError.Forbidden -> UserError(
        R.string.publication_error_filesystem_forbidden,
        cause = this
    )
    is FileSystemError.IO -> UserError(
        R.string.publication_error_filesystem_unexpected,
        cause = this
    )
    is FileSystemError.InsufficientSpace -> UserError(
        R.string.publication_error_filesystem_insufficient_space,
        cause = this
    )
    is FileSystemError.FileNotFound -> UserError(
        R.string.publication_error_filesystem_not_found,
        cause = this
    )
}

fun ContentResolverError.toUserError(): UserError = when (this) {
    is ContentResolverError.FileNotFound -> UserError(
        R.string.publication_error_filesystem_not_found,
        cause = this
    )
    is ContentResolverError.IO -> UserError(
        R.string.publication_error_filesystem_unexpected,
        cause = this
    )
    is ContentResolverError.Forbidden -> UserError(
        R.string.publication_error_filesystem_forbidden,
        cause = this
    )
    is ContentResolverError.NotAvailable -> UserError(R.string.error_unexpected, cause = this)
}
