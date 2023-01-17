package com.sabina.project.project_manager.presentation

import androidx.annotation.StringRes
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.project_manager.presentation.dialogs.DeleteDialog
import com.sabina.project.project_manager.presentation.dialogs.RationalPermissionDialog
import com.sabina.project.project_manager.presentation.images.camera.CameraFragment
import com.sabina.project.project_manager.presentation.images.gallery.GalleryFragment
import com.sabina.project.project_manager.presentation.map.MapFragment
import com.sabina.project.project_manager.presentation.project_overview.ProjectOverviewFragment
import com.sabina.project.project_manager.presentation.project_overview.address.AddressFragment
import com.sabina.project.project_manager.presentation.project_overview.contacts.ContactsFragment
import com.sabina.project.project_manager.presentation.project_overview.group_overview.GroupOverviewFragment
import com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.ObjectOverviewFragment
import com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview.ImageOverviewFragment

internal class ProjectOverviewScreen(val item: String = "") : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project?item=$item",
    requestKey = ProjectOverviewFragment.TAG
)

internal class ContactsScreen(val item: String = "") : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/contacts?item=$item",
    requestKey = ContactsFragment.TAG
)

internal class AddressScreen(val item: String = "") : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/address?item=$item",
    requestKey = AddressFragment.TAG
)

internal class DeleteDialogScreen(@StringRes val title: Int) : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/dialog_delete?title=$title",
    requestKey = DeleteDialog.TAG
)

internal class RationalPermissionDialogScreen(val title: String) : Screen(
    route = "rco-android-app://com.sabina.project/permission_dialog?title=$title",
    requestKey = RationalPermissionDialog.TAG
)

internal class MapScreen(val item: String) : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/map?item=$item",
    requestKey = MapFragment.TAG
)

internal class GroupOverviewScreen(
    group: String = "",
    project: String
) : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/group?project=$project&group=$group",
    requestKey = GroupOverviewFragment.TAG
)

internal class ObjectOverviewScreen(
    obj: String = "",
    groupUuid: String,
    project: String
) : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/group/object?project=$project&obj=$obj&groupUuid=$groupUuid",
    requestKey = ObjectOverviewFragment.TAG
)

internal class ImageOverviewScreen(
    val project: String,
    val image: String = "",
    val objUuid: String,
    val groupUuid: String
) : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/project/group/object/image?project=$project&objUuid=$objUuid&groupUuid=$groupUuid&image=$image",
    requestKey = ImageOverviewFragment.TAG
)

internal object CameraScreen : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/camera",
    requestKey = CameraFragment.TAG
)

internal object GalleryScreen : Screen(
    route = "rco-android-app://com.sabina.project/root_project_manager/gallery",
    requestKey = GalleryFragment.TAG
)