package me.chacham.urlshortener

sealed interface ShortUrlRecord

data class ShortUrlRecordData(val key: String, val url: String) : ShortUrlRecord
data object ShortUrlRecordEmpty : ShortUrlRecord
