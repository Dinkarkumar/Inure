package app.simple.inure.ui.subpanels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AnalyticsDataAdapter
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.AnalyticsSDKViewModelFactory
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.subviewers.AnalyticsDataViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry

class AnalyticsTargetSDK : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var count: TypeFaceTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var analyticsDataViewModel: AnalyticsDataViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analytics_sdk, container, false)

        back = view.findViewById(R.id.back_button)
        title = view.findViewById(R.id.sdk_name)
        count = view.findViewById(R.id.count)
        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.recycler_view)
        val analyticsSDKViewModelFactory = AnalyticsSDKViewModelFactory(requireArguments().parcelable(BundleConstants.entry)!!)
        analyticsDataViewModel = ViewModelProvider(this, analyticsSDKViewModelFactory)[AnalyticsDataViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (analyticsDataViewModel.getMinimumSDKData().value != null) {
            postponeEnterTransition()
        } else {
            startPostponedEnterTransition()
        }

        title.text = if (AnalyticsPreferences.getSDKValue()) {
            SDKHelper.getSdkTitle(SDKHelper.convertAndroidVersionToSDKCode(requireArguments().parcelable<PieEntry>(BundleConstants.entry)!!.label))
        } else {
            requireArguments().parcelable<PieEntry>(BundleConstants.entry)!!.label
        }

        back.setOnClickListener {
            popBackStack()
        }

        analyticsDataViewModel.getTargetSDKData().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            count.text = getString(R.string.total_apps, it.size.toString())
            val adapterAnalyticsSDK = AnalyticsDataAdapter(it)

            adapterAnalyticsSDK.setOnAdapterCallbacks(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            recyclerView.adapter = adapterAnalyticsSDK

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(e: Entry?): AnalyticsTargetSDK {
            val args = Bundle()
            args.putParcelable(BundleConstants.entry, e)
            val fragment = AnalyticsTargetSDK()
            fragment.arguments = args
            return fragment
        }
    }
}
