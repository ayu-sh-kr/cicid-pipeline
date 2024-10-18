package dev.archimedes.cicidpipeline;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CICDController {

  @GetMapping("/")
  public List<HelloWorld> hello() {
    return List.of(
        new HelloWorld("Created Continuous Integration"),
        new HelloWorld("Created Docker File"),
        new HelloWorld("Login to AWS ECR"),
        new HelloWorld("Uploaded Docker Image to ECR"),
        new HelloWorld("Created Continuous Deployment"),
        new HelloWorld("Login to docker via AWS ECR"),
        new HelloWorld("Pull the docker image AWS ECR"),
        new HelloWorld("Created container and deployed on EC2"),
        new HelloWorld("Create new EC2 instance, testing without AWS CLI")
    );
  }

  public record HelloWorld(
      String message
  ) {

  }
}
