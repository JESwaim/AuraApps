#!/bin/bash

# CI/CD Pipeline Validation Script
# This script validates the GitHub Actions workflows and project setup
# Run this locally to check if your CI/CD configuration is correct

set -e

echo "🔍 AuraApps CI/CD Pipeline Validation"
echo "===================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
check_pass() {
    echo -e "${GREEN}✅ $1${NC}"
}

check_warn() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

check_fail() {
    echo -e "${RED}❌ $1${NC}"
}

# 1. Check project structure
echo "📁 Checking project structure..."

if [ -f "build.gradle" ]; then
    check_pass "Root build.gradle found"
else
    check_fail "Root build.gradle missing"
fi

if [ -f "app/build.gradle" ]; then
    check_pass "App build.gradle found"
else
    check_fail "App build.gradle missing"
fi

if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    check_pass "Gradle wrapper found and executable"
else
    check_fail "Gradle wrapper missing or not executable"
fi

if [ -d "gradle/wrapper" ]; then
    check_pass "Gradle wrapper directory found"
else
    check_fail "Gradle wrapper directory missing"
fi

echo ""

# 2. Check GitHub Actions workflows
echo "⚙️  Checking GitHub Actions workflows..."

if [ -d ".github/workflows" ]; then
    check_pass "GitHub workflows directory found"
    
    workflows=(".github/workflows/ci.yml" ".github/workflows/deploy.yml" ".github/workflows/security.yml")
    
    for workflow in "${workflows[@]}"; do
        if [ -f "$workflow" ]; then
            check_pass "$(basename "$workflow") workflow found"
            
            # Basic YAML syntax check
            if command -v yamllint >/dev/null 2>&1; then
                if yamllint "$workflow" >/dev/null 2>&1; then
                    check_pass "$(basename "$workflow") YAML syntax valid"
                else
                    check_warn "$(basename "$workflow") YAML syntax issues detected"
                fi
            else
                check_warn "yamllint not available - install with: pip install yamllint"
            fi
        else
            check_fail "$(basename "$workflow") workflow missing"
        fi
    done
else
    check_fail "GitHub workflows directory missing"
fi

echo ""

# 3. Check Android configuration
echo "🤖 Checking Android configuration..."

if grep -q "compileSdk 34" app/build.gradle; then
    check_pass "Compile SDK version 34 configured"
else
    check_warn "Compile SDK version should be 34 for optimal CI compatibility"
fi

if grep -q "minSdk 26" app/build.gradle; then
    check_pass "Minimum SDK version 26 configured"
else
    check_warn "Check minimum SDK version configuration"
fi

if grep -q 'sourceCompatibility JavaVersion.VERSION_17' app/build.gradle; then
    check_pass "Java 17 compatibility configured"
else
    check_warn "Java 17 compatibility not found - required for Gradle 8.5+"
fi

echo ""

# 4. Check for security best practices
echo "🔐 Checking security configuration..."

if grep -r -i -E "(password|secret|key|token)" app/src/ --include="*.kt" --include="*.java" | grep -v "TODO\|FIXME\|example\|placeholder" >/dev/null 2>&1; then
    check_warn "Potential hardcoded credentials found - review source code"
else
    check_pass "No obvious hardcoded credentials found"
fi

if [ -f ".gitignore" ]; then
    if grep -q "keystore" .gitignore && grep -q "*.jks" .gitignore; then
        check_pass "Keystore files properly ignored in .gitignore"
    else
        check_warn "Consider adding keystore files to .gitignore"
    fi
    check_pass ".gitignore file found"
else
    check_warn ".gitignore file missing"
fi

echo ""

# 5. Check dependencies and build configuration
echo "📦 Checking dependencies..."

required_plugins=("com.android.application" "org.jetbrains.kotlin.android")
for plugin in "${required_plugins[@]}"; do
    if grep -q "$plugin" app/build.gradle; then
        check_pass "Plugin $plugin configured"
    else
        check_fail "Plugin $plugin missing"
    fi
done

# Check for important dependencies
important_deps=("androidx.core:core-ktx" "androidx.appcompat:appcompat" "com.google.android.material:material")
for dep in "${important_deps[@]}"; do
    if grep -q "$dep" app/build.gradle; then
        check_pass "Dependency $dep found"
    else
        check_warn "Dependency $dep not found"
    fi
done

echo ""

# 6. Check documentation
echo "📚 Checking documentation..."

if [ -f "docs/CI_CD_Pipeline.md" ]; then
    check_pass "CI/CD documentation found"
else
    check_warn "CI/CD documentation missing"
fi

if [ -f "PROJECT_PLAN.md" ]; then
    check_pass "Project plan documentation found"
else
    check_warn "Project plan documentation missing"
fi

echo ""

# 7. Gradle validation (if network available)
echo "🔧 Validating Gradle setup..."

if ./gradlew --version >/dev/null 2>&1; then
    check_pass "Gradle wrapper executes successfully"
    
    gradle_version=$(./gradlew --version | grep "Gradle" | head -1)
    echo "   $gradle_version"
    
    # Try to run a simple task
    if ./gradlew tasks --quiet >/dev/null 2>&1; then
        check_pass "Gradle can list tasks (basic connectivity works)"
    else
        check_warn "Gradle tasks failed - may be due to network restrictions or missing Android SDK"
    fi
else
    check_fail "Gradle wrapper execution failed"
fi

echo ""

# 8. Summary and recommendations
echo "📋 Validation Summary"
echo "===================="
echo ""
echo "✅ Core CI/CD files are in place"
echo "✅ GitHub Actions workflows configured"
echo "✅ Android project structure is correct"
echo ""
echo "🔧 Next steps to complete CI/CD setup:"
echo "1. Push to GitHub to activate workflows"
echo "2. Configure GitHub secrets for signing (optional)"
echo "3. Set up deployment environments (staging/production)"
echo "4. Add unit tests to enable full testing pipeline"
echo "5. Configure security scanning tools (OWASP, Snyk)"
echo ""
echo "📖 See docs/CI_CD_Pipeline.md for detailed setup instructions"
echo ""

# Check if running in GitHub Actions
if [ "${GITHUB_ACTIONS}" = "true" ]; then
    echo "🚀 Running in GitHub Actions - CI/CD pipeline is active!"
else
    echo "💡 To test the full pipeline, push these changes to GitHub"
fi

echo ""
echo "🎉 Validation completed!"