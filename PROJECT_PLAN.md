# AuraApps Project Plan

This document outlines the suggested structure for managing the AuraApps project using GitHub Issues, Projects, and Milestones.

## 1. Milestones

Milestones are used to group issues into larger versions or releases.

*   **v0.1: MVP**
    *   Focus: Core application flows working end-to-end.
    *   Issues: Basic functionality, remote APIs, initial UI.
*   **v0.2: On-device Models**
    *   Focus: Integrating local AI models for offline capabilities.
    *   Issues: On-device LLM (MLC), local Stable Diffusion (ONNX).
*   **v0.3: Platform Polish**
    *   Focus: Improving the application's foundation and reliability.
    *   Issues: Scoped storage, camera handling, threading, download worker.
*   **v0.4: Safety + UX**
    *   Focus: Enhancing user safety, experience, and application settings.
    *   Issues: GuardService, settings validation, voice input, general UI polish.

## 2. Labels

Labels help categorize issues for better filtering and prioritization.

### Area
*   `area:llm`: Related to the Large Language Model module.
*   `area:vision`: Related to image generation (local or remote).
*   `area:platform`: Core Android platform features (storage, camera, etc.).
*   `area:safety`: Related to safety features like GuardService.
*   `area:ux`: User experience and user interface.
*   `area:docs`: Documentation tasks.
*   `area:billing`: In-app purchases and billing.
*   `area:testing`: Related to testing infrastructure and test cases.

### Type
*   `type:feature`: A new feature request.
*   `type:bug`: A bug report.
*   `type:enhancement`: An improvement to an existing feature.
*   `type:chore`: Maintenance tasks (e.g., refactoring, dependency updates).

### Priority
*   `prio:critical`: Must be fixed immediately.
*   `prio:high`: High-priority task.
*   `prio:medium`: Medium-priority task.
*   `prio:low`: Low-priority task.

## 3. GitHub Project: AuraApps Roadmap

A GitHub Project board provides a visual way to track progress.

*   **Name:** `AuraApps Roadmap`
*   **Fields to Add:**
    *   `Status`: Todo, In Progress, Blocked, Done (default)
    *   `Priority`: (using priority labels)
    *   `Area/Module`: (using area labels)
    *   `Milestone`: (linked to the milestones above)
*   **Suggested Views:**
    *   **Board by Status:** The default Kanban board view.
    *   **Table by Milestone:** A table layout, with issues grouped by their milestone.
    *   **Area-specific Views:** Create filtered views for each area label (e.g., a view for `area:vision`).
*   **Automation:**
    *   Configure automation to automatically add any new issue in the repository to the project board.
    *   Enable the feature where issues are automatically moved to "Done" when a pull request that "Closes" them is merged.

## 4. Issue Templates

Create files in a `.github/ISSUE_TEMPLATE/` directory to standardize issue creation.

### Feature Request (`.github/ISSUE_TEMPLATE/feature_request.md`)
```markdown
---
name: '✨ Feature Request'
about: 'Suggest an idea for this project'
title: ''
labels: 'type:feature'
assignees: ''
---

**Is your feature request related to a problem? Please describe.**
A clear and concise description of what the problem is. Ex. I'm always frustrated when [...]

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Acceptance Criteria**
- [ ] Criteria 1
- [ ] Criteria 2

**Additional context**
Add any other context or screenshots about the feature request here.
```

### Bug Report (`.github/ISSUE_TEMPLATE/bug_report.md`)
```markdown
---
name: '🐛 Bug Report'
about: 'Create a report to help us improve'
title: ''
labels: 'type:bug'
assignees: ''
---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Device (please complete the following information):**
 - OS: [e.g. Android 13]
 - App Version [e.g. 1.0]

**Additional context**
Add any other context about the problem here.
```
