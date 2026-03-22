package com.morningalarm.desktopadmin.ui.workspace

import androidx.lifecycle.ViewModel
import com.morningalarm.desktopadmin.data.AdminRingtoneRepository
import com.morningalarm.desktopadmin.data.SessionExpiredException
import com.morningalarm.desktopadmin.ui.RingtoneDraft
import com.morningalarm.desktopadmin.ui.toCreateOrUpdateRequest
import com.morningalarm.desktopadmin.ui.toCreateRequest
import com.morningalarm.desktopadmin.ui.toDraft
import com.morningalarm.desktopadmin.ui.toUpdateRequest
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import com.morningalarm.dto.upload.UploadedMediaDto
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.io.File

internal data class WorkspaceState(
    val ringtones: List<AdminRingtoneListItemDto> = emptyList(),
    val searchQuery: String = "",
    val selectedId: String? = null,
    val draft: RingtoneDraft = RingtoneDraft(),
    val selectedPreview: RingtoneListItemDto? = null,
    val clientPreviewItems: List<RingtoneListItemDto> = emptyList(),
    val isBusy: Boolean = false,
    val baseUrl: String = "",
)

internal sealed interface WorkspaceSideEffect {
    data class ShowSnackbar(val message: String) : WorkspaceSideEffect
    data class SessionExpired(val message: String) : WorkspaceSideEffect
}

internal class WorkspaceViewModel(
    private val repository: AdminRingtoneRepository,
    baseUrl: String,
) : ViewModel(), ContainerHost<WorkspaceState, WorkspaceSideEffect> {

    override val container = container<WorkspaceState, WorkspaceSideEffect>(
        WorkspaceState(baseUrl = baseUrl),
    ) {
        loadInitialData()
    }

    private fun loadInitialData() = intent {
        executeWithErrorHandling {
            val items = repository.listRingtones()
            val clientItems = repository.getClientPreview()
            reduce { state.copy(ringtones = items, clientPreviewItems = clientItems) }
        }
    }

    fun refresh() = intent {
        executeWithErrorHandling {
            val items = repository.listRingtones()
            val clientItems = repository.getClientPreview()
            reduce { state.copy(ringtones = items, clientPreviewItems = clientItems) }
        }
    }

    fun updateSearchQuery(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }

    fun selectRingtone(ringtoneId: String) = intent {
        executeWithErrorHandling {
            val detail = repository.getRingtoneDetail(ringtoneId)
            reduce {
                state.copy(
                    selectedId = detail.id,
                    draft = detail.toDraft(),
                    selectedPreview = detail.preview,
                )
            }
        }
    }

    fun newRingtone() = intent {
        reduce {
            state.copy(
                selectedId = null,
                draft = RingtoneDraft(),
                selectedPreview = null,
            )
        }
    }

    fun updateDraft(draft: RingtoneDraft) = intent {
        reduce { state.copy(draft = draft) }
    }

    fun save() = intent {
        executeWithErrorHandling {
            val request = state.draft.toCreateOrUpdateRequest()
            val ringtoneId = state.draft.id
            val saved = if (ringtoneId == null) {
                repository.createRingtone(request.toCreateRequest())
            } else {
                repository.updateRingtone(ringtoneId, request.toUpdateRequest())
            }
            reduce {
                state.copy(
                    draft = saved.toDraft(),
                    selectedId = saved.id,
                    selectedPreview = saved.preview,
                )
            }
            reloadLists()
            postSideEffect(WorkspaceSideEffect.ShowSnackbar("Ringtone saved"))
        }
    }

    fun delete() = intent {
        val ringtoneId = state.draft.id ?: return@intent
        executeWithErrorHandling {
            repository.deleteRingtone(ringtoneId)
            reduce {
                state.copy(
                    selectedId = null,
                    draft = RingtoneDraft(),
                    selectedPreview = null,
                )
            }
            reloadLists()
            postSideEffect(WorkspaceSideEffect.ShowSnackbar("Ringtone deleted"))
        }
    }

    fun setVisibility(visibility: RingtoneVisibilityDto) = intent {
        val ringtoneId = state.draft.id ?: return@intent
        executeWithErrorHandling {
            val result = repository.setVisibility(ringtoneId, visibility)
            reduce { state.copy(draft = state.draft.copy(visibility = result.visibility)) }
            reloadLists()
            val preview = repository.getPreview(ringtoneId)
            reduce { state.copy(selectedPreview = preview) }
        }
    }

    fun togglePremium() = intent {
        val ringtoneId = state.draft.id ?: return@intent
        executeWithErrorHandling {
            val result = repository.togglePremium(ringtoneId)
            reduce { state.copy(draft = state.draft.copy(isPremium = result.isPremium)) }
            reloadLists()
            val preview = repository.getPreview(ringtoneId)
            reduce { state.copy(selectedPreview = preview) }
        }
    }

    fun uploadImage(file: File) = intent {
        executeWithErrorHandling {
            val media = repository.uploadImage(file)
            reduce { state.copy(draft = state.draft.copy(imageUrl = media.url)) }
            postSideEffect(WorkspaceSideEffect.ShowSnackbar("Image uploaded: ${media.fileName}"))
        }
    }

    fun uploadAudio(file: File) = intent {
        executeWithErrorHandling {
            val media = repository.uploadAudio(file)
            reduce {
                state.copy(
                    draft = state.draft.copy(
                        audioUrl = media.url,
                        durationSeconds = media.durationSeconds?.toString() ?: state.draft.durationSeconds,
                    ),
                )
            }
            val message = if (media.durationSeconds != null) {
                "Audio uploaded: ${media.fileName}. Duration: ${media.durationSeconds}s"
            } else {
                "Audio uploaded: ${media.fileName}. Enter duration manually."
            }
            postSideEffect(WorkspaceSideEffect.ShowSnackbar(message))
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }

    private suspend fun IntentScope.reloadLists() {
        val items = repository.listRingtones()
        val clientItems = repository.getClientPreview()
        reduce { state.copy(ringtones = items, clientPreviewItems = clientItems) }
    }

    private suspend fun IntentScope.executeWithErrorHandling(action: suspend IntentScope.() -> Unit) {
        reduce { state.copy(isBusy = true) }
        try {
            action()
        } catch (_: SessionExpiredException) {
            postSideEffect(WorkspaceSideEffect.SessionExpired("Сессия истекла. Войдите снова."))
        } catch (error: Exception) {
            postSideEffect(WorkspaceSideEffect.ShowSnackbar(error.message ?: "Unexpected error"))
        } finally {
            reduce { state.copy(isBusy = false) }
        }
    }
}

private typealias IntentScope = org.orbitmvi.orbit.syntax.Syntax<WorkspaceState, WorkspaceSideEffect>
