package dev.archimedes.cicidpipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CICDController {

  @GetMapping("/")
  public String hello() {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>CI/CD Pipeline Setup</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        margin: 20px;
                    }
                    h1, h2, h3 {
                        color: #333;
                    }
                    code {
                        background-color: #f4f4f4;
                        padding: 2px 4px;
                        border-radius: 4px;
                    }
                    pre {
                        background-color: #f4f4f4;
                        padding: 10px;
                        border-radius: 4px;
                        overflow-x: auto;
                    }
                </style>
            </head>
            <body>
                <h1>CI/CD Pipeline Setup</h1>
                <p>This document provides a step-by-step guide to set up a CI/CD pipeline using GitHub Actions, Docker, and Amazon ECR.</p>
            
                <h2>Step 1: Create a GitHub Repository</h2>
                <p>Create a new repository on GitHub to host your project code.</p>
            
                <h2>Step 2: Add a Dockerfile</h2>
                <p>Create a <code>Dockerfile</code> in the root of your project:</p>
                <pre><code class="language-dockerfile">
            # Use an official OpenJDK runtime as a parent image
            FROM maven:3.9.6-eclipse-temurin-21 AS build
            COPY . .
            RUN mvn clean package -DskipTests
            FROM eclipse-temurin:21
            COPY --from=build /target/cicid-pipeline-0.0.1-SNAPSHOT.jar myapp.jar
            COPY --from=build /target/classes /target/classes
            
            EXPOSE 8080
            ENTRYPOINT ["java", "-jar", "myapp.jar"]
                </code></pre>
            
                <h2>Step 3: Set Up CI Workflow</h2>
                <p>Create a <code>.github/workflows/ci.yaml</code> file:</p>
                <pre><code class="language-yaml">
            name: CI
            
            on:
              push:
                branches: ['master']
            
            jobs:
              push-to-ecr:
                runs-on: ubuntu-latest
            
                steps:
                  - name: Checkout code
                    uses: actions/checkout@v4
            
                  - name: Set up JDK 21
                    uses: actions/setup-java@v4
                    with:
                      distribution: 'temurin'
                      java-version: '21'
            
                  - name: Cache Maven packages
                    uses: actions/cache@v4
                    with:
                      path: ~/.m2
                      key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
                      restore-keys: ${{ runner.os }}-maven
            
                  - name: Clean Maven Packages
                    run: mvn clean package
            
                  - name: Configure AWS credentials
                    uses: aws-actions/configure-aws-credentials@v4
                    with:
                      aws-region: ${{secrets.AWS_REGION}}
                      aws-access-key-id: ${{secrets.AWS_ACCESS_KEY_ID}}
                      aws-secret-access-key: ${{secrets.AWS_ACCESS_KEY_SECRET}}
            
                  - name: Log in to Amazon ECR
                    id: login-ecr
                    uses: aws-actions/amazon-ecr-login@v1
            
                  - name: Build, tag, and push Docker image
                    env:
                      ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
                      ECR_REPOSITORY: ${{secrets.AWS_REPO}}
                      IMAGE_TAG: ${{ secrets.AWS_IMAGE_TAG}}
                    run: |
                      docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
                      docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
                </code></pre>
            
                <h2>Step 4: Set Up CD Workflow</h2>
                <p>Create a <code>.github/workflows/cd.yaml</code> file:</p>
                <pre><code class="language-yaml">
            name: CD
            
            on:
              workflow_run:
                workflows: [ "CI" ]
                types:
                  - completed
            
            jobs:
              deploy:
                runs-on: self-hosted
                env:
                  REGION: ${{secrets.AWS_REGION}}
                  ECR_REPOSITORY: ${{secrets.AWS_REPO}}
                  IMAGE_TAG: ${{ secrets.AWS_IMAGE_TAG}}
            
                steps:
                  - name: Checkout code
                    uses: actions/checkout@v4
            
                  - name: Set up AWS CLI
                    uses: aws-actions/configure-aws-credentials@v4
                    with:
                      aws-region: ${{secrets.AWS_REGION}}
                      aws-access-key-id: ${{secrets.AWS_ACCESS_KEY_ID}}
                      aws-secret-access-key: ${{secrets.AWS_ACCESS_KEY_SECRET}}
            
                  - name: Login to Amazon ECR
                    id: login-ecr
                    uses: aws-actions/amazon-ecr-login@v2
                    with:
                      mask-password: 'false'
            
                  - name: Provide docker permissions
                    run: sudo usermod -aG docker ${USER}
            
                  - name: Docker Login and Image Pull
                    run: |
                      aws ecr get-login-password --region ${{secrets.AWS_REGION}} | sudo docker login --username AWS --password-stdin ${{steps.login-ecr.outputs.registry}}
                      sudo docker pull ${{steps.login-ecr.outputs.registry}}/${{secrets.AWS_REPO}}:${{secrets.AWS_IMAGE_TAG}}
            
                  - name: Current Status Check
                    run: sudo docker ps
            
                  - name: Stopping Current Container If Any
                    run: sudo docker stop my-app || true
            
                  - name: Removing Container
                    run: sudo docker rm my-app || true
            
                  - name: Building New Container
                    run: sudo docker run -d --name my-app -p 8080:8080 ${{steps.login-ecr.outputs.registry}}/${{secrets.AWS_REPO}}:${{secrets.AWS_IMAGE_TAG}}
            
                  - name: Image Cleaning
                    run: |
                      echo "Starting Image Pruning"
                      sudo docker image prune -a -f
                </code></pre>
            
                <h2>Step 5: Configure AWS Secrets</h2>
                <p>In your GitHub repository, go to <strong>Settings</strong> &gt; <strong>Secrets</strong> and add the following secrets:</p>
                <ul>
                    <li><code>AWS_REGION</code></li>
                    <li><code>AWS_ACCESS_KEY_ID</code></li>
                    <li><code>AWS_ACCESS_KEY_SECRET</code></li>
                    <li><code>AWS_REPO</code></li>
                    <li><code>AWS_IMAGE_TAG</code></li>
                </ul>
            
                <h2>Step 6: Commit and Push</h2>
                <p>Commit your changes and push them to the <code>master</code> branch of your GitHub repository. This will trigger the CI/CD pipeline.</p>
            
                <h2>Conclusion</h2>
                <p>Following these steps, you will have a CI/CD pipeline set up using GitHub Actions, Docker, and Amazon ECR. This pipeline will build, test, and deploy your application automatically.</p>
            </body>
            </html>
            """;
  }

  public record Message(
      @JsonProperty("message")
      String value
  ) {

  }
}
