package me.chacham.urlshortener

import org.springframework.stereotype.Repository

@Repository
class ShortUrlInMemoryRepository : ShortUrlRepository {
    val lock = Any()
    val keyToUrl = mutableMapOf<String, String>()
    val urlToKey = mutableMapOf<String, String>()

    override suspend fun findByKey(key: String): ShortUrlRecord {
        if (keyToUrl.containsKey(key)) {
            return ShortUrlRecordData(key, keyToUrl[key]!!)
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun findByUrl(url: String): ShortUrlRecord {
        if (urlToKey.containsKey(url)) {
            return ShortUrlRecordData(urlToKey[url]!!, url)
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun save(key: String, url: String): SaveResult {
        synchronized(lock) {
            if (keyToUrl[key] == url) {
                return SaveSuccess(ShortUrlRecordData(key, keyToUrl[key]!!))
            }
            if (keyToUrl.containsKey(key) || urlToKey.containsKey(url)) {
                return FailedByDuplicateKeyOrUrl
            }
            keyToUrl[key] = url
            urlToKey[url] = key
            return SaveSuccess(ShortUrlRecordData(key, url))
        }
    }
}
