package com.example.Zan.nrfuart;

/**
 * Created by Angel on 2017/4/7.
 */


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class NoBTDeviceAlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder NoBTDeviceBuilder = new AlertDialog.Builder(getActivity());
        NoBTDeviceBuilder.setMessage("BlueTooth is not available. Continue using UART?"
        ).setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ActivityCollecter.finishall();
            }
        });

        return NoBTDeviceBuilder.create();
    }
}
