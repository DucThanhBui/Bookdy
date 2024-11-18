package com.example.bookdy

import android.content.Context
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

class PublicationHelper(context: Context) {

    val httpClient =
        DefaultHttpClient()

    val assetRetriever =
        AssetRetriever(context.contentResolver, httpClient)

    val publicationOpener = PublicationOpener(
        publicationParser = DefaultPublicationParser(
            context,
            assetRetriever = assetRetriever,
            httpClient = httpClient,
            // For supporting PDF files using the PDFium adapter.
            pdfFactory = PdfiumDocumentFactory(context)
        )
    )
}

@OptIn(ExperimentalReadiumApi::class)
val FontFamily.Companion.LITERATA: FontFamily get() = FontFamily("Literata")
