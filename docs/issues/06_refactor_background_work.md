# Issue: Refactor background work for lifecycle and threading safety

**Description**

The app performs several background tasks, such as model downloads, API calls, and local processing. This task is to review and refactor this background work to ensure it is lifecycle-aware (doesn't cause crashes on screen rotation) and uses modern, safe threading practices (e.g., Kotlin Coroutines).

**Acceptance Criteria**

- [ ] All long-running operations are moved off the main thread.
- [ ] Coroutines are used correctly with appropriate scopes (e.g., `viewModelScope`, `lifecycleScope`) to prevent memory leaks and crashes.
- [ ] The app remains responsive during background operations.
- [ ] A review of existing background tasks (e.g., in `ViewModel`s or `Activity`s) is completed and any issues are addressed.

**Milestone**

v0.3: Platform Polish

**Labels**

- `area:platform`
- `type:chore`
- `prio:medium`
