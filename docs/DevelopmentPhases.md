# SousChef – Development Phases

> Each phase below is a **self-contained, copy-pasteable prompt** you can hand to an AI coding agent (or use as a developer briefing) to build that slice of the app from scratch. Each phase builds on the previous ones.
>
> **Before starting any phase**, ensure:
> - The project compiles cleanly.
> - All previous phases are merged.
> - `google-services.json` is in `app/`.
> - `local.properties` contains all required keys.
> - All guidelines in `docs/Coding Guidelines.md` and `docs/DesignGuidelines.md` are followed strictly.

---

## Phase Reference Map

| Phase | Epic | Title |
|-------|------|-------|
| [Phase 0](#phase-0--project-foundation--ci-skeleton) | — | Project Foundation & CI Skeleton |
| [Phase 1](#phase-1--authentication--role-management) | Epic 1 | Authentication & Role Management |
| [Phase 2](#phase-2--core-recipe-data-model--creation) | Epic 2 (partial) | Core Recipe Data Model & Creation |
| [Phase 3](#phase-3--recipe-overview--flavor-customization) | Epic 2 (partial) | Recipe Overview & Flavor Customization |
| [Phase 4](#phase-4--step-by-step-cooking-mode) | Epic 2 (partial) | Step-by-Step Cooking Mode |
| [Phase 5](#phase-5--hardware-integration--smart-spice-dispensing) | Epic 3 | Hardware Integration & Smart Spice Dispensing |
| [Phase 6](#phase-6--ai-assisted-recipe-generation) | Epic 4 | AI-Assisted Recipe Generation (Gemini) |
| [Phase 7](#phase-7--recipe-sharing--forking--personal-collections) | Epic 5 | Recipe Sharing, Forking & Personal Collections |
| [Phase 8](#phase-8--admin-panel--verified-chef-management) | Epic 1 (Admin) | Admin Panel & Verified Chef Management |
| [Phase 9](#phase-9--polish--notifications--offline-support) | Cross-cutting | Polish, Notifications & Offline Support |

---

---

# Phase 0 – Project Foundation & CI Skeleton

## Objective
Set up the complete Android project skeleton: package structure, Gradle configuration, Firebase integration, Koin DI wiring, navigation skeleton, theming, and a Design System demo screen. No feature logic yet — only infrastructure.

## Context Files to Read First
- `docs/Coding Guidelines.md` — full reference for all architectural decisions
- `docs/DesignGuidelines.md` — full reference for all UI/theming decisions
- `app/build.gradle.kts` — current build config
- `gradle/libs.versions.toml` — current version catalog
- `app/google-services.json` — already present, do not modify

## Package Root
`com.souschef.app`

---

## Step-by-Step Instructions

### 1. Version Catalog (`gradle/libs.versions.toml`)
Add / confirm the following entries are present:

**Versions:**
```toml
[versions]
koinAndroid          = "4.1.0"
koinCompose          = "4.1.0"
roomRuntime          = "2.7.2"
firebaseBom          = "33.7.0"
coroutinesPlayServices = "1.9.0"
navigationCompose3   = "1.0.0-alpha04"
coilCompose          = "3.1.0"
datastorePreferences = "1.1.4"
googleServices       = "4.4.2"
ksp                  = "2.1.10-1.0.31"
```

**Libraries:**
```toml
koin-android                  = { module = "io.insert-koin:koin-android",                  version.ref = "koinAndroid" }
koin-androidx-compose         = { module = "io.insert-koin:koin-androidx-compose",         version.ref = "koinCompose" }
koin-androidx-compose-navigation = { module = "io.insert-koin:koin-androidx-compose-navigation", version.ref = "koinCompose" }
androidx-room-runtime         = { module = "androidx.room:room-runtime",                   version.ref = "roomRuntime" }
androidx-room-ktx             = { module = "androidx.room:room-ktx",                       version.ref = "roomRuntime" }
androidx-room-compiler        = { module = "androidx.room:room-compiler",                  version.ref = "roomRuntime" }
firebase-bom                  = { module = "com.google.firebase:firebase-bom",             version.ref = "firebaseBom" }
firebase-firestore            = { module = "com.google.firebase:firebase-firestore-ktx" }
firebase-auth                 = { module = "com.google.firebase:firebase-auth-ktx" }
kotlinx-coroutines-play-services = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services", version.ref = "coroutinesPlayServices" }
androidx-navigation3-compose  = { module = "androidx.navigation3:navigation3-compose",     version.ref = "navigationCompose3" }
coil-compose                  = { module = "io.coil-kt.coil3:coil-compose",               version.ref = "coilCompose" }
coil-network-okhttp           = { module = "io.coil-kt.coil3:coil-network-okhttp",        version.ref = "coilCompose" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences",   version.ref = "datastorePreferences" }
```

**Plugins:**
```toml
google-services  = { id = "com.google.gms.google-services", version.ref = "googleServices" }
ksp              = { id = "com.google.devtools.ksp",         version.ref = "ksp" }
```

---

### 2. `app/build.gradle.kts`
Apply plugins: `kotlin-android`, `com.google.devtools.ksp`, `com.google.gms.google-services`.

Read `local.properties` at config time and expose `WEB_CLIENT_ID` as a `resValue`:
```kotlin
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}
fun propOrEnv(name: String, default: String = "") =
    localProperties.getProperty(name) ?: System.getenv(name) ?: default

buildTypes {
    debug {
        resValue("string", "WEB_CLIENT_ID", propOrEnv("WEB_CLIENT_ID"))
    }
    release {
        resValue("string", "WEB_CLIENT_ID", propOrEnv("PROD_WEB_CLIENT_ID"))
    }
}
```

Add all dependencies from the version catalog above.

---

### 3. `local.properties` entries required
Ensure these keys exist (do not hardcode values):
```properties
WEB_CLIENT_ID=<your-web-client-id-from-google-cloud-console>
PROD_WEB_CLIENT_ID=<prod-web-client-id>
```

---

### 4. Package Structure
Create all packages under `com.souschef.app`:
```
application/
api/
di/
model/
repository/
service/
ui/
  components/
  navigation/
  screens/
    designtest/
  theme/
  viewmodels/
usecases/
util/
preferences/
notifications/
```

---

### 5. Application Class (`application/SousChefApplication.kt`)
```kotlin
class SousChefApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SousChefApplication)
            modules(
                databaseModule,
                repositoryModule,
                serviceModule,
                viewModelModule,
                useCaseModule,
                preferencesModule
            )
        }
    }
}
```
Register it in `AndroidManifest.xml`.

---

### 6. Theme (`ui/theme/`)

#### `Color.kt`
Define ALL named colors used in the design system. Provide both light and dark variants as separate named constants. Use the exact hex values from `docs/DesignGuidelines.md`.

Key colors to define (non-exhaustive — add all from the design doc):
```kotlin
// Brand
val GoldMuted = Color(0xFFD4AF6A)
val GoldLight = Color(0xFFE8C97A)     // lighter gold for dark theme highlights
val GoldDark  = Color(0xFFB8943A)     // deeper gold for light theme use

// Light Theme Surfaces
val IvoryWhite    = Color(0xFFFAF9F7)
val PearlWhite    = Color(0xFFFFFFFF)
val CreamLight    = Color(0xFFF5F3F0)
val GlassWhiteLight = Color(0xCCFFFFFF)  // 80% white

// Dark Theme Surfaces
val CharcoalDeep   = Color(0xFF1A1A1A)
val CharcoalMedium = Color(0xFF2D2D2D)
val CharcoalLight  = Color(0xFF3A3A3A)
val GlassWhiteDark = Color(0x33FFFFFF)   // 20% white

// Text — Light
val TextPrimaryLight   = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF666666)
val TextTertiaryLight  = Color(0xFF999999)
val TextDisabledLight  = Color(0xFFCCCCCC)

// Text — Dark
val TextPrimaryDark    = Color(0xFFFAFAFA)
val TextSecondaryDark  = Color(0xFFB3B3B3)
val TextTertiaryDark   = Color(0xFF808080)
val TextDisabledDark   = Color(0xFF4D4D4D)

// Borders
val BorderLight = Color(0x1A000000)   // 10% black
val BorderDark  = Color(0x33FFFFFF)   // 20% white

// Accents
val SageGreen      = Color(0xFF7D9B76)
val TerracottaMuted = Color(0xFFC17F6B)
val DeepBurgundy   = Color(0xFF6B2D3E)
val DeepOlive      = Color(0xFF5C6B3A)

// Semantic
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed     = Color(0xFFE53935)
val WarningAmber = Color(0xFFFFA000)
```

#### `Theme.kt`
Create `lightColorScheme` and `darkColorScheme` using Material3. Map:
- `primary` → `GoldMuted`
- `background` → `IvoryWhite` (light) / `CharcoalDeep` (dark)
- `surface` → `PearlWhite` (light) / `CharcoalMedium` (dark)
- `surfaceVariant` → `CreamLight` (light) / `CharcoalLight` (dark)
- `onPrimary` → `PearlWhite` (light) / `CharcoalDeep` (dark)
- `onBackground` → `TextPrimaryLight` (light) / `TextPrimaryDark` (dark)
- `onSurface` → `TextPrimaryLight` (light) / `TextPrimaryDark` (dark)
- `onSurfaceVariant` → `TextSecondaryLight` (light) / `TextSecondaryDark` (dark)
- `outline` → `BorderLight` (light) / `BorderDark` (dark)
- `error` → `ErrorRed`

Expose a `SousChefTheme` composable that wraps `MaterialTheme` with dark/light detection via `isSystemInDarkTheme()`.

#### `Type.kt`
Load `Playfair Display` (serif) and `Inter` (sans-serif) via Google Fonts. Define the full typography scale from the design doc.

---

### 7. Navigation Skeleton (`ui/navigation/`)

#### `Screens.kt`
```kotlin
sealed interface Screens : NavKey {
    @Serializable data object NavHomeRoute : Screens
    @Serializable data object NavLoginRoute : Screens
    @Serializable data object NavSignUpRoute : Screens
    @Serializable data object NavDesignTestRoute : Screens
}
```

#### `AppNavigation.kt`
Wire up `NavDisplay` with the routes above. Initially, each route shows a placeholder `Text("Coming in Phase X")` composable, except `NavDesignTestRoute` which shows the real design test screen.

---

### 8. Design System Demo Screen (`ui/screens/designtest/DesignTestScreen.kt`)

This screen must be a long, scrollable showcase of **every** reusable UI component in the design system. It must:

- Use `SousChefTheme` properly (no hardcoded colors anywhere).
- Have a **top toggle** to switch between light/dark preview (using a local `var isDark` state passed into theme).
- Organize content in clearly labeled `Section` blocks.

**Sections to include (in order):**
1. **Color Palette** — swatches for all brand colors (light + dark variants side by side)
2. **Typography** — every text style from the type scale with a label
3. **Standard Cards** — flat card, outlined card, elevated card
4. **Glass Cards** — glass card on gradient background
5. **Image Cards** — food photo card with gradient overlay
6. **Buttons** — Primary (gold filled), Secondary (outlined), Ghost (text only), Icon button, FAB
7. **Input Fields** — Standard text field, Search field, Password field, Multi-line textarea
8. **Chips & Tags** — filter chip, selected chip, verified chef badge, dietary tags (vegan, spicy, etc.)
9. **List Items** — ingredient row, step row (with flame indicator)
10. **Loading States** — shimmer placeholder card, circular progress
11. **Empty State** — centered illustration + text + CTA button
12. **Snackbar / Toast** — trigger button + snackbar host example
13. **Rating Row** — star rating display
14. **Avatar Row** — circular avatar with fallback initials

**No hardcoded colors** — all colors via `MaterialTheme.colorScheme` or the named constants exposed by the theme.

---

### 9. Reusable UI Components (`ui/components/`)

Create the following composable files, each with `@Preview` for light and dark:

| File | Component(s) |
|------|--------------|
| `SousChefCard.kt` | `StandardCard`, `GlassCard`, `ImageCard` |
| `SousChefButton.kt` | `PrimaryButton`, `SecondaryButton`, `GhostButton`, `IconButton` |
| `SousChefTextField.kt` | `SousChefTextField`, `SearchField`, `PasswordField` |
| `SousChefChip.kt` | `FilterChip`, `StatusTag`, `VerifiedChefBadge` |
| `SousChefListItem.kt` | `IngredientRow`, `StepRow` |
| `LoadingIndicator.kt` | `ShimmerCard`, `FullScreenLoader` |
| `EmptyState.kt` | `EmptyStateView` |
| `SectionHeader.kt` | `SectionHeader` |
| `RatingRow.kt` | `RatingDisplay` |
| `UserAvatar.kt` | `UserAvatar` |

---

### 10. Utility Classes (`util/`)
- `Resource.kt` — `Resource<T>` sealed class with `Loading`, `Success`, `Failure` states
- `ResponseError.kt` — enum of error types (`NETWORK_ERROR`, `AUTH_ERROR`, `NOT_FOUND`, `UNKNOWN`)
- `FirestoreExtensions.kt` — `safeFirestoreCall { }` helper

---

### Acceptance Criteria for Phase 0
- [ ] App compiles and launches without crash.
- [ ] `WEB_CLIENT_ID` is read from `local.properties` and accessible at runtime via `R.string.WEB_CLIENT_ID`.
- [ ] Design Test screen is reachable from a debug launcher button.
- [ ] Design Test screen renders correctly in both light and dark modes (toggle works).
- [ ] All component files have `@Preview` annotations for both themes.
- [ ] No hardcoded colors or dimensions in any composable.
- [ ] Koin starts without error (all modules registered, no missing dependencies).
- [ ] Firebase is initialized (no crash on startup).

---

---

# Phase 1 – Authentication & Role Management

> **Prerequisites:** Phase 0 complete and merged.

## Objective
Implement full user authentication: Google Sign-In, email/password sign-up & login, role assignment, session persistence, and login/signup screens. Covers Epic 1 (US 1.1 – 1.5), excluding admin verification (US 1.6 — covered in Phase 8).

## Context Files to Read First
- `docs/Coding Guidelines.md` — architecture rules
- `docs/DesignGuidelines.md` — UI rules, especially button, text field, and card styles
- `Phase 0` output — `Screens.kt`, component library, `Resource.kt`, theme

---

## Firestore Data Model

### Collection: `users`
Document ID = Firebase Auth UID.

```
users/{uid}
├── uid: String
├── email: String
├── displayName: String
├── profileImageUrl: String?
├── role: String              // "user" | "admin"
├── isVerifiedChef: Boolean   // default: false
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

**Rules:**
- `role` is always `"user"` on registration. Admin promotion is done directly in Firestore console or via Phase 8 admin panel.
- `isVerifiedChef` starts `false`. Only admin can set to `true`.
- Never trust `role` from the client for security — but for display purposes, read it and show appropriate UI.

---

## Architecture

### Model (`model/auth/`)
```kotlin
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val role: String = "user",
    val isVerifiedChef: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

### Service (`service/auth/FirebaseAuthService.kt`)
Raw Firebase operations:
- `signInWithGoogle(idToken: String): FirebaseUser` — exchange Google ID token for Firebase user
- `signInWithEmail(email: String, password: String): FirebaseUser`
- `signUpWithEmail(email: String, password: String, displayName: String): FirebaseUser`
- `signOut()`
- `getCurrentUser(): FirebaseUser?`
- `getUserProfile(uid: String): UserProfile?` — Firestore read
- `createUserProfile(profile: UserProfile)` — Firestore write
- `updateUserProfile(uid: String, updates: Map<String, Any>)` — partial update

### Repository (`repository/auth/`)
Interface `AuthRepository` + implementation `FirebaseAuthRepository`.

`AuthRepository` methods:
- `signInWithGoogle(idToken: String): Flow<Resource<UserProfile>>`
- `signInWithEmail(email: String, password: String): Flow<Resource<UserProfile>>`
- `signUpWithEmail(email: String, password: String, displayName: String): Flow<Resource<UserProfile>>`
- `signOut(): Flow<Resource<Unit>>`
- `getCurrentUserProfile(): Flow<Resource<UserProfile>>`
- `observeAuthState(): Flow<FirebaseUser?>`

All methods emit `Resource.Loading` first, then `Resource.Success` or `Resource.Failure`.

### AppViewModel (`ui/viewmodels/AppViewModel.kt`)
- Holds `val currentUser: StateFlow<UserProfile?>` — derived from `observeAuthState()`.
- Exposes `val isLoggedIn: StateFlow<Boolean>`.
- Exposes `val isAdmin: StateFlow<Boolean>`.
- Handles `signOut()`.
- Navigation root observes `isLoggedIn` to decide initial destination (`NavLoginRoute` vs `NavHomeRoute`).

---

## Screens

### `LoginScreen` (`ui/screens/auth/login/`)
**Files:** `LoginScreen.kt`, `LoginViewModel.kt`, `LoginUiState.kt`

**UI (follow DesignGuidelines):**
- Full-screen background: `IvoryWhite` (light) / `CharcoalDeep` (dark).
- Centered logo / app name using serif display font.
- `SousChefTextField` for email and password inputs (password field with toggle visibility).
- `PrimaryButton` for "Sign In".
- `SecondaryButton` for "Continue with Google" (with Google logo icon).
- `GhostButton` for "Create Account" that navigates to Sign-Up.
- Error displayed inline below the relevant field using `TextSecondaryLight/Dark`.
- Loading state: button shows `CircularProgressIndicator` inside.

**ViewModel events:**
- `onEmailChange(email: String)`
- `onPasswordChange(password: String)`
- `onSignIn()` — triggers `AuthRepository.signInWithEmail`
- `onGoogleSignIn(idToken: String)` — triggers `AuthRepository.signInWithGoogle`

**Navigation:** On success, navigate to `NavHomeRoute` via `AppViewModel`.

---

### `SignUpScreen` (`ui/screens/auth/signup/`)
**Files:** `SignUpScreen.kt`, `SignUpViewModel.kt`, `SignUpUiState.kt`

**UI:**
- `SousChefTextField` for display name, email, password, confirm password.
- Inline validation: password match, email format.
- `PrimaryButton` for "Create Account".
- `GhostButton` for "Back to Login".

**ViewModel events:**
- `onNameChange`, `onEmailChange`, `onPasswordChange`, `onConfirmPasswordChange`
- `onSignUp()` — validates inputs, calls `AuthRepository.signUpWithEmail`, then creates Firestore user profile with `role = "user"` and `isVerifiedChef = false`.

---

## Google Sign-In Setup
- Add `WEB_CLIENT_ID` dependency: access via `context.getString(R.string.WEB_CLIENT_ID)`.
- Use `CredentialManager` API (Android Credential Manager, not legacy `GoogleSignInClient`).
- Create `api/GoogleAuthProvider.kt` with `suspend fun getGoogleIdToken(context: Context): String?`.
- Call from the Screen composable's button `onClick`, then pass the token to ViewModel.

---

## Session Persistence
- `AppViewModel` listens to `FirebaseAuth.getInstance().addAuthStateListener` via `observeAuthState()`.
- On app start, if `FirebaseAuth.currentUser != null`, fetch user profile from Firestore and populate `currentUser`.
- Navigation root: if `isLoggedIn == true` when app launches, go directly to `NavHomeRoute`.

---

## Koin Modules to Update
- `ServiceModule` — add `FirebaseAuthService`
- `RepositoryModule` — add `FirebaseAuthRepository`
- `ViewModelModule` — add `AppViewModel` (single), `LoginViewModel` (factory), `SignUpViewModel` (factory)

---

## Navigation Updates (`Screens.kt` + `AppNavigation.kt`)
Add:
```kotlin
@Serializable data object NavLoginRoute : Screens
@Serializable data object NavSignUpRoute : Screens
@Serializable data object NavHomeRoute : Screens  // placeholder for now
```
In `AppNavigation`, observe `appViewModel.isLoggedIn` and redirect accordingly.

---

## Acceptance Criteria for Phase 1
- [ ] User can register with email/password; profile created in Firestore with `role = "user"`.
- [ ] User can log in with email/password.
- [ ] User can sign in with Google (Credential Manager flow).
- [ ] On successful login, navigates to `NavHomeRoute`.
- [ ] On app relaunch, auth state is restored (no login prompt if already signed in).
- [ ] Sign-out clears session and redirects to `NavLoginRoute`.
- [ ] All form fields validate correctly (empty, email format, password match).
- [ ] All UI follows design guidelines (colors, typography, no hardcoded values).
- [ ] Both light and dark mode previews present for all new composables.

---

---

# Phase 2 – Core Recipe Data Model & Creation

> **Prerequisites:** Phase 1 complete. User is logged in and `currentUser` is available from `AppViewModel`.

## Objective
Implement the complete recipe data model in Firestore, and build the recipe creation flow: metadata entry, ingredient addition with flavor attributes, and saving to Firestore. Covers Epic 2 (US 2.1 – 2.3) and Epic 5 (US 5.1).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- `docs/DesignGuidelines.md`
- Phase 1 output — `UserProfile`, `AppViewModel`, auth state

---

## Firestore Data Model

### Collection: `recipes`
```
recipes/{recipeId}
├── recipeId: String               // auto-generated doc ID
├── title: String
├── description: String
├── creatorId: String              // uid of the creator
├── creatorName: String
├── isVerifiedChefRecipe: Boolean
├── baseServingSize: Int           // e.g. 4
├── minServingSize: Int?           // optional creator restriction
├── maxServingSize: Int?
├── coverImageUrl: String?
├── isPublished: Boolean           // false until creator publishes
├── originalRecipeId: String?      // null if original; parent ID if forked
├── createdAt: Timestamp
├── updatedAt: Timestamp
├── tags: List<String>             // e.g. ["vegetarian", "quick", "italian"]
└── ingredients: List<Ingredient>  // embedded sub-documents
```

### Embedded: `Ingredient`
```
├── ingredientId: String           // UUID generated client-side
├── name: String
├── quantity: Double               // for baseServingSize
├── unit: String                   // "grams", "tsp", "cups", etc.
├── perPersonQuantity: Double      // system-calculated: quantity / baseServingSize
├── isDispensable: Boolean         // can the hardware dispense this? (for Epic 3)
├── spiceIntensityValue: Double    // 0.0 – 10.0
├── sweetnessValue: Double
├── saltnessValue: Double
```

### Sub-collection: `recipes/{recipeId}/steps`
Steps are a sub-collection (not embedded) to allow independent ordering and large counts.
```
steps/{stepId}
├── stepId: String
├── stepNumber: Int                // 1-based ordering
├── instructionText: String
├── timerSeconds: Int?             // null if no timer
├── flameLevel: String?            // "low" | "medium" | "high" | null
├── expectedVisualCue: String?
├── mediaUrl: String?
└── ingredientReferences: List<String>  // list of ingredientIds used in this step
```

---

## Architecture

### Models (`model/recipe/`)
```kotlin
data class Recipe(...)
data class Ingredient(...)
data class RecipeStep(...)
```
All fields must have default values for Firestore deserialization.

### Service (`service/recipe/FirebaseRecipeService.kt`)
```kotlin
suspend fun createRecipe(recipe: Recipe): String          // returns new recipeId
suspend fun updateRecipe(recipeId: String, updates: Map<String, Any>)
suspend fun addIngredient(recipeId: String, ingredient: Ingredient)
suspend fun addStep(recipeId: String, step: RecipeStep)
suspend fun getRecipe(recipeId: String): Recipe?
fun getRecipesFlow(creatorId: String): Flow<List<Recipe>>
```

### Repository (`repository/recipe/`)
`RecipeRepository` interface + `FirestoreRecipeRepository` implementation wrapping the service.

### Use Cases (`usecases/recipe/`)
**`CreateRecipeUseCase`**
- Accepts recipe metadata + ingredient list.
- Calculates `perPersonQuantity = quantity / baseServingSize` for each ingredient before saving.
- Creates the Firestore document.
- Returns the new `recipeId`.

**`PublishRecipeUseCase`**
- Sets `isPublished = true` on the recipe document.
- Only the recipe creator can publish their own recipe (validate `creatorId == currentUser.uid`).

---

## Screens

### `CreateRecipeScreen` (`ui/screens/recipe/create/`)
A multi-step form wizard with three distinct steps. Use a `HorizontalPager` or custom step indicator.

#### Step 1 — Recipe Details
- `SousChefTextField` for Title (required).
- `SousChefTextField` for Description (multi-line, 4 lines).
- Serving size stepper: `–` / number / `+` buttons. Min/max restriction toggles with secondary steppers.
- Tags: `FilterChip` row, predefined list (Vegetarian, Vegan, Spicy, Quick, Italian, Indian, etc.), multi-select.
- Cover image picker (optional — use `ImagePicker` composable; store URI locally until recipe is saved, then upload).

#### Step 2 — Ingredients
- `LazyColumn` of added ingredients using `IngredientRow` component.
- "Add Ingredient" FAB opens `AddIngredientBottomSheet`.

**`AddIngredientBottomSheet`:**
- Name (text field)
- Quantity (number field) + Unit dropdown (grams, ml, tsp, tbsp, cups, pieces)
- Dispensable toggle (for hardware compatibility)
- Flavor sliders: Spice (0–10), Sweetness (0–10), Saltiness (0–10) — collapsed by default with "Advanced" expand button
- Save button adds ingredient to the list (local state, not yet saved to Firestore)

#### Step 3 — Review & Save
- Summary card: recipe title, serving size, ingredient count.
- "Save as Draft" button — saves to Firestore with `isPublished = false`.
- "Save & Publish" button — saves and publishes (calls `PublishRecipeUseCase`).

### `CreateRecipeViewModel`
- `UiState` holds: `currentStep`, `recipeTitle`, `description`, `baseServingSize`, `minServingSize`, `maxServingSize`, `ingredients: List<Ingredient>`, `tags`, `isLoading`, `error`.
- Functions: `onNextStep()`, `onPreviousStep()`, `onAddIngredient(ingredient)`, `onRemoveIngredient(id)`, `onSave(publish: Boolean)`.

---

## Home Screen Placeholder (`ui/screens/home/HomeScreen.kt`)
A minimal home screen showing the current user's name and a "Create Recipe" button. Full home screen built in Phase 7.

---

## Navigation Updates
Add routes:
```kotlin
@Serializable data object NavCreateRecipeRoute : Screens
@Serializable data class NavRecipeDetailRoute(val recipeId: String) : Screens
```

---

## Koin Updates
Add `CreateRecipeUseCase`, `PublishRecipeUseCase`, `FirebaseRecipeService`, `FirestoreRecipeRepository`, `CreateRecipeViewModel` to respective modules.

---

## Acceptance Criteria for Phase 2
- [ ] Recipe can be created with title, description, base serving size, min/max restrictions, and tags.
- [ ] Ingredients can be added with name, quantity, unit, dispensable flag, and flavor values.
- [ ] `perPersonQuantity` is calculated correctly before saving.
- [ ] Recipe is saved to Firestore as a draft (`isPublished = false`).
- [ ] Recipe can be published (`isPublished = true`).
- [ ] Form validation: title required, at least 1 ingredient required.
- [ ] Multi-step form navigation works (Next / Back).
- [ ] UI follows design guidelines; no hardcoded colors.
- [ ] Both light and dark previews for all new composables.

---

---

# Phase 3 – Recipe Overview & Flavor Customization

> **Prerequisites:** Phase 2 complete. Recipes exist in Firestore.

## Objective
Build the pre-cooking overview screen where users can customize serving size and flavor preferences (spice, salt, sweetness). Ingredient quantities must update dynamically in real-time. Covers Epic 2 (US 2.4 – 2.8).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- `docs/DesignGuidelines.md`
- Phase 2 output — `Recipe`, `Ingredient`, `RecipeStep` models, Firestore structure

---

## Core Calculation Logic (`usecases/recipe/RecipeCalculationUseCase.kt`)
This is a **pure computation use case** with no Firestore calls. Inject it into the ViewModel.

```kotlin
class RecipeCalculationUseCase {

    /**
     * Calculates final ingredient quantities after applying serving size and flavor adjustments.
     *
     * @param ingredients Original ingredient list from the recipe.
     * @param baseServingSize The serving size the recipe was authored for.
     * @param selectedServings User-selected serving size.
     * @param spiceLevel User spice preference: -1.0 (less) to +1.0 (more), 0.0 = as-is.
     * @param saltLevel User salt preference: -1.0 to +1.0.
     * @param sweetnessLevel User sweetness preference: -1.0 to +1.0.
     * @return New list of ingredients with adjusted quantities.
     */
    fun calculate(
        ingredients: List<Ingredient>,
        baseServingSize: Int,
        selectedServings: Int,
        spiceLevel: Float,        // -1f..+1f
        saltLevel: Float,
        sweetnessLevel: Float
    ): List<Ingredient>
}
```

**Calculation algorithm:**
1. Scale by servings: `scaledQty = ingredient.perPersonQuantity * selectedServings`
2. For each flavor dimension (spice, salt, sweetness), if the user has adjusted that preference:
   - Identify ingredients that contribute to that flavor (value > 0).
   - Scale those ingredients' `scaledQty` proportionally:
     ```
     adjustedQty = scaledQty * (1 + (level * ingredient.flavorValue / 10))
     ```
   - `level` is the user's preference (-1f to +1f). A value of 0 means no change.
3. Round quantities to 1 decimal place.
4. Return a new `List<Ingredient>` with updated quantities. Do not mutate the originals.

---

## Architecture

### Service addition (`FirebaseRecipeService`)
```kotlin
fun getRecipeWithSteps(recipeId: String): Flow<Pair<Recipe, List<RecipeStep>>>
```
Combines the recipe document with its `steps` sub-collection into a single Flow.

### Use Case: `RecipeCalculationUseCase` (pure, no Firestore)

---

## Screens

### `RecipeOverviewScreen` (`ui/screens/recipe/overview/`)
**Files:** `RecipeOverviewScreen.kt`, `RecipeOverviewViewModel.kt`, `RecipeOverviewUiState.kt`

**Navigation param:** `recipeId: String`

**UiState:**
```kotlin
data class RecipeOverviewUiState(
    val recipe: Recipe? = null,
    val steps: List<RecipeStep> = emptyList(),
    val adjustedIngredients: List<Ingredient> = emptyList(),
    val selectedServings: Int = 1,
    val spiceLevel: Float = 0f,
    val saltLevel: Float = 0f,
    val sweetnessLevel: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

**ViewModel events:**
- `loadRecipe(recipeId: String)`
- `onServingsChanged(servings: Int)` — re-runs `RecipeCalculationUseCase`
- `onSpiceLevelChanged(level: Float)` — same
- `onSaltLevelChanged(level: Float)` — same
- `onSweetnessLevelChanged(level: Float)` — same
- `onStartCooking()` — navigates to cooking mode (Phase 4)

**UI Layout (scrollable):**

1. **Hero Section** — full-width cover image with gradient overlay, recipe title, chef name, `VerifiedChefBadge` if applicable.

2. **Serving Selector** — horizontal card:
   - Title: "How many are you cooking for?"
   - `–` / servings count / `+` stepper.
   - Show min/max restriction if defined by creator: "Serves 2–6".
   - Disable `–` at `minServingSize`, disable `+` at `maxServingSize`.

3. **Flavor Customization Card** — collapsible `GlassCard`:
   - Three labeled sliders (`Slider` from Material3):
     - 🌶️ Spice: Less — Original — More
     - 🧂 Salt: Less — Original — More
     - 🍯 Sweetness: Less — Original — More
   - Center position (0.5 mapped to 0f) = original recipe.
   - Slider range: 0f..1f mapped to -1f..+1f internally.

4. **Ingredients List** — `SectionHeader` "Ingredients", then `LazyColumn` of `IngredientRow` showing adjusted quantities. Quantities animate smoothly on change using `animateFloatAsState`.

5. **Start Cooking Button** — `PrimaryButton` fixed at the bottom (outside scroll), full-width. Navigates to cooking mode.

---

## Navigation Updates
```kotlin
@Serializable data class NavRecipeOverviewRoute(val recipeId: String) : Screens
@Serializable data class NavCookingModeRoute(
    val recipeId: String,
    val selectedServings: Int,
    val spiceLevel: Float,
    val saltLevel: Float,
    val sweetnessLevel: Float
) : Screens
```

---

## Acceptance Criteria for Phase 3
- [ ] Recipe overview screen loads recipe data from Firestore.
- [ ] Serving selector respects min/max restrictions.
- [ ] All ingredient quantities update in real-time when servings change.
- [ ] Spice, salt, sweetness sliders re-calculate only the relevant ingredients.
- [ ] Multi-flavor ingredients (e.g. contributes to both spice and salt) are handled correctly.
- [ ] Slider at center position (0f) = original quantities.
- [ ] Quantity values animate smoothly on change.
- [ ] "Start Cooking" passes all parameters to the next screen via nav route.
- [ ] UI follows design guidelines; no hardcoded colors.
- [ ] Both light and dark previews for all new composables.

---

---

# Phase 4 – Step-by-Step Cooking Mode

> **Prerequisites:** Phase 3 complete. `NavCookingModeRoute` is navigable with serving and flavor parameters.

## Objective
Build the immersive step-by-step cooking mode screen. Each step is shown one at a time with timers, flame level indicator, visual cues, and media. Covers Epic 2 (US 2.4) completion.

## Context Files to Read First
- `docs/Coding Guidelines.md`
- `docs/DesignGuidelines.md`
- Phase 3 output — `RecipeStep` model, flavor-adjusted `Ingredient` list, nav params

---

## Architecture

### Use Case: `CookingSessionUseCase` (`usecases/recipe/CookingSessionUseCase.kt`)
Wraps step navigation and timer logic:
- Holds the list of steps and current step index.
- `nextStep()`, `previousStep()`, `goToStep(index)`.
- Timer: exposes `timerMillisRemaining: StateFlow<Long>`, `isTimerRunning: StateFlow<Boolean>`.
- `startTimer()`, `pauseTimer()`, `resetTimer()` — use a `CountDownTimer` wrapped in coroutines.

---

## Screen: `CookingModeScreen` (`ui/screens/recipe/cooking/`)
**Files:** `CookingModeScreen.kt`, `CookingModeViewModel.kt`, `CookingModeUiState.kt`

**Navigation params** (received from `NavCookingModeRoute`):
`recipeId`, `selectedServings`, `spiceLevel`, `saltLevel`, `sweetnessLevel`

**UiState:**
```kotlin
data class CookingModeUiState(
    val steps: List<RecipeStep> = emptyList(),
    val adjustedIngredients: List<Ingredient> = emptyList(),
    val currentStepIndex: Int = 0,
    val timerMillisRemaining: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val error: String? = null
)
```

**ViewModel:**
- On init: fetch steps via `FirebaseRecipeService`, re-run `RecipeCalculationUseCase` with received params.
- Delegates step navigation and timer to `CookingSessionUseCase`.

---

## UI Layout

### Navigation Bar
- Fixed at top: Step `X / N` progress indicator (segmented progress bar using the gold accent).
- Back `<` and Forward `>` arrows.
- Tapping a progress segment jumps to that step.

### Step Card (main content, full-screen scrollable)
Use a `GlassCard` over a blurred food-themed background.

**Card content (top to bottom):**
1. **Step number** badge — `labelLarge`, gold background circle.
2. **Instruction text** — `bodyLarge`, serif, generous line height.
3. **Flame level indicator** (if present):
   - Row of 3 flame icons 🔥; filled count based on level (low=1, medium=2, high=3).
   - Label: "Flame: Low / Medium / High".
4. **Visual cue** (if present):
   - Italic `bodyMedium` text: *"Look for: golden brown edges"*.
5. **Media** (if present):
   - `AsyncImage` or video thumbnail with play button overlay.
6. **Ingredients for this step** (if `ingredientReferences` not empty):
   - `SectionHeader` "You'll need:"
   - `IngredientRow` for each referenced ingredient (showing adjusted quantity).
7. **Timer section** (if `timerSeconds` not null):
   - Large countdown display: `MM:SS` in `headlineLarge`, serif.
   - Row of buttons: ▶️ Start / ⏸ Pause / 🔄 Reset.
   - When timer reaches 0: pulse animation + optional sound cue (use `MediaPlayer` for a short beep).

### Bottom Navigation Bar
- Fixed at bottom.
- "← Previous" and "Next Step →" buttons (`SecondaryButton` / `PrimaryButton`).
- On last step: "Next Step" becomes "Finish Cooking 🎉" — navigate to a simple `CookingCompleteScreen`.

### `CookingCompleteScreen`
- Celebration animation (Lottie or simple `AnimatedVisibility` with checkmark).
- "Great work!" heading.
- "Back to Recipe" and "Share Recipe" buttons.

---

## Keep-Screen-Awake
While the cooking screen is active, acquire `FLAG_KEEP_SCREEN_ON` using:
```kotlin
DisposableEffect(Unit) {
    val window = (context as? Activity)?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
}
```

---

## Acceptance Criteria for Phase 4
- [ ] Steps load correctly for the given recipe.
- [ ] Step navigation (next / previous / jump) works.
- [ ] Progress bar updates correctly.
- [ ] Flame level renders visually (1–3 icons).
- [ ] Timer counts down, can be paused/reset.
- [ ] Timer completion triggers visual feedback.
- [ ] Ingredient quantities shown in each step are the flavor-adjusted amounts from Phase 3.
- [ ] Screen stays awake while cooking mode is active.
- [ ] Last step shows "Finish Cooking" button.
- [ ] `CookingCompleteScreen` shows on finish.
- [ ] UI follows design guidelines; no hardcoded colors.

---

---

# Phase 5 – Hardware Integration & Smart Spice Dispensing

> **Prerequisites:** Phase 4 complete.

## Objective
Integrate with the 6-compartment spice dispensing hardware device via BLE (Bluetooth Low Energy). Manage compartment configuration, automate dispensing during cooking steps, track inventory, and send refill alerts. Covers Epic 3 (US 3.1 – 3.8).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- Phase 4 output — `CookingModeScreen`, `RecipeStep`, `Ingredient` with `isDispensable` flag

---

## Firestore Data Model

### Collection: `devices`
One document per user device.
```
devices/{userId}
├── userId: String
├── deviceMacAddress: String?      // null until device is paired
├── isConnected: Boolean
├── compartments: List<Compartment>  // always 6 items
└── lastSyncedAt: Timestamp
```

### Embedded: `Compartment`
```
├── compartmentId: Int             // 1–6
├── ingredientName: String?        // null if empty
├── ingredientId: String?          // links to recipe ingredient
├── currentQuantityGrams: Double
├── maxCapacityGrams: Double
├── lastRefillAt: Timestamp?
├── lowThresholdGrams: Double      // default: 20g
```

### Sub-collection: `devices/{userId}/dispenseLogs`
```
dispenseLogs/{logId}
├── logId: String
├── timestamp: Timestamp
├── recipeId: String
├── stepId: String
├── ingredientName: String
├── quantityDispensedGrams: Double
├── compartmentId: Int
└── userId: String
```

---

## BLE Layer (`api/ble/`)

### `BleDeviceManager.kt`
- Manages BLE scanning, connection, and GATT communication.
- Exposes `connectionState: StateFlow<BleConnectionState>` (`Disconnected`, `Scanning`, `Connecting`, `Connected`).
- `scanAndConnect()` — scan for a device named `"SousChef-Dispenser"`, attempt to connect.
- `sendDispenseCommand(compartmentId: Int, quantityGrams: Double)` — write a BLE characteristic to trigger dispense.
- BLE characteristic UUIDs must be declared as constants in a `BleConstants.kt` file.

**Note:** Define a simple binary command format, e.g., `[compartmentId (1 byte), quantityGrams * 10 as Int16 (2 bytes)]`. Document this in comments.

### `BleConnectionState.kt` (sealed class)
```kotlin
sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Scanning : BleConnectionState()
    object Connecting : BleConnectionState()
    object Connected : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}
```

---

## Architecture

### Service (`service/device/FirebaseDeviceService.kt`)
Firestore CRUD for device configuration and dispense logs.

### Repository (`repository/device/DeviceRepository.kt`)
Wraps both `FirebaseDeviceService` and `BleDeviceManager`.

### Use Case: `DispenseSpiceUseCase` (`usecases/device/DispenseSpiceUseCase.kt`)
Called during cooking mode when a step references a dispensable ingredient:
1. Check if ingredient's `isDispensable == true`.
2. Find which compartment holds that ingredient (`ingredientId` match).
3. Check if `currentQuantityGrams >= requiredQuantity`.
4. If yes: call `BleDeviceManager.sendDispenseCommand(compartmentId, qty)`.
5. Update Firestore `currentQuantityGrams` for that compartment.
6. Write a dispense log entry.
7. If any check fails: return `DispenseResult.ManualAdditionRequired`.

### Use Case: `RefillAlertUseCase`
After every dispense, check if `currentQuantityGrams <= lowThresholdGrams`. If true, emit a notification via `NotificationHelper`.

---

## Screens

### `DeviceSetupScreen` (`ui/screens/device/setup/`)
- BLE permission request (use `permissions/BlePermissionHelper.kt`).
- Connection status card: shows `BleConnectionState`.
- "Scan for Device" `PrimaryButton`.
- Once connected: shows compartment configuration grid.

### `CompartmentManagerScreen` (`ui/screens/device/compartments/`)
- 2-column grid of 6 `CompartmentCard` composables.
- Each card shows: compartment number, ingredient name (or "Empty"), quantity bar.
- Tap card → `EditCompartmentBottomSheet`:
  - Search + assign an ingredient from recipe history.
  - Set current quantity (number field).
  - Set max capacity.
  - Save button.

### Cooking Mode Integration (update `CookingModeScreen`)
When a step's `ingredientReferences` contains a dispensable ingredient:
- Show a "🤖 Auto-Dispense" button next to that ingredient row.
- On tap: call `DispenseSpiceUseCase`.
- Show `CircularProgressIndicator` during dispensing.
- On success: show a brief "Dispensed ✓" SnackBar.
- On failure / manual required: show "Add manually" instruction.

### `DispenseHistoryScreen` (`ui/screens/device/history/`)
- `LazyColumn` of dispense log entries.
- Each row: ingredient name, quantity, timestamp, compartment ID.

---

## Permissions
Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
Use `permissions/BlePermissionHelper.kt` to request at runtime before scanning.

---

## Acceptance Criteria for Phase 5
- [ ] BLE scanning finds the device named `"SousChef-Dispenser"`.
- [ ] Device connection state is reflected in the UI.
- [ ] All 6 compartments can be configured with ingredient and quantity.
- [ ] During cooking, dispensable ingredients show an "Auto-Dispense" button.
- [ ] Dispense command is sent via BLE with correct compartment ID and quantity.
- [ ] Firestore quantity is decremented after each dispense.
- [ ] Low stock notification fires when quantity ≤ threshold.
- [ ] Dispense log is written to Firestore for every successful dispense.
- [ ] Manual addition message shown when ingredient not in device.
- [ ] All UI follows design guidelines.

---

---

# Phase 6 – AI-Assisted Recipe Generation (Gemini)

> **Prerequisites:** Phase 2 complete (recipe creation flow exists).

## Objective
Add an AI-assisted step generation flow using the Gemini API. The creator provides a high-level recipe description; Gemini generates detailed atomic cooking steps. Creator reviews, edits, and approves before saving. Covers Epic 4 (US 4.1 – 4.7).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- `docs/DesignGuidelines.md`
- Phase 2 output — `RecipeStep` model, `CreateRecipeScreen` (Step 3 will link here)

---

## Gemini API Setup
- Add dependency: `com.google.ai.client.generativeai:generativeai` (check latest version).
- Store the API key in `local.properties` as `GEMINI_API_KEY`.
- Expose via `resValue("string", "GEMINI_API_KEY", propOrEnv("GEMINI_API_KEY"))` in `build.gradle.kts`.
- Create `api/GeminiProvider.kt`:
  ```kotlin
  object GeminiProvider {
      fun getModel(context: Context): GenerativeModel =
          GenerativeModel(
              modelName = "gemini-1.5-flash",
              apiKey = context.getString(R.string.GEMINI_API_KEY)
          )
  }
  ```

---

## Prompt Engineering (`api/GeminiRecipePrompt.kt`)

The prompt must produce a JSON array of steps matching the `RecipeStep` schema.

**System prompt template:**
```
You are a professional chef and recipe writer. Your task is to take a recipe description and convert it into a structured list of atomic cooking steps.

Each step must:
- Be a single, specific, executable action.
- Never assume prior preparation unless explicitly stated.
- Include all implicit steps (washing, chopping, preheating, etc.).
- Be written in plain, clear language.

Return a JSON array. Each object must have these fields:
- stepNumber: Int (starting from 1)
- instructionText: String
- timerSeconds: Int or null (only if a specific time is mentioned)
- flameLevel: "low" | "medium" | "high" | null
- expectedVisualCue: String or null
- ingredientReferences: String[] (ingredient names used in this step, empty if none)

Recipe: {{RECIPE_DESCRIPTION}}
Ingredients: {{INGREDIENT_LIST}}

Return ONLY the JSON array. No explanation. No markdown.
```

---

## Architecture

### Service (`service/ai/GeminiRecipeService.kt`)
```kotlin
class GeminiRecipeService(private val model: GenerativeModel) {
    suspend fun generateSteps(
        recipeDescription: String,
        ingredients: List<Ingredient>
    ): List<RecipeStep>  // parses Gemini JSON response
}
```

**Parsing:**
- Use `kotlinx.serialization` or `Gson` to parse the JSON array from Gemini's text response.
- Wrap in try/catch; on parse failure, throw a descriptive exception.

### Repository (`repository/ai/`)
`AiRepository` interface + `GeminiAiRepository` implementation.

### Use Case: `GenerateRecipeStepsUseCase`
- Calls `GeminiRecipeService` to get generated steps.
- Maps ingredient names in `ingredientReferences` back to real `ingredientId`s using the passed ingredient list.
- Returns `Flow<Resource<List<RecipeStep>>>`.

---

## Screens

### `AiStepGenerationScreen` (`ui/screens/recipe/aigeneration/`)
**Entry point:** From `CreateRecipeScreen` Step 3, a button "✨ Generate Steps with AI" navigates here.

**Navigation param:** `recipeId: String`

**UI Flow:**

#### Stage 1 — Input
- `SousChefTextField` (multi-line, 8 lines): "Describe your recipe in your own words…"
- Readonly ingredient list summary (chips showing ingredient names already added).
- `PrimaryButton`: "Generate Steps"
- On tap: transitions to loading state.

#### Stage 2 — Loading
- `FullScreenLoader` with animated text: "Chef AI is crafting your recipe…"
- Cancel button to abort.

#### Stage 3 — Review & Edit
A `LazyColumn` of editable step cards.

**Step Card (editable):**
- Step number (non-editable label).
- `SousChefTextField` for instruction text (pre-filled with AI output, editable).
- Collapsible "Details" section: timer field, flame level dropdown, visual cue field.
- Delete step icon button (top-right).
- Drag handle icon (for reordering — use `ReorderableColumn` from a reorder library).

**Action Row (fixed at bottom):**
- "➕ Add Step Manually" button — appends a blank step card.
- `PrimaryButton`: "Save Steps" — saves all steps to Firestore sub-collection and navigates back to recipe detail.

### `AiStepGenerationViewModel`
- `UiState`: `inputText`, `generatedSteps: List<RecipeStep>`, `stage: Stage` (Input / Loading / Review), `error`.
- `onGenerateSteps()` → calls `GenerateRecipeStepsUseCase`.
- `onEditStep(index, updatedStep)`, `onDeleteStep(index)`, `onReorderSteps(from, to)`, `onAddManualStep()`.
- `onSaveSteps(recipeId)` → writes all steps to Firestore.

---

## Acceptance Criteria for Phase 6
- [ ] `GEMINI_API_KEY` is read from `local.properties` (not hardcoded).
- [ ] Gemini generates a valid JSON array of steps from the recipe description.
- [ ] Parse errors are caught and shown to the user with a retry option.
- [ ] All generated steps are displayed as editable cards.
- [ ] Creator can edit, delete, reorder, and add manual steps.
- [ ] Saving stores all steps in Firestore `steps` sub-collection with correct `stepNumber` ordering.
- [ ] Ingredient name references are resolved to ingredient IDs on save.
- [ ] Loading state is shown during API call with a cancel option.
- [ ] UI follows design guidelines; no hardcoded colors.
- [ ] Both light and dark previews present.

---

---

# Phase 7 – Recipe Sharing, Forking & Personal Collections

> **Prerequisites:** Phases 1–3 complete. Recipes exist in Firestore, user is authenticated.

## Objective
Build the public recipe feed (home screen), recipe detail page, recipe forking (copying), and personal saved collection. Covers Epic 5 (US 5.1 – 5.7).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- `docs/DesignGuidelines.md`
- Phase 2 output — `Recipe` model, `RecipeRepository`

---

## Firestore Updates

### `recipes` collection additions
- `savedByCount: Int` — denormalized counter for how many users saved the recipe.
- `forkCount: Int` — how many times it's been forked.

### New collection: `savedRecipes`
```
savedRecipes/{userId}/recipes/{recipeId}
├── recipeId: String
├── savedAt: Timestamp
```

---

## Architecture

### Service additions (`FirebaseRecipeService`)
```kotlin
fun getPublicRecipesFlow(): Flow<List<Recipe>>       // isPublished == true, ordered by createdAt desc
fun getMyRecipesFlow(userId: String): Flow<List<Recipe>>
suspend fun forkRecipe(originalRecipe: Recipe, newCreatorId: String): String  // returns new recipeId
suspend fun saveRecipe(userId: String, recipeId: String)
suspend fun unsaveRecipe(userId: String, recipeId: String)
fun getSavedRecipesFlow(userId: String): Flow<List<Recipe>>
suspend fun isRecipeSaved(userId: String, recipeId: String): Boolean
```

### Use Cases
- **`ForkRecipeUseCase`** — creates a deep copy of a recipe (recipe doc + steps sub-collection) with `originalRecipeId` set, `isPublished = false`, `creatorId = currentUser.uid`.
- **`SaveRecipeUseCase`** — toggles save/unsave, increments/decrements `savedByCount` counter.

---

## Screens

### `HomeScreen` (`ui/screens/home/`)
Replace the Phase 2 placeholder.

**UI Layout:**
- Top app bar: App logo, search icon, profile avatar (tappable → profile screen).
- Search bar (`SearchField` component) — filters the feed client-side.
- **Horizontal scrolling category chips**: All, Quick, Vegetarian, Spicy, Indian, Italian, etc.
- **Featured Recipe** — prominent `GlassCard` with hero image, at the top.
- **Recipe Feed** — `LazyColumn` of `RecipeCard` components.

**`RecipeCard` component (add to `ui/components/`):**
- Horizontal card with food image on the left (80dp × 80dp, rounded 8dp).
- Right side: title (`titleMedium`), chef name + `VerifiedChefBadge`, metadata row (time estimate, serves count).
- Save icon button (bookmark) on top-right — filled if saved.
- Tap → navigate to `NavRecipeDetailRoute`.

### `RecipeDetailScreen` (`ui/screens/recipe/detail/`)
**Navigation param:** `recipeId: String`

**UI Layout:**
- Hero image (full-width, 250dp height, gradient overlay at bottom).
- Title (`headlineMedium`, serif).
- Chef info row: avatar, name, `VerifiedChefBadge`, "Follow" button (placeholder for future).
- Metadata row: serves, prep time (estimated from step timers), tags chips.
- **Action Row**: Save (bookmark) button + Fork ("Make My Copy" button).
- `SectionHeader` "Ingredients" + ingredient list (non-adjustable here — that's in Overview).
- `SectionHeader` "Steps Preview" + first 2 steps with a "See All" link.
- `PrimaryButton` "Start Cooking" → navigates to `NavRecipeOverviewRoute`.

If `originalRecipeId != null`: show a "🍴 Forked from [original title]" attribution banner below the title.

### `ForkConfirmationBottomSheet`
Triggered by "Make My Copy" button on detail screen:
- Brief explanation: "This will create an editable copy in your recipes. The original won't be affected."
- "Fork Recipe" confirm button → calls `ForkRecipeUseCase` → navigates to `NavCreateRecipeRoute` with the new forked recipeId pre-loaded.

### `SavedRecipesScreen` (`ui/screens/savedrecipes/`)
- Tab layout: "My Recipes" | "Saved"
- Each tab shows a `LazyColumn` of `RecipeCard`.
- Empty state: `EmptyStateView` with appropriate icon + "No saved recipes yet".

---

## Navigation Updates
Add:
```kotlin
@Serializable data object NavHomeRoute : Screens
@Serializable data class NavRecipeDetailRoute(val recipeId: String) : Screens
@Serializable data object NavSavedRecipesRoute : Screens
@Serializable data object NavProfileRoute : Screens
```

---

## Bottom Navigation Bar
Add a persistent `BottomNavigationBar` to the `Scaffold` in `AppNavigation` (shown only when logged in):
- 🏠 Home (`NavHomeRoute`)
- 🔖 Saved (`NavSavedRecipesRoute`)
- ➕ Create (`NavCreateRecipeRoute`) — center button, gold FAB style
- 👤 Profile (`NavProfileRoute`)

---

## Acceptance Criteria for Phase 7
- [ ] Home screen shows all published recipes in a feed.
- [ ] Category chips filter the feed.
- [ ] Search filters by recipe title.
- [ ] Recipe detail screen shows full recipe info with fork/save buttons.
- [ ] Forking creates a new Firestore document with `originalRecipeId` set.
- [ ] `forkCount` on the original recipe increments on fork.
- [ ] Saved recipes appear in the Saved tab.
- [ ] `savedByCount` increments/decrements correctly.
- [ ] Attribution banner shown for forked recipes.
- [ ] Bottom navigation bar is visible across all main screens.
- [ ] All UI follows design guidelines; no hardcoded colors.

---

---

# Phase 8 – Admin Panel & Verified Chef Management

> **Prerequisites:** Phase 1 complete. Admin user exists in Firestore with `role = "admin"`.

## Objective
Build the admin-only panel where admins can view all users, assign/remove the Verified Chef tag, and view platform statistics. Covers Epic 1 (US 1.5 – 1.6).

## Context Files to Read First
- `docs/Coding Guidelines.md`
- Phase 1 output — `UserProfile`, `AppViewModel.isAdmin`, `AuthRepository`

---

## Security Rules (Firestore)
> Document these Firestore security rules for deployment. Do not implement server-side enforcement in the app, but note that these rules must be applied in the Firebase console.

```
// Firestore security rules (pseudo-code — apply in Firebase console)
match /users/{userId} {
  allow read: if request.auth != null;
  allow write: if request.auth.uid == userId;  // users can edit own profile
  allow update: if get(/databases/.../users/$(request.auth.uid)).data.role == "admin";
}
```

---

## Architecture

### Service addition (`FirebaseAuthService`)
```kotlin
fun getAllUsersFlow(): Flow<List<UserProfile>>
suspend fun setVerifiedChef(uid: String, isVerified: Boolean)
```

### Use Case: `ChefVerificationUseCase`
- Validates that `currentUser.isAdmin == true` before calling `setVerifiedChef`.
- Returns `Flow<Resource<Unit>>`.

---

## Screens

### Admin access
In `AppNavigation`, if `appViewModel.isAdmin == true`, show an "Admin" icon in the top app bar on the Home screen that navigates to `NavAdminRoute`.

### `AdminDashboardScreen` (`ui/screens/admin/`)
**Navigation guard:** If `!appViewModel.isAdmin`, show an `EmptyStateView` with "Access Denied" message. Never crash.

**UI Layout (tabs):**

#### Tab 1 — Users
- Search field + `LazyColumn` of `UserAdminRow` components.
- `UserAdminRow`: avatar, display name, email, current verified status chip, and a "Verify" / "Revoke" button.
- Confirmation dialog before toggling verification.
- On confirm: calls `ChefVerificationUseCase`.

#### Tab 2 — Statistics (read-only)
- Aggregate cards:
  - Total users
  - Total published recipes
  - Total verified chefs
  - Total forks
- Use Firestore aggregation queries (`count()`) or maintain denormalized counters in a `stats` document.

---

## Acceptance Criteria for Phase 8
- [ ] Admin screen is only accessible when `currentUser.role == "admin"`.
- [ ] Non-admin users see "Access Denied" if they somehow reach the route.
- [ ] Admin can view all users in a searchable list.
- [ ] Admin can grant or revoke Verified Chef status.
- [ ] Change is reflected immediately in the user's Firestore document.
- [ ] Statistics tab shows accurate counts.
- [ ] Confirmation dialog shown before verification change.
- [ ] UI follows design guidelines; no hardcoded colors.

---

---

# Phase 9 – Polish, Notifications & Offline Support

> **Prerequisites:** All previous phases complete.

## Objective
Add production-grade polish: FCM push notifications (refill alerts, chef verification), offline Firestore caching, WorkManager background sync, app-wide error handling, and performance optimizations.

## Context Files to Read First
- `docs/Coding Guidelines.md` — notification, preferences, and Worker patterns
- Phase 5 output — refill alert logic

---

## 1. Push Notifications (FCM)

### Setup
- Add `firebase-messaging-ktx` to dependencies.
- Create `notifications/SousChefMessagingService.kt` extending `FirebaseMessagingService`.
- Register in `AndroidManifest.xml`.
- Store FCM token in Firestore `users/{uid}/fcmToken` on login and on token refresh.

### Notification Types
| Type | Trigger | Target |
|------|---------|--------|
| `REFILL_ALERT` | Compartment qty ≤ threshold | Device owner |
| `CHEF_VERIFIED` | Admin grants Verified Chef tag | Verified user |
| `RECIPE_FORKED` | Someone forks your recipe | Recipe creator |

### Notification Channel Setup (`notifications/NotificationHelper.kt`)
- Create a `SousChef` channel with `IMPORTANCE_DEFAULT`.
- Use `PendingIntent` to deep-link into the relevant screen.

---

## 2. Offline Support (Firestore Caching)

Enable Firestore offline persistence in `FirebaseProvider`:
```kotlin
val settings = firestoreSettings {
    isPersistenceEnabled = true
    cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
}
FirebaseFirestore.getInstance().firestoreSettings = settings
```

This provides offline reads for all previously fetched data. No additional code required for basic offline reads — Firestore handles it automatically.

For **offline writes** (creating a recipe while offline): Firestore queues the write and syncs when connectivity is restored. This works automatically.

Add a `ConnectivityObserver` utility that emits `NetworkStatus.Available` / `Unavailable`:
```kotlin
// util/ConnectivityObserver.kt
class ConnectivityObserver(context: Context) {
    val networkStatus: StateFlow<NetworkStatus>
    // uses ConnectivityManager.NetworkCallback
}
```

In `AppViewModel`, observe `ConnectivityObserver` and expose `isOffline: StateFlow<Boolean>`. Show an `OfflineBanner` composable at the top of every screen when offline.

---

## 3. Background Sync (WorkManager)

### `SpiceInventorySyncWorker` (`worker/`)
Runs every 6 hours. Reads device compartment quantities from Firestore and checks for low-stock conditions. Fires a local notification if any compartment is below threshold.

```kotlin
class SpiceInventorySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // check inventory, fire notifications
        return Result.success()
    }
}
```

Schedule on app start with `PeriodicWorkRequest` (6-hour interval, `KEEP` existing policy).

---

## 4. App-Wide Error Handling

Ensure all screens that use `BaseViewModel` forward `errorFlow` to the shared `SnackbarHostState` passed from `AppNavigation`. Standardize the `ResponseError` enum to cover all failure cases encountered across phases.

---

## 5. Performance

- **Pagination:** If public recipe feed exceeds 50 items, implement Firestore cursor-based pagination (`startAfter(lastDocument)`). Add a `LazyColumn` bottom-load trigger.
- **Image Caching:** Verify Coil is caching images properly; set `diskCachePolicy = CachePolicy.ENABLED`.
- **Recomposition:** Add `key(recipe.recipeId)` to all `LazyColumn` items.

---

## Acceptance Criteria for Phase 9
- [ ] App shows an offline banner when there is no network.
- [ ] Recipes load from cache when offline.
- [ ] FCM token is stored in Firestore on login.
- [ ] `REFILL_ALERT` notification appears when a compartment drops below threshold.
- [ ] `CHEF_VERIFIED` notification is sent when admin verifies a chef.
- [ ] `RECIPE_FORKED` notification is sent when a recipe is forked.
- [ ] `SpiceInventorySyncWorker` runs in background every 6 hours.
- [ ] Recipe feed paginates beyond 50 items without loading all at once.
- [ ] All error types across all phases are handled gracefully (no unhandled exceptions).
- [ ] App passes a release build without crashes.

---

---

## Appendix: Dependency Quick Reference

| Library | Purpose | Added in Phase |
|---------|---------|----------------|
| `firebase-auth-ktx` | Authentication | Phase 0 |
| `firebase-firestore-ktx` | Database | Phase 0 |
| `kotlinx-coroutines-play-services` | `await()` on Firebase Tasks | Phase 0 |
| `koin-android`, `koin-androidx-compose` | Dependency Injection | Phase 0 |
| `androidx-room-*` | Local DB (if needed) | Phase 0 |
| `coil-compose` | Image loading | Phase 0 |
| `datastore-preferences` | Token / prefs storage | Phase 0 |
| `androidx-navigation3-compose` | Navigation | Phase 0 |
| `credentials` (CredentialManager) | Google Sign-In | Phase 1 |
| `play-services-auth` | Google Sign-In | Phase 1 |
| `generativeai` (Gemini) | AI step generation | Phase 6 |
| `firebase-messaging-ktx` | Push notifications | Phase 9 |
| `work-runtime-ktx` | Background sync | Phase 9 |

---

## Appendix: Firestore Collection Summary

| Collection | Purpose | Added in Phase |
|-----------|---------|----------------|
| `users` | User profiles, roles | Phase 1 |
| `recipes` | Recipe documents | Phase 2 |
| `recipes/{id}/steps` | Recipe steps sub-collection | Phase 2 |
| `devices` | Device configuration | Phase 5 |
| `devices/{id}/dispenseLogs` | Dispense history | Phase 5 |
| `savedRecipes` | User bookmarks | Phase 7 |
| `stats` | Platform statistics (admin) | Phase 8 |

