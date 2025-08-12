# Issue: Update image picking and camera capture flows for scoped storage

**Description**

Modern Android versions require the use of Scoped Storage, which changes how apps can access files on the device. This task is to update the app's functionality for picking images from the gallery and saving photos from the camera to be compliant with these new requirements.

**Acceptance Criteria**

- [ ] The app uses the `MediaStore` API or `ACTION_OPEN_DOCUMENT` for picking images from the gallery.
- [ ] Photos taken with the in-app camera are saved to a location compliant with Scoped Storage (e.g., the app's private directory or the shared media collections).
- [ ] The app correctly handles all necessary permissions for file access.
- [ ] The user experience for picking and saving images is smooth and intuitive.

**Milestone**

v0.3: Platform Polish

**Labels**

- `area:platform`
- `type:chore`
- `prio:high`
