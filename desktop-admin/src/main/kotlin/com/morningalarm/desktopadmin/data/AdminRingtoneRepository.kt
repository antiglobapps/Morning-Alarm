package com.morningalarm.desktopadmin.data

import com.morningalarm.dto.admin.ringtone.AdminRingtoneDetailDto
import com.morningalarm.dto.admin.ringtone.AdminRingtoneListItemDto
import com.morningalarm.dto.admin.ringtone.CreateAdminRingtoneRequestDto
import com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityRequestDto
import com.morningalarm.dto.admin.ringtone.UpdateAdminRingtoneRequestDto
import com.morningalarm.dto.ringtone.RingtoneListItemDto
import com.morningalarm.dto.ringtone.RingtoneVisibilityDto
import com.morningalarm.dto.upload.UploadedMediaDto
import java.io.File

internal interface AdminRingtoneRepository {
    suspend fun listRingtones(): List<AdminRingtoneListItemDto>
    suspend fun getRingtoneDetail(ringtoneId: String): AdminRingtoneDetailDto
    suspend fun createRingtone(request: CreateAdminRingtoneRequestDto): AdminRingtoneDetailDto
    suspend fun updateRingtone(ringtoneId: String, request: UpdateAdminRingtoneRequestDto): AdminRingtoneDetailDto
    suspend fun deleteRingtone(ringtoneId: String)
    suspend fun setVisibility(ringtoneId: String, visibility: RingtoneVisibilityDto): com.morningalarm.dto.admin.ringtone.SetRingtoneVisibilityResponseDto
    suspend fun togglePremium(ringtoneId: String): com.morningalarm.dto.admin.ringtone.ToggleRingtonePremiumResponseDto
    suspend fun getPreview(ringtoneId: String): RingtoneListItemDto
    suspend fun getClientPreview(): List<RingtoneListItemDto>
    suspend fun uploadImage(file: File): UploadedMediaDto
    suspend fun uploadAudio(file: File): UploadedMediaDto
    fun close()
}

internal class AdminRingtoneRepositoryImpl(
    private val apiClient: AdminApiClient,
    private val bearerToken: String,
    private val adminSecret: String,
) : AdminRingtoneRepository {

    override suspend fun listRingtones(): List<AdminRingtoneListItemDto> =
        apiClient.listRingtones(bearerToken, adminSecret)

    override suspend fun getRingtoneDetail(ringtoneId: String): AdminRingtoneDetailDto =
        apiClient.getRingtoneDetail(bearerToken, adminSecret, ringtoneId)

    override suspend fun createRingtone(request: CreateAdminRingtoneRequestDto): AdminRingtoneDetailDto =
        apiClient.createRingtone(bearerToken, adminSecret, request)

    override suspend fun updateRingtone(ringtoneId: String, request: UpdateAdminRingtoneRequestDto): AdminRingtoneDetailDto =
        apiClient.updateRingtone(bearerToken, adminSecret, ringtoneId, request)

    override suspend fun deleteRingtone(ringtoneId: String) =
        apiClient.deleteRingtone(bearerToken, adminSecret, ringtoneId)

    override suspend fun setVisibility(ringtoneId: String, visibility: RingtoneVisibilityDto) =
        apiClient.setVisibility(bearerToken, adminSecret, ringtoneId, SetRingtoneVisibilityRequestDto(visibility))

    override suspend fun togglePremium(ringtoneId: String) =
        apiClient.togglePremium(bearerToken, adminSecret, ringtoneId)

    override suspend fun getPreview(ringtoneId: String): RingtoneListItemDto =
        apiClient.getPreview(bearerToken, adminSecret, ringtoneId)

    override suspend fun getClientPreview(): List<RingtoneListItemDto> =
        apiClient.getClientPreview(bearerToken, adminSecret)

    override suspend fun uploadImage(file: File): UploadedMediaDto =
        apiClient.uploadImage(bearerToken, adminSecret, file)

    override suspend fun uploadAudio(file: File): UploadedMediaDto =
        apiClient.uploadAudio(bearerToken, adminSecret, file)

    override fun close() = apiClient.close()
}
