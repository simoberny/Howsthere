package it.howsthere.howsthere2.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;
import it.howsthere.howsthere2.R;

public final class AppDatePicker {
    private static final String SELECTION = "selection";
    private AppDatePicker() { }

    public static void register(FragmentManager manager, LifecycleOwner owner, String key,
                                Consumer<Date> onSelected) {
        manager.setFragmentResultListener(key, owner, (requestKey, result) ->
                onSelected.accept(CalendarSelection.fromPicker(result.getLong(SELECTION), ZoneId.systemDefault())));
        Fragment restored = manager.findFragmentByTag(key);
        if (restored instanceof MaterialDatePicker) connect(manager, key, (MaterialDatePicker<?>) restored);
    }

    public static void show(FragmentManager manager, String key, Date selected) {
        if (manager.isStateSaved() || manager.findFragmentByTag(key) != null) return;
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.date_selection)
                .setSelection(CalendarSelection.toPicker(selected, ZoneId.systemDefault()))
                .build();
        connect(manager, key, picker);
        picker.show(manager, key);
    }

    private static void connect(FragmentManager manager, String key, MaterialDatePicker<?> picker) {
        picker.clearOnPositiveButtonClickListeners();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (!(selection instanceof Long)) return;
            Bundle result = new Bundle();
            result.putLong(SELECTION, (Long) selection);
            manager.setFragmentResult(key, result);
        });
    }
}
