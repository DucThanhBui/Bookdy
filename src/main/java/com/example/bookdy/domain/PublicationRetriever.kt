package com.example.bookdy.domain

import android.content.Context
import android.net.Uri
import com.example.bookdy.utils.copyToNewFile
import java.io.File
import java.util.UUID
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.data.ReadError
import org.readium.r2.shared.util.file.FileSystemError
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.format.FormatHints
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.mediatype.MediaType
import com.example.bookdy.utils.extensions.copyToTempFile
import com.example.bookdy.utils.extensions.moveTo
import com.example.bookdy.utils.tryOrLog
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.http.HttpRequest
import timber.log.Timber

/**
 * Retrieves a publication from a remote or local source and import it into the bookshelf storage.
 *
 * If the source file is a LCP license document, the protected publication will be downloaded.
 */
class PublicationRetriever(
    context: Context,
    private val assetRetriever: AssetRetriever,
    private val bookshelfDir: File,
    private val httpClient: HttpClient,
    private val tempDir: File
) {
    data class Result(
        val publication: File,
        val format: Format,
        val coverUrl: AbsoluteUrl?
    )

    private val localPublicationRetriever: LocalPublicationRetriever =
        LocalPublicationRetriever(context, tempDir, assetRetriever)

    suspend fun retrieveFromStorage(
        uri: Uri
    ): Try<Result, ImportError> {
        val localResult = localPublicationRetriever
            .retrieve(uri)
            .getOrElse { return Try.failure(it) }

        val finalResult = moveToBookshelfDir(
            localResult.tempFile,
            localResult.format,
            localResult.coverUrl
        )
            .getOrElse {
                tryOrLog { localResult.tempFile.delete() }
                return Try.failure(it)
            }

        return Try.success(
            Result(finalResult.publication, finalResult.format, finalResult.coverUrl)
        )
    }

    suspend fun retrieveFromHttp(
        url: AbsoluteUrl
    ): Try<Result, ImportError> {
        val request = HttpRequest(
            url,
            headers = emptyMap()
        )

        val tempFile = when (val result = httpClient.stream(request)) {
            is Try.Failure ->
                return Try.failure(ImportError.Download(result.value))
            is Try.Success -> {
                result.value.body
                    .copyToNewFile(tempDir)
                    .getOrElse { return Try.failure(ImportError.FileSystem(it)) }
            }
        }

        val localResult = localPublicationRetriever
            .retrieve(tempFile)
            .getOrElse {
                tryOrLog { tempFile.delete() }
                return Try.failure(it)
            }

        val finalResult = moveToBookshelfDir(
            localResult.tempFile,
            localResult.format,
            localResult.coverUrl
        )
            .getOrElse {
                tryOrLog { localResult.tempFile.delete() }
                return Try.failure(it)
            }

        return Try.success(
            Result(finalResult.publication, finalResult.format, finalResult.coverUrl)
        )
    }

    private suspend fun moveToBookshelfDir(
        tempFile: File,
        format: Format?,
        coverUrl: AbsoluteUrl?
    ): Try<Result, ImportError> {
        val actualFormat = format
            ?: assetRetriever.sniffFormat(tempFile)
                .getOrElse {
                    return Try.failure(ImportError.Publication(PublicationError(it)))
                }

        val fileName = "${UUID.randomUUID()}.${actualFormat.fileExtension.value}"
        val bookshelfFile = File(bookshelfDir, fileName)

        try {
            tempFile.moveTo(bookshelfFile)
        } catch (e: Exception) {
            Timber.d(e)
            tryOrLog { bookshelfFile.delete() }
            return Try.failure(
                ImportError.Publication(
                    PublicationError.Reading(
                        ReadError.Access(FileSystemError.IO(e))
                    )
                )
            )
        }

        return Try.success(
            Result(bookshelfFile, actualFormat, coverUrl)
        )
    }
}

/**
 * Retrieves a publication from a file (publication or LCP license document) stored on the device.
 */
private class LocalPublicationRetriever(
    private val context: Context,
    private val tempDir: File,
    private val assetRetriever: AssetRetriever
) {

    data class Result(
        val tempFile: File,
        val format: Format?,
        val coverUrl: AbsoluteUrl?
    )

    /**
     * Retrieves the publication from the given local [uri].
     */
    suspend fun retrieve(
        uri: Uri
    ): Try<Result, ImportError> {
        val tempFile = uri.copyToTempFile(context, tempDir)
            .getOrElse {
                return Try.failure(ImportError.FileSystem(FileSystemError.IO(it)))
            }
        return retrieveFromStorage(tempFile, coverUrl = null)
            .onFailure { tryOrLog { tempFile.delete() } }
    }

    /**
     * Retrieves the publication stored at the given [tempFile].
     */
    suspend fun retrieve(
        tempFile: File,
        mediaType: MediaType? = null,
        coverUrl: AbsoluteUrl? = null
    ): Try<Result, ImportError> {
        return retrieveFromStorage(tempFile, mediaType, coverUrl)
    }

    private suspend fun retrieveFromStorage(
        tempFile: File,
        mediaType: MediaType? = null,
        coverUrl: AbsoluteUrl? = null
    ): Try<Result, ImportError> {
        val sourceAsset = assetRetriever.retrieve(tempFile, FormatHints(mediaType))
            .getOrElse {
                return Try.failure(ImportError.Publication(PublicationError(it)))
            }
        sourceAsset.close()
        return Try.success(
            Result(tempFile, sourceAsset.format, coverUrl)
        )
    }
}