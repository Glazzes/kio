package com.kio.events.folder

import org.springframework.context.ApplicationEvent

data class FolderDeleteEvent(val folder: Folder) : ApplicationEvent(folder)