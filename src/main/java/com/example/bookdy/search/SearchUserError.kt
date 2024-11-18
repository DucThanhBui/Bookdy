package com.example.bookdy.search

import com.example.bookdy.R
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.services.search.SearchError
import com.example.bookdy.utils.UserError

@OptIn(ExperimentalReadiumApi::class)
fun SearchError.toUserError(): UserError = when (this) {
    is SearchError.Engine -> UserError(R.string.search_error_other, cause = this)
    is SearchError.Reading -> UserError(R.string.search_error_other, cause = this)
}
