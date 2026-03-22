package com.morningalarm.desktopadmin.ui.workspace

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.morningalarm.desktopadmin.media.DesktopMediaPlaybackController
import com.morningalarm.desktopadmin.media.toPlayableMediaOrNull
import com.morningalarm.desktopadmin.ui.AdminSession
import com.morningalarm.desktopadmin.ui.RingtoneDraft
import com.morningalarm.desktopadmin.ui.components.Badge
import com.morningalarm.desktopadmin.ui.components.PreviewCard
import com.morningalarm.desktopadmin.ui.components.RingtoneForm
import com.morningalarm.desktopadmin.ui.toPreview
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal fun DesktopAdminWorkspace(
    session: AdminSession,
    onLogout: (message: String?) -> Unit,
) {
    val viewModel = koinInject<WorkspaceViewModel> { parametersOf(session) }
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val mediaPlaybackController = remember { DesktopMediaPlaybackController() }
    val playbackState by mediaPlaybackController.state.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WorkspaceSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(sideEffect.message)
            is WorkspaceSideEffect.SessionExpired -> onLogout(sideEffect.message)
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
                        state.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Refresh")
                    }
                    TextButton(onClick = { viewModel.newRingtone() }) {
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
            // Left panel: ringtone list
            RingtoneListPanel(
                ringtones = state.ringtones,
                searchQuery = state.searchQuery,
                selectedId = state.selectedId,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSelectRingtone = viewModel::selectRingtone,
            )

            val livePreview = state.draft.toPreview(previousPreview = state.selectedPreview)
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Center panel: editor
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
                            if (state.draft.id == null) "Create Ringtone" else "Edit Ringtone",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (state.isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                        }
                    }

                    RingtoneForm(
                        draft = state.draft,
                        onDraftChange = viewModel::updateDraft,
                        onUploadImage = {
                            chooseFile("Choose image", arrayOf("jpg", "jpeg", "png", "webp", "gif"))?.let { file ->
                                viewModel.uploadImage(file)
                            }
                        },
                        onUploadAudio = {
                            chooseFile("Choose audio", arrayOf("mp3", "wav", "ogg", "m4a"))?.let { file ->
                                viewModel.uploadAudio(file)
                            }
                        },
                        onSave = { viewModel.save() },
                        onDelete = if (state.draft.id != null) {
                            { viewModel.delete() }
                        } else {
                            null
                        },
                        onSetVisibility = if (state.draft.id != null) {
                            { visibility -> viewModel.setVisibility(visibility) }
                        } else {
                            null
                        },
                        onTogglePremium = if (state.draft.id != null) {
                            { viewModel.togglePremium() }
                        } else {
                            null
                        },
                    )
                }

                // Right panel: preview
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
                            playableMedia = state.draft.audioUrl.toPlayableMediaOrNull(livePreview.title),
                            onTogglePlayback = mediaPlaybackController::toggle,
                        )
                    }

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Client View Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (state.clientPreviewItems.isEmpty()) {
                            Text("No client-visible ringtones yet.", color = Color(0xFF94A3B8))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.clientPreviewItems.forEach { item ->
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
private fun RingtoneListPanel(
    ringtones: List<AdminRingtoneListItemDto>,
    searchQuery: String,
    selectedId: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectRingtone: (String) -> Unit,
) {
    Column(
        modifier = Modifier.width(360.dp).fillMaxHeight().background(Color(0xFF141C28), RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search or filter") },
            singleLine = true,
        )
        Text(
            "All ringtones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        val filteredItems = ringtones.filter {
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
                    onClick = { onSelectRingtone(item.id) },
                )
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
