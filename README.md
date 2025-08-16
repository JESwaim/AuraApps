# Aura Apps (Android)

An AI-powered Android application featuring on-device language models, image generation, and intelligent conversation capabilities.

## 🚀 Quick Start

This repo is ready to push to GitHub. Open in Android Studio and run.

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 26+ (target SDK 34)
- Java 17
- 4GB+ RAM recommended for on-device AI models

### Building
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew lintDebug         # Run code quality checks
./gradlew testDebugUnitTest # Run unit tests
```

## 📱 Features

- **AI Tutor Module**: Intelligent Q&A using on-device LLM
- **Image Generation**: Local and remote AI image creation
- **Smart Camera**: Camera integration with AI analysis
- **Hot or Not Module**: AI-powered content rating
- **Chat History**: Persistent conversation storage
- **Settings Management**: Configurable AI endpoints and models

## 🤖 Models

- Place on-device models under `app/src/main/assets/models/` (see `/docs`).
- Use Settings to test a Remote Image endpoint and to download models.
- Supports MLC LLM and ONNX Runtime for local inference

## 🔧 CI/CD Pipeline

This project includes a comprehensive CI/CD pipeline with GitHub Actions:

- **Continuous Integration**: Automated building, testing, and linting on all PRs
- **Automated Deployment**: Staging and production deployments from main branch  
- **Security Scanning**: Weekly dependency and vulnerability checks
- **Code Quality**: Android lint, ktlint, and static analysis

See [CI/CD Pipeline Documentation](docs/CI_CD_Pipeline.md) for detailed setup and configuration.

## 📊 Project Status

![CI](https://github.com/JESwaim/AuraApps/workflows/CI/badge.svg)
![Deploy](https://github.com/JESwaim/AuraApps/workflows/Deploy/badge.svg)
![Security](https://github.com/JESwaim/AuraApps/workflows/Security%20&%20Dependencies/badge.svg)

Current version: 1.0 (Generated 2025-08-10)

## 📖 Documentation

- [Build Instructions](docs/MLC_Android_Build_Steps.md)
- [CI/CD Pipeline](docs/CI_CD_Pipeline.md) 
- [Project Planning](PROJECT_PLAN.md)
- [Issue Templates](.github/ISSUE_TEMPLATE/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)  
5. Open a Pull Request

The CI pipeline will automatically run tests and quality checks on your PR.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
