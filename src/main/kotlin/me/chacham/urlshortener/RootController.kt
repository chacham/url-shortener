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

    @PutMapping("/api/v1/{key}")
    suspend fun save(
        @PathVariable("key") key: String,
        @RequestBody request: SaveUrlRequest
    ): ResponseEntity<SaveUrlResponse> {
        return when (val saveResult = shortUrlRepository.save(Key(key), Url(request.url))) {
            is SaveSuccess -> {
                val key = saveResult.shortUrlRecordData.key
                val url = saveResult.shortUrlRecordData.url
                return ResponseEntity.ok(SaveUrlResponse(key.value, url.value))
            }

            SaveFailedByDuplicateKeyOrUrl -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}

data class SaveUrlRequest(val url: String)
data class SaveUrlResponse(val key: String, val url: String)
