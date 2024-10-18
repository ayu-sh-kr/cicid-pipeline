package dev.archimedes.cicidpipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CICDController {

  @GetMapping("/")
  public List<Message> hello() {
    return List.of(
        new Message("Created Continuous Integration"),
        new Message("Created Docker File"),
        new Message("Login to AWS ECR"),
        new Message("Uploaded Docker Image to ECR"),
        new Message("Created Continuous Deployment"),
        new Message("Login to docker via AWS ECR"),
        new Message("Pull the docker image AWS ECR"),
        new Message("Created container and deployed on EC2"),
        new Message("Create new EC2 instance, testing without AWS CLI"),
        new Message("Test with new EC2 instance completed successfully"),
        new Message("Testing Automation by removing hardcoded value from CD action file")
    );
  }

  public record Message(
      @JsonProperty("message")
      String value
  ) {

  }
}
