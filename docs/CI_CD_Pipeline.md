# CI/CD Pipeline Documentation

This document describes the CI/CD pipeline configuration for the AuraApps Android project.

## Overview

The CI/CD pipeline is implemented using GitHub Actions and consists of three main workflows:

1. **CI Pipeline** (`ci.yml`) - Continuous Integration for all branches and PRs
2. **Deployment Pipeline** (`deploy.yml`) - Automated deployment for main branch
3. **Security & Dependencies** (`security.yml`) - Weekly security and dependency checks

## Workflows

### 1. CI Pipeline (`.github/workflows/ci.yml`)

**Triggers:**
- Pull requests to `main` and `develop` branches
- Pushes to `main`, `develop`, and feature branches (`feature/*`, `hotfix/*`)
- Manual workflow dispatch

**Jobs:**
- **Build**: Compiles debug and release APKs, uploads artifacts
- **Lint**: Runs Android lint and ktlint for code quality
- **Test**: Executes unit tests (placeholder for future implementation)
- **Security**: Basic security scanning (placeholder for future tools)
- **Notify**: Provides build status summary

**Artifacts Generated:**
- Debug APK (7-day retention)
- Release APK (30-day retention)
- Lint reports (HTML and XML formats)
- Test results (when tests exist)

### 2. Deployment Pipeline (`.github/workflows/deploy.yml`)

**Triggers:**
- Pushes to `main` branch
- Manual workflow dispatch with environment selection

**Jobs:**
- **Validate**: Pre-deployment validation and version extraction
- **Build Release**: Creates production-ready APK with version tagging
- **Deploy**: Deploys to staging or production (placeholder implementation)
- **Create Release**: Creates GitHub releases for production deployments
- **Notify**: Deployment status notifications

**Security Features:**
- Environment-based deployments with approval gates
- Secure secrets handling for signing and deployment credentials
- Artifact cleanup after deployment

### 3. Security & Dependencies (`.github/workflows/security.yml`)

**Triggers:**
- Weekly schedule (Mondays at 2 AM UTC)
- Manual workflow dispatch
- Changes to dependency files

**Jobs:**
- **Dependency Scan**: Vulnerability scanning and dependency analysis
- **License Check**: License compliance verification
- **Code Security**: Static analysis for security patterns
- **Dependency Updates**: Checks for available updates
- **Security Summary**: Consolidated security report

## Setup Instructions

### Prerequisites

1. **Android SDK**: The project requires Android SDK API 34
2. **Java 17**: Required for building with Gradle 8.5
3. **GitHub Secrets**: Configure the following secrets for full functionality:

```
# For APK signing (optional - currently unsigned builds)
KEYSTORE_FILE          # Base64 encoded keystore file
KEYSTORE_PASSWORD      # Keystore password
KEY_ALIAS             # Key alias for signing
KEY_PASSWORD          # Key password

# For deployment (when implementing actual deployment)
FIREBASE_TOKEN        # Firebase App Distribution token
FIREBASE_APP_ID       # Firebase app ID
GOOGLE_PLAY_SERVICE_ACCOUNT  # Google Play Console service account JSON
```

### Workflow Configuration

The workflows are configured with optimal settings for Android builds:

- **Gradle Optimization**: Parallel builds, daemon disabled for CI
- **Caching**: Gradle dependencies and wrapper cached between runs
- **Java Memory**: 3GB heap for large Android builds
- **Artifact Management**: Appropriate retention periods for different artifact types

### Branch Strategy

The pipeline supports the following branch strategy:

- `main`: Production-ready code, triggers deployments
- `develop`: Integration branch for features
- `feature/*`: Feature development branches
- `hotfix/*`: Critical bug fixes

### Environment Deployments

Two deployment environments are configured:

1. **Staging**: For internal testing and QA
   - Triggered on pushes to main
   - Uses Firebase App Distribution (placeholder)
   
2. **Production**: For end-user releases
   - Manual approval required
   - Deploys to Google Play Store (placeholder)
   - Creates GitHub releases

## Future Enhancements

### Security Tools Integration

The current security workflow includes placeholders for:

1. **OWASP Dependency Check**: Vulnerability scanning
   ```gradle
   plugins {
     id 'org.owasp.dependencycheck' version '8.4.0'
   }
   ```

2. **Snyk Integration**: Commercial vulnerability scanning
   ```yaml
   - name: Run Snyk to check for vulnerabilities
     uses: snyk/actions/gradle@master
     env:
       SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
   ```

3. **CodeQL Analysis**: GitHub's semantic code analysis
   ```yaml
   - name: Initialize CodeQL
     uses: github/codeql-action/init@v2
     with:
       languages: java
   ```

### Testing Enhancements

When implementing tests, add these dependencies to `app/build.gradle`:

```gradle
dependencies {
  // Unit testing
  testImplementation 'junit:junit:4.13.2'
  testImplementation 'org.mockito:mockito-core:5.1.1'
  testImplementation 'androidx.test:core:1.5.0'
  
  // Android instrumentation tests
  androidTestImplementation 'androidx.test.ext:junit:1.1.5'
  androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### Deployment Automation

For production deployment, integrate with:

1. **Fastlane**: Automated App Store deployment
2. **Firebase App Distribution**: Beta testing distribution
3. **Google Play Console API**: Automated Play Store uploads

### Code Quality Tools

Additional linting and quality tools to consider:

1. **Detekt**: Kotlin static analysis
2. **SpotBugs**: Java/Android bug detection  
3. **SonarQube**: Comprehensive code quality analysis

## Troubleshooting

### Common Issues

1. **Build Failures**: Check Java version and Android SDK installation
2. **Gradle Issues**: Clear cache with `./gradlew clean --build-cache`
3. **Signing Issues**: Verify keystore secrets are properly configured
4. **Network Issues**: Some environments may block Google Maven repository

### Monitoring

Monitor workflow performance and success rates through:

- GitHub Actions dashboard
- Workflow run history
- Artifact download statistics
- Build time trends

## Contributing

When contributing to the CI/CD pipeline:

1. Test changes in a fork first
2. Update documentation for any new features
3. Follow security best practices for secrets handling
4. Maintain backward compatibility when possible

For questions or issues with the CI/CD pipeline, create an issue with the `area:testing` label.