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
        val key = "key"
        val url = "http://url"
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordData(key, url)

        val responseEntity = runBlocking { cut.accessByKey(key) }

        assertEquals(responseEntity.statusCode, HttpStatus.FOUND)
        assertEquals(responseEntity.headers["Location"]?.get(0), url)
    }

    @Test
    fun accessByKey_whenRecordNotFound() {
        val key = "key"
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordEmpty

        val responseEntity = runBlocking { cut.accessByKey(key) }

        assertEquals(responseEntity.statusCode, HttpStatus.NOT_FOUND)
    }

    @Test
    fun save_success() {
        val key = "key"
        val request = SaveUrlRequest("http://url")
        coEvery { shortUrlRepository.save(key, request.url) } returns SaveSuccess(ShortUrlRecordData(key, request.url))

        val responseEntity = runBlocking { cut.save(key, request) }

        assertEquals(responseEntity.statusCode, HttpStatus.OK)
        assertEquals(responseEntity.body, ShortUrlRecordData(key, request.url))
    }

    @Test
    fun save_failByDuplicateKey() {
        val key = "key"
        val request = SaveUrlRequest("http://url")
        coEvery { shortUrlRepository.save(key, request.url) } returns FailedByDuplicateKeyOrUrl

        val responseEntity = runBlocking { cut.save(key, request) }

        assertEquals(responseEntity.statusCode, HttpStatus.CONFLICT)
    }
}
