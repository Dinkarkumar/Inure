package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterExtras
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Extras : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var total: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_extras, container, false)

        recyclerView = view.findViewById(R.id.extras_recycler_view)
        total = view.findViewById(R.id.total)
        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getExtras().observe(viewLifecycleOwner, {
            val adapterExtras = AdapterExtras(APKParser.getExtraFiles(packageInfo.applicationInfo.sourceDir))

            recyclerView.adapter = adapterExtras
            total.text = getString(R.string.total, adapterExtras.list.size)

            adapterExtras.setOnResourceClickListener(object : AdapterExtras.ExtrasCallbacks {
                override fun onExtrasClicked(path: String) {
                    clearEnterTransition()
                    clearExitTransition()
                    when {
                        path.endsWith(".ttf") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Font.newInstance(packageInfo, path),
                                                        "ttf_viewer")
                        }
                        path.endsWith(".html") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        HtmlViewer.newInstance(packageInfo, path),
                                                        "html_viewer")
                        }
                        /**
                         * TODO - Add a delicious looking code viewer
                         */
                        path.endsWith(".java") ||
                                path.endsWith(".css") ||
                                path.endsWith(".json") ||
                                path.endsWith(".proto") ||
                                path.endsWith(".js") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                        path.endsWith(".md") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Markdown.newInstance(packageInfo, path),
                                                        "md_viewer")
                        }
                        else -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                    }
                }

                override fun onExtrasLongClicked(path: String) {
                    clearEnterTransition()
                    clearExitTransition()
                    when {
                        path.endsWith(".ttf") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        Font.newInstance(packageInfo, path),
                                                        "ttf_viewer")
                        }
                        path.endsWith(".html") ||
                                path.endsWith(".java") ||
                                path.endsWith(".css") ||
                                path.endsWith(".json") ||
                                path.endsWith(".proto") ||
                                path.endsWith(".js") ||
                                path.endsWith(".md") -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                        else -> {
                            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                        TextViewer.newInstance(packageInfo, path),
                                                        "text_viewer")
                        }
                    }
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Extras {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = Extras()
            fragment.arguments = args
            return fragment
        }
    }
}