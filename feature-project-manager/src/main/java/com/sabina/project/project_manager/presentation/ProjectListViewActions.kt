package com.sabina.project.project_manager.presentation

import com.sabina.project.project_manager.domain.model.Project

internal sealed class ProjectListViewActions {
    object CreateProject : ProjectListViewActions()
    object SortByCreateTime : ProjectListViewActions()
    class OpenProject(val item: Project) : ProjectListViewActions()
    object EnableSearch : ProjectListViewActions()
    object DisableSearch : ProjectListViewActions()
    object LoadInfo : ProjectListViewActions()
}