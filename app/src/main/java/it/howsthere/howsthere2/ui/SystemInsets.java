package it.howsthere.howsthere2.ui;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Apply safe areas once at the page root, including side cutouts and the keyboard. */
public final class SystemInsets {
    private SystemInsets() { }

    public static void apply(View root) {
        apply(root, false);
    }

    /** Let each page protect its controls while its background reaches the status bar. */
    public static void applyWithStatusBarBackground(View root) {
        apply(root, true);
    }

    private static void apply(View root, boolean drawBehindStatusBar) {
        int left = root.getPaddingLeft(), top = root.getPaddingTop();
        int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(left + safe.left, top + (drawBehindStatusBar ? 0 : safe.top),
                    right + safe.right, bottom + safe.bottom);
            if (drawBehindStatusBar) {
                // Forward only the space not already handled by this container.
                return insets.inset(safe.left, 0, safe.right, safe.bottom);
            }
            // BottomNavigationView otherwise applies the navigation inset a second time.
            return WindowInsetsCompat.CONSUMED;
        });
        root.post(() -> ViewCompat.requestApplyInsets(root));
    }
}
