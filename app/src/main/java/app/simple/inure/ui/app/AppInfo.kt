package app.simple.inure.ui.app

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.menus.AdapterMenu
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.popups.app.PopupSure
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.panels.AllAppsData
import app.simple.inure.viewmodels.panels.InfoPanelMenuData


class AppInfo : ScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var storage: DynamicRippleTextView
    private lateinit var directories: DynamicRippleTextView
    private lateinit var menu: RecyclerView
    private lateinit var options: RecyclerView

    private lateinit var adapterMenu: AdapterMenu
    private lateinit var componentsViewModel: InfoPanelMenuData
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var allAppsData: AllAppsData

    private var spanCount = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        appInformation = view.findViewById(R.id.app_info_information_tv)
        storage = view.findViewById(R.id.app_info_storage_tv)
        directories = view.findViewById(R.id.app_info_directories_tv)
        menu = view.findViewById(R.id.app_info_menu)
        options = view.findViewById(R.id.app_info_options)

        packageInfo = requireArguments().getParcelable("application_info")!!

        spanCount = if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            3
        } else {
            6
        }

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(InfoPanelMenuData::class.java)
        allAppsData = ViewModelProvider(requireActivity()).get(AllAppsData::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getMenuItems().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            adapterMenu = AdapterMenu(it)
            adapterMenu.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            menu.layoutManager = GridLayoutManager(requireContext(), spanCount)
            menu.adapter = adapterMenu
            menu.scheduleLayoutAnimation()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.manifest) -> {
                            if (ConfigurationPreferences.isXmlViewerTextView()) {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerTextView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                             icon, "manifest")
                            } else {
                                openFragment(requireActivity().supportFragmentManager,
                                             XMLViewerWebView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                             icon, "manifest")
                            }
                        }
                        getString(R.string.services) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Services.newInstance(packageInfo),
                                         icon, "services")
                        }
                        getString(R.string.activities) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Activities.newInstance(packageInfo),
                                         icon, "activities")
                        }
                        getString(R.string.providers) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Providers.newInstance(packageInfo),
                                         icon, "providers")
                        }
                        getString(R.string.permissions) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Permissions.newInstance(packageInfo),
                                         icon, "permissions")
                        }
                        getString(R.string.certificate) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Certificate.newInstance(packageInfo),
                                         icon, "certificate")
                        }
                        getString(R.string.receivers) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Receivers.newInstance(packageInfo),
                                         icon, "broadcasts")
                        }
                        getString(R.string.resources) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Resources.newInstance(packageInfo),
                                         icon, "resources")
                        }
                        getString(R.string.uses_feature) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Features.newInstance(packageInfo),
                                         icon, "uses_feature")
                        }
                        getString(R.string.graphics) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Graphics.newInstance(packageInfo),
                                         icon, "graphics")
                        }
                        getString(R.string.extras) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Extras.newInstance(packageInfo),
                                         icon, "extras")
                        }
                        getString(R.string.dex_classes) -> {
                            openFragment(requireActivity().supportFragmentManager,
                                         Dexs.newInstance(packageInfo),
                                         icon, "dexs")
                        }
                    }
                }
            })
        })

        componentsViewModel.getMenuOptions().observe(requireActivity(), {
            val adapterAppInfoMenu = AdapterMenu(it)
            options.layoutManager = GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
            options.adapter = adapterAppInfoMenu
            options.scheduleLayoutAnimation()

            adapterAppInfoMenu.setOnAppInfoMenuCallback(object : AdapterMenu.AdapterMenuCallbacks {
                override fun onAppInfoMenuClicked(source: String, icon: ImageView) {
                    when (source) {
                        getString(R.string.launch) -> {
                            packageInfo.launchThisPackage(requireActivity())
                        }
                        getString(R.string.uninstall) -> {
                            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                                val popupMenu = PopupSure(icon)
                                popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                    override fun onMenuItemClicked(source: String) {
                                        when (source) {
                                            getString(R.string.yes) -> {
                                                val f = ShellExecutorDialog.newInstance("pm uninstall -k --user 0 ${packageInfo.packageName}")

                                                f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                    override fun onCommandExecuted(result: String) {
                                                        if (result == "Success") {
                                                            onAppUninstalled(true)
                                                        }
                                                    }
                                                })

                                                f.show(parentFragmentManager, "shell_executor")
                                            }
                                        }
                                    }
                                })
                            } else {
                                if (ConfigurationPreferences.isUsingRoot()) {
                                    val popupMenu = PopupSure(icon)
                                    popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                        override fun onMenuItemClicked(source: String) {
                                            when (source) {
                                                getString(R.string.yes) -> {
                                                    val f = ShellExecutorDialog.newInstance("pm uninstall ${packageInfo.packageName}")

                                                    f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                        override fun onCommandExecuted(result: String) {
                                                            if (result == "Success") {
                                                                onAppUninstalled(true)
                                                            }
                                                        }
                                                    })

                                                    f.show(parentFragmentManager, "shell_executor")
                                                }
                                            }
                                        }
                                    })
                                } else {
                                    packageInfo.uninstallThisPackage(appUninstallObserver, -1)
                                }
                            }
                        }
                        getString(R.string.send) -> {
                            Preparing.newInstance(packageInfo)
                                    .show(childFragmentManager, "prepare_send_files")
                        }
                        getString(R.string.clear_data) -> {
                            val popupMenu = PopupSure(icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance("pm clear ${packageInfo.packageName}")
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.clear_cache) -> {
                            val popupMenu = PopupSure(icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance(
                                                "rm -r -v /data/data/${packageInfo.packageName}/cache " +
                                                        "& rm -r -v /data/data/${packageInfo.packageName}/app_cache " +
                                                        "& rm -r -v /data/data/${packageInfo.packageName}/app_texture " +
                                                        "& rm -r -v /data/data/${packageInfo.packageName}/app_webview " +
                                                        "& rm -r -v /data/data/${packageInfo.packageName}/code_cache",
                                            )
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.force_stop) -> {
                            val popupMenu = PopupSure(icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            ShellExecutorDialog.newInstance("am force-stop ${packageInfo.packageName}")
                                                    .show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.disable) -> {
                            val popupMenu = PopupSure(icon)
                            popupMenu.setOnMenuClickListener(object : PopupMenuCallback {
                                override fun onMenuItemClicked(source: String) {
                                    when (source) {
                                        getString(R.string.yes) -> {
                                            val f = ShellExecutorDialog.newInstance("pm disable ${packageInfo.packageName}")

                                            f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                                override fun onCommandExecuted(result: String) {
                                                    if (result.contains("disabled")) {
                                                        componentsViewModel.loadOptions()
                                                    }
                                                }
                                            })

                                            f.show(parentFragmentManager, "shell_executor")
                                        }
                                    }
                                }
                            })
                        }
                        getString(R.string.enable) -> {
                            val f = ShellExecutorDialog.newInstance("pm enable ${packageInfo.packageName}")

                            f.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                override fun onCommandExecuted(result: String) {
                                    if (result.contains("enabled")) {
                                        componentsViewModel.loadOptions()
                                    }
                                }
                            })

                            f.show(parentFragmentManager, "shell_executor")
                        }
                        getString(R.string.open_in_settings) -> {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageInfo.packageName, null)
                            })
                        }
                    }
                }
            })
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(packageInfo.packageName)

        name.text = packageInfo.applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), packageInfo)

        appInformation.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Information.newInstance(packageInfo),
                         "information")
        }

        storage.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Storage.newInstance(packageInfo),
                         getString(R.string.storage))
        }

        directories.setOnClickListener {
            clearExitTransition()
            openFragment(requireActivity().supportFragmentManager,
                         Directories.newInstance(packageInfo),
                         getString(R.string.directories))
        }
    }

    override fun onAppUninstalled(result: Boolean) {
        if (result) {
            with(allAppsData) {
                loadAppData()

                appLoaded.observe(viewLifecycleOwner, { appsEvent ->
                    appsEvent.getContentIfNotHandledOrReturnNull()?.let {
                        if (it) {
                            requireActivity().supportFragmentManager
                                    .popBackStack()
                        }
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("transition_name", transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}