package pan.alexander.tordnscrypt.settings;
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
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import pan.alexander.tordnscrypt.R;
import pan.alexander.tordnscrypt.dialogs.NotificationDialogFragment;
import pan.alexander.tordnscrypt.dialogs.NotificationHelper;
import pan.alexander.tordnscrypt.modules.ModulesRestarter;
import pan.alexander.tordnscrypt.utils.GetNewBridges;
import pan.alexander.tordnscrypt.utils.PrefManager;
import pan.alexander.tordnscrypt.utils.Verifier;
import pan.alexander.tordnscrypt.utils.enums.FileOperationsVariants;
import pan.alexander.tordnscrypt.utils.file_operations.FileOperations;
import pan.alexander.tordnscrypt.utils.file_operations.OnTextFileOperationsCompleteListener;

import static pan.alexander.tordnscrypt.TopFragment.TOP_BROADCAST;
import static pan.alexander.tordnscrypt.TopFragment.appSign;
import static pan.alexander.tordnscrypt.TopFragment.wrongSign;
import static pan.alexander.tordnscrypt.utils.GetNewBridges.dialogPleaseWait;
import static pan.alexander.tordnscrypt.utils.RootExecService.LOG_TAG;
import static pan.alexander.tordnscrypt.utils.enums.FileOperationsVariants.readTextFile;


public class PreferencesTorBridges extends Fragment implements View.OnClickListener, OnTextFileOperationsCompleteListener {
    private RadioButton rbNoBridges;
    private RadioButton rbDefaultBridges;
    private Spinner spDefaultBridges;
    private RadioButton rbOwnBridges;
    private Spinner spOwnBridges;
    private TextView tvBridgesListEmpty;
    private RecyclerView rvBridges;

    private PathVars pathVars;
    private List<String> tor_conf;
    private List<String> tor_conf_orig;
    private List<ObfsBridge> bridgeList;
    private List<String> currentBridges;
    private List<String> anotherBridges;
    private String currentBridgesType = "";
    private BridgeAdapter bridgeAdapter;
    private String bridges_file_path;
    private String bridges_custom_file_path;
    private final String torConfTag = "pan.alexander.tordnscrypt/app_data/tor/tor.conf";
    private final String defaultBridgesOperationTag = "pan.alexander.tordnscrypt/abstract_default_bridges_operation";
    private final String ownBridgesOperationTag = "pan.alexander.tordnscrypt/abstract_own_bridges_operation";
    private final String addBridgesTag = "pan.alexander.tordnscrypt/abstract_add_bridges";
    private final String addRequestedBridgesTag = "pan.alexander.tordnscrypt/abstract_add_requested_bridges";
    private String requestedBridgesToAdd;


    public PreferencesTorBridges() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() == null) {
            return;
        }

        pathVars = new PathVars(getActivity());

        bridges_custom_file_path = pathVars.appDataDir + "/app_data/tor/bridges_custom.lst";
        bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_default.lst";

        tor_conf = new LinkedList<>();
        tor_conf_orig = new LinkedList<>();

        bridgeList = new LinkedList<>();
        currentBridges = new LinkedList<>();
        anotherBridges = new LinkedList<>();

        FileOperations.setOnFileOperationCompleteListener(this);

        FileOperations.readTextFile(getActivity(), pathVars.appDataDir + "/app_data/tor/tor.conf", torConfTag);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preferences_tor_bridges, container, false);

        rbNoBridges = view.findViewById(R.id.rbNoBridges);
        rbNoBridges.setOnCheckedChangeListener(onCheckedChangeListener);

        rbDefaultBridges = view.findViewById(R.id.rbDefaultBridges);
        rbDefaultBridges.setOnCheckedChangeListener(onCheckedChangeListener);

        spDefaultBridges = view.findViewById(R.id.spDefaultBridges);
        spDefaultBridges.setOnItemSelectedListener(onItemSelectedListener);
        spDefaultBridges.setPrompt(getString(R.string.pref_fast_use_tor_bridges_obfs));

        rbOwnBridges = view.findViewById(R.id.rbOwnBridges);
        rbOwnBridges.setOnCheckedChangeListener(onCheckedChangeListener);

        spOwnBridges = view.findViewById(R.id.spOwnBridges);
        spOwnBridges.setOnItemSelectedListener(onItemSelectedListener);
        spOwnBridges.setPrompt(getString(R.string.pref_fast_use_tor_bridges_obfs));

        Button btnRequestBridges = view.findViewById(R.id.btnRequestBridges);
        btnRequestBridges.setOnClickListener(this);
        Button btnAddBridges = view.findViewById(R.id.btnAddBridges);
        btnAddBridges.setOnClickListener(this);

        tvBridgesListEmpty = view.findViewById(R.id.tvBridgesListEmpty);

        rvBridges = view.findViewById(R.id.rvBridges);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvBridges.setLayoutManager(mLayoutManager);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.pref_fast_use_tor_bridges);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null) {
            return;
        }

        if (!new PrefManager(getActivity()).getStrPref("defaultBridgesObfs").isEmpty())
            spDefaultBridges.setSelection(Integer.parseInt(new PrefManager(getActivity()).getStrPref("defaultBridgesObfs")));

        if (!new PrefManager(getActivity()).getStrPref("ownBridgesObfs").isEmpty())
            spOwnBridges.setSelection(Integer.parseInt(new PrefManager(getActivity()).getStrPref("ownBridgesObfs")));


        bridgeAdapter = new BridgeAdapter(bridgeList);
        rvBridges.setAdapter(bridgeAdapter);

        boolean useNoBridges = new PrefManager(getActivity()).getBoolPref("useNoBridges");
        boolean useDefaultBridges = new PrefManager(getActivity()).getBoolPref("useDefaultBridges");
        boolean useOwnBridges = new PrefManager(getActivity()).getBoolPref("useOwnBridges");

        if (!useNoBridges && !useDefaultBridges && !useOwnBridges) {
            rbNoBridges.setChecked(true);
            tvBridgesListEmpty.setVisibility(View.GONE);
        } else if (useNoBridges) {
            noBridgesOperation();
            rbNoBridges.setChecked(true);
        } else if (useDefaultBridges) {
            FileOperations.readTextFile(getActivity(), bridges_file_path, defaultBridgesOperationTag);

            rbDefaultBridges.setChecked(true);
        } else {
            bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_custom.lst";
            FileOperations.readTextFile(getActivity(), bridges_file_path, ownBridgesOperationTag);

            rbOwnBridges.setChecked(true);
        }

        Thread thread = new Thread(() -> {
            try {
                Verifier verifier = new Verifier(getActivity());
                String appSignAlt = verifier.getApkSignature();
                if (!verifier.decryptStr(wrongSign, appSign, appSignAlt).equals(TOP_BROADCAST)) {

                    if (getFragmentManager() != null) {
                        NotificationHelper notificationHelper = NotificationHelper.setHelperMessage(
                                getActivity(), getText(R.string.verifier_error).toString(), "3458");
                        if (notificationHelper != null) {
                            notificationHelper.show(getFragmentManager(), NotificationHelper.TAG_HELPER);
                        }
                    }
                }

            } catch (Exception e) {
                if (getFragmentManager() != null) {
                    NotificationHelper notificationHelper = NotificationHelper.setHelperMessage(
                            getActivity(), getText(R.string.verifier_error).toString(), "64539");
                    if (notificationHelper != null) {
                        notificationHelper.show(getFragmentManager(), NotificationHelper.TAG_HELPER);
                    }
                }
                Log.e(LOG_TAG, "PreferencesTorBridges fault " + e.getMessage() + " " + e.getCause() + System.lineSeparator() +
                        Arrays.toString(e.getStackTrace()));
            }
        });
        thread.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getActivity() == null) {
            return;
        }

        List<String> tor_conf_clean = new LinkedList<>();

        for (int i = 0; i < tor_conf.size(); i++) {
            String line = tor_conf.get(i);
            if ((line.contains("#") || (!line.contains("Bridge ") && !line.contains("ClientTransportPlugin "))) && !line.isEmpty()) {
                tor_conf_clean.add(line);
            }
        }

        tor_conf = tor_conf_clean;

        String currentBridgesTypeToSave;
        if (currentBridgesType.equals("none")) {
            currentBridgesTypeToSave = "";
        } else {
            currentBridgesTypeToSave = currentBridgesType;
        }

        if (!currentBridges.isEmpty() && !currentBridgesType.isEmpty()) {

            if (!currentBridgesType.equals("none")) {
                String clientTransportPlugin = "ClientTransportPlugin " + currentBridgesTypeToSave + " exec "
                        + pathVars.obfsPath;

                tor_conf.add(clientTransportPlugin);
            }

            for (int i = 0; i < currentBridges.size(); i++) {
                tor_conf.add("Bridge " + currentBridges.get(i));
            }
        } else {
            for (int i = 0; i < tor_conf.size(); i++) {
                if (tor_conf.get(i).contains("UseBridges")) {
                    String line = tor_conf.get(i);
                    String result = line.replace("1", "0");
                    if (!result.equals(line)) {
                        tor_conf.set(i, result);
                    }
                }
            }
        }

        if (Arrays.equals(tor_conf.toArray(), tor_conf_orig.toArray()))
            return;

        FileOperations.writeToTextFile(getActivity(), pathVars.appDataDir + "/app_data/tor/tor.conf", tor_conf, "ignored");

        ///////////////////////Tor restart/////////////////////////////////////////////
        boolean torRunning = new PrefManager(getActivity()).getBoolPref("Tor Running");

        if (torRunning) {
            ModulesRestarter.restartTor(getActivity());
            Toast.makeText(getActivity(), getText(R.string.toastSettings_saved), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dialogPleaseWait != null) {
            dialogPleaseWait.cancel();
        }

        FileOperations.deleteOnFileOperationCompleteListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddBridges:
                FileOperations.readTextFile(getActivity(), pathVars.appDataDir + "/app_data/tor/bridges_custom.lst", addBridgesTag);
                break;
            case R.id.btnRequestBridges:
                GetNewBridges getNewBridges = new GetNewBridges(getActivity());
                getNewBridges.selectTransport();
                break;
        }
    }

    private void addBridges(final List<String> persistList) {

        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View inputView = inflater.inflate(R.layout.edit_text_for_dialog, null, false);
        final EditText input = inputView.findViewById(R.id.etForDialog);
        input.setSingleLine(false);
        builder.setView(inputView);

        builder.setPositiveButton(getText(R.string.ok), (dialogInterface, i) -> {
            List<String> bridgesListNew = new LinkedList<>();
            String[] bridgesArrNew = input.getText().toString().split(System.lineSeparator());

            if (bridgesArrNew.length != 0) {
                for (String brgNew : bridgesArrNew) {
                    if (!brgNew.isEmpty()) {
                        bridgesListNew.add(brgNew.trim());
                    }
                }

                if (persistList != null) {
                    List<String> retainList = new LinkedList<>(persistList);
                    retainList.retainAll(bridgesListNew);
                    bridgesListNew.removeAll(retainList);
                    persistList.addAll(bridgesListNew);
                    bridgesListNew = persistList;
                }


                Collections.sort(bridgesListNew);
                FileOperations.writeToTextFile(getActivity(), bridges_custom_file_path, bridgesListNew, "ignored");

                if (getActivity() == null) {
                    return;
                }

                boolean useOwnBridges = new PrefManager(getActivity()).getBoolPref("useOwnBridges");
                if (useOwnBridges) {
                    ownBridgesOperation(bridgesListNew);
                } else {
                    rbOwnBridges.performClick();
                }
            }
        });

        builder.setNegativeButton(getText(R.string.cancel), (dialog, i) -> dialog.cancel());
        builder.setTitle(R.string.pref_fast_use_tor_bridges_add);
        builder.show();
    }

    private void addRequestedBridges(String bridges, List<String> persistList) {
        List<String> bridgesListNew = new LinkedList<>();
        String[] bridgesArrNew = bridges.split(System.lineSeparator());

        if (bridgesArrNew.length != 0) {
            for (String brgNew : bridgesArrNew) {
                if (!brgNew.isEmpty()) {
                    bridgesListNew.add(brgNew.trim());
                }
            }

            if (persistList != null) {
                List<String> retainList = new LinkedList<>(persistList);
                retainList.retainAll(bridgesListNew);
                bridgesListNew.removeAll(retainList);
                persistList.addAll(bridgesListNew);
                bridgesListNew = persistList;
            }


            Collections.sort(bridgesListNew);
            FileOperations.writeToTextFile(getActivity(), bridges_custom_file_path, bridgesListNew, "ignored");

            if (!bridges.isEmpty()) {
                if (bridges.contains("obfs4")) {
                    currentBridgesType = "obfs4";
                    if (!spOwnBridges.getSelectedItem().toString().equals("obfs4")) {
                        spOwnBridges.setSelection(0);
                    } else {
                        ownBridgesOperation(bridgesListNew);
                    }
                } else if (bridges.contains("obfs3")) {
                    currentBridgesType = "obfs3";
                    if (!spOwnBridges.getSelectedItem().toString().equals("obfs3")) {
                        spOwnBridges.setSelection(1);
                    } else {
                        ownBridgesOperation(bridgesListNew);
                    }
                } else if (bridges.contains("scramblesuit")) {
                    currentBridgesType = "scramblesuit";
                    if (!spOwnBridges.getSelectedItem().toString().equals("scramblesuit")) {
                        spOwnBridges.setSelection(2);
                    } else {
                        ownBridgesOperation(bridgesListNew);
                    }
                } else if (bridges.contains("meek_lite")) {
                    currentBridgesType = "meek_lite";
                    if (!spOwnBridges.getSelectedItem().toString().equals("meek_lite")) {
                        spOwnBridges.setSelection(3);
                    } else {
                        ownBridgesOperation(bridgesListNew);
                    }
                } else {
                    currentBridgesType = "none";
                    if (!spOwnBridges.getSelectedItem().toString().equals("none")) {
                        spOwnBridges.setSelection(4);
                    } else {
                        ownBridgesOperation(bridgesListNew);
                    }
                }

            }

            if (getActivity() == null) {
                return;
            }

            boolean useOwnBridges = new PrefManager(getActivity()).getBoolPref("useOwnBridges");

            if (!useOwnBridges) {
                rbOwnBridges.performClick();
            }
        }
    }

    public void readCurrentCustomBridges(String bridges) {
        requestedBridgesToAdd = bridges;

        FileOperations.readTextFile(getActivity(), bridges_custom_file_path, addRequestedBridgesTag);
    }


    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean newValue) {

            if (getActivity() == null) {
                return;
            }

            switch (compoundButton.getId()) {
                case R.id.rbNoBridges:
                    if (newValue) {
                        new PrefManager(getActivity()).setBoolPref("useNoBridges", true);
                        new PrefManager(getActivity()).setBoolPref("useDefaultBridges", false);
                        new PrefManager(getActivity()).setBoolPref("useOwnBridges", false);

                        noBridgesOperation();

                        for (int i = 0; i < tor_conf.size(); i++) {
                            if (tor_conf.get(i).contains("UseBridges")) {
                                String line = tor_conf.get(i);
                                String result = line.replace("1", "0");
                                if (!result.equals(line)) {
                                    tor_conf.set(i, result);
                                }
                            }
                        }
                    }

                    break;
                case R.id.rbDefaultBridges:
                    if (newValue) {
                        new PrefManager(getActivity()).setBoolPref("useNoBridges", false);
                        new PrefManager(getActivity()).setBoolPref("useDefaultBridges", true);
                        new PrefManager(getActivity()).setBoolPref("useOwnBridges", false);

                        bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_default.lst";

                        FileOperations.readTextFile(getActivity(), bridges_file_path, defaultBridgesOperationTag);

                        for (int i = 0; i < tor_conf.size(); i++) {
                            if (tor_conf.get(i).contains("UseBridges")) {
                                String line = tor_conf.get(i);
                                String result = line.replace("0", "1");
                                if (!result.equals(line)) {
                                    tor_conf.set(i, result);
                                }
                            }
                        }
                    }

                    break;
                case R.id.rbOwnBridges:
                    if (newValue) {
                        new PrefManager(getActivity()).setBoolPref("useNoBridges", false);
                        new PrefManager(getActivity()).setBoolPref("useDefaultBridges", false);
                        new PrefManager(getActivity()).setBoolPref("useOwnBridges", true);

                        bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_custom.lst";

                        FileOperations.readTextFile(getActivity(), bridges_file_path, ownBridgesOperationTag);

                        for (int i = 0; i < tor_conf.size(); i++) {
                            if (tor_conf.get(i).contains("UseBridges")) {
                                String line = tor_conf.get(i);
                                String result = line.replace("0", "1");
                                if (!result.equals(line)) {
                                    tor_conf.set(i, result);
                                }
                            }
                        }
                    }

                    break;
            }

        }
    };

    private void noBridgesOperation() {
        rbDefaultBridges.setChecked(false);
        rbOwnBridges.setChecked(false);

        bridgeList.clear();
        currentBridges.clear();
        bridgeAdapter.notifyDataSetChanged();

        tvBridgesListEmpty.setVisibility(View.GONE);
    }

    private void defaultBridgesOperation(List<String> bridges_default) {
        rbNoBridges.setChecked(false);
        rbOwnBridges.setChecked(false);

        bridgeList.clear();
        anotherBridges.clear();

        String obfsTypeSp = spDefaultBridges.getSelectedItem().toString();

        if (bridges_default == null)
            return;

        for (String line : bridges_default) {
            ObfsBridge obfsBridge;
            if (line.contains(obfsTypeSp)) {
                obfsBridge = new ObfsBridge(line, obfsTypeSp, false);
                if (currentBridges.contains(line)) {
                    obfsBridge.active = true;
                }
                bridgeList.add(obfsBridge);
            } else {
                anotherBridges.add(line);
            }
        }

        bridgeAdapter.notifyDataSetChanged();

        if (bridgeList.isEmpty()) {
            tvBridgesListEmpty.setVisibility(View.VISIBLE);
        } else {
            tvBridgesListEmpty.setVisibility(View.GONE);
        }
    }

    private void ownBridgesOperation(List<String> bridges_custom) {
        rbNoBridges.setChecked(false);
        rbDefaultBridges.setChecked(false);

        bridgeList.clear();
        anotherBridges.clear();

        String obfsTypeSp = spOwnBridges.getSelectedItem().toString();

        if (bridges_custom == null)
            return;

        for (String line : bridges_custom) {
            ObfsBridge obfsBridge;
            if (!obfsTypeSp.equals("none") && line.contains(obfsTypeSp)) {
                obfsBridge = new ObfsBridge(line, obfsTypeSp, false);
                if (currentBridges.contains(line)) {
                    obfsBridge.active = true;
                }
                bridgeList.add(obfsBridge);
            } else if (obfsTypeSp.equals("none") && !line.contains("obfs4") && !line.contains("obfs3")
                    && !line.contains("scramblesuit") && !line.contains("meek_lite") && !line.isEmpty()) {
                obfsBridge = new ObfsBridge(line, obfsTypeSp, false);
                if (currentBridges.contains(line)) {
                    obfsBridge.active = true;
                }
                bridgeList.add(obfsBridge);
            } else {
                anotherBridges.add(line);
            }
        }

        bridgeAdapter.notifyDataSetChanged();

        if (bridgeList.isEmpty()) {
            tvBridgesListEmpty.setVisibility(View.VISIBLE);
        } else {
            tvBridgesListEmpty.setVisibility(View.GONE);
        }
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            if (getActivity() == null) {
                return;
            }

            switch (adapterView.getId()) {
                case R.id.spDefaultBridges:
                    new PrefManager(getActivity()).setStrPref("defaultBridgesObfs", String.valueOf(i));
                    if (rbDefaultBridges.isChecked()) {
                        bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_default.lst";

                        FileOperations.readTextFile(getActivity(), bridges_file_path, defaultBridgesOperationTag);
                    }
                    break;
                case R.id.spOwnBridges:
                    new PrefManager(getActivity()).setStrPref("ownBridgesObfs", String.valueOf(i));
                    if (rbOwnBridges.isChecked()) {
                        bridges_file_path = pathVars.appDataDir + "/app_data/tor/bridges_custom.lst";
                        FileOperations.readTextFile(getActivity(), bridges_file_path, ownBridgesOperationTag);
                    }
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public void OnFileOperationComplete(FileOperationsVariants currentFileOperation, boolean fileOperationResult, String path, String tag, List<String> lines) {

        if (getActivity() == null) {
            return;
        }

        if (fileOperationResult && currentFileOperation == readTextFile) {
            switch (tag) {
                case torConfTag:
                    tor_conf = lines;

                    if (tor_conf == null) return;

                    tor_conf_orig.addAll(tor_conf);

                    for (int i = 0; i < tor_conf.size(); i++) {
                        String line = tor_conf.get(i);
                        if (!line.contains("#") && line.contains("Bridge ")) {
                            currentBridges.add(line.replace("Bridge ", "").trim());
                        }
                    }

                    if (!currentBridges.isEmpty()) {
                        String testBridge = currentBridges.get(0);
                        if (testBridge.contains("obfs4")) {
                            currentBridgesType = "obfs4";
                        } else if (testBridge.contains("obfs3")) {
                            currentBridgesType = "obfs3";
                        } else if (testBridge.contains("scramblesuit")) {
                            currentBridgesType = "scramblesuit";
                        } else if (testBridge.contains("meek_lite")) {
                            currentBridgesType = "meek_lite";
                        } else {
                            currentBridgesType = "none";
                        }
                    } else {
                        currentBridgesType = "";
                    }
                    break;
                case addBridgesTag: {
                    final List<String> bridges_lst = lines;
                    if (bridges_lst != null) {
                        getActivity().runOnUiThread(() -> addBridges(bridges_lst));
                    }
                    break;
                }
                case defaultBridgesOperationTag: {
                    final List<String> bridges_lst = lines;
                    if (bridges_lst != null) {
                        getActivity().runOnUiThread(() -> defaultBridgesOperation(bridges_lst));
                    }
                    break;
                }
                case ownBridgesOperationTag: {
                    final List<String> bridges_lst = lines;
                    if (bridges_lst != null) {
                        getActivity().runOnUiThread(() -> ownBridgesOperation(bridges_lst));
                    }
                    break;
                }
                case addRequestedBridgesTag: {
                    final List<String> bridges_lst = lines;
                    if (bridges_lst != null) {
                        getActivity().runOnUiThread(() -> addRequestedBridges(requestedBridgesToAdd, bridges_lst));
                    }
                    break;
                }
            }
        }
    }


    private class ObfsBridge {
        String bridge;
        String obfsType;
        boolean active;

        ObfsBridge(String bridge, String obfsType, boolean active) {
            this.bridge = bridge;
            this.obfsType = obfsType;
            this.active = active;
        }
    }

    private class BridgeAdapter extends RecyclerView.Adapter<PreferencesTorBridges.BridgeAdapter.BridgeViewHolder> {
        private List<ObfsBridge> listBridges;

        private LayoutInflater lInflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        private BridgeAdapter(List<ObfsBridge> listBridges) {
            this.listBridges = listBridges;
        }

        @NonNull
        @Override
        public BridgeAdapter.BridgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = lInflater.inflate(R.layout.item_bridge, parent, false);
            return new BridgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BridgeAdapter.BridgeViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return listBridges.size();
        }

        private ObfsBridge getItem(int position) {
            return listBridges.get(position);
        }

        private void setActive(int position, boolean active) {
            ObfsBridge brg = listBridges.get(position);
            brg.active = active;
            listBridges.set(position, brg);
        }

        private class BridgeViewHolder extends RecyclerView.ViewHolder {

            private TextView tvBridge;
            private SwitchCompat swBridge;

            BridgeViewHolder(@NonNull View itemView) {
                super(itemView);

                tvBridge = itemView.findViewById(R.id.tvBridge);
                swBridge = itemView.findViewById(R.id.swBridge);
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, newValue) -> {
                    if (newValue) {
                        String obfsType = getItem(getAdapterPosition()).obfsType;
                        if (!obfsType.equals(currentBridgesType)) {
                            currentBridges.clear();
                            currentBridgesType = obfsType;
                        }

                        boolean unicBridge = true;
                        for (int i = 0; i < currentBridges.size(); i++) {
                            String brg = currentBridges.get(i);
                            if (brg.equals(getItem(getAdapterPosition()).bridge)) {
                                unicBridge = false;
                                break;
                            }
                        }
                        if (unicBridge)
                            currentBridges.add(getItem(getAdapterPosition()).bridge);
                    } else {
                        for (int i = 0; i < currentBridges.size(); i++) {
                            String brg = currentBridges.get(i);
                            if (brg.equals(getItem(getAdapterPosition()).bridge)) {
                                currentBridges.remove(i);
                                break;
                            }

                        }
                    }
                    setActive(getAdapterPosition(), newValue);
                };
                swBridge.setOnCheckedChangeListener(onCheckedChangeListener);
                ImageButton ibtnBridgeDel = itemView.findViewById(R.id.ibtnBridgeDel);
                View.OnClickListener onClickListener = view -> {
                    switch (view.getId()) {
                        case R.id.cardBridge:
                            editBridge(getAdapterPosition());
                            break;
                        case R.id.ibtnBridgeDel:
                            deleteBridge(getAdapterPosition());
                            break;
                    }
                };
                ibtnBridgeDel.setOnClickListener(onClickListener);
                CardView cardBridge = itemView.findViewById(R.id.cardBridge);
                cardBridge.setOnClickListener(onClickListener);
            }

            private void bind(int position) {
                String[] bridgeIP = listBridges.get(position).bridge.split(" ");
                String tvBridgeText;
                if (bridgeIP[0].contains("obfs3") || bridgeIP[0].contains("obfs4")
                        || bridgeIP[0].contains("scramblesuit") || bridgeIP[0].contains("meek_lite")) {
                    tvBridgeText = bridgeIP[0] + " " + bridgeIP[1];
                } else {
                    tvBridgeText = bridgeIP[0];
                }

                tvBridge.setText(tvBridgeText);
                if (listBridges.get(position).active) {
                    swBridge.setChecked(true);
                } else {
                    swBridge.setChecked(false);
                }

            }

            private void editBridge(final int position) {

                if (getActivity() == null) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogTheme);
                builder.setTitle(R.string.pref_fast_use_tor_bridges_edit);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                @SuppressLint("InflateParams") final View inputView = inflater.inflate(R.layout.edit_text_for_dialog, null, false);
                final EditText input = inputView.findViewById(R.id.etForDialog);
                input.setSingleLine(false);
                String brgEdit = bridgeList.get(position).bridge;
                final String obfsTypeEdit = bridgeList.get(position).obfsType;
                final boolean activeEdit = bridgeList.get(position).active;
                if (activeEdit && getFragmentManager() != null) {
                    DialogFragment commandResult
                            = NotificationDialogFragment.newInstance(getString(R.string.pref_fast_use_tor_bridges_deactivate));
                    commandResult.show(getFragmentManager(), "NotificationDialogFragment");
                    return;
                }
                input.setText(brgEdit, TextView.BufferType.EDITABLE);
                builder.setView(inputView);

                builder.setPositiveButton(getText(R.string.ok), (dialog, i) -> {
                    ObfsBridge brg = new ObfsBridge(input.getText().toString(), obfsTypeEdit, false);
                    bridgeList.set(position, brg);
                    bridgeAdapter.notifyItemChanged(position);

                    List<String> tmpList = new LinkedList<>();
                    for (ObfsBridge tmpObfs : bridgeList) {
                        tmpList.add(tmpObfs.bridge);
                    }
                    tmpList.addAll(anotherBridges);
                    Collections.sort(tmpList);
                    if (bridges_file_path != null)
                        FileOperations.writeToTextFile(getActivity(), bridges_file_path, tmpList, "ignored");
                });
                builder.setNegativeButton(getText(R.string.cancel), (dialog, i) -> dialog.cancel());
                builder.show();
            }

            private void deleteBridge(int position) {
                if (bridgeList.get(position).active && getFragmentManager() != null) {
                    DialogFragment commandResult
                            = NotificationDialogFragment.newInstance(getString(R.string.pref_fast_use_tor_bridges_deactivate));
                    commandResult.show(getFragmentManager(), "NotificationDialogFragment");
                    return;
                }
                bridgeList.remove(position);
                bridgeAdapter.notifyItemRemoved(position);

                List<String> tmpList = new LinkedList<>();
                for (ObfsBridge tmpObfs : bridgeList) {
                    tmpList.add(tmpObfs.bridge);
                }
                tmpList.addAll(anotherBridges);
                Collections.sort(tmpList);
                if (bridges_file_path != null)
                    FileOperations.writeToTextFile(getActivity(), bridges_file_path, tmpList, "ignored");
            }
        }
    }
}
