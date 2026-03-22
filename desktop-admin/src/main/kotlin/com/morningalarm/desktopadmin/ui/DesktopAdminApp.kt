package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.data.AdminApiClient
import com.morningalarm.desktopadmin.data.ApiClientException
import com.morningalarm.desktopadmin.data.SessionExpiredException
import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.auth.AuthSessionDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private data class AdminSession(
    val baseUrl: String,
    val adminSecret: String,
    val authSession: AuthSessionDto,
)

private data class RingtoneDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val durationSeconds: String = "30",
    val visibility: RingtoneVisibilityDto = RingtoneVisibilityDto.INACTIVE,
    val isPremium: Boolean = false,
    val createdByUserId: String? = null,
)

@Composable
fun DesktopAdminApp() {
    val preferences = remember { AppPreferences() }
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf<AdminSession?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginInProgress by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFB86B),
            secondary = Color(0xFF9BD1FF),
            background = Color(0xFF10151F),
            surface = Color(0xFF17202F),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (session == null) {
                LoginScreen(
                    initialBaseUrl = preferences.baseUrl,
                    errorMessage = loginError,
                    inProgress = loginInProgress,
                    onLogin = { baseUrl, email, password, adminSecret ->
                        scope.launch {
                            loginError = null
                            loginInProgress = true
                            val client = AdminApiClient(baseUrl)
                            runCatching {
                                preferences.baseUrl = baseUrl
                                val authSession = client.adminLogin(email, password, adminSecret)
                                session = AdminSession(
                                    baseUrl = baseUrl,
                                    adminSecret = adminSecret,
                                    authSession = authSession,
                                )
                            }.onFailure { error ->
                                loginError = error.message ?: "Login failed"
                            }
                            client.close()
                            loginInProgress = false
                        }
                    },
                )
            } else {
                DesktopAdminWorkspace(
                    session = session!!,
                    onLogout = { message ->
                        loginError = message
                        session = null
                    },
                )
            }
        }
    }
}

@Composable
private fun LoginScreen(
    initialBaseUrl: String,
    errorMessage: String?,
    inProgress: Boolean,
    onLogin: (baseUrl: String, email: String, password: String, adminSecret: String) -> Unit,
) {
    var baseUrl by rememberSaveable { mutableStateOf(initialBaseUrl) }
    var email by rememberSaveable { mutableStateOf("admin@example.com") }
    var password by rememberSaveable { mutableStateOf("") }
    var adminSecret by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F141D)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.width(440.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17202F)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Morning Alarm Admin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Вход для администратора и конфигурация server base URL.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB4C0D4),
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server Base URL") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = adminSecret,
                    onValueChange = { adminSecret = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Admin Secret") },
                    singleLine = true,
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { onLogin(baseUrl.trim(), email.trim(), password, adminSecret.trim()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !inProgress,
                ) {
                    if (inProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Log In")
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopAdminWorkspace(
    session: AdminSession,
    onLogout: (message: String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apiClient = remember(session.baseUrl) { AdminApiClient(session.baseUrl) }
    val ringtoneItems = remember { mutableStateListOf<AdminRingtoneListItemDto>() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf(RingtoneDraft()) }
    var selectedPreview by remember { mutableStateOf<RingtoneListItemDto?>(null) }
    var clientPreviewItems by remember { mutableStateOf<List<RingtoneListItemDto>>(emptyList()) }
    var isBusy by remember { mutableStateOf(false) }

    DisposableEffect(apiClient) {
        onDispose {
            apiClient.close()
        }
    }

    fun resetDraft() {
        selectedId = null
        draft = RingtoneDraft()
        selectedPreview = null
    }

    suspend fun execute(action: suspend () -> Unit) {
        isBusy = true
        try {
            action()
        } catch (_: SessionExpiredException) {
            onLogout("Сессия истекла. Войдите снова.")
        } catch (error: ApiClientException) {
            snackbarHostState.showSnackbar(error.message)
        } catch (error: Exception) {
            snackbarHostState.showSnackbar(error.message ?: "Unexpected error")
        } finally {
            isBusy = false
        }
    }

    suspend fun reloadList() {
        val items = apiClient.listRingtones(session.authSession.bearerToken, session.adminSecret)
        ringtoneItems.clear()
        ringtoneItems.addAll(items)
    }

    suspend fun loadDetail(ringtoneId: String) {
        val detail = apiClient.getRingtoneDetail(session.authSession.bearerToken, session.adminSecret, ringtoneId)
        selectedId = detail.id
        draft = detail.toDraft()
        selectedPreview = detail.preview
    }

    suspend fun reloadClientPreview() {
        clientPreviewItems = apiClient.getClientPreview(session.authSession.bearerToken, session.adminSecret)
    }

    LaunchedEffect(session.baseUrl) {
        execute {
            reloadList()
            reloadClientPreview()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF131C2A)).padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Desktop Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        session.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { scope.launch { execute { reloadList(); reloadClientPreview() } } }) {
                        Text("Refresh")
                    }
                    TextButton(onClick = { resetDraft() }) {
                        Text("New Ringtone")
                    }
                    TextButton(onClick = { onLogout(null) }) {
                        Text("Logout")
                    }
                }
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.width(360.dp).fillMaxHeight().background(Color(0xFF141C28), RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search or filter") },
                    singleLine = true,
                )
                Text(
                    "All ringtones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val filteredItems = ringtoneItems.filter {
                    searchQuery.isBlank() ||
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                }
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    filteredItems.forEach { item ->
                        RingtoneListRow(
                            item = item,
                            isSelected = item.id == selectedId,
                            onClick = {
                                scope.launch {
                                    execute {
                                        loadDetail(item.id)
                                    }
                                }
                            },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF141C28), RoundedCornerShape(20.dp)).padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            if (draft.id == null) "Create Ringtone" else "Edit Ringtone",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Shared contracts from `shared`, centralized auth and admin API usage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                        )
                    }
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                    }
                }

                RingtoneForm(
                    draft = draft,
                    onDraftChange = { draft = it },
                    onUploadImage = {
                        chooseFile("Choose image", arrayOf("jpg", "jpeg", "png", "webp", "gif"))?.let { file ->
                            scope.launch {
                                execute {
                                    val media = apiClient.uploadImage(session.authSession.bearerToken, session.adminSecret, file)
                                    draft = draft.copy(imageUrl = media.url)
                                    snackbarHostState.showSnackbar("Image uploaded: ${media.fileName}")
                                }
                            }
                        }
                    },
                    onUploadAudio = {
                        chooseFile("Choose audio", arrayOf("mp3", "wav", "ogg", "m4a"))?.let { file ->
                            scope.launch {
                                execute {
                                    val media = apiClient.uploadAudio(session.authSession.bearerToken, session.adminSecret, file)
                                    draft = draft.copy(audioUrl = media.url)
                                    snackbarHostState.showSnackbar("Audio uploaded: ${media.fileName}")
                                }
                            }
                        }
                    },
                    onSave = {
                        scope.launch {
                            execute {
                                val request = draft.toCreateOrUpdateRequest()
                                val saved = if (draft.id == null) {
                                    apiClient.createRingtone(
                                        session.authSession.bearerToken,
                                        session.adminSecret,
                                        request.toCreateRequest(),
                                    )
                                } else {
                                    apiClient.updateRingtone(
                                        session.authSession.bearerToken,
                                        session.adminSecret,
                                        draft.id,
                                        request.toUpdateRequest(),
                                    )
                                }
                                draft = saved.toDraft()
                                selectedId = saved.id
                                selectedPreview = saved.preview
                                reloadList()
                                reloadClientPreview()
                                snackbarHostState.showSnackbar("Ringtone saved")
                            }
                        }
                    },
                    onDelete = if (draft.id != null) {
                        {
                            scope.launch {
                                execute {
                                    apiClient.deleteRingtone(session.authSession.bearerToken, session.adminSecret, draft.id)
                                    resetDraft()
                                    reloadList()
                                    reloadClientPreview()
                                    snackbarHostState.showSnackbar("Ringtone deleted")
                                }
                            }
                        }
                    } else {
                        null
                    },
                    onSetVisibility = if (draft.id != null) {
                        { targetVisibility ->
                            scope.launch {
                                execute {
                                    val result = apiClient.setVisibility(
                                        session.authSession.bearerToken,
                                        session.adminSecret,
                                        draft.id,
                                        SetRingtoneVisibilityRequestDto(targetVisibility),
                                    )
                                    draft = draft.copy(visibility = result.visibility)
                                    reloadList()
                                    if (draft.id != null) {
                                        selectedPreview = apiClient.getPreview(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            draft.id,
                                        )
                                    }
                                    reloadClientPreview()
                                }
                            }
                        }
                    } else {
                        null
                    },
                    onTogglePremium = if (draft.id != null) {
                        {
                            scope.launch {
                                execute {
                                    val result = apiClient.togglePremium(session.authSession.bearerToken, session.adminSecret, draft.id)
                                    draft = draft.copy(isPremium = result.isPremium)
                                    reloadList()
                                    if (draft.id != null) {
                                        selectedPreview = apiClient.getPreview(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            draft.id,
                                        )
                                    }
                                    reloadClientPreview()
                                }
                            }
                        }
                    } else {
                        null
                    },
                    onRefreshPreview = if (draft.id != null) {
                        {
                            scope.launch {
                                execute {
                                    selectedPreview = apiClient.getPreview(session.authSession.bearerToken, session.adminSecret, draft.id)
                                    reloadClientPreview()
                                }
                            }
                        }
                    } else {
                        null
                    },
                )

                HorizontalDivider()

                Text("Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                selectedPreview?.let {
                    PreviewCard(it)
                } ?: Text("Save ringtone first to see the exact client-facing preview.", color = Color(0xFF94A3B8))

                HorizontalDivider()

                Text("Client View Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (clientPreviewItems.isEmpty()) {
                    Text("No client-visible ringtones yet.", color = Color(0xFF94A3B8))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        clientPreviewItems.forEach { PreviewCard(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RingtoneListRow(
    item: AdminRingtoneListItemDto,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0xFFFFB86B) else Color(0xFF263447)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(item.title, fontWeight = FontWeight.SemiBold)
        Text(item.description, color = Color(0xFF94A3B8), maxLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val visibilityLabel = when (item.visibility) {
                RingtoneVisibilityDto.PUBLIC -> "Public"
                RingtoneVisibilityDto.PRIVATE -> "Private"
                RingtoneVisibilityDto.INACTIVE -> "Inactive"
            }
            val visibilityColor = when (item.visibility) {
                RingtoneVisibilityDto.PUBLIC -> Color(0xFF1F7A4D)
                RingtoneVisibilityDto.PRIVATE -> Color(0xFF5C3B00)
                RingtoneVisibilityDto.INACTIVE -> Color(0xFF4A4A4A)
            }
            Badge(visibilityLabel, visibilityColor)
            if (item.isPremium) Badge("Premium", Color(0xFF334E8D))
            if (item.createdByUserId != null) Badge("User-created", Color(0xFF6B3FA0))
            Badge("${item.likesCount} likes", Color(0xFF2C3443))
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier.background(color, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RingtoneForm(
    draft: RingtoneDraft,
    onDraftChange: (RingtoneDraft) -> Unit,
    onUploadImage: () -> Unit,
    onUploadAudio: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onSetVisibility: ((RingtoneVisibilityDto) -> Unit)?,
    onTogglePremium: (() -> Unit)?,
    onRefreshPreview: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = draft.title,
            onValueChange = { onDraftChange(draft.copy(title = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
        )
        OutlinedTextField(
            value = draft.description,
            onValueChange = { onDraftChange(draft.copy(description = it)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            label = { Text("Description") },
        )
        OutlinedTextField(
            value = draft.imageUrl,
            onValueChange = { onDraftChange(draft.copy(imageUrl = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Image URL") },
        )
        OutlinedTextField(
            value = draft.audioUrl,
            onValueChange = { onDraftChange(draft.copy(audioUrl = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Audio URL") },
        )
        OutlinedTextField(
            value = draft.durationSeconds,
            onValueChange = { onDraftChange(draft.copy(durationSeconds = it.filter(Char::isDigit))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Duration Seconds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Visibility:")
            RingtoneVisibilityDto.entries.forEach { visibility ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = draft.visibility == visibility,
                        onCheckedChange = { if (it) onDraftChange(draft.copy(visibility = visibility)) },
                    )
                    Text(visibility.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = draft.isPremium,
                    onCheckedChange = { onDraftChange(draft.copy(isPremium = it)) },
                )
                Text("Premium")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onUploadImage, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234B78))) {
                Text("Upload Image")
            }
            Button(onClick = onUploadAudio, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF375F3A))) {
                Text("Upload Audio")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSave) {
                Text(if (draft.id == null) "Create" else "Save")
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
            if (onSetVisibility != null) {
                RingtoneVisibilityDto.entries
                    .filter { it != draft.visibility }
                    .forEach { targetVisibility ->
                        TextButton(onClick = { onSetVisibility(targetVisibility) }) {
                            Text("Set ${targetVisibility.name.lowercase().replaceFirstChar { it.uppercase() }}")
                        }
                    }
            }
            if (onTogglePremium != null) {
                TextButton(onClick = onTogglePremium) {
                    Text(if (draft.isPremium) "Unset Premium" else "Set Premium")
                }
            }
            if (onRefreshPreview != null) {
                TextButton(onClick = onRefreshPreview) {
                    Text("Refresh Preview")
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(item: RingtoneListItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192230)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(item.description, color = Color(0xFFB4C0D4))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.isPremium) Badge("Premium", Color(0xFF334E8D))
                Badge("${item.durationSeconds}s", Color(0xFF2C3443))
                Badge("${item.likesCount} likes", Color(0xFF2C3443))
            }
            Text("Image: ${item.imageUrl}", color = Color(0xFF94A3B8))
            Text("Audio: ${item.audioUrl}", color = Color(0xFF94A3B8))
        }
    }
}

private fun chooseFile(title: String, extensions: Array<String>): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = title
        fileFilter = FileNameExtensionFilter(title, *extensions)
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

private fun AdminRingtoneDetailDto.toDraft(): RingtoneDraft = RingtoneDraft(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds.toString(),
    visibility = visibility,
    isPremium = isPremium,
    createdByUserId = createdByUserId,
)

private data class RingtoneRequestData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val visibility: RingtoneVisibilityDto,
    val isPremium: Boolean,
)

private fun RingtoneDraft.toCreateOrUpdateRequest(): RingtoneRequestData {
    val duration = durationSeconds.toIntOrNull()
        ?: throw ApiClientException("Duration must be a valid integer")
    return RingtoneRequestData(
        title = title.trim(),
        description = description.trim(),
        imageUrl = imageUrl.trim(),
        audioUrl = audioUrl.trim(),
        durationSeconds = duration,
        visibility = visibility,
        isPremium = isPremium,
    )
}

private fun RingtoneRequestData.toCreateRequest(): CreateAdminRingtoneRequestDto = CreateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    visibility = visibility,
    isPremium = isPremium,
)

private fun RingtoneRequestData.toUpdateRequest(): UpdateAdminRingtoneRequestDto = UpdateAdminRingtoneRequestDto(
    title = title,
    description = description,
    imageUrl = imageUrl,
    audioUrl = audioUrl,
    durationSeconds = durationSeconds,
    visibility = visibility,
    isPremium = isPremium,
)
