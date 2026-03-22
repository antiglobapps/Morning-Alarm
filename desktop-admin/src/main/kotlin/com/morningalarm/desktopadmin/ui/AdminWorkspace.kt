package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.morningalarm.desktopadmin.data.AdminApiClient
import com.morningalarm.desktopadmin.data.ApiClientException
import com.morningalarm.desktopadmin.data.SessionExpiredException
import com.morningalarm.desktopadmin.media.DesktopMediaPlaybackController
import com.morningalarm.desktopadmin.media.toPlayableMediaOrNull
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityRequestDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal fun DesktopAdminWorkspace(
    session: AdminSession,
    onLogout: (message: String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apiClient = remember(session.baseUrl) { AdminApiClient(session.baseUrl) }
    val mediaPlaybackController = remember { DesktopMediaPlaybackController() }
    val playbackState by mediaPlaybackController.state.collectAsState()
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

    DisposableEffect(mediaPlaybackController) {
        onDispose {
            mediaPlaybackController.close()
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

            val livePreview = draft.toPreview(previousPreview = selectedPreview)
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF141C28), RoundedCornerShape(20.dp)).padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (draft.id == null) "Create Ringtone" else "Edit Ringtone",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
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
                                        draft = draft.copy(
                                            audioUrl = media.url,
                                            durationSeconds = media.durationSeconds?.toString() ?: draft.durationSeconds,
                                        )
                                        val message = if (media.durationSeconds != null) {
                                            "Audio uploaded: ${media.fileName}. Duration: ${media.durationSeconds}s"
                                        } else {
                                            "Audio uploaded: ${media.fileName}. Enter duration manually."
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            }
                        },
                        onSave = {
                            scope.launch {
                                execute {
                                    val request = draft.toCreateOrUpdateRequest()
                                    val ringtoneId = draft.id
                                    val saved = if (ringtoneId == null) {
                                        apiClient.createRingtone(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            request.toCreateRequest(),
                                        )
                                    } else {
                                        apiClient.updateRingtone(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
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
                                        val ringtoneId = draft.id ?: throw ApiClientException("Save ringtone first")
                                        apiClient.deleteRingtone(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
                                        )
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
                                        val ringtoneId = draft.id ?: throw ApiClientException("Save ringtone first")
                                        val result = apiClient.setVisibility(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
                                            SetRingtoneVisibilityRequestDto(targetVisibility),
                                        )
                                        draft = draft.copy(visibility = result.visibility)
                                        reloadList()
                                        selectedPreview = apiClient.getPreview(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
                                        )
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
                                        val ringtoneId = draft.id ?: throw ApiClientException("Save ringtone first")
                                        val result = apiClient.togglePremium(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
                                        )
                                        draft = draft.copy(isPremium = result.isPremium)
                                        reloadList()
                                        selectedPreview = apiClient.getPreview(
                                            session.authSession.bearerToken,
                                            session.adminSecret,
                                            ringtoneId,
                                        )
                                        reloadClientPreview()
                                    }
                                }
                            }
                        } else {
                            null
                        },
                    )
                }

                Column(
                    modifier = Modifier.width(420.dp).fillMaxHeight().background(Color(0xFF141C28), RoundedCornerShape(20.dp)).padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Live Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Карточка обновляется по мере ввода. Лайки берутся из последнего сохранённого состояния.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                        )
                        PreviewCard(
                            item = livePreview,
                            playbackState = playbackState,
                            playableMedia = draft.audioUrl.toPlayableMediaOrNull(livePreview.title),
                            onTogglePlayback = mediaPlaybackController::toggle,
                        )
                    }

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Client View Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (clientPreviewItems.isEmpty()) {
                            Text("No client-visible ringtones yet.", color = Color(0xFF94A3B8))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                clientPreviewItems.forEach { item ->
                                    PreviewCard(
                                        item = item,
                                        playbackState = playbackState,
                                        playableMedia = item.audioUrl.toPlayableMediaOrNull(item.title),
                                        onTogglePlayback = mediaPlaybackController::toggle,
                                    )
                                }
                            }
                        }
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

private fun chooseFile(title: String, extensions: Array<String>): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = title
        fileFilter = FileNameExtensionFilter(title, *extensions)
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}
