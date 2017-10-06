package de.jordsand.birdcensus.infrastructure;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Filter to ensure that the numeric content of an {@link android.widget.EditText} is larger than a certain value
 * @author Rico Bergmann
 */
public class MinInputFilter implements InputFilter {
    private int min;

    public MinInputFilter(int min) {
        this.min = min;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int inp = Integer.parseInt(dest.toString() + source.toString());
            if (inp >= min) {
                return null;
            }
        } catch (NumberFormatException e) { return ""; }
        return "";
    }
}
