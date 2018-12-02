/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bittube.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bittube.wallet.dialog.HelpFragment;
import com.bittube.wallet.layout.WalletInfoAdapter;
import com.bittube.wallet.model.NetworkType;
import com.bittube.wallet.model.WalletManager;
import com.bittube.wallet.network.Callback;
import com.bittube.wallet.network.impl.FirebaseCloudFunctions;
import com.bittube.wallet.network.models.OnlineWallet;
import com.bittube.wallet.util.Helper;
import com.bittube.wallet.util.NodeList;
import com.bittube.wallet.widget.DropDownEditText;
import com.bittube.wallet.widget.InputLayout;
import com.bittube.wallet.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class LoginFragment extends Fragment implements WalletInfoAdapter.OnInteractionListener,
        View.OnClickListener {

    private Context mContext;

    private WalletInfoAdapter adapter;

    private List<WalletManager.WalletInfo> walletList = new ArrayList<>();
    private List<WalletManager.WalletInfo> displayedList = new ArrayList<>();

    private EditText etDummy;
    private ImageView ivGunther;
    private DropDownEditText etDaemonAddress;
    private InputLayout dropDownIL;
    private ArrayAdapter<String> nodeAdapter;

    private View llXmrToEnabled;
    private View ibXmrToInfoClose;

    private Listener activityCallback;

    // Container Activity must implement this interface
    public interface Listener {
        SharedPreferences getPrefs();

        File getStorageRoot();

        boolean onWalletSelected(String wallet, String daemon);

        void onWalletDetails(String wallet);

        void onWalletReceive(String wallet);

        void onWalletRename(String name);

        void onWalletBackup(String name);

        void onWalletArchive(String walletName);

        void onAddWallet(String type);

        void showNet();

        void setToolbarButton(int type);

        void setTitle(String title);

        void setNetworkType(NetworkType networkType);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof Listener) {
            this.activityCallback = (Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause()");
        savePrefs();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        activityCallback.setTitle(null);
        activityCallback.setToolbarButton(Toolbar.BUTTON_CREDITS);
        activityCallback.showNet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView");
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        ivGunther = (ImageView) view.findViewById(R.id.ivGunther);
        fabScreen = (FrameLayout) view.findViewById(R.id.fabScreen);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fabNew = (FloatingActionButton) view.findViewById(R.id.fabNew);
        fabView = (FloatingActionButton) view.findViewById(R.id.fabView);
        fabKey = (FloatingActionButton) view.findViewById(R.id.fabKey);
        fabSeed = (FloatingActionButton) view.findViewById(R.id.fabSeed);

        fabNewL = (RelativeLayout) view.findViewById(R.id.fabNewL);
        fabViewL = (RelativeLayout) view.findViewById(R.id.fabViewL);
        fabKeyL = (RelativeLayout) view.findViewById(R.id.fabKeyL);
        fabSeedL = (RelativeLayout) view.findViewById(R.id.fabSeedL);

        fab_pulse = AnimationUtils.loadAnimation(getContext(), R.anim.fab_pulse);
        fab_open_screen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open_screen);
        fab_close_screen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close_screen);
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward);
        fab.setOnClickListener(this);
        fabNew.setOnClickListener(this);
        fabView.setOnClickListener(this);
        fabKey.setOnClickListener(this);
        fabSeed.setOnClickListener(this);
        fabScreen.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        registerForContextMenu(recyclerView);
        this.adapter = new WalletInfoAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);

        etDummy = (EditText) view.findViewById(R.id.etDummy);

        llXmrToEnabled = view.findViewById(R.id.llXmrToEnabled);
        llXmrToEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpFragment.display(getChildFragmentManager(), R.string.help_xmrto);

            }
        });
        ibXmrToInfoClose = view.findViewById(R.id.ibXmrToInfoClose);
        ibXmrToInfoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llXmrToEnabled.setVisibility(View.GONE);
                showXmrtoEnabled = false;
                saveXmrToPrefs();
            }
        });

        dropDownIL = view.findViewById(R.id.il_dropdown);

        etDaemonAddress = dropDownIL.getDropEditText();
        nodeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
        etDaemonAddress.setAdapter(nodeAdapter);

        Helper.hideKeyboard(getActivity());

        etDaemonAddress.setThreshold(0);
        etDaemonAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDaemonAddress.showDropDown();
                Helper.showKeyboard(getActivity());
            }
        });

        etDaemonAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !getActivity().isFinishing() && etDaemonAddress.isLaidOut()) {
                    etDaemonAddress.showDropDown();
                    Helper.showKeyboard(getActivity());
                }
            }
        });

        etDaemonAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Helper.hideKeyboard(getActivity());
                    etDummy.requestFocus();
                    return true;
                }
                return false;
            }
        });

        etDaemonAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                Helper.hideKeyboard(getActivity());
                etDummy.requestFocus();

            }
        });

        loadPrefs();
        if (!showXmrtoEnabled) {
            llXmrToEnabled.setVisibility(View.GONE);
        }

        return view;
    }

    // Callbacks from WalletInfoAdapter
    @Override
    public void onInteraction(final View view, final WalletManager.WalletInfo infoItem) {
        String addressPrefix = addressPrefix();
        if (addressPrefix.indexOf(infoItem.address.charAt(0)) < 0) {
            Toast.makeText(mContext, getString(R.string.prompt_wrong_net), Toast.LENGTH_LONG).show();
            return;
        }

        if (activityCallback.onWalletSelected(infoItem.name, getDaemon())) {
            savePrefs();
        }
    }

    @Override
    public boolean onContextInteraction(MenuItem item, WalletManager.WalletInfo listItem) {
        switch (item.getItemId()) {
            case R.id.action_info:
                showInfo(listItem.name);
                break;
            case R.id.action_receive:
                showReceive(listItem.name);
                break;
            case R.id.action_rename:
                activityCallback.onWalletRename(listItem.name);
                break;
            case R.id.action_backup:
                activityCallback.onWalletBackup(listItem.name);
                break;
            case R.id.action_archive:
                activityCallback.onWalletArchive(listItem.name);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private String addressPrefix() {
        switch (WalletManager.getInstance().getNetworkType()) {
            case NetworkType_Testnet:
                return "b";
            case NetworkType_Mainnet:
                return "b";
            case NetworkType_Stagenet:
                return "b";
            default:
                throw new IllegalStateException("Unsupported Network: " + WalletManager.getInstance().getNetworkType());
        }
    }

    private void filterList() {
        displayedList.clear();
        String addressPrefix = addressPrefix();
        for (WalletManager.WalletInfo s : walletList) {
            if (addressPrefix.indexOf(s.address.charAt(0)) >= 0) displayedList.add(s);
        }
    }


    private void loadWalletsFromCloud() {
        // Load Local wallets and update list
        final List<WalletManager.WalletInfo> localWallets = loadLocalWallets();
        updateWalletList(localWallets);

        // Load online wallets(Using AUTH user and endpoint  update list from cloud functions
        String usertoken = XmrWalletApplication.getUserToken();
        FirebaseCloudFunctions fcf = new FirebaseCloudFunctions();
        fcf.getUserWallets(usertoken, new Callback<List<OnlineWallet>>() {
            @Override
            public void success(List<OnlineWallet> wallets) {
                Log.d("DYMTEK", "Online wallets SUCCESS");

                List<OnlineWallet> wallets2Restore = new ArrayList<>(wallets);
                // Compare online wallets with local(by address)
                for (OnlineWallet onlineWallet : wallets) {
                    String onlineAddr = onlineWallet.getAddress();
                    for (WalletManager.WalletInfo localWallet : localWallets) {
                        if (localWallet.address.equals(onlineAddr)) {
                            int index = wallets2Restore.indexOf(onlineWallet);
                            if(index != -1){
                                wallets2Restore.remove(index);
                            }

                        }
                    }
                }

                Log.d("DYMTEK", "NEW WALLETS: "+wallets2Restore.size());
                if (wallets.size() > 0) {
                    ((GenerateFragment.Listener) getActivity()).onGenerateMultipleWallets(wallets2Restore);
                }


            }

            @Override
            public void error(String errMsg) {
                Log.d("DYMTEK", "Online wallets ERROR: " + errMsg);
            }
        });


    }

    private List<WalletManager.WalletInfo> loadLocalWallets() {
        Timber.d("loadLocalWallets()");
        WalletManager mgr = WalletManager.getInstance();
        return mgr.findWallets(activityCallback.getStorageRoot());
    }

    private void updateWalletList(List<WalletManager.WalletInfo> walletInfos) {
        walletList.clear();
        walletList.addAll(walletInfos);
        filterList();
        adapter.setInfos(displayedList);
        adapter.notifyDataSetChanged();

        // deal with Gunther & FAB animation
        if (displayedList.isEmpty()) {
            fab.startAnimation(fab_pulse);
            /*if (ivGunther.getDrawable() == null) {
                ivGunther.setImageResource(R.drawable.gunther_desaturated);
            }*/
        } else {
            fab.clearAnimation();
            if (ivGunther.getDrawable() != null) {
                ivGunther.setImageDrawable(null);
            }
        }
    }


    public void loadWalletListFromLocal() {
        Timber.d("loadWalletListFromLocal()");
        final List<WalletManager.WalletInfo> localWallets = loadLocalWallets();
        updateWalletList(localWallets);
    }

    private void showInfo(@NonNull String name) {
        activityCallback.onWalletDetails(name);
    }

    private void showReceive(@NonNull String name) {
        activityCallback.onWalletReceive(name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        //menu.findItem(R.id.action_testnet).setChecked(testnetCheckMenu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean testnetCheckMenu = false; // BuildConfig.DEBUG;

    //boolean isTestnet() {
    //    return testnet;
    //}

    public boolean onTestnetMenuItem() {
        boolean lastState = testnetCheckMenu;
        setNet(!lastState, true); // set and save
        return !lastState;
    }

    public void setNet(boolean testnetChecked, boolean save) {
        this.testnetCheckMenu = testnetChecked;
        NetworkType net = testnetChecked ? NetworkType.NetworkType_Testnet : NetworkType.NetworkType_Mainnet;
        activityCallback.setNetworkType(net);
        activityCallback.showNet();
        if (save) {
            savePrefs(true); // use previous state as we just clicked it
        }
        if (testnetChecked) {
            setDaemon(daemonTestNet);
        } else {
            setDaemon(daemonMainNet);
        }
        loadWalletsFromCloud();
    }

    private static final String PREF_DAEMON_TESTNET = "daemon_testnet";
    private static final String PREF_DAEMON_MAINNET = "daemon_mainnet";
    private static final String PREF_SHOW_XMRTO_ENABLED = "info_xmrto_enabled_login";

    private static final String PREF_DAEMONLIST_MAINNET =
            "seed1.bit.tube:24182;seed2.bit.tube:24182;seed3.bit.tube:24182";

    private static final String PREF_DAEMONLIST_TESTNET =
            "testnet.xmrchain.net";

    private NodeList daemonTestNet;
    private NodeList daemonMainNet;

    boolean showXmrtoEnabled = false;

    void loadPrefs() {
        SharedPreferences sharedPref = activityCallback.getPrefs();

        daemonMainNet = new NodeList(sharedPref.getString(PREF_DAEMON_MAINNET, PREF_DAEMONLIST_MAINNET));
        daemonTestNet = new NodeList(sharedPref.getString(PREF_DAEMON_TESTNET, PREF_DAEMONLIST_TESTNET));
        setNet(testnetCheckMenu, false);

        //showXmrtoEnabled = sharedPref.getBoolean(PREF_SHOW_XMRTO_ENABLED, false);
    }

    void saveXmrToPrefs() {
        SharedPreferences sharedPref = activityCallback.getPrefs();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_SHOW_XMRTO_ENABLED, showXmrtoEnabled);
        editor.apply();
    }

    void savePrefs() {
        savePrefs(false);
    }

    void savePrefs(boolean usePreviousTestnetState) {
        Timber.d("SAVE / %s", usePreviousTestnetState);
        // save the daemon address for the net
        boolean testnet = testnetCheckMenu ^ usePreviousTestnetState;
        String daemon = getDaemon();
        if (testnet) {
            daemonTestNet.setRecent(daemon);
        } else {
            Timber.d("SAVE daemonMainnet");
            daemonMainNet.setRecent(daemon);
        }

        SharedPreferences sharedPref = activityCallback.getPrefs();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_DAEMON_MAINNET, daemonMainNet.toString());
        editor.putString(PREF_DAEMON_TESTNET, daemonTestNet.toString());
        editor.putBoolean(PREF_SHOW_XMRTO_ENABLED, showXmrtoEnabled);
        editor.apply();
    }

    String getDaemon() {
        return etDaemonAddress.getText().toString().trim();
    }

    void setDaemon(NodeList nodeList) {
        Timber.d("setDaemon() %s", nodeList.toString());
        String[] nodes = nodeList.getNodes().toArray(new String[0]);
        nodeAdapter.clear();
        nodeAdapter.addAll(nodes);
        etDaemonAddress.getText().clear();
        if (nodes.length > 0) {
            etDaemonAddress.setText(nodes[0]);
        }
        etDaemonAddress.dismissDropDown();
        etDummy.requestFocus();
        Helper.hideKeyboard(getActivity());
    }

    private boolean isFabOpen = false;
    private FloatingActionButton fab, fabNew, fabView, fabKey, fabSeed;
    private FrameLayout fabScreen;
    private RelativeLayout fabNewL, fabViewL, fabKeyL, fabSeedL;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fab_open_screen, fab_close_screen;
    private Animation fab_pulse;

    public boolean isFabOpen() {
        return isFabOpen;
    }

    public void animateFAB() {
        if (isFabOpen) {
            fabScreen.setVisibility(View.INVISIBLE);
            fabScreen.setClickable(false);
            fabScreen.startAnimation(fab_close_screen);
            fab.startAnimation(rotate_backward);
            fabNewL.startAnimation(fab_close);
            fabNew.setClickable(false);
            fabViewL.startAnimation(fab_close);
            fabView.setClickable(false);
            fabKeyL.startAnimation(fab_close);
            fabKey.setClickable(false);
            fabSeedL.startAnimation(fab_close);
            fabSeed.setClickable(false);
            isFabOpen = false;
        } else {
            fabScreen.setClickable(true);
            fabScreen.startAnimation(fab_open_screen);
            fab.startAnimation(rotate_forward);
            fabNewL.startAnimation(fab_open);
            fabNew.setClickable(true);
            fabViewL.startAnimation(fab_open);
            fabView.setClickable(true);
            fabKeyL.startAnimation(fab_open);
            fabKey.setClickable(true);
            fabSeedL.startAnimation(fab_open);
            fabSeed.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fabNew:
                fabScreen.setVisibility(View.INVISIBLE);
                isFabOpen = false;
                activityCallback.onAddWallet(GenerateFragment.TYPE_NEW);
                break;
            case R.id.fabView:
                animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_VIEWONLY);
                break;
            case R.id.fabKey:
                animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_KEY);
                break;
            case R.id.fabSeed:
                animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);
                break;
            case R.id.fabScreen:
                animateFAB();
                break;
        }
    }
}
