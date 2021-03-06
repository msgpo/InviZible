package pan.alexander.tordnscrypt.utils;
/*
    This file is part of InviZible Pro.

    InviZible Pro is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    InviZible Pro is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with InviZible Pro.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2019-2020 by Garmatin Oleksandr invizible.soft@gmail.com
*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import pan.alexander.tordnscrypt.MainActivity;
import pan.alexander.tordnscrypt.R;
import pan.alexander.tordnscrypt.TopFragment;

public class Registration {
    private Context context;
    public static boolean wrongRegistrationCode = true;

    public Registration(Context context) {
        this.context = context;
    }

    public void showDonateDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
            builder.setMessage(R.string.donate_project)
                    .setTitle(R.string.donate)
                    .setPositiveButton(R.string.enter_code_button, (dialog, which) -> {
                        showEnterCodeDialog();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                    .setNeutralButton(R.string.donate_button, (dialogInterface, i) -> {
                        Intent donatePage = new Intent(Intent.ACTION_VIEW, Uri.parse("https://invizible.net/donate"));
                        context.startActivity(donatePage);
                        dialogInterface.dismiss();
                    })
                    .setCancelable(false);
            if (wrongRegistrationCode) {
                builder.show();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

    public void showEnterCodeDialog() {

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        @SuppressLint("InflateParams") final View inputView = inflater.inflate(R.layout.edit_text_for_dialog, null, false);
        final EditText editText = inputView.findViewById(R.id.etForDialog);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
        builder .setTitle(R.string.enter_code)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    new PrefManager(context).setStrPref("registrationCode",editText.getText().toString().trim());

                    wrongRegistrationCode = false;

                    TopFragment topFragment = (TopFragment) ((MainActivity)context).getSupportFragmentManager().findFragmentByTag("topFragmentTAG");
                    if (topFragment!=null) {
                        topFragment.checkNewVer();
                        MainActivity.modernDialog = ((MainActivity)context).modernProgressDialog();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .setCancelable(false)
                .setView(inputView);
        builder.show();
    }
}
