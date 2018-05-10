package com.example.Crofun.Hmilab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by nodgd on 2017/09/24.
 */

public class CreateCardIgnoreFragment extends DialogFragment {

    private String ignoreMessage;

    public CreateCardIgnoreFragment(String ignoreMessage) {
        super();
        this.ignoreMessage = ignoreMessage;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(ignoreMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return builder.create();
    }
}
