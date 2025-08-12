# Issue: Document and standardize remote image generation API contract

**Description**

The app communicates with a remote server for image generation. To ensure stability and ease of maintenance, the API contract (request/response format, authentication, endpoints) needs to be clearly documented and standardized.

**Acceptance Criteria**

- [ ] A document is created (e.g., in OpenAPI/Swagger format or a clear Markdown file) that specifies the remote API contract.
- [ ] The document includes details on request parameters, response objects, error codes, and authentication methods.
- [ ] The Android client code in `ImageGenRemote.kt` is updated to strictly adhere to this contract.
- [ ] The documentation is added to the repository's `docs` folder.

**Milestone**

v0.1: MVP

**Labels**

- `area:vision`
- `area:docs`
- `type:enhancement`
- `prio:medium`
