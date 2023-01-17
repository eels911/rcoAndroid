package com.sabina.project.project_manager.presentation.map

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sabina.project.base.external.map.MapUtils
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.base.external.models.asMapKitPoint
import com.sabina.project.base.external.permission.PermissionUtils
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.MapFragmentBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class MapFragment : BaseFragment(R.layout.map_fragment) {

    private val args: MapFragmentArgs by navArgs()

    companion object {
        const val TAG = "MapFragment"
        const val RESULT_KEY = "RESULT_KEY"
        const val DATA_KEY = "DATA_KEY"
        private const val PERMISSIONS_REQUEST_CODE = 1000
        private const val ANIMATION_DURATION = 1.0f

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): SabinaGeoPoint = bundle.getParcelable(DATA_KEY)!!
    }

    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()
    private val navController by lazy { findNavController() }
    private val binding by viewBinding(MapFragmentBinding::bind)

    @Inject
    lateinit var viewModelFactory: MapViewModel.Factory
    private val viewModel: MapViewModel by viewModels {
        MapViewModel.provideFactory(
            item = args.item,
            assistedFactory = viewModelFactory
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewsConfigurator.initStartState()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
        updateStatuses()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE)
            updateStatuses()
    }

    private fun updateStatuses() {
        val hasPermissions = PermissionUtils.isLocationPermissionsGranted(requireContext())
        val isGpsEnabled = MapUtils.isGpsEnabled(requireContext())
        viewModel.obtainAction(MapViewActions.UpdateStatus(hasPermissions, isGpsEnabled))
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle).filterNotNull().onEach {
            viewsConfigurator.renderState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.navigateBack.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            onClose(it.first, it.second)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            eventsConfigurator.handleViewEvent(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.intentSender.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onClose(result: Result = Result.CANCELED, mark: SabinaGeoPoint? = null) {
        val bundle = if (mark == null)
            bundleOf(RESULT_KEY to result.name)
        else
            bundleOf(RESULT_KEY to result.name, DATA_KEY to mark)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
        navController.popBackStack()
    }

    private inner class ViewsConfigurator {
        private val inputListener = object : InputListener {
            override fun onMapLongTap(p0: Map, p1: Point) {}

            override fun onMapTap(p0: Map, p1: Point) {
                putMark(p1.latitude, p1.longitude)
            }
        }

        fun initStartState() {
            binding.mapView.map.isNightModeEnabled = isCurrentModeDark(binding.root.background as ColorDrawable)
            binding.mapView.map.addInputListener(inputListener)
            binding.btnSave.clicksWithDebounce {
                viewModel.obtainAction(MapViewActions.SaveMark)
            }
            binding.ivLocation.clicksWithDebounce {
                if (MapUtils.isGpsEnabled(requireContext())) {
                    binding.ivLocation.startAnimation(AnimationUtils.loadAnimation(binding.ivLocation.context, R.anim.anim_loading))
                    viewModel.obtainAction(MapViewActions.LoadLocation {
                        binding.ivLocation.animation = null
                        putUserGeolocationMark(it.latitude, it.longitude)
                    })
                } else {
                    goToEnableGps()
                }
            }
            binding.ivGps.clicksWithDebounce {
                goToEnableGps()
            }
            binding.ivPermission.clicksWithDebounce {
                if (shouldShowRequestPermissionRationale(PermissionUtils.getLocationPermissionsArray().first())) {
                    viewModel.obtainAction(MapViewActions.RationalPermissionRequest)
                } else {
                    PermissionUtils.requestPermissions(
                        fragment = this@MapFragment,
                        permissions = PermissionUtils.getLocationPermissionsArray(),
                        requestCode = PERMISSIONS_REQUEST_CODE
                    )
                }
            }

            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(MapViewActions.OnBackClick)
            }
        }

        private fun goToEnableGps() {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        fun renderState(state: MapViewState) {
            when (state) {
                is MapViewState.DefaultState -> renderDefaultState(state)
            }
        }

        private fun renderDefaultState(state: MapViewState.DefaultState) {
            binding.mapView.map.mapObjects.clear()

            if (state.mark == null) {
                binding.btnSave.text = getString(R.string.map_btn_text_tip)
                binding.btnSave.alpha = 0.6f
                binding.btnSave.isEnabled = false
            } else {
                binding.btnSave.text = getString(R.string.map_btn_text_save)
                binding.btnSave.alpha = 1.0f
                binding.btnSave.isEnabled = true

                val view = View(requireContext())
                view.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_mark_24)
                binding.mapView.map.mapObjects.addPlacemark(state.mark.asMapKitPoint(), ViewProvider(view))
                if (state.userGeolocationMark == null)
                    moveToMark(state.mark)
            }

            if (state.userGeolocationMark != null) {
                val view = View(requireContext())
                view.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location_24)
                binding.mapView.map.mapObjects.addPlacemark(state.userGeolocationMark.asMapKitPoint(), ViewProvider(view))
            }

            binding.ivPermission.isVisible = !state.hasPermissions
            binding.ivGps.isVisible = state.hasPermissions && !state.isGpsEnabled
            binding.ivLocation.isVisible = state.hasPermissions && state.isGpsEnabled
        }

        private fun isCurrentModeDark(drawable: ColorDrawable): Boolean {
            return drawable.color == ContextCompat.getColor(requireContext(), R.color.for_dark_mode_definition)
        }

        private fun putMark(latitude: Double, longitude: Double) {
            viewModel.obtainAction(MapViewActions.PutMark(SabinaGeoPoint(latitude, longitude)))
        }

        private fun putUserGeolocationMark(latitude: Double, longitude: Double) {
            val point = SabinaGeoPoint(latitude, longitude)
            moveToMark(point)
            viewModel.obtainAction(MapViewActions.PutUserGeolocationMark(point))
        }

        private fun moveToMark(mark: SabinaGeoPoint) {
            val cameraPosition = CameraPosition(mark.asMapKitPoint(), binding.mapView.map.maxZoom, 0.0f, 0.0f)
            val animation = Animation(Animation.Type.SMOOTH, ANIMATION_DURATION)
            binding.mapView.map.move(cameraPosition, animation, null)
        }
    }

    private inner class EventsConfigurator {
        fun handleViewEvent(event: ViewEvent) {
            when (event) {
                is ViewEvent.Navigation -> handleScreen(event.screen)
                is ViewEvent.AlmagestSnackbar -> handleSnackbar(event)
                is ViewEvent.Loading -> Unit
            }
        }

        private fun handleSnackbar(event: ViewEvent.AlmagestSnackbar) {
            AlmagestSnackbar.Builder(binding.root.context)
                .setStatus(event.status.index)
                .setMessage(event.messageId)
                .setMessage(event.message)
                .setGravity(event.gravity)
                .setDelay(event.delayMillis)
                .setOnDismissCallback(event.onDismiss)
                .build()
                .show(binding.root)
        }

        private fun handleScreen(screen: Screen) {
            binding.loadingView.isVisible = false
            viewModel.viewEvent.setResultListener(this@MapFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }

    enum class Result {
        CANCELED,
        UPDATED;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }
}