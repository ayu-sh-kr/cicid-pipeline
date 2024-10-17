package dev.archimedes.cicidpipeline;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CICDController {

  @GetMapping("/")
  public HelloWorld hello() {
    return new HelloWorld("Hello, World!");
  }

  public record HelloWorld(
      String message
  ) {

  }
}
