package com.material.flashbar;

import android.app.Activity;

/** Ready-made Material 3 configurations for common in-app events. */
public final class FlashbarPresets {
    private FlashbarPresets() { }

    public static Flashbar.Builder success(Activity activity, CharSequence title, CharSequence message) {
        return new Flashbar.Builder(activity).type(Flashbar.Type.SUCCESS).title(title).message(message);
    }

    public static Flashbar.Builder error(Activity activity, CharSequence title, CharSequence message) {
        return new Flashbar.Builder(activity).type(Flashbar.Type.ERROR).title(title).message(message);
    }

    public static Flashbar.Builder warning(Activity activity, CharSequence title, CharSequence message) {
        return new Flashbar.Builder(activity).type(Flashbar.Type.WARNING).title(title).message(message);
    }

    public static Flashbar.Builder info(Activity activity, CharSequence title, CharSequence message) {
        return new Flashbar.Builder(activity).type(Flashbar.Type.INFO).title(title).message(message);
    }

    public static Flashbar.Builder loading(Activity activity, CharSequence message) {
        return new Flashbar.Builder(activity)
                .type(Flashbar.Type.LOADING)
                .message(message)
                .showCloseButton(false)
                .swipeToDismiss(false)
                .duration(Flashbar.DURATION_INDEFINITE);
    }
}
