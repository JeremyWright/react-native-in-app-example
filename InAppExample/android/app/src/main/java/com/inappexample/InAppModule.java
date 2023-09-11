package com.inappexample;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import android.widget.Toast;
import android.*;
import android.os.Bundle;
import android.content.Context;
import android.util.*;
public class InAppModule extends ReactContextBaseJavaModule {
    Context context;
    public InAppModule(ReactApplicationContext context) {
        super(context);
        this.context = context.getApplicationContext(); // This is where you get the context
    }
    @Override
    public String getName() {
        return "InAppModule";
    }

    @ReactMethod
    public void configure(
            String url,
            String organizationId,
            String developerName,
            String conversationId
    ) {

        CharSequence text = "Configure Android SDK!\n";
        text += "URL: " + url + "\n";
        text += "Organization ID: " + organizationId + "\n";
        text += "Developer Name: " + developerName + "\n";
        text += "Conversation ID: " + conversationId + "\n";

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text + url, duration);
        toast.show();    }

    @ReactMethod
    public void launch() {
        CharSequence text = "Launch Android SDK!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}