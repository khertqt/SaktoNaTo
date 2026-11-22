# Virtual T-Shirt Try-On System Enhancement Plan

Improve the virtual T-shirt try-on system in **SaktoNaTo** to provide high camera tracking accuracy, accurate body measurement estimation, product-specific size charts, real-time 3D garment fitting, fit classification, and side-by-side size comparison.

## User Review Required

> [!IMPORTANT]
> - **Product-Specific Sizing Database**: Each listing/product will now support distinct, brand-specific size charts (XS to XXL dimensions for chest width, shoulder width, shirt length, sleeve length, sleeve opening, and hem width) rather than a single universal scale.
> - **Camera Calibration & Guidance**: Added a pre-try-on calibration step with visual framing guides (distance, centering, and upper body visibility) and smoothing algorithms to ensure stable tracking.
> - **Real-Time Fit Analysis & Comparison**: Added dynamic sizing controls during live try-on and a side-by-side size comparison view (XS vs S vs M vs L vs XL vs XXL) showing real-time fit recommendations (Too tight, Slim fit, Regular fit, Relaxed fit, Oversized).

## Open Questions

- None. The design builds cleanly upon the existing architecture, Compose UI components, and state management in `Store` and `FitLogic`.

## Proposed Changes

### Data & Domain Layer (`com.example.saktonato.data` & `domain`)

#### [MODIFY] [Model.kt](file:///C:/Users/qwert/AndroidStudioProjects/SaktoNaTo/app/src/main/java/com/example/saktonato/data/Model.kt)
- Expand `Listing` data class to include product-specific size charts (`Map<String, GarmentDimensions>`), fit type, fabric stretch properties, and brand metadata.
- Define `GarmentDimensions` data class (`chestWidth`, `shoulderWidth`, `length`, `sleeveLength`, `sleeveOpening`, `hemWidth`).
- Add sample product size charts for various brands and styles.
- Add `BodyMeasurements` data class for user profile (shoulder width, chest circumference, waist circumference, torso length, sleeve length, neck measurement, upper arm circumference).

#### [MODIFY] [FitLogic.kt](file:///C:/Users/qwert/AndroidStudioProjects/SaktoNaTo/app/src/main/java/com/example/saktonato/domain/FitLogic.kt)
- Enhance fit computation to evaluate product-specific size chart dimensions against user body measurements.
- Add granular fit classifications ("Too tight", "Slim fit", "Regular fit", "Relaxed fit", "Oversized") based on ease calculations.

### UI & Try-On Layer (`com.example.saktonato.ui.screens` & `components`)

#### [MODIFY] [CommonComponents.kt](file:///C:/Users/qwert/AndroidStudioProjects/SaktoNaTo/app/src/main/java/com/example/saktonato/ui/components/CommonComponents.kt)
- Enhance `ShirtShape` and `GarmentGraphic` to render non-uniform scaling based on garment dimensions (chest, shoulders, length, sleeves).
- Add calibration guide overlay and tracking confidence indicator components.

#### [MODIFY] [TryOnCaptureSavedCardScreens.kt](file:///C:/Users/qwert/AndroidStudioProjects/SaktoNaTo/app/src/main/java/com/example/saktonato/ui/screens/TryOnCaptureSavedCardScreens.kt)
- Implement Camera Calibration flow (distance check, upper body framing, tracking stability check).
- Add live size selector (XS to XXL) that instantly updates 3D garment dimensions and fit feedback without restarting the camera.
- Add side-by-side size comparison mode to contrast different sizes on the user's body.
- Implement smoothing algorithms and perspective correction for camera tracking.

## Verification Plan

### Automated Tests
- Run existing unit tests: `gradle_build(":app:testDebugUnitTest")`
- Add unit tests for product-specific sizing calculations and fit verdict algorithms.

### Manual Verification
- Deploy app to connected emulator/device using `deploy`.
- Verify camera calibration flow, live tracking stability, size switching, and side-by-side size comparison.
