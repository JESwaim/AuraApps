# Issue: Harden model download and extraction (ModelDownloadWorker)

**Description**

The `ModelDownloadWorker` is responsible for downloading and extracting the AI models. This task is to make this process more robust and user-friendly by adding better error handling, progress reporting, and recovery mechanisms.

**Acceptance Criteria**

- [ ] The download worker correctly reports download progress to the UI.
- [ ] The worker handles network interruptions and can resume downloads if possible.
- [ ] The extraction process is reliable and handles corrupted or incomplete zip files.
- [ ] Clear error messages are shown to the user if a download or extraction fails.
- [ ] The worker verifies the integrity of the downloaded files (e.g., via checksums if available).

**Milestone**

v0.3: Platform Polish

**Labels**

- `area:platform`
- `type:enhancement`
- `prio:medium`
