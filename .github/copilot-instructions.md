# Architecture Rules

## Core Principles

- Scan the relevant codebase before making changes.
- Follow a standard CVM architecture using `core`, `modules`, `routes`, and `main.dart`.
- Models are the single source of truth.
- Services must receive payload models and return UI models parsed from API JSON through `toJson` and `fromJson`.
- Controllers must expose reactive UI models and update them through `copyWith`.
- Controllers must call services through the API error handler only.
- The API error handler must return a fixed response model defined in the same file, containing:

  - `data<T>`
  - `success`
  - `errorCode`
- The API error handler must also handle user-facing errors consistently.
- Every module must have its own dedicated `model`, `service`, `controller`, and `view`.
- Shared logic across modules must go into a shared module or shared submodule.
- Cross-cutting services, models, controllers, and app infrastructure must go into `core`.
- `routes` must remain the single source of truth for route names and page registration.

## Base App Initialization

When initializing the base app, keep this exact root structure:

- `analysis_options.yaml`
- `README.md`
- `pubspec.yaml`
- `pubspec.lock`
- `italir_pothe.iml`
- `android/`
- `ios/`
- `lib/`
- `linux/`
- `macos/`
- `test/`
- `web/`
- `windows/`

Under `lib/`, keep this exact structure:

- `lib/main.dart`
- `lib/core/`
- `lib/core/bindings/`
- `lib/core/bootstrap/`
- `lib/core/localization/`
- `lib/core/localization/app_translations.dart`
- `lib/core/models/`
- `lib/core/services/`
- `lib/core/services/api_client.dart`
- `lib/core/services/api_error_handler.dart`
- `lib/core/services/auth_service.dart`
- `lib/core/themes/`
- `lib/core/utils/`
- `lib/core/widgets/`
- `lib/modules/`
- `lib/routes/`
- `lib/routes/app_pages.dart`
- `lib/routes/app_routes.dart`

Rules for initialization:

- Create only the folders and empty files listed above.
- Do not generate boilerplate code unless explicitly requested.
- Every new module under `lib/modules/` must include dedicated folders or files for:

  - model
  - service
  - controller
  - view

## Backend Integration Rules

- Use backend response values directly.
- Refer to the Postman collection JSON for exact variable names.
- Do not guess field names.
- Do not use fallback lists of possible keys.
- Do not map multiple possible backend names for the same field.
- Only use documented backend variable names from the Postman collection JSON.
- Only hydrate existing UI models from backend data.
- Do not create new UI models, fields, or UI elements from undocumented backend response data.
- If the required JSON field name is missing from the Postman collection:

  - use one temporary variable name only
  - add this exact comment: `Postman does not contain the proper variable name`
  - immediately report that missing variable-name issue back to the user

## Code Quality Rules

- Avoid redundant wrapper methods.
- Do not create pass-through methods that only call another method without adding logic.
- Do not create both `fromJson` and `fromMap` unless they have clearly different responsibilities.
- Prefer a single parsing entry point per model.
- Every method must have a clear responsibility such as:

  - transformation
  - validation
  - abstraction with domain value
- If a method only forwards parameters without modification, remove it.
- Direct delegation without logic is not allowed.
- Wrapper methods are allowed only when they add transformation, validation, or domain logic.
- Redundant abstraction is not allowed.

## Implementation Expectations

- Keep architecture consistent with existing project structure.
- Do not introduce patterns that conflict with CVM.
- Keep controllers thin and focused on state orchestration.
- Keep services responsible for API communication and model conversion.
- Keep models responsible for typed data representation and parsing.
- Keep shared reusable app concerns inside `core`.
- Keep module-specific concerns inside their respective module folders.
- When implementing a module , do not go beyond the scope of that module's model, service, controller, and view.
