package me.chacham.urlshortener

sealed interface ShortUrlRecord

data class ShortUrlRecordData(val key: Key, val url: Url) : ShortUrlRecord
data object ShortUrlRecordEmpty : ShortUrlRecord
