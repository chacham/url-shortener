package me.chacham.urlshortener

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RootControllerTest {
    private val shortUrlRepository: ShortUrlInMemoryRepository = mockk()
    private val cut = RootController(shortUrlRepository)

    @Test
    fun accessByKey_whenRecordFound() {
        val key = Key("key")
        val url = Url("http://url")
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordData(key, url)

        val responseEntity = runBlocking { cut.accessByKey(key) }

        assertEquals(HttpStatus.FOUND, responseEntity.statusCode)
        assertEquals(url.value, responseEntity.headers["Location"]?.get(0))
    }

    @Test
    fun accessByKey_whenRecordNotFound() {
        val key = Key("key")
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordEmpty

        val responseEntity = runBlocking { cut.accessByKey(key) }

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
    }

    @Test
    fun save_success() {
        val key = Key("key")
        val url = Url("http://url")
        val request = SaveUrlRequest(url.value)
        coEvery { shortUrlRepository.save(key, url) } returns SaveSuccess(ShortUrlRecordData(key, url))

        val responseEntity = runBlocking { cut.save(key.value, request) }

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertEquals(SaveUrlResponse(key.value, url.value), responseEntity.body)
    }

    @Test
    fun save_failByDuplicateKey() {
        val key = Key("key")
        val url = Url("http://url")
        val request = SaveUrlRequest(url.value)
        coEvery { shortUrlRepository.save(key, url) } returns SaveFailedByDuplicateKeyOrUrl

        val responseEntity = runBlocking { cut.save(key.value, request) }

        assertEquals(HttpStatus.CONFLICT, responseEntity.statusCode)
    }
}
