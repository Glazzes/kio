package com.kio.events.folder

import com.kio.entities.Folder
import org.springframework.context.ApplicationEvent

data class FolderDeleteEvent(val folder: Folder) : ApplicationEvent(folder)