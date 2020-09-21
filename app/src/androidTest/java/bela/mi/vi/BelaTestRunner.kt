package bela.mi.vi

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import bela.mi.vi.interactor.TestApplication

@Suppress("UNUSED") // it is used build.gradle as testInstrumentationRunner
class BelaTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}
