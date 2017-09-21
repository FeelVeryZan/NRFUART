package com.example.Zan.nrfuart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.PopupWindow;

/**
 * Created by Administrator_nodgd on 2017/09/21.
 */

public class CreateCardDialogFragment extends DialogFragment {

    private PopupWindow mWindow;

    public CreateCardDialogFragment(PopupWindow window) {
        super();
        mWindow = window;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure to close and abandon your content?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mWindow.dismiss();
            }
        });
        return builder.create();
    }
}
