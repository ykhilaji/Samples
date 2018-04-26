package com.github.gr1f0n6x.controller

import com.github.gr1f0n6x.service.AuthorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

@RestController
@RequestMapping(value = Array("/author"))
class AuthorController {
  @Autowired
  var service: AuthorService = _

  @GetMapping(value = Array("/{id}"))
  def get(@PathVariable id: Long): String = service.select(id)

  @GetMapping(value = Array(""))
  def getAll(): String = service.select()

  @PutMapping(value = Array(""))
  def save(@RequestBody body: String): String = service.insert(body)

  @PostMapping(value = Array(""))
  def update(@RequestBody body: String): String = service.update(body)

  @DeleteMapping(value = Array("/{id}"))
  def delete(@PathVariable id: Long): String = service.delete(id)
}
