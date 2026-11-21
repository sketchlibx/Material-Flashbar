package com.material.flashbar;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;


/**
 * A Material 3 notification surface designed for compact, in-app feedback.
 *
 * <p>Instances are attached to the Activity content view and should be dismissed
 * when their host screen is no longer relevant.</p>
 */
public final class Flashbar {
    public static final long DURATION_SHORT = 2_500L;
    public static final long DURATION_LONG = 4_500L;
    public static final long DURATION_INDEFINITE = -1L;

    public enum Gravity { TOP, BOTTOM }
    public enum Style { MATERIAL, GLASS }
    public enum Type {
        DEFAULT("•"), SUCCESS("✓"), ERROR("!"), WARNING("!"), INFO("i"), LOADING("•");

        final String symbol;

        Type(String symbol) {
            this.symbol = symbol;
        }
    }

    public interface OnActionClickListener {
        void onClick(Flashbar flashbar);
    }

    private final Activity activity;
    private final Builder config;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::dismiss;

    private FrameLayout overlay;
    private SwipeCard card;
    private boolean showing;
    private boolean shown;
    private boolean dismissing;

    private Flashbar(Builder config) {
        this.config = config;
        this.activity = config.activity;
    }

    /** Creates a configured Flashbar instance. */
    public static Builder with(Activity activity) {
        return new Builder(activity);
    }

    /** Adds the notification to the current Activity. */
    public void show() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(this::show);
            return;
        }
        if (showing || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return;
        }

        ViewGroup contentRoot = (ViewGroup) content;
        overlay = new FrameLayout(activity);
        overlay.setClipChildren(false);
        overlay.setClipToPadding(false);
        overlay.setClickable(false);
        overlay.setFocusable(false);
        overlay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        card = createCard();
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                config.gravity == Gravity.TOP ? android.view.Gravity.TOP : android.view.Gravity.BOTTOM);
        int horizontal = dp(16);
        int vertical = dp(20);
        cardParams.setMargins(horizontal, vertical, horizontal, vertical);
        overlay.addView(card, cardParams);
        contentRoot.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setOnApplyWindowInsetsListener((view, insets) -> {
            if (card != null && card.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams current = (FrameLayout.LayoutParams) card.getLayoutParams();
                int safeTop = insets.getSystemWindowInsetTop();
                int safeBottom = insets.getSystemWindowInsetBottom();
                current.leftMargin = horizontal + insets.getSystemWindowInsetLeft();
                current.rightMargin = horizontal + insets.getSystemWindowInsetRight();
                if (config.gravity == Gravity.TOP) {
                    current.topMargin = vertical + safeTop;
                } else {
                    current.bottomMargin = vertical + safeBottom;
                }
                card.setLayoutParams(current);
            }
            return insets;
        });
        overlay.requestApplyInsets();

        showing = true;
        shown = false;
        dismissing = false;

        float offset = dp(34);
        card.setAlpha(0f);
        card.setTranslationY(config.gravity == Gravity.TOP ? -offset : offset);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(280L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> shown = showing)
                .start();

        if (config.hapticFeedback) {
            card.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        scheduleTimeout();
    }

    /** Dismisses the notification with its exit animation. */
    public void dismiss() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(this::dismiss);
            return;
        }
        if (!showing || dismissing) {
            return;
        }
        dismissing = true;
        handler.removeCallbacks(timeoutRunnable);

        if (card == null || overlay == null) {
            removeFromHost();
            return;
        }

        float offset = dp(24);
        card.animate()
                .alpha(0f)
                .translationY(config.gravity == Gravity.TOP ? -offset : offset)
                .setDuration(190L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(this::removeFromHost)
                .start();
    }

    /** Returns true while the notification is attached or animating. */
    public boolean isShowing() {
        return showing;
    }

    /** Returns true after the entrance animation has completed. */
    public boolean isShown() {
        return shown && showing;
    }

    private void scheduleTimeout() {
        handler.removeCallbacks(timeoutRunnable);
        if (config.duration != DURATION_INDEFINITE && config.duration > 0L) {
            handler.postDelayed(timeoutRunnable, config.duration);
        }
    }

    private void removeAfterSwipe() {
        handler.removeCallbacks(timeoutRunnable);
        removeFromHost();
    }

    private void removeFromHost() {
        handler.removeCallbacks(timeoutRunnable);
        if (overlay != null) {
            ViewGroup parent = (ViewGroup) overlay.getParent();
            if (parent != null) {
                parent.removeView(overlay);
            }
        }
        showing = false;
        shown = false;
        dismissing = false;
        card = null;
        overlay = null;
    }

    private SwipeCard createCard() {
        final int surface = MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorSurface, Color.WHITE);
        final int onSurface = MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        final int primary = MaterialColors.getColor(activity,
                androidx.appcompat.R.attr.colorPrimary, 0xFF6750A4);
        final int onPrimary = MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorOnPrimary, Color.WHITE);
        final int outline = MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorOutline, 0xFF777777);
        final int accent = resolveAccent(primary);

        SwipeCard result = new SwipeCard(activity);
        result.setRadius(dp(config.cornerRadiusDp));
        result.setCardElevation(dp(7));
        result.setUseCompatPadding(false);
        result.setPreventCornerOverlap(true);
        result.setClickable(false);
        result.setFocusable(false);
        result.setCardBackgroundColor(resolveBackground(surface));
        result.setStrokeWidth(dp(config.style == Style.GLASS ? 1 : 0));
        result.setStrokeColor(ColorStateList.valueOf(config.style == Style.GLASS
                ? withAlpha(Color.WHITE, 0.58f)
                : withAlpha(outline, 0.10f)));
        result.setSwipeEnabled(config.swipeToDismiss);
        result.setOnSwipeDismiss(this::removeAfterSwipe);

        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(14), dp(12), dp(14));
        result.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        body.addView(topRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (config.showIcon) {
            FrameLayout iconFrame = new FrameLayout(activity);
            GradientDrawable iconBackground = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{withAlpha(accent, 0.25f), withAlpha(accent, 0.10f)});
            iconBackground.setCornerRadius(dp(16));
            iconFrame.setBackground(iconBackground);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(46), dp(46));
            iconParams.rightMargin = dp(13);
            topRow.addView(iconFrame, iconParams);

            if (config.type == Type.LOADING) {
                CircularProgressIndicator spinner = new CircularProgressIndicator(activity);
                spinner.setIndeterminate(true);
                spinner.setIndicatorSize(dp(25));
                spinner.setTrackThickness(dp(3));
                spinner.setIndicatorColor(accent);
                FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(30), dp(30), android.view.Gravity.CENTER);
                iconFrame.addView(spinner, spinnerParams);
            } else if (config.icon != null) {
                ImageView image = new ImageView(activity);
                image.setImageDrawable(config.icon);
                image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(dp(26), dp(26), android.view.Gravity.CENTER);
                iconFrame.addView(image, imageParams);
            } else {
                MaterialTextView symbol = new MaterialTextView(activity);
                symbol.setText(config.type.symbol);
                symbol.setTextSize(22f);
                symbol.setTextColor(accent);
                symbol.setGravity(android.view.Gravity.CENTER);
                symbol.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
                iconFrame.addView(symbol, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }

        LinearLayout textColumn = new LinearLayout(activity);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topRow.addView(textColumn, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (config.title != null && config.title.length() > 0) {
            MaterialTextView titleView = new MaterialTextView(activity);
            titleView.setText(config.title);
            titleView.setTextColor(config.titleColor != null ? config.titleColor : onSurface);
            titleView.setTextSize(16f);
            titleView.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
            titleView.setMaxLines(2);
            textColumn.addView(titleView, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        if (config.message != null && config.message.length() > 0) {
            MaterialTextView messageView = new MaterialTextView(activity);
            messageView.setText(config.message);
            messageView.setTextColor(config.messageColor != null
                    ? config.messageColor : withAlpha(onSurface, 0.82f));
            messageView.setTextSize(14f);
            messageView.setLineSpacing(dp(2), 1f);
            messageView.setMaxLines(4);
            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (config.title != null && config.title.length() > 0) {
                messageParams.topMargin = dp(3);
            }
            textColumn.addView(messageView, messageParams);
        }

        if (config.progress >= 0 || config.indeterminateProgress) {
            LinearProgressIndicator progress = new LinearProgressIndicator(activity);
            progress.setIndicatorColor(accent);
            progress.setTrackColor(withAlpha(accent, 0.18f));
            progress.setIndeterminate(config.indeterminateProgress);
            if (!config.indeterminateProgress) {
                progress.setProgressCompat(config.progress, false);
            }
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(4));
            progressParams.topMargin = dp(10);
            textColumn.addView(progress, progressParams);
        }

        if (config.showCloseButton) {
            MaterialTextView close = new MaterialTextView(activity);
            close.setText("×");
            close.setTextSize(25f);
            close.setTextColor(withAlpha(onSurface, 0.72f));
            close.setGravity(android.view.Gravity.CENTER);
            close.setContentDescription(activity.getString(R.string.flashbar_dismiss));
            close.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(withAlpha(accent, 0.16f)),
                    new ColorDrawable(Color.TRANSPARENT), null));
            close.setOnClickListener(v -> dismiss());
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            closeParams.leftMargin = dp(5);
            topRow.addView(close, closeParams);
        }

        boolean hasPrimary = config.primaryActionText != null && config.primaryActionText.length() > 0;
        boolean hasSecondary = config.secondaryActionText != null && config.secondaryActionText.length() > 0;
        if (hasPrimary || hasSecondary) {
            LinearLayout actions = new LinearLayout(activity);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            actionsParams.topMargin = dp(10);
            body.addView(actions, actionsParams);

            if (hasSecondary) {
                MaterialButton secondary = createActionButton(config.secondaryActionText,
                        false, accent, onPrimary);
                LinearLayout.LayoutParams secondaryParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
                secondaryParams.rightMargin = dp(8);
                actions.addView(secondary, secondaryParams);
                secondary.setOnClickListener(v -> invokeAction(config.secondaryActionListener));
            }
            if (hasPrimary) {
                MaterialButton primaryButton = createActionButton(config.primaryActionText,
                        true, accent, onPrimary);
                actions.addView(primaryButton, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));
                primaryButton.setOnClickListener(v -> invokeAction(config.primaryActionListener));
            }
        }

        return result;
    }

    private MaterialButton createActionButton(CharSequence text, boolean filled,
                                               int accent, int onPrimary) {
        MaterialButton button = new MaterialButton(activity);
        button.setText(text);
        button.setTextSize(13f);
        button.setAllCaps(false);
        button.setCornerRadius(dp(22));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinHeight(dp(48));
        button.setMinimumHeight(dp(48));
        button.setPadding(dp(14), 0, dp(14), 0);
        if (filled) {
            button.setBackgroundTintList(ColorStateList.valueOf(accent));
            button.setTextColor(onPrimary);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(accent);
            button.setStrokeWidth(dp(1));
            button.setStrokeColor(ColorStateList.valueOf(withAlpha(accent, 0.32f)));
        }
        return button;
    }

    private void invokeAction(OnActionClickListener listener) {
        try {
            if (listener != null) {
                listener.onClick(this);
            }
        } finally {
            if (config.dismissOnAction) {
                dismiss();
            }
        }
    }

    private int resolveBackground(int surface) {
        if (config.backgroundColor != null) {
            return config.style == Style.GLASS
                    ? withAlpha(config.backgroundColor, config.glassOpacity)
                    : config.backgroundColor;
        }
        if (config.style == Style.GLASS) {
            return withAlpha(surface, config.glassOpacity);
        }
        return surface;
    }

    private int resolveAccent(int primary) {
        switch (config.type) {
            case SUCCESS: return 0xFF23865B;
            case ERROR:
                return MaterialColors.getColor(activity,
                        android.R.attr.colorError, 0xFFBA1A1A);
            case WARNING: return 0xFFB36B16;
            case INFO: return primary;
            case LOADING: return primary;
            case DEFAULT:
            default: return primary;
        }
    }

    private int dp(float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private static int withAlpha(int color, float alpha) {
        int sourceAlpha = Color.alpha(color);
        int a = Math.max(0, Math.min(255, Math.round(sourceAlpha * alpha)));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private static final class SwipeCard extends MaterialCardView {
        private final int touchSlop;
        private float downX;
        private float downY;
        private boolean dragging;
        private boolean swipeEnabled = true;
        private Runnable swipeDismiss;

        SwipeCard(Activity activity) {
            super(activity);
            touchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        }

        void setSwipeEnabled(boolean enabled) {
            swipeEnabled = enabled;
        }

        void setOnSwipeDismiss(Runnable callback) {
            swipeDismiss = callback;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (!swipeEnabled) {
                return super.onInterceptTouchEvent(event);
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    dragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - downX;
                    float dy = event.getY() - downY;
                    if (Math.abs(dx) > touchSlop * 1.4f && Math.abs(dx) > Math.abs(dy) * 1.15f) {
                        dragging = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    dragging = false;
                    break;
                default:
                    break;
            }
            return super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!dragging) {
                return super.onTouchEvent(event);
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    setTranslationX(event.getX() - downX);
                    return true;
                case MotionEvent.ACTION_UP:
                    float distance = Math.abs(getTranslationX());
                    boolean dismiss = distance > getWidth() * 0.28f;
                    dragging = false;
                    if (dismiss && swipeDismiss != null) {
                        animate().alpha(0f).translationX(Math.signum(getTranslationX()) * getWidth())
                                .setDuration(150L).withEndAction(swipeDismiss).start();
                    } else {
                        animate().alpha(1f).translationX(0f).setDuration(180L)
                                .setInterpolator(new DecelerateInterpolator()).start();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    animate().alpha(1f).translationX(0f).setDuration(180L)
                            .setInterpolator(new DecelerateInterpolator()).start();
                    return true;
                default:
                    return true;
            }
        }
    }

    /** Fluent configuration for a Flashbar. */
    public static final class Builder {
        private final Activity activity;
        private CharSequence title;
        private CharSequence message;
        private CharSequence primaryActionText;
        private CharSequence secondaryActionText;
        private OnActionClickListener primaryActionListener;
        private OnActionClickListener secondaryActionListener;
        private Drawable icon;
        private Gravity gravity = Gravity.BOTTOM;
        private Style style = Style.MATERIAL;
        private Type type = Type.DEFAULT;
        private long duration = DURATION_SHORT;
        private boolean showIcon = true;
        private boolean showCloseButton = true;
        private boolean swipeToDismiss = true;
        private boolean dismissOnAction = true;
        private boolean hapticFeedback;
        private boolean indeterminateProgress;
        private int progress = -1;
        private float glassOpacity = 0.82f;
        private float cornerRadiusDp = 26f;
        private Integer backgroundColor;
        private Integer titleColor;
        private Integer messageColor;

        public Builder(Activity activity) {
            if (activity == null) {
                throw new IllegalArgumentException("Activity must not be null.");
            }
            this.activity = activity;
        }

        public Builder title(CharSequence value) { title = value; return this; }
        public Builder message(CharSequence value) { message = value; return this; }
        public Builder gravity(Gravity value) { if (value != null) gravity = value; return this; }
        public Builder type(Type value) { if (value != null) type = value; return this; }
        public Builder style(Style value) { if (value != null) style = value; return this; }
        public Builder glassEffect(boolean enabled) { style = enabled ? Style.GLASS : Style.MATERIAL; return this; }
        public Builder glassOpacity(float value) {
            glassOpacity = Math.max(0.35f, Math.min(0.96f, value));
            return this;
        }
        public Builder duration(long value) {
            if (value != DURATION_INDEFINITE && value < 0L) {
                throw new IllegalArgumentException("Duration must be non-negative or DURATION_INDEFINITE.");
            }
            duration = value;
            return this;
        }
        public Builder icon(Drawable value) { icon = value; return this; }
        public Builder showIcon(boolean value) { showIcon = value; return this; }
        public Builder showCloseButton(boolean value) { showCloseButton = value; return this; }
        public Builder swipeToDismiss(boolean value) { swipeToDismiss = value; return this; }
        public Builder dismissOnAction(boolean value) { dismissOnAction = value; return this; }
        public Builder hapticFeedback(boolean value) { hapticFeedback = value; return this; }
        public Builder cornerRadius(float dp) {
            cornerRadiusDp = Math.max(8f, Math.min(40f, dp));
            return this;
        }
        public Builder backgroundColor(int color) { backgroundColor = color; return this; }
        public Builder titleColor(int color) { titleColor = color; return this; }
        public Builder messageColor(int color) { messageColor = color; return this; }
        public Builder progress(int value) {
            progress = Math.max(0, Math.min(100, value));
            indeterminateProgress = false;
            return this;
        }
        public Builder indeterminateProgress() {
            indeterminateProgress = true;
            progress = -1;
            return this;
        }
        public Builder noProgress() { progress = -1; indeterminateProgress = false; return this; }
        public Builder primaryActionText(CharSequence value) { primaryActionText = value; return this; }
        public Builder primaryActionTapListener(OnActionClickListener value) { primaryActionListener = value; return this; }
        public Builder primaryActionText(CharSequence text, OnActionClickListener listener) {
            primaryActionText = text;
            primaryActionListener = listener;
            return this;
        }
        public Builder secondaryActionText(CharSequence value) { secondaryActionText = value; return this; }
        public Builder secondaryActionTapListener(OnActionClickListener value) { secondaryActionListener = value; return this; }
        public Builder secondaryActionText(CharSequence text, OnActionClickListener listener) {
            secondaryActionText = text;
            secondaryActionListener = listener;
            return this;
        }

        public Flashbar build() {
            if ((title == null || title.length() == 0)
                    && (message == null || message.length() == 0)) {
                throw new IllegalStateException("Set at least a title or a message before building Flashbar.");
            }
            return new Flashbar(this);
        }

        /** Builds and immediately displays the notification. */
        public Flashbar show() {
            Flashbar flashbar = build();
            flashbar.show();
            return flashbar;
        }
    }
}
