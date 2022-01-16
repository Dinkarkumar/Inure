package app.simple.inure.ui.panels

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.popups.app.PopupAnalytics
import app.simple.inure.ui.deviceinfo.SystemInfo
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.SDKHelper
import app.simple.inure.viewmodels.deviceinfo.PanelItemsViewModel
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceInfo : ScopedFragment() {

    private lateinit var panels: RecyclerView

    private lateinit var osVersion: TypeFaceTextView
    private lateinit var securityUpdate: TypeFaceTextView
    private lateinit var root: TypeFaceTextView
    private lateinit var busybox: TypeFaceTextView
    private lateinit var availableRam: TypeFaceTextView
    private lateinit var usedRam: TypeFaceTextView

    private lateinit var ramIndicator: CustomProgressBar

    private lateinit var search: DynamicRippleImageButton
    private lateinit var popup: DynamicRippleImageButton

    private lateinit var adapterPanelItems: AdapterMenu
    private val panelItemsViewModel: PanelItemsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_info, container, false)

        osVersion = view.findViewById(R.id.analytics_os_version)
        securityUpdate = view.findViewById(R.id.analytics_security_update)
        root = view.findViewById(R.id.analytics_root)
        busybox = view.findViewById(R.id.analytics_busybox)
        availableRam = view.findViewById(R.id.analytics_total_ram)
        usedRam = view.findViewById(R.id.analytics_total_used)
        popup = view.findViewById(R.id.device_info_option_button)
        search = view.findViewById(R.id.device_info_search_button)

        ramIndicator = view.findViewById(R.id.analytics_ram_progress_bar)

        panels = view.findViewById(R.id.device_info_panel_rv)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        panelItemsViewModel.getPanelItems().observe(viewLifecycleOwner, {
            adapterPanelItems = AdapterMenu(it)

            adapterPanelItems.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.system) -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        SystemInfo.newInstance(),
                                                        icon, "system_info")
                        }
                    }
                }
            })

            panels.layoutManager = GridLayoutManager(requireContext(), getInteger(R.integer.span_count))
            panels.adapter = adapterPanelItems
            panels.scheduleLayoutAnimation()
        })

        setEverything()

        popup.setOnClickListener {
            PopupAnalytics(it).setOnPopupMenuCallback(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    when (source) {
                        getString(R.string.refresh) -> {
                            setEverything()
                        }
                    }
                }
            })
        }

        search.setOnClickListener {
            clearEnterTransition()
            clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Search.newInstance(true),
                                        "search")
        }
    }

    private fun setEverything() {
        setDeviceAnalytics()
        setRamAnalytics()
        handler.postDelayed(ramRunnable, 1000)
    }

    private fun setDeviceAnalytics() {
        viewLifecycleOwner.lifecycleScope.launch {
            val osVersion: String
            val securityUpdate: String
            val root: String
            val busyBox: String

            withContext(Dispatchers.Default) {
                osVersion = SDKHelper.getSdkTitle(Build.VERSION.SDK_INT)
                securityUpdate = Build.VERSION.SECURITY_PATCH

                with(RootBeer(requireContext())) {
                    root = if (isRooted) {
                        getString(R.string.available)
                    } else {
                        getString(R.string.not_available)
                    }

                    busyBox = if (checkForBusyBoxBinary()) {
                        getString(R.string.available)
                    } else {
                        getString(R.string.not_available)
                    }
                }
            }

            this@DeviceInfo.osVersion.text = osVersion
            this@DeviceInfo.securityUpdate.text = securityUpdate
            this@DeviceInfo.root.text = root
            this@DeviceInfo.busybox.text = busyBox
        }
    }

    private fun setRamAnalytics() {
        viewLifecycleOwner.lifecycleScope.launch {
            val available: Long
            val used: Long

            withContext(Dispatchers.Default) {
                val mi = ActivityManager.MemoryInfo()
                val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.getMemoryInfo(mi)

                available = mi.totalMem
                used = mi.totalMem - mi.availMem
            }

            ramIndicator.max = (available / 1000).toInt()
            ramIndicator.setProgress((used / 1000).toInt(), animate = true, fromStart = false)

            availableRam.text = available.toSize()
            usedRam.text = used.toSize()
        }
    }

    private val ramRunnable = object : Runnable {
        override fun run() {
            setRamAnalytics()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ramRunnable)
        ramIndicator.clearAnimation()
    }

    companion object {
        fun newInstance(): DeviceInfo {
            val args = Bundle()
            val fragment = DeviceInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
