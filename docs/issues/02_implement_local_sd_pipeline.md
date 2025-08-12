# Issue: Implement local Stable Diffusion (ONNX) image generation pipeline

**Description**

This task is to build the end-to-end pipeline for generating images locally using a Stable Diffusion model in the ONNX format. This will allow users to create images without relying on a remote server.

**Acceptance Criteria**

- [ ] The ONNX Runtime is correctly configured for the Stable Diffusion model.
- [ ] The `ImageGenLocal` service can take a prompt and generate an image.
- [ ] The generated image is displayed to the user in the `ImageGenActivity`.
- [ ] The process is reasonably performant on a mid-range device.
- [ ] The UI provides feedback to the user while the image is being generated.

**Milestone**

v0.2: On-device Models

**Labels**

- `area:vision`
- `type:feature`
- `prio:high`
