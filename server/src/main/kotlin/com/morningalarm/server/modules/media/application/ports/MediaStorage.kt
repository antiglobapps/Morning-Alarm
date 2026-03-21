package com.morningalarm.server.modules.media.application.ports

import com.morningalarm.server.modules.media.domain.MediaKind
import com.morningalarm.server.modules.media.domain.MediaUpload
import com.morningalarm.server.modules.media.domain.StoredMedia
import java.io.File

interface MediaStorage {
    fun store(kind: MediaKind, upload: MediaUpload): StoredMedia
    fun resolve(kind: MediaKind, fileName: String): File?
}
