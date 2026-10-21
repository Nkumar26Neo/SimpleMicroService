# SimpleMicroService CI/CD Pipeline

This repository uses GitHub Actions for continuous integration and deployment. The pipeline follows trunk-based development with two main workflows: a Pull Request Quality Gate and a Main Branch Release Engine.

## Architecture Overview

1. **PR Quality Gate (`pr-check.yml`)**: Runs unit tests and uploads JaCoCo code coverage to SonarCloud whenever a Pull Request is opened against the `main` branch. It blocks merges if the code fails quality checks.
2. **Main Release Engine (`main-release.yml`)**: Runs when code is merged to `main`. It re-runs tests, updates the baseline SonarCloud metrics, builds a Docker image, and pushes it to the GitHub Container Registry (GHCR).

---

## Step 1: Configure Secrets

Before running the pipelines, you must configure authentication for SonarCloud. GHCR authentication is handled automatically by GitHub Actions.

1. **Generate SonarCloud Token**:
   * Log into SonarCloud.
   * Click your profile icon (top right) -> **My Account** -> **Security**.
   * Generate a new token (e.g., "GitHub Actions CI") and copy it.
2. **Add to GitHub Secrets**:
   * In this GitHub repository, go to **Settings > Secrets and variables > Actions**.
   * Click **New repository secret**.
   * Name: `SONAR_TOKEN`
   * Secret: (Paste your token here)

---

## Step 2: Project Files

Ensure your repository contains the following files. 

### 1. The CI/CD Dockerfile
Place this optimized, single-stage `Dockerfile` in the root of your project. It relies on GitHub Actions to build the `.jar` first.

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S springgroup && adduser -S springuser -G springgroup
USER springuser:springgroup
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Pull Request Workflow
Create `.github/workflows/pr-check.yml`:

```yaml
name: Pull Request Quality Gate
on:
  pull_request:
    branches:
      - main
jobs:
  test-and-analyze:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0 
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
      - name: Build, Test, and Analyze with SonarCloud
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }} 
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }} 
        run: mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=Nkumar26Neo_SimpleMicroService -Dsonar.organization=nkumar26neo
```

### 3. Main Release Workflow
Create `.github/workflows/main-release.yml`:

```yaml
name: Main Branch Release Engine
on:
  push:
    branches:
      - main

permissions:
  contents: read
  packages: write

jobs:
  build-test-push:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build, Test, and Analyze with SonarCloud
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=Nkumar26Neo_SimpleMicroService -Dsonar.organization=nkumar26neo

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Lowercase Repository Name
        run: |
          echo "REPO=${GITHUB_REPOSITORY,,}" >> ${GITHUB_ENV}

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ghcr.io/${{ env.REPO }}:latest
            ghcr.io/${{ env.REPO }}:${{ github.sha }}
```

*(Note: Replace `Nkumar26Neo_SimpleMicroService` and `nkumar26neo` if your SonarCloud project keys change).*

---

## Step 3: Managing the Docker Image in GHCR

After your first successful merge to `main`, the Docker image will be pushed to the GitHub Container Registry. 

**Make the image public:**
1. On your GitHub repository homepage, look at the bottom right sidebar under **Packages** and click the new package.
2. Click **Package settings** (right sidebar).
3. Scroll down to **Danger Zone** -> **Change package visibility** -> Select **Public**.

**Run the image locally:**
Once public, anyone can run your application using Podman or Docker:
```bash
podman run -d -p 8080:8080 ghcr.io/nkumar26neo/simplemicroservice:latest
```