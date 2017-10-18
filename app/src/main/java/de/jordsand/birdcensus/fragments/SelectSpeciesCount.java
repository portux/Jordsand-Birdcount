package de.jordsand.birdcensus.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.activities.AddSighting;
import de.jordsand.birdcensus.activities.SpeciesCounter;
import de.jordsand.birdcensus.infrastructure.MinInputFilter;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Dialog to enter the count of a observed species
 */
public class SelectSpeciesCount extends DialogFragment {
    private static final int MIN_SPECIES_COUNT = 1;

    public interface OnSpeciesCountSelectedListener {
        void onSpeciesCountSelected(int count);
    }

    private EditText count;
    private Button startCounter;

    /**
     * Our callbacks will be sent here
     */
    private OnSpeciesCountSelectedListener mListener;

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_select_species_count, null);

        count = view.findViewById(R.id.sighting_count);
        count.setFilters(new MinInputFilter[]{new MinInputFilter(MIN_SPECIES_COUNT)});
        count.setText(String.format(Locale.GERMANY, "%d", MIN_SPECIES_COUNT));
        count.setSelection(count.getText().length());

        startCounter = view.findViewById(R.id.start_counter);
        startCounter.setOnClickListener(new OpenCounterOnClickListener());

        builder.setTitle(R.string.add_sighting_count)
                .setView(view)
                .setPositiveButton(R.string.ok, new CountSelectedOnClickListener());

        return builder.create();
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mListener = (OnSpeciesCountSelectedListener) ctx;
        } catch (ClassCastException e) {
            throw new ClassCastException(ctx.toString() + " must implement NoticeDialogListener");
        }
    }

    /**
     * The count was selected
     */
    private class CountSelectedOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            int selectedCount;
            try {
                selectedCount = Integer.parseInt(count.getText().toString());
                mListener.onSpeciesCountSelected(selectedCount);
            } catch (NumberFormatException e) {
                dismiss();
            }
        }
    }

    /**
     * A dedicated counter has been started
     */
    private class OpenCounterOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), SpeciesCounter.class);
            getActivity().startActivityForResult(intent, AddSighting.RQ_COUNTER);
        }
    }
}
