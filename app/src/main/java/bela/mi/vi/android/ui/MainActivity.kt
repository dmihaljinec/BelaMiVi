package bela.mi.vi.android.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import bela.mi.vi.android.R
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val topLevelDestinations = setOf(
        R.id.match_summaries_fragment,
        R.id.player_list_fragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navHostFragment.navController)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        require(drawerLayout != null)
        val appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navHostFragment.navController, appBarConfiguration)
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                in topLevelDestinations -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                else -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    // close vkb
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun clearToolbarMenu() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
    }

    fun setupToolbarMenu(menuId: Int, clickListener: Toolbar.OnMenuItemClickListener) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(menuId)
        toolbar.setOnMenuItemClickListener(clickListener)
    }

    fun setToolbarTitle(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
    }
}

@ExperimentalCoroutinesApi
fun Fragment.requireMainActivity(): MainActivity {
    val mainActivity = activity as? MainActivity
    return mainActivity ?: throw IllegalStateException("Fragment $this not attached to MainActivity.")
}