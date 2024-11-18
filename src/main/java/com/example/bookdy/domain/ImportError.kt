package com.example.bookdy.domain

import com.example.bookdy.R
import org.readium.r2.shared.util.DebugError
import org.readium.r2.shared.util.Error
import org.readium.r2.shared.util.file.FileSystemError
import org.readium.r2.shared.util.http.HttpError
import com.example.bookdy.domain.PublicationError
import com.example.bookdy.domain.toUserError
import com.example.bookdy.utils.UserError

sealed class ImportError(
    override val cause: Error?
) : Error {

    override val message: String =
        "Import failed"

    class Publication(
        override val cause: PublicationError
    ) : ImportError(cause)

    class FileSystem(
        override val cause: FileSystemError
    ) : ImportError(cause)

    class Download(
        override val cause: HttpError
    ) : ImportError(cause)


    class Database(override val cause: Error) :
        ImportError(cause)

    class InconsistentState(override val cause: DebugError) :
        ImportError(cause)

    fun toUserError(): UserError = when (this) {
        is Database -> UserError(R.string.import_publication_unable_add_pub_database, cause = this)
        is Download -> UserError(R.string.import_publication_download_failed, cause = this)
        is Publication -> cause.toUserError()
        is FileSystem -> cause.toUserError()
        is InconsistentState -> UserError(
            R.string.import_publication_inconsistent_state,
            cause = this
        )
    }
}
