package com.example.videoplayer.data

import com.example.videoplayer.domain.model.VideoFolder as DomainVideoFolder
import com.example.videoplayer.domain.model.VideoItem as DomainVideoItem

/**
 * Maps a [VideoItem] from the data layer to a [DomainVideoItem] in the domain layer.
 */
fun VideoItem.toDomain(): DomainVideoItem {
    return DomainVideoItem(
        uri = this.uri,
        name = this.name,
        duration = this.duration,
        folderName = this.folderName
    )
}

/**
 * Maps a [VideoFolder] from the data layer to a [DomainVideoFolder] in the domain layer.
 */
fun VideoFolder.toDomain(): DomainVideoFolder {
    return DomainVideoFolder(
        id = this.id,
        name = this.name,
        videos = this.videos.map { it.toDomain() }
    )
}
