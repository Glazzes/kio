package com.kio.events.folder

import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.stereotype.Component

@Component
abstract class FolderApplicationPublisher : ApplicationEventMulticaster