package com.souschef.ui.screens.profile

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Supported target languages (source is always English) ────────────────────

data class SupportedLanguage(
    val displayName: String,
    val mlKitCode: String,   // TranslateLanguage constant
    val sizeEstimateMb: Int = 30
)

val SUPPORTED_LANGUAGES = listOf(
    SupportedLanguage("French", TranslateLanguage.FRENCH),
    SupportedLanguage("German", TranslateLanguage.GERMAN),
    SupportedLanguage("Spanish", TranslateLanguage.SPANISH),
    SupportedLanguage("Hindi", TranslateLanguage.HINDI),
    SupportedLanguage("Japanese", TranslateLanguage.JAPANESE),
    SupportedLanguage("Chinese (Simplified)", TranslateLanguage.CHINESE),
    SupportedLanguage("Arabic", TranslateLanguage.ARABIC),
    SupportedLanguage("Portuguese", TranslateLanguage.PORTUGUESE),
    SupportedLanguage("Italian", TranslateLanguage.ITALIAN),
    SupportedLanguage("Korean", TranslateLanguage.KOREAN),
)

// ── Download state ────────────────────────────────────────────────────────────

enum class ModelDownloadState { NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, ERROR }

// ── Translation helper ────────────────────────────────────────────────────────

/**
 * Translates [text] from English to [targetLanguageCode] (a [TranslateLanguage] constant).
 * The model must already be downloaded before calling this.
 *
 * @return the translated string, or null on failure.
 */
suspend fun translateText(text: String, targetLanguageCode: String): String? {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(targetLanguageCode)
        .build()
    val translator = Translation.getClient(options)
    return try {
        translator.translate(text).await()
    } catch (e: Exception) {
        null
    } finally {
        translator.close()
    }
}

// ── Profile Screen ────────────────────────────────────────────────────────────

/**
 * Profile screen with:
 *  • User info & avatar
 *  • Translation model download manager
 *  • Language preference selector
 *  • Sign-out action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    preferredLanguageCode: String?,
    onSetPreferredLanguage: (String?) -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val modelManager = remember { RemoteModelManager.getInstance() }

    // Track download state per language code
    val downloadStates = remember { mutableStateMapOf<String, ModelDownloadState>() }
    var isInitializing by remember { mutableStateOf(true) }

    // Selected language preference
    val selectedLanguage = remember(preferredLanguageCode) {
        SUPPORTED_LANGUAGES.find { it.mlKitCode == preferredLanguageCode }
    }

    // Section expand toggles
    var modelsExpanded by remember { mutableStateOf(false) }
    var langPrefExpanded by remember { mutableStateOf(false) }

    // Seed initial states from on-device models
    LaunchedEffect(Unit) {
        try {
            val downloaded = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
            val downloadedCodes = downloaded.map { it.language }
            SUPPORTED_LANGUAGES.forEach { lang ->
                downloadStates[lang.mlKitCode] =
                    if (lang.mlKitCode in downloadedCodes) ModelDownloadState.DOWNLOADED
                    else ModelDownloadState.NOT_DOWNLOADED
            }
        } catch (_: Exception) {
        } finally {
            isInitializing = false
        }
    }

    // Helper: download a model
    fun downloadModel(lang: SupportedLanguage) {
        downloadStates[lang.mlKitCode] = ModelDownloadState.DOWNLOADING
        val model = TranslateRemoteModel.Builder(lang.mlKitCode).build()
        val conditions = DownloadConditions.Builder().build()
        scope.launch {
            try {
                modelManager.download(model, conditions).await()
                downloadStates[lang.mlKitCode] = ModelDownloadState.DOWNLOADED
            } catch (_: Exception) {
                downloadStates[lang.mlKitCode] = ModelDownloadState.ERROR
            }
        }
    }

    // Helper: delete a model
    fun deleteModel(lang: SupportedLanguage) {
        val model = TranslateRemoteModel.Builder(lang.mlKitCode).build()
        scope.launch {
            try {
                modelManager.deleteDownloadedModel(model).await()
                downloadStates[lang.mlKitCode] = ModelDownloadState.NOT_DOWNLOADED
                // Clear preference if user deletes the selected model
                if (preferredLanguageCode == lang.mlKitCode) {
                    onSetPreferredLanguage(null)
                }
            } catch (_: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            windowInsets = WindowInsets(top = 0.dp),
            title = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary()
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Avatar ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientGold))
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientGold)),
                contentAlignment = Alignment.Center
            ) {
                val initials = userProfile?.displayName
                    ?.split(" ")
                    ?.take(2)
                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
                    ?.joinToString("") ?: "?"

                Text(
                    text = initials,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.heroBackground()
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Identity ──────────────────────────────────────────────────────
            Text(
                text = userProfile?.displayName ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary()
            )
            Text(
                text = userProfile?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary()
            )
            if (userProfile?.isVerifiedChef == true) {
                Spacer(Modifier.height(12.dp))
                VerifiedChefBadge()
            }

            Spacer(Modifier.height(40.dp))

            // ── Translation Models Section ────────────────────────────────────
            SectionCard(
                title = "Translation Models",
                subtitle = "Downloads convert English → other languages offline",
                icon = Icons.Outlined.CloudDownload,
                expanded = modelsExpanded,
                onToggle = { modelsExpanded = !modelsExpanded }
            ) {
                SUPPORTED_LANGUAGES.forEach { lang ->
                    val state = downloadStates[lang.mlKitCode] ?: ModelDownloadState.NOT_DOWNLOADED
                    LanguageModelRow(
                        language = lang,
                        state = state,
                        isSelected = preferredLanguageCode == lang.mlKitCode,
                        onDownload = { downloadModel(lang) },
                        onDelete = { deleteModel(lang) }
                    )
                    if (lang != SUPPORTED_LANGUAGES.last()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Language Preference Section ───────────────────────────────────
            val currentLangName = selectedLanguage?.displayName ?: "English (Default)"
            SectionCard(
                title = "Preferred Language",
                subtitle = "Active: $currentLangName",
                icon = Icons.Outlined.Language,
                expanded = langPrefExpanded,
                onToggle = { langPrefExpanded = !langPrefExpanded }
            ) {
                val downloadedLanguages = SUPPORTED_LANGUAGES.filter {
                    downloadStates[it.mlKitCode] == ModelDownloadState.DOWNLOADED
                }

                if (isInitializing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (downloadedLanguages.isEmpty()) {
                    Text(
                        text = "Download at least one language model above to set a preference.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary(),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                    )
                    // If check finished and previously selected language model is missing, clear preference
                    if (preferredLanguageCode != null && downloadStates[preferredLanguageCode] != ModelDownloadState.DOWNLOADED) {
                        onSetPreferredLanguage(null)
                    }
                } else {
                    // Option to stay in English
                    LanguagePreferenceRow(
                        language = SupportedLanguage(
                            "English (Default)",
                            TranslateLanguage.ENGLISH,
                            0
                        ),
                        isSelected = preferredLanguageCode == null,
                        onSelect = { onSetPreferredLanguage(null) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    downloadedLanguages.forEach { lang ->
                        LanguagePreferenceRow(
                            language = lang,
                            isSelected = selectedLanguage?.mlKitCode == lang.mlKitCode,
                            onSelect = { onSetPreferredLanguage(lang.mlKitCode) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Sign Out ──────────────────────────────────────────────────────
            PremiumOutlinedButton(
                text = "Sign Out",
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 48.dp),
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

// ── Section Card ──────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary()
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.textSecondary()
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = AppColors.textSecondary(),
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation)
                )
            }

            // Collapsible content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    content()
                }
            }
        }
    }
}

// ── Language Model Row ────────────────────────────────────────────────────────

@Composable
private fun LanguageModelRow(
    language: SupportedLanguage,
    state: ModelDownloadState,
    isSelected: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = language.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary()
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "~${language.sizeEstimateMb} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary()
                )
            }

            Spacer(Modifier.width(12.dp))

            when (state) {
                ModelDownloadState.NOT_DOWNLOADED -> {
                    IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.CloudDownload,
                            contentDescription = "Download ${language.displayName}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                ModelDownloadState.DOWNLOADING -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(22.dp),
//                        strokeWidth = 2.dp
//                    )
                }

                ModelDownloadState.DOWNLOADED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete ${language.displayName}",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                ModelDownloadState.ERROR -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Outlined.CloudDownload,
                                contentDescription = "Retry download",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
        if (state == ModelDownloadState.DOWNLOADING) {
            Text(
                text = "Downloading",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary()
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
// ── Language Preference Row ───────────────────────────────────────────────────

@Composable
private fun LanguagePreferenceRow(
    language: SupportedLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = language.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else AppColors.textPrimary()
            )
            if (isSelected) {
                Text(
                    text = "Active – translations will use this language",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview() {
    SousChefTheme {
        ProfileScreen(
            userProfile = UserProfile(
                displayName = "Hemang Mishra",
                email = "hemang@souschef.com",
                role = "user"
            ),
            preferredLanguageCode = null,
            onSetPreferredLanguage = {},
            onSignOut = {}
        )
    }
}

//package com.souschef.ui.screens.profile
//
//import android.app.DownloadManager
//import android.content.Context
//import android.content.res.Configuration
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.outlined.Logout
//import androidx.compose.material.icons.outlined.CheckCircle
//import androidx.compose.material.icons.outlined.CloudDownload
//import androidx.compose.material.icons.outlined.Delete
//import androidx.compose.material.icons.outlined.ErrorOutline
//import androidx.compose.material.icons.outlined.ExpandMore
//import androidx.compose.material.icons.outlined.Language
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.RadioButtonDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.google.mlkit.common.model.DownloadConditions
//import com.google.mlkit.common.model.RemoteModelManager
//import com.google.mlkit.nl.translate.TranslateLanguage
//import com.google.mlkit.nl.translate.TranslateRemoteModel
//import com.google.mlkit.nl.translate.Translation
//import com.google.mlkit.nl.translate.TranslatorOptions
//import com.souschef.model.auth.UserProfile
//import com.souschef.ui.components.PremiumOutlinedButton
//import com.souschef.ui.components.VerifiedChefBadge
//import com.souschef.ui.theme.AppColors
//import com.souschef.ui.theme.GradientGold
//import com.souschef.ui.theme.SousChefTheme
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//// ── Supported Languages ───────────────────────────────────────────────────────
//
//data class SupportedLanguage(
//    val displayName: String,
//    val mlKitCode: String,
//    val estimatedSizeMb: Int = 30
//)
//
//val SUPPORTED_LANGUAGES = listOf(
//    SupportedLanguage("French",               TranslateLanguage.FRENCH),
//    SupportedLanguage("German",               TranslateLanguage.GERMAN),
//    SupportedLanguage("Spanish",              TranslateLanguage.SPANISH),
//    SupportedLanguage("Hindi",                TranslateLanguage.HINDI),
//    SupportedLanguage("Japanese",             TranslateLanguage.JAPANESE),
//    SupportedLanguage("Chinese (Simplified)", TranslateLanguage.CHINESE),
//    SupportedLanguage("Arabic",               TranslateLanguage.ARABIC),
//    SupportedLanguage("Portuguese",           TranslateLanguage.PORTUGUESE),
//    SupportedLanguage("Italian",              TranslateLanguage.ITALIAN),
//    SupportedLanguage("Korean",               TranslateLanguage.KOREAN),
//)
//
//// ── Download State ────────────────────────────────────────────────────────────
//
///**
// * Sealed class representing the full download lifecycle of an ML Kit model.
// *
// * [Downloading.progress]     — 0f..1f when total size is known, -1f = indeterminate
// * [Downloading.downloadedMb] — bytes received so far, converted to MB
// * [Downloading.totalMb]      — total file size in MB (0 if unknown)
// */
//sealed class ModelDownloadState {
//    object NotDownloaded : ModelDownloadState()
//    data class Downloading(
//        val progress: Float = -1f,
//        val downloadedMb: Float = 0f,
//        val totalMb: Float = 0f
//    ) : ModelDownloadState()
//    object Downloaded : ModelDownloadState()
//    data class Error(val message: String = "Download failed") : ModelDownloadState()
//}
//
//// ── DownloadManager Progress Poller ──────────────────────────────────────────
//
///**
// * Polls Android's [DownloadManager] every [intervalMs] ms for [downloadId],
// * reporting real byte-level progress through [onProgress].
// *
// * ML Kit uses DownloadManager internally for model downloads but does not
// * expose progress callbacks itself — this bridges that gap by querying
// * DownloadManager directly.
// */
//suspend fun pollDownloadProgress(
//    context: Context,
//    downloadId: Long,
//    intervalMs: Long = 300L,
//    onProgress: (ModelDownloadState.Downloading) -> Unit,
//    onComplete: () -> Unit,
//    onError: (String) -> Unit
//) {
//    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//
//    while (true) {
//        val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
//
//        // If the cursor is empty, ML Kit has finished and cleaned up the DM entry
//        if (cursor == null || !cursor.moveToFirst()) {
//            onComplete()
//            cursor?.close()
//            return
//        }
//
//        val status    = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
//        val received  = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//        val total     = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//        cursor.close()
//
//        when (status) {
//            DownloadManager.STATUS_RUNNING,
//            DownloadManager.STATUS_PENDING -> {
//                val progress = if (total > 0L) received.toFloat() / total.toFloat() else -1f
//                onProgress(
//                    ModelDownloadState.Downloading(
//                        progress     = progress,
//                        downloadedMb = received / 1_048_576f,
//                        totalMb      = if (total > 0L) total / 1_048_576f else 0f
//                    )
//                )
//            }
//            DownloadManager.STATUS_SUCCESSFUL -> { onComplete(); return }
//            DownloadManager.STATUS_FAILED     -> { onError("Download failed"); return }
//        }
//
//        delay(intervalMs)
//    }
//}
//
//// ── Translation Helper ────────────────────────────────────────────────────────
//
///**
// * Suspending function that translates [text] from English to [targetLanguageCode].
// *
// * Requires the model for [targetLanguageCode] to already be on-device.
// * Returns the translated string, or null on any failure.
// *
// * Example:
// *   val result = translateText("Add salt to taste", TranslateLanguage.FRENCH)
// *   // → "Ajouter du sel selon le goût"
// */
//suspend fun translateText(text: String, targetLanguageCode: String): String? {
//    val options = TranslatorOptions.Builder()
//        .setSourceLanguage(TranslateLanguage.ENGLISH)
//        .setTargetLanguage(targetLanguageCode)
//        .build()
//    val translator = Translation.getClient(options)
//    return try {
//        translator.translate(text).await()
//    } catch (_: Exception) {
//        null
//    } finally {
//        translator.close()
//    }
//}
//
//// ── Profile Screen ────────────────────────────────────────────────────────────
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProfileScreen(
//    userProfile: UserProfile?,
//    onSignOut: () -> Unit
//) {
//    val context      = LocalContext.current
//    val scope        = rememberCoroutineScope()
//    val modelManager = remember { RemoteModelManager.getInstance() }
//
//    val downloadStates = remember {
//        mutableStateMapOf<String, ModelDownloadState>().also { map ->
//            SUPPORTED_LANGUAGES.forEach { map[it.mlKitCode] = ModelDownloadState.NotDownloaded }
//        }
//    }
//
//    var selectedLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }
//    var modelsExpanded   by remember { mutableStateOf(true) }
//    var langPrefExpanded by remember { mutableStateOf(true) }
//
//    // Seed on-device model states on first composition
//    LaunchedEffect(Unit) {
//        try {
//            val onDevice = modelManager
//                .getDownloadedModels(TranslateRemoteModel::class.java)
//                .await()
//                .map { it.language }
//                .toSet()
//            SUPPORTED_LANGUAGES.forEach { lang ->
//                downloadStates[lang.mlKitCode] =
//                    if (lang.mlKitCode in onDevice) ModelDownloadState.Downloaded
//                    else ModelDownloadState.NotDownloaded
//            }
//        } catch (_: Exception) {}
//    }
//
//    // Starts ML Kit download, then polls DownloadManager for real byte progress
//    fun downloadModel(lang: SupportedLanguage) {
//        downloadStates[lang.mlKitCode] = ModelDownloadState.Downloading()
//        val model      = TranslateRemoteModel.Builder(lang.mlKitCode).build()
//        val conditions = DownloadConditions.Builder().requireWifi().build()
//
//        scope.launch {
//            // Fire the ML Kit task
//            val mlKitTask = modelManager.download(model, conditions)
//
//            // Try to locate the matching DownloadManager entry (ML Kit registers it there)
//            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            var downloadId: Long? = null
//            var waited = 0L
//
//            while (downloadId == null && waited < 2_000L) {
//                val cursor = dm.query(
//                    DownloadManager.Query().setFilterByStatus(
//                        DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING
//                    )
//                )
//                cursor?.use { c ->
//                    val idCol   = c.getColumnIndex(DownloadManager.COLUMN_ID)
//                    val descCol = c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)
//                    while (c.moveToNext()) {
//                        val desc = c.getString(descCol) ?: ""
//                        // ML Kit tags its entries with the language code or "mlkit"
//                        if (lang.mlKitCode in desc || desc.contains("mlkit", ignoreCase = true)) {
//                            downloadId = c.getLong(idCol)
//                            break
//                        }
//                    }
//                }
//                if (downloadId == null) { delay(300); waited += 300 }
//            }
//
//            if (downloadId != null) {
//                // Real byte-level progress via DownloadManager polling
//                pollDownloadProgress(
//                    context    = context,
//                    downloadId = downloadId!!,
//                    onProgress = { state -> downloadStates[lang.mlKitCode] = state },
//                    onComplete = { downloadStates[lang.mlKitCode] = ModelDownloadState.Downloaded },
//                    onError    = { msg   -> downloadStates[lang.mlKitCode] = ModelDownloadState.Error(msg) }
//                )
//            } else {
//                // Fallback: await ML Kit task directly (indeterminate bar)
//                try {
//                    mlKitTask.await()
//                    downloadStates[lang.mlKitCode] = ModelDownloadState.Downloaded
//                } catch (e: Exception) {
//                    downloadStates[lang.mlKitCode] = ModelDownloadState.Error(e.message ?: "Failed")
//                }
//            }
//        }
//    }
//
//    fun deleteModel(lang: SupportedLanguage) {
//        scope.launch {
//            try {
//                val model = TranslateRemoteModel.Builder(lang.mlKitCode).build()
//                modelManager.deleteDownloadedModel(model).await()
//                downloadStates[lang.mlKitCode] = ModelDownloadState.NotDownloaded
//                if (selectedLanguage?.mlKitCode == lang.mlKitCode) selectedLanguage = null
//            } catch (_: Exception) {}
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        TopAppBar(
//            windowInsets = WindowInsets(top = 0.dp),
//            title = {
//                Text(
//                    text = "Profile",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold,
//                    color = AppColors.textPrimary()
//                )
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.background
//            )
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(horizontal = 24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(Modifier.height(40.dp))
//
//            // ── Avatar ────────────────────────────────────────────────────────
//            Box(
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(CircleShape)
//                    .background(Brush.linearGradient(GradientGold))
//                    .padding(4.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.background)
//                    .padding(4.dp)
//                    .clip(CircleShape)
//                    .background(Brush.linearGradient(GradientGold)),
//                contentAlignment = Alignment.Center
//            ) {
//                val initials = userProfile?.displayName
//                    ?.split(" ")?.take(2)
//                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
//                    ?.joinToString("") ?: "?"
//                Text(
//                    text = initials,
//                    style = MaterialTheme.typography.displaySmall,
//                    fontWeight = FontWeight.ExtraBold,
//                    color = AppColors.heroBackground()
//                )
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                text = userProfile?.displayName ?: "User",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary()
//            )
//            Text(
//                text = userProfile?.email ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                color = AppColors.textSecondary()
//            )
//            if (userProfile?.isVerifiedChef == true) {
//                Spacer(Modifier.height(12.dp))
//                VerifiedChefBadge()
//            }
//
//            Spacer(Modifier.height(40.dp))
//
//            // ── Translation Models ────────────────────────────────────────────
//            SectionCard(
//                title    = "Translation Models",
//                subtitle = "English → other languages • Wi-Fi required",
//                icon     = Icons.Outlined.CloudDownload,
//                expanded = modelsExpanded,
//                onToggle = { modelsExpanded = !modelsExpanded }
//            ) {
//                SUPPORTED_LANGUAGES.forEachIndexed { index, lang ->
//                    LanguageModelRow(
//                        language  = lang,
//                        state     = downloadStates[lang.mlKitCode] ?: ModelDownloadState.NotDownloaded,
//                        onDownload = { downloadModel(lang) },
//                        onDelete   = { deleteModel(lang) }
//                    )
//                    if (index < SUPPORTED_LANGUAGES.lastIndex) {
//                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // ── Language Preference ───────────────────────────────────────────
//            SectionCard(
//                title    = "Preferred Language",
//                subtitle = "Recipes & instructions translated to this language",
//                icon     = Icons.Outlined.Language,
//                expanded = langPrefExpanded,
//                onToggle = { langPrefExpanded = !langPrefExpanded }
//            ) {
//                val ready = SUPPORTED_LANGUAGES.filter {
//                    downloadStates[it.mlKitCode] == ModelDownloadState.Downloaded
//                }
//                if (ready.isEmpty()) {
//                    Text(
//                        text = "Download a language model above to set your preference.",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = AppColors.textSecondary(),
//                        modifier = Modifier.padding(vertical = 12.dp)
//                    )
//                } else {
//                    ready.forEach { lang ->
//                        LanguagePreferenceRow(
//                            language   = lang,
//                            isSelected = selectedLanguage?.mlKitCode == lang.mlKitCode,
//                            onSelect   = { selectedLanguage = lang }
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(40.dp))
//
//            PremiumOutlinedButton(
//                text = "Sign Out",
//                onClick = onSignOut,
//                modifier = Modifier
//                    .fillMaxWidth(0.8f)
//                    .padding(bottom = 48.dp),
//                leadingIcon = {
//                    Icon(
//                        Icons.AutoMirrored.Outlined.Logout,
//                        contentDescription = null,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            )
//        }
//    }
//}
//
//// ── Section Card ──────────────────────────────────────────────────────────────
//
//@Composable
//private fun SectionCard(
//    title: String,
//    subtitle: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    expanded: Boolean,
//    onToggle: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    val arrowRotation by animateFloatAsState(
//        targetValue = if (expanded) 180f else 0f,
//        label = "arrow"
//    )
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//        tonalElevation = 1.dp
//    ) {
//        Column {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(onClick = onToggle)
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(imageVector = icon, contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
//                Spacer(Modifier.width(12.dp))
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(text = title, style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary())
//                    Text(text = subtitle, style = MaterialTheme.typography.labelSmall,
//                        color = AppColors.textSecondary())
//                }
//                Icon(imageVector = Icons.Outlined.ExpandMore, contentDescription = null,
//                    tint = AppColors.textSecondary(),
//                    modifier = Modifier.size(20.dp).rotate(arrowRotation))
//            }
//            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
//                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
//                    content()
//                }
//            }
//        }
//    }
//}
//
//// ── Language Model Row ────────────────────────────────────────────────────────
//
//@Composable
//private fun LanguageModelRow(
//    language: SupportedLanguage,
//    state: ModelDownloadState,
//    onDownload: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 10.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Left: name + dynamic subtitle
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = language.displayName,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    color = AppColors.textPrimary()
//                )
//                val subtitle = when (state) {
//                    is ModelDownloadState.NotDownloaded -> "~${language.estimatedSizeMb} MB"
//                    is ModelDownloadState.Downloading   ->
//                        if (state.totalMb > 0f)
//                            "${"%.1f".format(state.downloadedMb)} MB / ${"%.1f".format(state.totalMb)} MB"
//                        else
//                            "Connecting…"
//                    is ModelDownloadState.Downloaded    -> "On device · ~${language.estimatedSizeMb} MB"
//                    is ModelDownloadState.Error         -> state.message
//                }
//                Text(
//                    text  = subtitle,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = when (state) {
//                        is ModelDownloadState.Error -> MaterialTheme.colorScheme.error
//                        else -> AppColors.textSecondary()
//                    }
//                )
//            }
//
//            Spacer(Modifier.width(12.dp))
//
//            // Right: action / status
//            when (state) {
//                is ModelDownloadState.NotDownloaded ->
//                    IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
//                        Icon(Icons.Outlined.CloudDownload,
//                            contentDescription = "Download ${language.displayName}",
//                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
//                    }
//
//                is ModelDownloadState.Downloading ->
//                    // Show percentage when progress is known
//                    if (state.progress >= 0f) {
//                        Text(
//                            text = "${(state.progress * 100).toInt()}%",
//                            style = MaterialTheme.typography.labelMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//
//                is ModelDownloadState.Downloaded ->
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(Icons.Outlined.CheckCircle, contentDescription = "Downloaded",
//                            tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
//                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
//                            Icon(Icons.Outlined.Delete,
//                                contentDescription = "Delete ${language.displayName}",
//                                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
//                        }
//                    }
//
//                is ModelDownloadState.Error ->
//                    IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
//                        Icon(Icons.Outlined.ErrorOutline, contentDescription = "Retry",
//                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
//                    }
//            }
//        }
//
//        // Progress bar — shown only while downloading
//        if (state is ModelDownloadState.Downloading) {
//            Spacer(Modifier.height(6.dp))
//            if (state.progress >= 0f) {
//                // Determinate: we have real byte counts
//                val animatedProgress by animateFloatAsState(
//                    targetValue = state.progress,
//                    animationSpec = tween(durationMillis = 200),
//                    label = "dl_progress_${language.mlKitCode}"
//                )
//                LinearProgressIndicator(
//                    progress = { animatedProgress },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(6.dp)
//                        .clip(RoundedCornerShape(3.dp)),
//                    strokeCap = StrokeCap.Round,
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            } else {
//                // Indeterminate: size not yet reported by DownloadManager
//                LinearProgressIndicator(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(6.dp)
//                        .clip(RoundedCornerShape(3.dp)),
//                    strokeCap = StrokeCap.Round,
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            }
//        }
//    }
//}
//
//// ── Language Preference Row ───────────────────────────────────────────────────
//
//@Composable
//private fun LanguagePreferenceRow(
//    language: SupportedLanguage,
//    isSelected: Boolean,
//    onSelect: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onSelect)
//            .padding(vertical = 4.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        RadioButton(
//            selected = isSelected,
//            onClick  = onSelect,
//            colors   = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
//        )
//        Spacer(Modifier.width(8.dp))
//        Column {
//            Text(
//                text = language.displayName,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
//                color = if (isSelected) MaterialTheme.colorScheme.primary else AppColors.textPrimary()
//            )
//            if (isSelected) {
//                Text(
//                    text  = "Active – recipes will be translated to this language",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
//                )
//            }
//        }
//    }
//}
//
//// ── Preview ───────────────────────────────────────────────────────────────────
//
//@Preview(showBackground = true, showSystemUi = true)
//@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//private fun ProfileScreenPreview() {
//    SousChefTheme {
//        ProfileScreen(
//            userProfile = UserProfile(
//                displayName = "Hemang Mishra",
//                email       = "hemang@souschef.com",
//                role        = "user"
//            ),
//            onSignOut = {}
//        )
//    }
//}