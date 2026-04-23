package com.silveira.care360.ui;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

public final class UiMessageUtils {

    private UiMessageUtils() {
    }

    public static void show(Activity activity, String message) {
        if (activity == null || isBlank(message)) {
            return;
        }

        View root = activity.findViewById(android.R.id.content);
        if (root != null) {
            Snackbar.make(root, message.trim(), Snackbar.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(activity)
                .setMessage(message.trim())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
