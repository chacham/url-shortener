package me.chacham.urlshortener

interface ShortUrlRepository {
    suspend fun findByKey(key: Key): ShortUrlRecord
    suspend fun findByUrl(url: Url): ShortUrlRecord
    suspend fun save(key: Key, url: Url): SaveResult
}

sealed interface SaveResult
data class SaveSuccess(val shortUrlRecordData: ShortUrlRecordData) : SaveResult
data object SaveFailedByDuplicateKeyOrUrl : SaveResult
