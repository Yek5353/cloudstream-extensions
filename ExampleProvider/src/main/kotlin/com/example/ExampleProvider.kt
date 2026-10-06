package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType

class ExampleProvider : MainAPI() {
    override var mainUrl = "https://example.com/"
    override var name = "Example provider"
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.Movie)
    override val hasMainPage = true

    override suspend fun search(query: String): List<SearchResponse> = emptyList()
}
