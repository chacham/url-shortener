package me.chacham.urlshortener

import org.springframework.stereotype.Repository

@Repository
class ShortUrlInMemoryRepository : ShortUrlRepository {
    val lock = Any()
    val keyToUrl = mutableMapOf<Key, Url>()
    val urlToKey = mutableMapOf<Url, Key>()

    override suspend fun findByKey(key: Key): ShortUrlRecord {
        if (keyToUrl.containsKey(key)) {
            return ShortUrlRecordData(key, keyToUrl[key]!!)
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun findByUrl(url: Url): ShortUrlRecord {
        if (urlToKey.containsKey(url)) {
            return ShortUrlRecordData(urlToKey[url]!!, url)
        }
        return ShortUrlRecordEmpty
    }

    override suspend fun save(key: Key, url: Url): SaveResult {
        synchronized(lock) {
            if (keyToUrl[key] == url) {
                return SaveSuccess(ShortUrlRecordData(key, keyToUrl[key]!!))
            }
            if (keyToUrl.containsKey(key) || urlToKey.containsKey(url)) {
                return SaveFailedByDuplicateKeyOrUrl
            }
            keyToUrl[key] = url
            urlToKey[url] = key
            return SaveSuccess(ShortUrlRecordData(key, url))
        }
    }
}
