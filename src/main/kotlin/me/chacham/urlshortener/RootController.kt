package me.chacham.urlshortener

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RootController(private val shortUrlRepository: ShortUrlRepository) {
    @GetMapping("/{key}")
    suspend fun accessByKey(@PathVariable("key") key: String): ResponseEntity<Unit> {
        return when (val shortUrlRecord = shortUrlRepository.findByKey(key)) {
            is ShortUrlRecordData -> ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", shortUrlRecord.url)
                .build()
            is ShortUrlRecordEmpty -> ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/api/v1/{key}")
    suspend fun save(@PathVariable("key") key: String, @RequestBody request: SaveUrlRequest): ResponseEntity<ShortUrlRecordData> {
        return when (val saveResult = shortUrlRepository.save(key, request.url)) {
            is SaveSuccess -> ResponseEntity.ok(saveResult.shortUrlRecordData)
            FailedByDuplicateKeyOrUrl -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}

data class SaveUrlRequest(val url: String)
