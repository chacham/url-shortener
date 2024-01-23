package me.chacham.urlshortener

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ShortUrlInMemoryRepositoryTest {
    @Test
    fun findByKey_returnsEmptyRecord() {
        val repository = ShortUrlInMemoryRepository()
        val result = runBlocking { repository.findByKey("key") }
        assert(result is ShortUrlRecordEmpty)
    }

    @Test
    fun findByKey_returnsRecord_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData("key", "url")
        runBlocking { repository.save(record.key, record.url) }

        val result = runBlocking { repository.findByKey("key") }
        assert(result is ShortUrlRecordData)
        assert(result == record)
    }

    @Test
    fun findByUrl_returnsEmptyRecord() {
        val repository = ShortUrlInMemoryRepository()
        val result = runBlocking { repository.findByUrl("url") }
        assert(result is ShortUrlRecordEmpty)
    }

    @Test
    fun findByUrl_returnsRecord_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData("key", "url")
        runBlocking { repository.save(record.key, record.url) }

        val result = runBlocking { repository.findByUrl("url") }
        assert(result is ShortUrlRecordData)
        assert(result == record)
    }

    @Test
    fun save_returnsSuccess_ifRecordSaved() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData("key", "url")
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is SaveSuccess)
        assert(result == SaveSuccess(record))
    }

    @Test
    fun save_returnsFailedByDuplicateKey_ifKeyAlreadyExists() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData("key", "url")
        runBlocking { repository.save(record.key, "alreadySavedUrl") }
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is FailedByDuplicateKeyOrUrl)
    }

    @Test
    fun save_returnsFailedByDuplicateKey_ifUrlAlreadyExists() {
        val repository = ShortUrlInMemoryRepository()
        val record = ShortUrlRecordData("key", "url")
        runBlocking { repository.save("alreadySavedKey", record.url) }
        val result = runBlocking { repository.save(record.key, record.url) }
        assert(result is FailedByDuplicateKeyOrUrl)
    }
}
