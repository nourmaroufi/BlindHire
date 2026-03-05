package Utils;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple navigation history stack.
 *
 * Usage — before swapping to a new view:
 *   NavigationManager.push(homeBorderPane.getCenter(), homeBorderPane);
 *   homeBorderPane.setCenter(newView);
 *
 * In every handleBack():
 *   NavigationManager.goBack();
 */
public class NavigationManager {

    private static final Deque<NavEntry> history = new ArrayDeque<>();

    /** Snapshot the current center node + the borderPane it lives in, then navigate. */
    public static void navigateTo(BorderPane borderPane, Node newView) {
        Node current = borderPane.getCenter();
        if (current != null) {
            history.push(new NavEntry(borderPane, current));
        }
        borderPane.setCenter(newView);
    }

    /** Pop and restore the previous view. Does nothing if history is empty. */
    public static void goBack() {
        if (history.isEmpty()) return;
        NavEntry prev = history.pop();
        prev.borderPane.setCenter(prev.view);
    }

    /** Check if there is anything to go back to. */
    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    /** Clear all history (e.g. on logout). */
    public static void clear() {
        history.clear();
    }

    // ── Internal record ───────────────────────────────────────────────────────

    private static class NavEntry {
        final BorderPane borderPane;
        final Node       view;

        NavEntry(BorderPane bp, Node v) {
            this.borderPane = bp;
            this.view       = v;
        }
    }
}