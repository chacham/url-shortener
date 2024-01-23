package me.chacham.urlshortener

interface ShortUrlRepository {
    suspend fun findByKey(key: String): ShortUrlRecord
    suspend fun findByUrl(url: String): ShortUrlRecord
    suspend fun save(key: String, url: String): SaveResult
}

sealed interface SaveResult
data class SaveSuccess(val shortUrlRecordData: ShortUrlRecordData) : SaveResult
data object FailedByDuplicateKeyOrUrl : SaveResult
