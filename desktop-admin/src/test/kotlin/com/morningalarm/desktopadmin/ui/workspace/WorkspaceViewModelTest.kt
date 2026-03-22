package com.morningalarm.desktopadmin.ui.workspace

import com.morningalarm.desktopadmin.FakeAdminRingtoneRepository
import com.morningalarm.desktopadmin.data.SessionExpiredException
import com.morningalarm.desktopadmin.testRingtoneDetail
import com.morningalarm.desktopadmin.testRingtoneListItem
import com.morningalarm.desktopadmin.ui.RingtoneDraft
import com.morningalarm.desktopadmin.ui.toDraft
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.test
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkspaceViewModelTest {

    private val fakeRepo = FakeAdminRingtoneRepository()

    private fun createViewModel() = WorkspaceViewModel(
        repository = fakeRepo,
        baseUrl = "http://localhost:8080",
    )

    @Test
    fun `onCreate loads ringtones and client preview`() = runTest {
        createViewModel().test(this) {
            runOnCreate()

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectState { copy(isBusy = false) }
        }
    }

    @Test
    fun `refresh reloads ringtones and client preview`() = runTest {
        createViewModel().test(this) {
            containerHost.refresh()

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectState { copy(isBusy = false) }
        }
    }

    @Test
    fun `updateSearchQuery changes search query`() = runTest {
        createViewModel().test(this) {
            containerHost.updateSearchQuery("sunrise")

            expectState { copy(searchQuery = "sunrise") }
        }
    }

    @Test
    fun `selectRingtone loads detail and updates state`() = runTest {
        val detail = testRingtoneDetail()

        createViewModel().test(this) {
            containerHost.selectRingtone("ringtone-1")

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    selectedId = detail.id,
                    draft = detail.toDraft(),
                    selectedPreview = detail.preview,
                )
            }
            expectState { copy(isBusy = false) }
        }
    }

    @Test
    fun `newRingtone resets draft and selection`() = runTest {
        createViewModel().test(this,
            initialState = WorkspaceState(
                selectedId = "ringtone-1",
                draft = testRingtoneDetail().toDraft(),
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.newRingtone()

            expectState {
                copy(
                    selectedId = null,
                    draft = RingtoneDraft(),
                    selectedPreview = null,
                )
            }
        }
    }

    @Test
    fun `updateDraft changes draft`() = runTest {
        createViewModel().test(this) {
            val newDraft = RingtoneDraft(title = "New Title")
            containerHost.updateDraft(newDraft)

            expectState { copy(draft = newDraft) }
        }
    }

    @Test
    fun `save creates ringtone when draft has no id`() = runTest {
        val detail = testRingtoneDetail()

        createViewModel().test(this,
            initialState = WorkspaceState(
                draft = RingtoneDraft(
                    title = "New Ringtone",
                    description = "Description",
                    imageUrl = "https://example.com/img.jpg",
                    audioUrl = "https://example.com/audio.mp3",
                    durationSeconds = "30",
                ),
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.save()

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    draft = detail.toDraft(),
                    selectedId = detail.id,
                    selectedPreview = detail.preview,
                )
            }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Ringtone saved"))
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.createCallCount)
    }

    @Test
    fun `save updates ringtone when draft has id`() = runTest {
        val detail = testRingtoneDetail()

        createViewModel().test(this,
            initialState = WorkspaceState(
                draft = detail.toDraft(),
                selectedId = detail.id,
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.save()

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    draft = detail.toDraft(),
                    selectedId = detail.id,
                    selectedPreview = detail.preview,
                )
            }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Ringtone saved"))
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.updateCallCount)
    }

    @Test
    fun `delete removes ringtone and resets draft`() = runTest {
        val detail = testRingtoneDetail()

        createViewModel().test(this,
            initialState = WorkspaceState(
                draft = detail.toDraft(),
                selectedId = detail.id,
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.delete()

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    selectedId = null,
                    draft = RingtoneDraft(),
                    selectedPreview = null,
                )
            }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Ringtone deleted"))
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.deleteCallCount)
    }

    @Test
    fun `delete does nothing when no ringtone selected`() = runTest {
        createViewModel().test(this) {
            containerHost.delete()
            // No state changes or side effects expected
        }

        assertEquals(0, fakeRepo.deleteCallCount)
    }

    @Test
    fun `setVisibility updates visibility in draft`() = runTest {
        val detail = testRingtoneDetail(visibility = RingtoneVisibilityDto.PUBLIC)

        createViewModel().test(this,
            initialState = WorkspaceState(
                draft = detail.toDraft(),
                selectedId = detail.id,
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.setVisibility(RingtoneVisibilityDto.PRIVATE)

            expectState { copy(isBusy = true) }
            expectState { copy(draft = draft.copy(visibility = RingtoneVisibilityDto.PRIVATE)) }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectState { copy(selectedPreview = detail.preview) }
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.setVisibilityCallCount)
    }

    @Test
    fun `togglePremium flips premium flag`() = runTest {
        val detail = testRingtoneDetail(isPremium = false)

        createViewModel().test(this,
            initialState = WorkspaceState(
                draft = detail.toDraft(),
                selectedId = detail.id,
                baseUrl = "http://localhost:8080",
            ),
        ) {
            containerHost.togglePremium()

            expectState { copy(isBusy = true) }
            expectState { copy(draft = draft.copy(isPremium = true)) }
            expectState {
                copy(
                    ringtones = fakeRepo.ringtones,
                    clientPreviewItems = fakeRepo.clientPreview,
                )
            }
            expectState { copy(selectedPreview = detail.preview) }
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.togglePremiumCallCount)
    }

    @Test
    fun `uploadImage updates draft imageUrl`() = runTest {
        createViewModel().test(this) {
            containerHost.uploadImage(File("photo.jpg"))

            expectState { copy(isBusy = true) }
            expectState { copy(draft = draft.copy(imageUrl = "https://storage.example.com/uploaded-image.jpg")) }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Image uploaded: photo.jpg"))
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.uploadImageCallCount)
    }

    @Test
    fun `uploadAudio updates draft audioUrl and duration`() = runTest {
        createViewModel().test(this) {
            containerHost.uploadAudio(File("song.mp3"))

            expectState { copy(isBusy = true) }
            expectState {
                copy(
                    draft = draft.copy(
                        audioUrl = "https://storage.example.com/uploaded-audio.mp3",
                        durationSeconds = "45",
                    ),
                )
            }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Audio uploaded: song.mp3. Duration: 45s"))
            expectState { copy(isBusy = false) }
        }

        assertEquals(1, fakeRepo.uploadAudioCallCount)
    }

    @Test
    fun `error during refresh shows snackbar`() = runTest {
        fakeRepo.shouldFail = RuntimeException("Network error")

        createViewModel().test(this) {
            containerHost.refresh()

            expectState { copy(isBusy = true) }
            expectSideEffect(WorkspaceSideEffect.ShowSnackbar("Network error"))
            expectState { copy(isBusy = false) }
        }
    }

    @Test
    fun `session expired triggers logout side effect`() = runTest {
        fakeRepo.shouldFail = SessionExpiredException()

        createViewModel().test(this) {
            containerHost.refresh()

            expectState { copy(isBusy = true) }
            expectSideEffect(WorkspaceSideEffect.SessionExpired("Сессия истекла. Войдите снова."))
            expectState { copy(isBusy = false) }
        }
    }
}
