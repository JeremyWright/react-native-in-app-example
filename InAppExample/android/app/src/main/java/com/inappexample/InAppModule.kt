package com.inappexample

import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.salesforce.android.smi.common.api.Result
import com.salesforce.android.smi.core.CoreClient
import com.salesforce.android.smi.core.CoreConfiguration
import com.salesforce.android.smi.core.PreChatValuesProvider
import com.salesforce.android.smi.network.data.domain.prechat.PreChatField
import com.salesforce.android.smi.ui.UIClient
import com.salesforce.android.smi.ui.UIConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

@ReactModule(name = "InAppModule")
class InAppModule(
        private val context: ReactApplicationContext,
        private val coroutineScope: CoroutineScope
) : ReactContextBaseJavaModule(context) {
    private var uiConfiguration: UIConfiguration? = null
    private var coreConfiguration: CoreConfiguration? = null
    private var uiClient: UIClient? = null
    private var coreClient: CoreClient? = null
    @ReactMethod
    fun configure(
            url: String,
            organizationId: String,
            developerName: String,
            conversationId: String
    ) {
        try {
            val apiUrl = URL(url)
            val uuid = UUID.fromString(conversationId)
            val coreConfiguration = CoreConfiguration(
                    apiUrl,
                    organizationId,
                    developerName,
                    false)
            val uiConfiguration = UIConfiguration(
                    coreConfiguration,
                    uuid,
                    false)

            coreClient = CoreClient.Factory.create(context.applicationContext, coreConfiguration)
            coreClient?.registerHiddenPreChatValuesProvider(
                    object : PreChatValuesProvider {
                        override suspend fun setValues(input: List<PreChatField>): List<PreChatField> {
                            println("test")

                            input.forEach {
                                if (it.name == "FavoriteFood") {
                                    it.userInput = "Apples"
                                }
                            }
                            return input
                        }

                    }
            )

            this.uiConfiguration = uiConfiguration
            this.coreConfiguration = coreConfiguration
            uiClient = UIClient.Factory.create(uiConfiguration)

            val duration = Toast.LENGTH_SHORT
            val text: String = "Android SDK configured with the following values:\n" +
                    "URL: $url\n" +
                    "Organization ID: $organizationId\n" +
                    "Developer Name: $developerName\n" +
                    "Conversation ID: $conversationId\n"

            val toast = Toast.makeText(context, "Configuration set.", duration)
            Log.i(TAG, text)
            toast.show()

        } catch (exception: MalformedURLException) {
            exception.message?.let { Log.i(TAG, it) }
        }
    }
    @ReactMethod
    fun launch() {
        val intent = uiClient?.createOpenConversationIntent(context.applicationContext)
        context.currentActivity?.startActivity(intent)
    }
    @ReactMethod
    fun destroyDB() = coroutineScope.launch {
        val result = CoreClient.destroyStorage(context.applicationContext)
        println(result)
        coreClient?.destroy()
    }

    @ReactMethod
    fun retrieveConversations(callback: Callback) = coroutineScope.launch {
        coreClient?.conversations(10, false)?.collectLatest {
            when(it) {
                is Result.Success -> {
                    var text = "Conversations found: ${it.data.count()}\n\n"
                            it.data.forEach { conversation ->
                                text += "$conversation\n"
                            }
                    callback.invoke(text)
                }
                else -> {}
            }
        }
    }

    override fun getName() = TAG
    companion object {
        const val TAG: String = "InAppModule"
    }

}

