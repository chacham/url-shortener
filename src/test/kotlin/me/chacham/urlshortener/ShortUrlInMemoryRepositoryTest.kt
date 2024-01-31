package me.chacham.urlshortener

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ShortUrlInMemoryRepositoryTest {
    @Test
    fun findByKey_returnsEmptyRecord() {
        val repository = ShortUrlInMemoryRepository()
        val result = runBlocking { repository.findByKey(Key("key")) }
        assert(result is ShortUrlRecordEmpty)
    }

    @Test
    fun findByKey_returnsRecord_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        runBlocking { repository.save(record.key, record.url) }

        val result = runBlocking { repository.findByKey(Key("key")) }
        assert(result is ShortUrlRecordData)
        assert(result == record)
    }

    @Test
    fun findByUrl_returnsEmptyRecord() {
        val repository = ShortUrlInMemoryRepository()
        val result = runBlocking { repository.findByUrl(Url("url")) }
        assert(result is ShortUrlRecordEmpty)
    }

    @Test
    fun findByUrl_returnsRecord_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        runBlocking { repository.save(record.key, record.url) }

        val result = runBlocking { repository.findByUrl(Url("url")) }
        assert(result is ShortUrlRecordData)
        assert(result == record)
    }

    @Test
    fun save_returnsSuccess_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is SaveSuccess)
        assert(result == SaveSuccess(record))
    }

    @Test
    fun save_returnSuccess_ifKeyAndUrlEqualRecordExists() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        runBlocking { repository.save(record.key, record.url) }
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is SaveSuccess)
        assert(result == SaveSuccess(record))
    }

    @Test
    fun save_returnsFailedByDuplicateKey_ifKeyAlreadyExists() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        runBlocking { repository.save(record.key, Url("alreadySavedUrl")) }
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is SaveFailedByDuplicateKeyOrUrl)
    }

    @Test
    fun save_returnsFailedByDuplicateKey_ifUrlAlreadyExists() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData(Key("key"), Url("url"))
        runBlocking { repository.save(Key("alreadySavedKey"), record.url) }
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is SaveFailedByDuplicateKeyOrUrl)
    }
}
