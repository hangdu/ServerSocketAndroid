package com.example.hang.socketserver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by hang on 11/24/17.
 */

public class LabelDialog extends AppCompatDialogFragment {
    private EditText editLabel;
    private LabelDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (LabelDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +"must implement LabelDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle("Label")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String label = editLabel.getText().toString();
                        listener.applyText(label);

                    }
                });

        editLabel = view.findViewById(R.id.edit_label);
        return builder.create();
    }

    public interface LabelDialogListener {
        void applyText(String label);
    }
}
