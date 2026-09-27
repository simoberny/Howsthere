package it.howsthere.howsthere2.objects;

import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class PanoramaStorageTest {
    private PanoramaStorage storage;
    private final Map<String, String> values = new HashMap<>();
    private int writes;

    @Before public void setUp() throws Exception {
        // In-memory preferences exercise the actual JSON round trip without an Android device.
        SharedPreferences.Editor editor = (SharedPreferences.Editor) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{SharedPreferences.Editor.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("putString")) values.put((String) args[0], (String) args[1]);
                    if (method.getName().equals("apply")) { writes++; return null; }
                    return proxy;
                });
        SharedPreferences preferences = (SharedPreferences) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{SharedPreferences.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getString")) return values.getOrDefault(args[0], (String) args[1]);
                    if (method.getName().equals("edit")) return editor;
                    throw new UnsupportedOperationException(method.getName());
                });
        storage = PanoramaStorage.getInstance();
        Field field = PanoramaStorage.class.getDeclaredField("pref");
        field.setAccessible(true);
        field.set(storage, preferences);
    }

    @Test public void batchDeletionWritesOnceAndKeepsUnselectedItems() {
        storage.addPanorama(panorama("a"));
        storage.addPanorama(panorama("b"));
        storage.addPanorama(panorama("c"));
        int before = writes;
        storage.deleteByIds(Arrays.asList("a", "c", "missing"));
        assertEquals(before + 1, writes);
        List<Panorama> remaining = storage.getAllPanorama();
        assertEquals(1, remaining.size());
        assertEquals("b", remaining.get(0).id);
    }

    @Test public void snapshotsCannotMutateStorageAndUpdatesReplaceExistingIds() {
        storage.addPanorama(panorama("a"));
        storage.getAllPanorama().clear();
        Panorama replacement = panorama("a");
        replacement.city = "Updated";
        storage.addPanorama(replacement);
        assertEquals(1, storage.getAllPanorama().size());
        assertEquals("Updated", storage.getPanoramaByID("a").city);
        assertNull(storage.getPanoramaByID("missing"));
    }

    private static Panorama panorama(String id) {
        Panorama panorama = new Panorama();
        panorama.id = id;
        return panorama;
    }
}
