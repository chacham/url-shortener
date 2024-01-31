package me.chacham.urlshortener

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@WebFluxTest(RootController::class)
@ActiveProfiles("test")
class RootControllerWebTest {
    @Autowired
    lateinit var webTestClient: WebTestClient;

    @MockkBean
    lateinit var shortUrlRepository: ShortUrlInMemoryRepository

    @Test
    fun accessByKeyRedirectTest() {
        val key = Key("test-key")
        val url = Url("http://test-url")
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordData(key, url)
        webTestClient.get()
            .uri("/${key.value}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueEquals("Location", url.value)
    }

    @Test
    fun accessByKeyFailedTest() {
        val key = Key("test-key")
        coEvery { shortUrlRepository.findByKey(key) } returns ShortUrlRecordEmpty
        webTestClient.get()
            .uri("/${key.value}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun pubSuccessCreating() {
        val key = Key("test-key")
        val url = Url("http://test-url")
        coEvery { shortUrlRepository.findByUrl(url) } returns ShortUrlRecordEmpty
        coEvery { shortUrlRepository.generateKey() } returns key
        coEvery { shortUrlRepository.save(key, url) } returns SaveSuccess(ShortUrlRecordData(key, url))
        webTestClient.put()
            .uri("/api/v1")
            .bodyValue(UrlRequest(url.value))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.key").isEqualTo(key.value)
            .jsonPath("$.url").isEqualTo(url.value)
    }

    @Test
    fun pubSuccessByExistingRecord() {
        val key = Key("test-key")
        val url = Url("http://test-url")
        coEvery { shortUrlRepository.findByUrl(url) } returns ShortUrlRecordData(key, url)
        webTestClient.put()
            .uri("/api/v1")
            .bodyValue(UrlRequest(url.value))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.key").isEqualTo(key.value)
            .jsonPath("$.url").isEqualTo(url.value)
    }

    @Test
    fun saveSuccessTest() {
        val key = Key("test-key")
        val url = Url("http://test-url")
        coEvery { shortUrlRepository.save(key, url) } returns SaveSuccess(ShortUrlRecordData(key, url))
        webTestClient.post()
            .uri("/api/v1/${key.value}")
            .bodyValue(UrlRequest(url.value))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.key").isEqualTo(key.value)
            .jsonPath("$.url").isEqualTo(url.value)
    }

    @Test
    fun saveFailureTest() {
        val key = Key("test-key")
        val url = Url("http://test-url")
        coEvery { shortUrlRepository.save(key, url) } returns SaveFailedByDuplicateKeyOrUrl
        webTestClient.post()
            .uri("/api/v1/${key.value}")
            .bodyValue(UrlRequest(url.value))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }
}
