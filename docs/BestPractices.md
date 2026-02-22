# SousChef - Best Practices & Architecture Guide

This document outlines the coding standards, architectural patterns, and best practices for the SousChef Android application. All contributors should follow these guidelines to maintain consistency and code quality.

---

## 📁 Project Structure

The project follows a **feature-based clean architecture** pattern with clear separation of concerns:

```
com.souschef/
├── core/                           # Shared utilities and DI
│   ├── di/                         # Koin dependency injection modules
│   │   └── AppModules.kt
│   └── util/                       # Utility classes and extensions
│       ├── Constants.kt            # App-wide constants
│       ├── Extensions.kt           # Kotlin extension functions
│       └── Result.kt               # Result wrapper for error handling
│
├── data/                           # Data layer (implementations)
│   └── repository/                 # Repository implementations
│       └── AuthRepositoryImpl.kt
│
├── domain/                         # Domain layer (business logic)
│   ├── model/                      # Domain models
│   │   ├── User.kt
│   │   └── AuthState.kt
│   └── repository/                 # Repository interfaces
│       └── AuthRepository.kt
│
├── feature/                        # Feature modules
│   ├── auth/                       # Authentication feature
│   │   └── presentation/           # UI layer
│   │       ├── components/         # Reusable UI components
│   │       │   ├── AuthButtons.kt
│   │       │   ├── AuthTextField.kt
│   │       │   └── SocialSignIn.kt
│   │       ├── AuthViewModel.kt
│   │       ├── LoginScreen.kt
│   │       ├── SignUpScreen.kt
│   │       └── EmailVerificationScreen.kt
│   │
│   └── dashboard/                  # Dashboard feature (example)
│       └── presentation/
│
├── navigation/                     # Navigation setup
│   └── Routes.kt                   # Route definitions
│
├── ui/                             # Shared UI components
│   └── theme/                      # Theme configuration
│       ├── Color.kt
│       └── Theme.kt
│
├── MainActivity.kt                 # Entry point
└── SousChefApplication.kt          # Application class (Koin init)
```

### Key Principles

1. **Feature-based organization**: Group code by feature, not by type
2. **Clean Architecture layers**:
   - `domain/` - Business logic, models, repository interfaces (no Android dependencies)
   - `data/` - Repository implementations, data sources, API clients
   - `presentation/` - ViewModels, Composables, UI state
3. **Shared code** lives in `core/` or root-level `ui/`

---

## 📛 Naming Conventions

### Files

| Type | Convention | Example |
|------|------------|---------|
| Screen Composable | `{Feature}Screen.kt` | `LoginScreen.kt` |
| ViewModel | `{Feature}ViewModel.kt` | `AuthViewModel.kt` |
| Repository Interface | `{Feature}Repository.kt` | `AuthRepository.kt` |
| Repository Implementation | `{Feature}RepositoryImpl.kt` | `AuthRepositoryImpl.kt` |
| UI Components | `{Name}.kt` (descriptive) | `AuthTextField.kt` |
| Domain Models | `{Name}.kt` (noun) | `User.kt`, `Recipe.kt` |
| Utility Files | `{Name}.kt` | `Extensions.kt`, `Constants.kt` |

### Classes & Functions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `AuthViewModel`, `User` |
| Functions | camelCase | `signInWithEmail()`, `validateInputs()` |
| Composables | PascalCase | `LoginScreen()`, `PrimaryButton()` |
| Constants | SCREAMING_SNAKE_CASE | `MIN_PASSWORD_LENGTH` |
| State variables | camelCase with underscore prefix for private | `_uiState`, `authState` |

### Packages

- All lowercase, no underscores
- Feature packages: `com.souschef.feature.{featurename}`
- Core packages: `com.souschef.core.{module}`

---

## 🏗️ Architecture Patterns

### State Management

Use **UiState data classes** with **StateFlow** for UI state:

```kotlin
// Define UI state
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null
)

// In ViewModel
private val _uiState = MutableStateFlow(AuthUiState())
val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

// Update state
fun onEmailChange(email: String) {
    _uiState.update { it.copy(email = email, emailError = null) }
}
```

### One-Time Events

Use **SharedFlow** for events that should be consumed once (snackbars, navigation):

```kotlin
// Define events
sealed class AuthEvent {
    data class ShowMessage(val message: String) : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

// In ViewModel
private val _events = MutableSharedFlow<AuthEvent>()
val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

// Emit events
_events.emit(AuthEvent.ShowError("Invalid email"))

// Collect in Composable
LaunchedEffect(Unit) {
    viewModel.events.collectLatest { event ->
        when (event) {
            is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            is AuthEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
        }
    }
}
```

### Result Wrapper

Always wrap data layer responses with the `Result` class:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Usage in repository
suspend fun signIn(email: String, password: String): Result<User> {
    return try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Result.Success(result.user.toDomainUser())
    } catch (e: Exception) {
        Result.Error(e, e.message)
    }
}

// Usage in ViewModel
when (val result = authRepository.signIn(email, password)) {
    is Result.Success -> { /* Handle success */ }
    is Result.Error -> { /* Handle error */ }
    is Result.Loading -> { /* Handle loading */ }
}
```

---

## 💉 Dependency Injection (Koin)

### Module Organization

Organize Koin modules by layer/feature:

```kotlin
// Firebase module
val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
}

// Repository module
val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

// ViewModel module
val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
}

// Combine all modules
val appModules = listOf(
    firebaseModule,
    repositoryModule,
    viewModelModule
)
```

### Accessing Dependencies

In Composables:
```kotlin
val viewModel: AuthViewModel = koinViewModel()
```

In regular classes (non-Composable):
```kotlin
class MyHelper : KoinComponent {
    private val authRepository: AuthRepository by inject()
}
```

---

## 🧭 Navigation Patterns

### Route Definitions

Use sealed interfaces for type-safe routes:

```kotlin
sealed interface Route {
    @Serializable
    data object Login : Route
    
    @Serializable
    data object SignUp : Route
    
    @Serializable
    data class RecipeDetail(val recipeId: String) : Route
}
```

### Auth-Aware Navigation

The app uses auth state to drive navigation:

```kotlin
when (authState) {
    is AuthState.Loading -> LoadingScreen()
    is AuthState.Unauthenticated -> AuthNavigation(...)
    is AuthState.Unverified -> EmailVerificationScreen(...)
    is AuthState.Authenticated -> DashboardScreen(...)
}
```

### Navigation Best Practices

1. **Single source of truth**: Auth state drives top-level navigation
2. **Clear back stack management**: Pop to login on sign-out
3. **Deep linking ready**: Use serializable routes
4. **State preservation**: Use `rememberSaveable` for form inputs

---

## 🎨 UI/UX Guidelines

Refer to [DesignGuidelines.md](./DesignGuidelines.md) for complete design specifications.

### Key Principles

1. **Spacing**: Use multiples of 4dp (prefer 8dp, 16dp, 24dp)
2. **Typography**: Serif for headings, Sans for body
3. **Colors**: Gold (#D4AF6A) for primary CTAs, neutrals for backgrounds
4. **Shapes**: 8dp radius for buttons, 12dp for cards

### Component Patterns

```kotlin
// Primary button
PrimaryButton(
    text = "Sign In",
    onClick = { viewModel.signIn() },
    isLoading = uiState.isLoading
)

// Text field with validation
AuthTextField(
    value = uiState.email,
    onValueChange = viewModel::onEmailChange,
    label = "Email address",
    error = uiState.emailError,
    keyboardType = KeyboardType.Email
)
```

---

## 🔒 Error Handling

### User-Friendly Error Messages

Convert technical errors to user-friendly messages:

```kotlin
fun Throwable.toUserMessage(): String {
    return when {
        message?.contains("email address is badly formatted") == true ->
            "Please enter a valid email address"
        message?.contains("no user record") == true ->
            "No account found with this email"
        else -> message ?: "An unexpected error occurred"
    }
}
```

### Validation

Validate inputs before API calls:

```kotlin
private fun validateSignInInputs(state: AuthUiState): Boolean {
    var isValid = true
    
    if (!state.email.isValidEmail()) {
        _uiState.update { it.copy(emailError = "Please enter a valid email") }
        isValid = false
    }
    
    return isValid
}
```

---

## ✅ Testing Guidelines

### Test File Naming

| Type | Convention | Example |
|------|------------|---------|
| Unit Test | `{ClassName}Test.kt` | `AuthViewModelTest.kt` |
| UI Test | `{ScreenName}Test.kt` | `LoginScreenTest.kt` |
| Integration Test | `{Feature}IntegrationTest.kt` | `AuthIntegrationTest.kt` |

### Test Structure

```kotlin
class AuthViewModelTest {
    
    @Test
    fun `signIn with valid credentials updates state to authenticated`() {
        // Given
        val viewModel = AuthViewModel(FakeAuthRepository())
        
        // When
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("password123")
        viewModel.signIn()
        
        // Then
        assertEquals(expected, viewModel.authState.value)
    }
}
```

---

## 📦 Dependencies

### Adding New Dependencies

1. Add version to `gradle/libs.versions.toml`:
   ```toml
   [versions]
   newLibrary = "1.0.0"
   
   [libraries]
   new-library = { group = "com.example", name = "library", version.ref = "newLibrary" }
   ```

2. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.new.library)
   ```

### Current Core Dependencies

- **UI**: Jetpack Compose, Material3
- **DI**: Koin
- **Auth**: Firebase Auth, Credential Manager
- **Async**: Kotlin Coroutines, Flow
- **Database**: Firebase Firestore

---

## 🚀 Quick Reference

### Creating a New Feature

1. Create feature package: `feature/{featurename}/`
2. Add subpackages: `presentation/`, `presentation/components/`
3. Create ViewModel with UiState and Events
4. Create Screen composables
5. Add route to `navigation/Routes.kt`
6. Register ViewModel in Koin module
7. Add navigation in MainActivity or NavHost

### Checklist Before PR

- [ ] Code follows naming conventions
- [ ] No hardcoded strings (use resources or constants)
- [ ] Error handling implemented
- [ ] Loading states handled
- [ ] Accessibility considered (content descriptions)
- [ ] No unused imports or dead code
- [ ] Tests added for new functionality

---

*Last updated: February 2026*

