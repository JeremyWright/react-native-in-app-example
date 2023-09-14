package com.inappexample

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import kotlinx.coroutines.CoroutineScope

class InAppPackage(private val coroutineScope: CoroutineScope) : ReactPackage {
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf<NativeModule>(
                InAppModule(reactContext, coroutineScope),
                // ...
        )
    }
}