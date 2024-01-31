package me.chacham.urlshortener

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RootController(private val shortUrlRepository: ShortUrlRepository) {
    @GetMapping("/{key}")
    suspend fun accessByKey(@PathVariable("key") key: Key): ResponseEntity<Unit> {
        return when (val shortUrlRecord = shortUrlRepository.findByKey(key)) {
            is ShortUrlRecordData -> ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", shortUrlRecord.url.value)
                .build()
            is ShortUrlRecordEmpty -> ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/api/v1")
    suspend fun put(
        @RequestBody request: UrlRequest
    ): ResponseEntity<ShortUrlRecordResponse> {
        val url = Url(request.url)

        val shortUrlRecord = shortUrlRepository.findByUrl(url)
        if (shortUrlRecord is ShortUrlRecordData) {
            return ResponseEntity.ok(ShortUrlRecordResponse(shortUrlRecord.key.value, shortUrlRecord.url.value))
        }

        val key = shortUrlRepository.generateKey()
        when (shortUrlRepository.save(key, url)) {
            is SaveSuccess -> {
                return ResponseEntity.ok(ShortUrlRecordResponse(key.value, url.value))
            }

            SaveFailedByDuplicateKeyOrUrl -> {
                val alreadyCreatedRecord = shortUrlRepository.findByUrl(url)
                if (alreadyCreatedRecord !is ShortUrlRecordData) {
                    // Unreachable
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
                return ResponseEntity.ok(
                    ShortUrlRecordResponse(
                        alreadyCreatedRecord.key.value,
                        alreadyCreatedRecord.url.value
                    )
                )
            }
        }
    }

    @PostMapping("/api/v1/{key}")
    suspend fun save(
        @PathVariable("key") key: String,
        @RequestBody request: UrlRequest
    ): ResponseEntity<ShortUrlRecordResponse> {
        return when (val saveResult = shortUrlRepository.save(Key(key), Url(request.url))) {
            is SaveSuccess -> {
                val key = saveResult.shortUrlRecordData.key
                val url = saveResult.shortUrlRecordData.url
                return ResponseEntity.ok(ShortUrlRecordResponse(key.value, url.value))
            }

            SaveFailedByDuplicateKeyOrUrl -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}

data class UrlRequest(val url: String)
data class ShortUrlRecordResponse(val key: String, val url: String)
