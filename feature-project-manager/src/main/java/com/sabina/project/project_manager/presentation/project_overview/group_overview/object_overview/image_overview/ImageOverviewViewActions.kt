package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview

internal sealed class ImageOverviewViewActions {
    object DeleteImage : ImageOverviewViewActions()
    object OpenFullScreenImage : ImageOverviewViewActions()
    object OpenMap : ImageOverviewViewActions()
    object OpenGallery : ImageOverviewViewActions()
    object OpenCamera : ImageOverviewViewActions()

    object OnBackClick : ImageOverviewViewActions()
    object SaveChangesOnExtraExit : ImageOverviewViewActions()
    class SaveInfo(
        val name: String,
        val comment: String,
    ) : ImageOverviewViewActions()
}