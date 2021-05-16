package app.simple.inure.extension.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.simple.inure.R
import app.simple.inure.exception.CacheDirectoryDeletionException
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.util.ThemeSetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {

        SharedPreferences.init(newBase)

        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        CoroutineScope(Dispatchers.Default).launch {
            kotlin.runCatching {
                if (File(getExternalFilesDir(null)?.path + "/send_cache/").deleteRecursively()) {
                    Log.d(packageName, "Deleted")
                } else {
                    throw CacheDirectoryDeletionException("Could not delete cache directory")
                }
            }.getOrElse {
                it.printStackTrace()
            }
        }

        setTheme()
        ThemeSetter.setAppTheme(AppearancePreferences.getAppTheme())
    }

    private fun setTheme() {
        when (AppearancePreferences.getAccentColor()) {
            ContextCompat.getColor(baseContext, R.color.inure) -> {
                setTheme(R.style.Inure)
            }
            ContextCompat.getColor(baseContext, R.color.blue) -> {
                setTheme(R.style.Blue)
            }
            ContextCompat.getColor(baseContext, R.color.blueGrey) -> {
                setTheme(R.style.BlueGrey)
            }
            ContextCompat.getColor(baseContext, R.color.darkBlue) -> {
                setTheme(R.style.DarkBlue)
            }
            ContextCompat.getColor(baseContext, R.color.red) -> {
                setTheme(R.style.Red)
            }
            ContextCompat.getColor(baseContext, R.color.green) -> {
                setTheme(R.style.Green)
            }
            ContextCompat.getColor(baseContext, R.color.orange) -> {
                setTheme(R.style.Orange)
            }
            ContextCompat.getColor(baseContext, R.color.purple) -> {
                setTheme(R.style.Purple)
            }
            ContextCompat.getColor(baseContext, R.color.brown) -> {
                setTheme(R.style.Brown)
            }
            ContextCompat.getColor(baseContext, R.color.caribbeanGreen) -> {
                setTheme(R.style.CaribbeanGreen)
            }
            ContextCompat.getColor(baseContext, R.color.persianGreen) -> {
                setTheme(R.style.PersianGreen)
            }
            ContextCompat.getColor(baseContext, R.color.amaranth) -> {
                setTheme(R.style.Amaranth)
            }
            else -> {
                setTheme(R.style.Inure)
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, R.color.inure))
            }
        }
    }
}