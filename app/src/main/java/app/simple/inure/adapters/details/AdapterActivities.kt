package app.simple.inure.adapters.details

import android.content.pm.PackageInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.ViewUtils.makeInvisible
import app.simple.inure.util.ViewUtils.makeVisible
import com.jaredrummler.apkparser.model.AndroidComponent

class AdapterActivities(private val applicationInfo: PackageInfo, private val activities: List<AndroidComponent>)
    : RecyclerView.Adapter<AdapterActivities.Holder>() {

    private lateinit var activitiesCallbacks: ActivitiesCallbacks
    private val isRootMode = ConfigurationPreferences.isUsingRoot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activities, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = activities[holder.absoluteAdapterPosition].name.substring(activities[holder.absoluteAdapterPosition].name.lastIndexOf(".") + 1)
        holder.activityPackageID.text = activities[position].name

        holder.activityStatus.text =
            holder.itemView.context
                    .getString(R.string.activity_status,
                               if (activities[holder.absoluteAdapterPosition].exported) {
                                   holder.itemView.context.getString(R.string.exported)
                               } else {
                                   holder.itemView.context.getString(R.string.not_exported)
                               },

                               if (ActivityUtils.isEnabled(holder.itemView.context, applicationInfo.packageName, activities[holder.absoluteAdapterPosition].name)) {
                                   holder.itemView.context.getString(R.string.enabled)
                               } else {
                                   holder.itemView.context.getString(R.string.disabled)
                               })

        holder.launch.setOnClickListener {
            ActivityUtils.launchPackage(holder.itemView.context, applicationInfo.packageName, activities[holder.absoluteAdapterPosition].name)
        }

        for (a in activities[position].intentFilters) {
            for (b in a.categories) {
                if (b == categoryLauncher || b == categoryLeanback) {
                    if (ActivityUtils.isEnabled(holder.itemView.context, applicationInfo.packageName, activities[holder.absoluteAdapterPosition].name)) {
                        holder.launch.makeVisible()
                        holder.divider.makeVisible()
                    } else {
                        holder.launch.makeInvisible()
                        holder.divider.makeInvisible()
                    }
                }
            }
        }

        holder.container.setOnClickListener {
            activitiesCallbacks.onActivityClicked(activities[holder.absoluteAdapterPosition], activities[holder.absoluteAdapterPosition].name)
        }

        if (isRootMode) {
            holder.container.setOnLongClickListener {
                activitiesCallbacks
                        .onActivityLongPressed(activities[holder.absoluteAdapterPosition].name,
                                               applicationInfo,
                                               it,
                                               ActivityUtils.isEnabled(holder.itemView.context, applicationInfo.packageName, activities[holder.absoluteAdapterPosition].name),
                                               holder.absoluteAdapterPosition)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_name)
        val activityStatus: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_status)
        val activityPackageID: TypeFaceTextView = itemView.findViewById(R.id.adapter_activity_package)
        val divider: View = itemView.findViewById(R.id.divider01)
        val launch: View = itemView.findViewById(R.id.adapter_activity_launch_button)
        val container: ConstraintLayout = itemView.findViewById(R.id.adapter_activity_container)
    }

    fun setOnActivitiesCallbacks(activitiesCallbacks: ActivitiesCallbacks) {
        this.activitiesCallbacks = activitiesCallbacks
    }

    companion object {
        private const val intentMain = "android.intent.action.MAIN"
        private const val categoryLauncher = "android.intent.category.LAUNCHER"
        private const val categoryLeanback = "android.intent.category.LEANBACK_LAUNCHER"

        interface ActivitiesCallbacks {
            fun onActivityClicked(androidComponent: AndroidComponent, packageId: String)
            fun onActivityLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int)
        }
    }
}
