# Android Coding Guidelines
> Based on patterns established in Jagrati-Android. Use this as the reference standard for all new Android (Jetpack Compose + Kotlin) projects.

> **Note:** This project uses **Firebase** for backend services, specifically **Cloud Firestore** for data storage and **Firebase Authentication** for user management.

---

## Table of Contents
1. [Project Structure](#1-project-structure)
2. [Gradle & Dependency Management](#2-gradle--dependency-management)
3. [Dependency Injection with Koin](#3-dependency-injection-with-koin)
4. [Room Database](#4-room-database)
5. [Repository & Service Layer](#5-repository--service-layer)
6. [ViewModel Guidelines](#6-viewmodel-guidelines)
7. [UI & Compose Screen Guidelines](#7-ui--compose-screen-guidelines)
8. [Preview Guidelines](#8-preview-guidelines)
9. [Pull-to-Refresh / App Refresh Guidelines](#9-pull-to-refresh--app-refresh-guidelines)
10. [Navigation](#10-navigation)
11. [Theming & Colors](#11-theming--colors)
12. [Error Handling](#12-error-handling)
13. [Preferences (DataStore)](#13-preferences-datastore)
14. [Use Cases](#14-use-cases)
15. [General Kotlin Conventions](#15-general-kotlin-conventions)

---

## 1. Project Structure

```
app/src/main/java/com/<org>/<appname>/
├── application/         # Application class, Koin initialisation
├── api/                 # Auth providers, token helpers
├── di/                  # One Koin module file per layer
│   ├── DatabaseModule.kt
│   ├── NetworkingModule.kt
│   ├── PreferencesModule.kt
│   ├── RepositoryModule.kt
│   ├── ServiceModule.kt
│   ├── UseCaseModule.kt
│   └── ViewModelModule.kt
├── model/               # Pure data/entity classes
│   ├── dao/             # Room DAOs (one file per entity)
│   ├── databases/       # RoomDatabase class + TypeConverters
│   ├── repository/      # Repository data models (response DTOs)
│   └── <domain>/        # Domain-specific sub-models
├── repository/          # Repository interfaces + Firebase implementations
│   └── <domain>/
│       ├── <Name>Repository.kt          # interface
│       └── Firestore<Name>Repository.kt # implementation
├── service/             # Firebase service classes (raw Firestore/Auth calls)
│   └── <domain>/
│       └── Firebase<Name>Service.kt
├── ui/
│   ├── components/      # Reusable Compose components
│   ├── navigation/
│   │   ├── AppNavigation.kt   # NavDisplay setup
│   │   └── Screens.kt         # Sealed interface NavKey destinations
│   ├── screens/
│   │   └── <feature>/         # One folder per feature/screen
│   │       ├── <Feature>Screen.kt
│   │       ├── <Feature>ViewModel.kt
│   │       └── <Feature>UiState.kt   # (optional, or co-locate in ViewModel)
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodels/      # App-level / shared ViewModels (e.g. AppViewModel)
├── usecases/            # Business logic use cases
│   └── <domain>/
│       └── <Name>UseCase.kt
├── util/                # Utilities, helpers, Resource wrapper
├── worker/              # WorkManager workers
├── notifications/       # FCM + notification helpers
├── permissions/         # Runtime permission helpers
└── preferences/         # DataStore preference helpers
```

### Rules
- **One folder per feature screen** under `ui/screens/`. The folder holds the Screen composable, ViewModel, and UiState.
- Keep **model classes** (Room entities, DTOs, API responses) under `model/`.
- Never put business logic in a Screen composable — push it to the ViewModel or UseCase.
- The `components/` folder holds **only reusable, stateless** composables.

---

## 2. Gradle & Dependency Management

### Version Catalog (`gradle/libs.versions.toml`)
All dependency versions live in the version catalog. Never hard-code a version string in `build.gradle.kts`.

```toml
# gradle/libs.versions.toml
[versions]
koinAndroid = "4.1.0"
roomRuntime  = "2.7.2"
firebaseBom = "33.0.0"

[libraries]
koin-android          = { module = "io.insert-koin:koin-android",          version.ref = "koinAndroid" }
androidx-room-runtime = { module = "androidx.room:room-runtime",           version.ref = "roomRuntime" }
firebase-bom          = { module = "com.google.firebase:firebase-bom",     version.ref = "firebaseBom" }
firebase-firestore    = { module = "com.google.firebase:firebase-firestore-ktx" }
firebase-auth         = { module = "com.google.firebase:firebase-auth-ktx" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

Reference in `build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.koin.android)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)   // use KSP, not kapt
    
    // Firebase (use BOM for version management)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
}
```

### Build-Type Sensitive Strings
Never store environment-specific URLs or keys in `strings.xml`. Use `resValue` per build type, sourced from `local.properties` (for local) or environment variables (for CI/CD).

```kotlin
// app/build.gradle.kts
fun propOrEnv(name: String, default: String = ""): String =
    localProperties?.getProperty(name) ?: System.getenv(name) ?: default

buildTypes {
    debug {
        applicationIdSuffix = ".debug"
        versionNameSuffix   = "-debug"
        resValue("string", "app_name",  "MyApp Debug")
        resValue("string", "BASE_URL",  propOrEnv("BASE_URL"))
        resValue("string", "WEB_CLIENT_ID", propOrEnv("WEB_CLIENT_ID"))
    }
    release {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        resValue("string", "app_name",  "MyApp")
        resValue("string", "BASE_URL",  propOrEnv("PROD_BASE_URL"))
        resValue("string", "WEB_CLIENT_ID", propOrEnv("PROD_WEB_CLIENT_ID"))
    }
}
```

Access at runtime:
```kotlin
val baseUrl = context.getString(R.string.BASE_URL)
// or inside a Koin module:
androidApplication().getString(R.string.BASE_URL)
```

### `local.properties` Template
Always commit a `local.properties.template` with all required keys. The real `local.properties` is in `.gitignore`.

```properties
# local.properties.template
BASE_URL=https://your-dev-server.com/
PROD_BASE_URL=https://your-prod-server.com/
WEB_CLIENT_ID=
PROD_WEB_CLIENT_ID=
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=
RELEASE_KEY_ALIAS=
RELEASE_KEY_PASSWORD=
```

### Signing Config
Read signing config from `local.properties` locally; fall back to environment variables in CI.

```kotlin
signingConfigs {
    create("release") {
        if (rootProject.file("local.properties").exists()) {
            val props = Properties().apply { load(FileInputStream(rootProject.file("local.properties"))) }
            storeFile     = file(props["RELEASE_STORE_FILE"] as String)
            storePassword = props["RELEASE_STORE_PASSWORD"] as String
            keyAlias      = props["RELEASE_KEY_ALIAS"] as String
            keyPassword   = props["RELEASE_KEY_PASSWORD"] as String
        } else {
            storeFile     = file(System.getenv("KEYSTORE_PATH") ?: "release.jks")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias      = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword   = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }
}
```

---

## 3. Dependency Injection with Koin

### Application Setup
Initialise Koin in `Application.onCreate()`. Register all modules explicitly.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            androidLogger()          // LogLevel.ERROR in release
            modules(
                viewModelModule,
                databaseModule,
                networkModule,
                preferencesModule,
                repositoryModule,
                serviceModule,
                useCaseModule
            )
        }
    }
}
```

### Module Conventions

| File | Koin scope | Used for |
|---|---|---|
| `DatabaseModule.kt` | `single` | RoomDatabase + all DAOs |
| `NetworkingModule.kt` | `single` | HttpClient, all Ktor services |
| `RepositoryModule.kt` | `single` | Repository interface → implementation bindings |
| `PreferencesModule.kt` | `factory` | DataStore preferences wrapper |
| `ServiceModule.kt` | `single` | Any non-Ktor services |
| `UseCaseModule.kt` | `single` | Use cases (stateless, can be `factory` if stateful) |
| `ViewModelModule.kt` | `factory` | Every ViewModel; `single` only for app-level VMs |

### Database Module
```kotlin
val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), PrimaryDatabase::class.java, "primary_db")
            .build()
    }
    // Expose each DAO as a separate single
    single { get<PrimaryDatabase>().studentDao() }
    single { get<PrimaryDatabase>().villageDao() }
    // ...one line per DAO
}
```

### Repository Module
Always bind the **interface** to the implementation:
```kotlin
val repositoryModule = module {
    single<StudentRepository> { FirestoreStudentRepository(get()) }
    single<AttendanceRepository> { FirestoreAttendanceRepository(get()) }
}
```

### ViewModel Module
- Use `factory` for screen-scoped ViewModels (created fresh each time the screen enters the composition).
- Use `single` only for app-level ViewModels that must persist across the entire activity lifecycle (e.g. `AppViewModel`).
- For ViewModels that need **runtime parameters**, use a Koin parametrised factory:

```kotlin
val viewModelModule = module {
    // App-level — single
    single { AppViewModel(get(), get()) }

    // Screen-scoped — factory
    factory { LoginViewModel(get()) }

    // Parametrised — factory with parameters
    factory { (pid: String?) -> StudentRegistrationViewModel(get(), get(), get(), pid) }
    factory { (studentPid: String) -> StudentProfileViewModel(studentPid, get(), get()) }
    factory { (hasPerms: Boolean, millis: Long) ->
        AttendanceMarkingViewModel(get(), get(), get(), hasPerms, millis)
    }
}
```

Inject a parametrised ViewModel from Compose:
```kotlin
val viewModel: StudentProfileViewModel = koinViewModel(
    parameters = { parametersOf(studentPid) }
)
```


## 4. Room Database

### Entity Definition
```kotlin
@Entity(tableName = "student")
data class Student(
    @PrimaryKey
    @ColumnInfo(name = "pid")
    val pid: String,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    // Use nullable for optional fields
    @ColumnInfo(name = "year_of_birth")
    val yearOfBirth: Int? = null
)
```

**Rules:**
- Always use `@ColumnInfo(name = "snake_case_name")` — never rely on field name defaults.
- Mark all optional columns as nullable with a default of `null`.
- Entity class must be a `data class`.
- Add computed properties (no DB backing) as `val` with `get()` lambdas outside the constructor.

### DAO Definition
```kotlin
@Dao
interface StudentDao {
    // Prefer @Upsert over separate @Insert/@Update
    @Upsert
    suspend fun upsertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Delete by primary key when you don't have the full entity
    @Query("DELETE FROM student WHERE pid = :pid")
    suspend fun deleteByPid(pid: String)

    // Return Flow for reactive lists
    @Query("SELECT * FROM student WHERE is_active = 1")
    fun getAllActiveStudents(): Flow<List<Student>>

    // Return suspend fun for one-shot reads
    @Query("SELECT * FROM student WHERE pid = :pid")
    suspend fun getStudentByPid(pid: String): Student?

    // Existence check
    @Query("SELECT EXISTS(SELECT 1 FROM student WHERE pid = :pid)")
    suspend fun exists(pid: String): Boolean
}
```

**Rules:**
- Live/reactive data → return `Flow<T>`.
- One-shot reads/writes → `suspend fun`.
- Always use `@Upsert` for insert-or-update patterns.
- Existence checks use `SELECT EXISTS(...)` for efficiency.

### RoomDatabase Class
```kotlin
@Database(
    entities = [Student::class, Village::class, /* ... */],
    version = 2,
    exportSchema = false          // set true if you track migrations
)
@TypeConverters(MyTypeConverter::class)
abstract class PrimaryDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun villageDao(): VillageDao

    fun clearAll() = clearAllTables()
}
```

**Rules:**
- Register every entity and TypeConverter in `@Database`.
- Increment `version` whenever the schema changes and provide a Migration.
- Expose one abstract function per DAO — never instantiate DAOs directly.

### TypeConverters
```kotlin
class MyTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMyObject(value: MyObject?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toMyObject(value: String?): MyObject? =
        value?.let { gson.fromJson(it, MyObject::class.java) }
}
```

---

## 5. Repository & Service Layer

### Layer Responsibilities

```
Screen / ViewModel
      ↓
  Repository (interface)          ← what the VM depends on
      ↓
  Firestore Repository (impl)     ← wraps service calls in Resource<T> flow
      ↓
  Firebase Service                ← raw Firestore/Auth calls via Firebase SDK
```

### Repository Interface
```kotlin
interface StudentRepository {
    suspend fun getStudent(pid: String): Flow<Resource<Student>>
    suspend fun createStudent(student: StudentRequest): Flow<Resource<Student>>
}
```

### Firestore Repository Implementation
```kotlin
class FirestoreStudentRepository(
    private val service: FirebaseStudentService
) : StudentRepository {

    override suspend fun getStudent(pid: String): Flow<Resource<Student>> = flow {
        emit(Resource.loading())
        val response = safeFirestoreCall { service.getStudent(pid) }
        emit(response)
    }
}
```

- Every repository function returns `Flow<Resource<T>>`.
- Always `emit(Resource.loading())` first so the ViewModel can show a loading state.
- Wrap every service call in `safeFirestoreCall { }` which catches exceptions and maps them to `Resource.failure(error = ResponseError.NETWORK_ERROR)`.

### Firebase Service Implementation
```kotlin
class FirebaseStudentService(
    private val firestore: FirebaseFirestore
) {
    private val studentsCollection = firestore.collection("students")

    suspend fun getStudent(pid: String): Student? {
        return studentsCollection.document(pid).get().await().toObject<Student>()
    }

    suspend fun createStudent(student: Student): Student {
        studentsCollection.document(student.pid).set(student).await()
        return student
    }

    fun getAllStudentsFlow(): Flow<List<Student>> = callbackFlow {
        val listenerRegistration = studentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val students = snapshot?.toObjects<Student>() ?: emptyList()
            trySend(students)
        }
        awaitClose { listenerRegistration.remove() }
    }
}
```

**Firebase Service Rules:**
- Use `await()` from `kotlinx-coroutines-play-services` for one-shot Firestore operations.
- Use `callbackFlow` for real-time listeners that emit updates.
- Always call `awaitClose { }` to clean up snapshot listeners.
- Services handle only raw Firestore operations; business logic belongs in repositories or use cases.

### Resource Wrapper
```kotlin
class Resource<T>(val status: Status, val data: T?, val error: ResponseError? = null) {
    enum class Status { LOADING, SUCCESS, FAILED }

    companion object {
        fun <T> success(data: T)  = Resource(Status.SUCCESS, data)
        fun <T> failure(data: T? = null, error: ResponseError? = null) = Resource(Status.FAILED, data, error)
        fun <T> loading(data: T? = null) = Resource(Status.LOADING, data)
    }
}
```

---

## 6. ViewModel Guidelines

### BaseViewModel
Every feature ViewModel extends `BaseViewModel<UiState>`:

```kotlin
abstract class BaseViewModel<UiState> : ViewModel() {
    val errorFlow   = MutableStateFlow<ResponseError?>(null)
    val successMsgFlow = MutableStateFlow<String?>(null)

    abstract val uiState: StateFlow<UiState>
    protected abstract fun createUiStateFlow(): StateFlow<UiState>

    protected fun emitError(error: ResponseError?) {
        viewModelScope.launch { errorFlow.emit(error) }
    }
    protected fun emitMsg(msg: String?) {
        viewModelScope.launch { successMsgFlow.emit(msg) }
    }
    fun clearErrorFlow() = emitError(null)
    fun clearMsgFlow()   = emitMsg(null)
}
```

### UiState Pattern
- Each screen has a **single immutable data class** as its UiState.
- All fields have sensible defaults so an empty/initial state is always valid.
- **Never** expose mutable state directly to the UI.

```kotlin
data class StudentListUiState(
    val students: List<Student>     = emptyList(),
    val villages: List<Village>     = emptyList(),
    val isLoading: Boolean          = true,
    val isRefreshing: Boolean       = false,
    val errorMessage: String?       = null
)
```

### Creating the StateFlow with `combine`
Use Koin's `combine` + `stateIn` for deriving a single `uiState` from multiple internal `MutableStateFlow`s:

```kotlin
class MyViewModel(private val repo: MyRepository) : BaseViewModel<MyUiState>() {

    private val _isLoading     = MutableStateFlow(false)
    private val _isRefreshing  = MutableStateFlow(false)
    private val _data          = MutableStateFlow<List<Item>>(emptyList())
    private val _selectedFilter = MutableStateFlow<String?>(null)

    override val uiState: StateFlow<MyUiState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<MyUiState> =
        combine(_isLoading, _isRefreshing, _data, _selectedFilter) {
            isLoading, isRefreshing, data, filter ->
            MyUiState(
                isLoading    = isLoading,
                isRefreshing = isRefreshing,
                items        = if (filter == null) data else data.filter { it.type == filter },
                selectedFilter = filter
            )
        }.stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = MyUiState()
        )

    init { loadData() }

    fun loadData(isRefreshing: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isRefreshing) _isRefreshing.value = true else _isLoading.value = true
            repo.getItems().collect { resource ->
                when (resource.status) {
                    Resource.Status.LOADING  -> { /* already set above */ }
                    Resource.Status.SUCCESS  -> {
                        _data.value         = resource.data ?: emptyList()
                        _isLoading.value    = false
                        _isRefreshing.value = false
                    }
                    Resource.Status.FAILED   -> {
                        _isLoading.value    = false
                        _isRefreshing.value = false
                        emitError(resource.error)
                    }
                }
            }
        }
    }

    fun refresh() = loadData(isRefreshing = true)
}
```

### ViewModel Rules
1. **Never** reference `Context`, `Activity`, or any Android view inside a ViewModel (except `AndroidViewModel` when absolutely needed).
2. All coroutines launched in a ViewModel use `viewModelScope`.
3. Use `Dispatchers.IO` for database / network work; `Dispatchers.Main` is the default for state updates.
4. Never expose `MutableStateFlow` — only expose `StateFlow` via `asStateFlow()` or through `uiState`.
5. The `init {}` block triggers initial data load (call the load function from `init`).
6. Provide public functions for user actions (`fun refresh()`, `fun selectFilter(f: String?)`) — screens should not manipulate internal state.
7. App-level ViewModels (e.g. `AppViewModel`) are registered as `single` in Koin so they survive navigation.

---

## 7. UI & Compose Screen Guidelines

### Two-Composable Pattern
Every screen is split into **two composables**:

| Composable | Responsibility |
|---|---|
| `<Feature>Screen` | Connects ViewModel; collects `uiState`; handles `LaunchedEffect` for errors/messages; passes lambdas down |
| `<Feature>ScreenLayout` | Purely presentational; receives only plain data + lambda callbacks; no ViewModel, no Koin |

```kotlin
// Stateful — wires ViewModel
@Composable
fun StudentListScreen(
    onBackPress: () -> Unit,
    onStudentClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: StudentListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Trigger initial load
    LaunchedEffect(Unit) { viewModel.loadStudents() }

    // Show errors via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Observe BaseViewModel errorFlow
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect { error ->
            error?.let {
                snackbarHostState.showSnackbar(it.toast)
                viewModel.clearErrorFlow()
            }
        }
    }

    StudentListScreenLayout(
        students          = uiState.students,
        isLoading         = uiState.isLoading,
        onBackPress       = onBackPress,
        onStudentClick    = onStudentClick,
        snackbarHostState = snackbarHostState
    )
}

// Stateless — only data + callbacks
@Composable
fun StudentListScreenLayout(
    students: List<Student>,
    isLoading: Boolean,
    onBackPress: () -> Unit,
    onStudentClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // ...UI
    }
}
```

### Reusable Components
- Place all reusable composables in `ui/components/`.
- Each component file should expose a matching `Preview`.
- Components must be stateless — pass state in, callbacks out.
- Create mapper extension functions (e.g. `Student.toPersonCardData()`) to decouple component models from domain models.

### Scaffold & Snackbar
- Always wrap screens in `Scaffold`.
- Pass `SnackbarHostState` down from the parent (navigation level) so all screens share one snackbar queue.

---

## 8. Preview Guidelines

> **Rule: Every composable that renders UI must have at least one `@Preview`.**

### Minimum Preview Set
```kotlin
// Light mode
@Preview(showBackground = true)
// Dark mode
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyComponentPreview() {
    JagratiAndroidTheme {
        MyComponent(/* hardcoded sample data */)
    }
}
```

### Preview Rules
1. **Always wrap in the app theme** (`YourAppTheme { … }`) so colors and typography are correct.
2. **Always provide both light and dark** previews using `uiMode = Configuration.UI_MODE_NIGHT_YES`.
3. Preview the **Layout composable**, not the Screen composable (Screen requires a ViewModel).
4. Use **hardcoded, realistic sample data** — never empty strings or placeholders like "TODO".
5. For screens with loading states, add a separate preview with `isLoading = true`.
6. For screens with error states, add a preview with an error message populated.
7. Complex screens may use `remember { }` inside the preview to build `SnackbarHostState`, list state, etc.

```kotlin
// Full screen layout preview example
@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudentListScreenLayoutPreview() {
    JagratiAndroidTheme {
        StudentListScreenLayout(
            students = listOf(
                Student(pid = "1", firstName = "Rahul", lastName = "Kumar",
                    gender = "Male", villageId = 1, groupId = 1),
                Student(pid = "2", firstName = "Priya", lastName = "Sharma",
                    gender = "Female", villageId = 2, groupId = 2)
            ),
            villages          = listOf(Village(id = 1, name = "Bargi")),
            groups            = listOf(Groups(id = 1, name = "Group A")),
            isLoading         = false,
            selectedVillage   = null,
            selectedGroup     = null,
            onVillageSelected = {},
            onGroupSelected   = {},
            onBackPress       = {},
            onStudentClick    = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

// Loading state preview
@Preview(showBackground = true)
@Composable
fun StudentListLoadingPreview() {
    JagratiAndroidTheme {
        StudentListScreenLayout(
            students          = emptyList(),
            villages          = emptyList(),
            groups            = emptyList(),
            isLoading         = true,
            selectedVillage   = null,
            selectedGroup     = null,
            onVillageSelected = {},
            onGroupSelected   = {},
            onBackPress       = {},
            onStudentClick    = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
```

### Preview for Reusable Components
```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PersonCardPreview() {
    JagratiAndroidTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PersonCard(
                data = PersonCardData(
                    title            = "Rahul Kumar Singh",
                    subtitle         = "Bargi Village",
                    extra            = "Group A",
                    profileImageUrl  = null
                ),
                onClick = {}
            )
        }
    }
}
```

---

## 9. Pull-to-Refresh / App Refresh Guidelines

### Pattern
Use Material3's `PullToRefreshBox` for all list/data screens.

```kotlin
// In UiState:
data class MyUiState(
    val isLoading: Boolean    = true,   // initial full-screen load
    val isRefreshing: Boolean = false,  // pull-to-refresh indicator
    // ...
)

// In ViewModel:
fun loadData(isRefreshing: Boolean = false) {
    viewModelScope.launch(Dispatchers.IO) {
        if (isRefreshing) _isRefreshing.value = true
        else              _isLoading.value    = true
        // ... fetch
        _isLoading.value    = false
        _isRefreshing.value = false
    }
}
fun refresh() = loadData(isRefreshing = true)

// In Layout composable:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreenLayout(
    uiState: MyUiState,
    onRefresh: () -> Unit,
    // ...
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh    = onRefresh,
        modifier     = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            // Full-screen shimmer / CircularProgressIndicator
        } else {
            LazyColumn { /* content */ }
        }
    }
}
```

### Rules
1. Distinguish `isLoading` (first load, show full shimmer/spinner) from `isRefreshing` (pull-to-refresh, show indicator at top).
2. **Never** block the date/filter header behind `isLoading` — keep navigation controls always visible.
3. Always reset both `isLoading` and `isRefreshing` to `false` in both success **and** failure paths.
4. Wire `onRefresh` in the Screen composable to `viewModel::refresh`.

---

## 10. Navigation

### Destination Definitions (`Screens.kt`)
Use a **sealed interface** implementing `NavKey` with `@Serializable` data objects/classes:

```kotlin
sealed interface Screens : NavKey {
    @Serializable data object NavHomeRoute : Screens
    @Serializable data object NavLoginRoute : Screens

    // Pass parameters via constructor properties
    @Serializable data class NavStudentProfileRoute(val studentPid: String) : Screens
    @Serializable data class NavSignUpDetailsRoute(val email: String) : Screens
}
```

### Navigation Setup (`AppNavigation.kt`)
Use Navigation 3 (`androidx.navigation3`):

```kotlin
@Composable
fun AppNavigation(snackbarHostState: SnackbarHostState, appViewModel: AppViewModel) {
    val backstack = rememberNavBackStack(Screens.NavHomeRoute)

    NavDisplay(
        backStack = backstack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { ContentTransform(
            targetContentEnter = slideInHorizontally(tween(300)) { it },
            initialContentExit = slideOutHorizontally(tween(300)) { -it }
        )},
        entryProvider = entryProvider {
            entry<Screens.NavHomeRoute> {
                HomeScreen(onNavigate = { backstack.add(it) })
            }
            entry<Screens.NavStudentProfileRoute> { key ->
                StudentProfileScreen(
                    studentPid  = key.studentPid,
                    onBackPress = { backstack.removeAt(backstack.size - 1) }
                )
            }
        }
    )
}
```

### Navigation Rules
1. Routes with dynamic data use `data class` with typed parameters — never string-encoded IDs in path segments.
2. Back navigation uses `backstack.removeAt(backstack.size - 1)`.
3. Inject `AppViewModel` at the navigation level as a `single` Koin dependency.
4. Handle logout at the navigation root by observing `AppViewModel.shouldLogout`.

---

---

## 13. Preferences (DataStore)

### Pattern
Wrap DataStore in a class with typed `DataStorePreference<T>` entries:

```kotlin
class AppPreferences(private val context: Context) {
    companion object {
        private val Context.dataStore by preferencesDataStore("app_prefs")
        private val ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessToken: DataStorePreference<String?> = object : DataStorePreference<String?> {
        override fun getFlow(): Flow<String?> =
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[ACCESS_TOKEN] }
                .distinctUntilChanged()

        override suspend fun set(value: String?) {
            context.dataStore.edit { prefs ->
                if (value == null) prefs.remove(ACCESS_TOKEN)
                else prefs[ACCESS_TOKEN] = value
            }
        }

        override suspend fun get(): String? = getFlow().first()
    }
}
```

- Register `AppPreferences` in `preferencesModule` as `factory` (DataStore handles its own singleton internally).
- Never use `SharedPreferences` — always DataStore.
- Use `distinctUntilChanged()` to avoid redundant emissions.

---

## 14. Use Cases

Use cases encapsulate **multi-step business logic** that involves multiple repositories or services.

```kotlin
class DataSyncUseCase(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences,
    private val syncRepository: SyncRepository
) {
    suspend fun fetchUserDetails(
        onSuccess: (UserDetails) -> Unit,
        onError: (ResponseError) -> Unit
    ) {
        val lastSync = appPreferences.lastSyncTime.get()
        userRepository.getUserDetails(lastSync).collect { result ->
            when (result.status) {
                Resource.Status.SUCCESS -> result.data?.let { onSuccess(it) }
                Resource.Status.FAILED  -> onError(result.error ?: ResponseError.UNKNOWN)
                else -> {}
            }
        }
    }
}
```

**Rules:**
- Use cases are registered as `single` in `UseCaseModule`.
- ViewModels depend on use cases; use cases depend on repositories.
- Background work (e.g. triggered by FCM) should use `CoroutineScope(Dispatchers.IO)` inside the use case, not in the ViewModel.

---

## 15. General Kotlin Conventions

### Naming
| Element | Convention | Example |
|---|---|---|
| Class / Object | `PascalCase` | `StudentListViewModel` |
| Function | `camelCase` | `loadStudents()` |
| Property | `camelCase` | `isLoading` |
| Private backing field | `_camelCase` | `_isLoading` |
| Constant | `SCREAMING_SNAKE_CASE` | `BASE_URL` |
| File | `PascalCase.kt` | `StudentListScreen.kt` |
| Package | `lowercase` | `ui.screens.studentlist` |

### Coroutines
- Use `viewModelScope.launch` inside ViewModels.
- Use `flow { }` builder in repositories.
- Prefer `Dispatchers.IO` for I/O; never block the main thread.
- Use `SharingStarted.WhileSubscribed(5_000)` for `stateIn` — the 5-second window handles configuration changes.

### Immutability
- Prefer `val` over `var` everywhere.
- UiState data classes are always immutable — use `.copy()` to produce updates.
- Expose `StateFlow` (not `MutableStateFlow`) to the UI layer.

### Null Safety
- Avoid `!!` (non-null assertion) — use `?.let`, `?: return`, or `requireNotNull` with a meaningful message.
- Make fields nullable only when `null` has semantic meaning (e.g. "not yet loaded").

### Comments
- Use KDoc (`/** */`) for public classes and functions.
- Use inline comments to explain **why**, not **what**.
- Tag app-level ViewModels and use cases with a KDoc describing their lifecycle scope.

---

*Last updated: March 2026 — Jagrati-Android project.*

