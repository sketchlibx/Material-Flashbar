package com.material.flashbar.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textview.MaterialTextView;
import com.material.flashbar.Flashbar;
import com.material.flashbar.FlashbarPresets;

/** Interactive preview of the library's visual modes and notification presets. */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);

        int surface = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorSurface, 0xFFFFFBFE);
        int onSurface = MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurface, 0xFF1D1B20);
        int primary = MaterialColors.getColor(this,
                androidx.appcompat.R.attr.colorPrimary, 0xFF6750A4);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(surface);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(28), dp(24), dp(32));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        MaterialTextView eyebrow = new MaterialTextView(this);
        eyebrow.setText("SK SKETCHLIBX  /  UI COMPONENTS");
        eyebrow.setTextColor(primary);
        eyebrow.setTextSize(12f);
        eyebrow.setLetterSpacing(0.08f);
        page.addView(eyebrow);

        MaterialTextView heading = new MaterialTextView(this);
        heading.setText("Material Flashbar");
        heading.setTextColor(onSurface);
        heading.setTextSize(30f);
        heading.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headingParams.topMargin = dp(8);
        page.addView(heading, headingParams);

        MaterialTextView description = new MaterialTextView(this);
        description.setText("Expressive in-app feedback with a translucent glass appearance, smooth motion and dynamic Material colours.");
        description.setTextColor(onSurface);
        description.setAlpha(0.78f);
        description.setTextSize(15f);
        description.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = dp(9);
        descriptionParams.bottomMargin = dp(25);
        page.addView(description, descriptionParams);

        addButton(page, "Material 3 · Basic", primary, v ->
                new Flashbar.Builder(this)
                        .title("Changes saved")
                        .message("Your preferences have been updated successfully.")
                        .primaryActionText("VIEW", bar -> bar.dismiss())
                        .show());

        addButton(page, "Glass · Success", primary, v ->
                FlashbarPresets.success(this, "All done", "Your new project is ready.")
                        .glassEffect(true)
                        .glassOpacity(0.84f)
                        .primaryActionText("OPEN", bar -> bar.dismiss())
                        .show());

        addButton(page, "Error · With retry action", primary, v ->
                FlashbarPresets.error(this, "Upload failed", "A network issue interrupted the upload.")
                        .primaryActionText("RETRY", bar -> bar.dismiss())
                        .secondaryActionText("LATER", bar -> bar.dismiss())
                        .show());

        addButton(page, "Warning · Top position", primary, v ->
                new Flashbar.Builder(this)
                        .type(Flashbar.Type.WARNING)
                        .gravity(Flashbar.Gravity.TOP)
                        .title("Storage almost full")
                        .message("Consider clearing temporary files before your next export.")
                        .show());

        addButton(page, "Loading · Indeterminate", primary, v ->
                FlashbarPresets.loading(this, "Preparing your workspace…").show());

        addButton(page, "Progress · 68%", primary, v ->
                new Flashbar.Builder(this)
                        .title("Exporting project")
                        .message("6 of 9 resources processed")
                        .progress(68)
                        .duration(Flashbar.DURATION_INDEFINITE)
                        .show());

        addButton(page, "Glass · Information", primary, v ->
                FlashbarPresets.info(this, "Glass appearance", "Translucent fill, a light outline and a soft elevation.")
                        .glassEffect(true)
                        .glassOpacity(0.76f)
                        .duration(Flashbar.DURATION_LONG)
                        .show());

        setContentView(scroll);
    }

    private void addButton(LinearLayout parent, String label, int tint,
                           android.view.View.OnClickListener listener) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14f);
        button.setCornerRadius(dp(18));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tint));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        params.bottomMargin = dp(11);
        parent.addView(button, params);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
