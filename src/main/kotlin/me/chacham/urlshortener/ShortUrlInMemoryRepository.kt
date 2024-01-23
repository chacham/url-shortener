package me.chacham.urlshortener

import org.springframework.stereotype.Repository

@Repository
class ShortUrlInMemoryRepository : ShortUrlRepository {
    val lock = Any()
    val recordsWithKey = mutableMapOf<String, ShortUrlRecordData>()
    val recordsWithUrl = mutableMapOf<String, ShortUrlRecordData>()

    override suspend fun findByKey(key: String): ShortUrlRecord {
        if (recordsWithKey.containsKey(key)) {
            return recordsWithKey[key]!!
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun findByUrl(url: String): ShortUrlRecord {
        if (recordsWithUrl.containsKey(url)) {
            return recordsWithUrl[url]!!
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun save(key: String, url: String): SaveResult {
        synchronized(lock) {
            if (recordsWithKey.containsKey(key) || recordsWithUrl.containsKey(url)) {
                return FailedByDuplicateKeyOrUrl
            }
            val record = ShortUrlRecordData(key, url)
            recordsWithKey[key] = record
            recordsWithUrl[url] = record
            return SaveSuccess(record)
        }
    }
}
