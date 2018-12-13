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

package com.bittube.wallet.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.system.ErrnoException;
import android.system.Os;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.bittube.wallet.BuildConfig;
import com.bittube.wallet.R;
import com.bittube.wallet.model.NetworkType;
import com.bittube.wallet.model.Wallet;
import com.bittube.wallet.model.WalletManager;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.HttpUrl;
import timber.log.Timber;

public class Helper {
    static private final String WALLET_DIR = "bittube";
    static private final String HOME_DIR = "bittube";

    static public int DISPLAY_DIGITS_INFO = 5;

    static public final String CRYPTO = "TUBE";

    static public File getWalletRoot(Context context) {
        String UserUUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return getStorage(context, WALLET_DIR + "/"+UserUUID);
    }

    static public File getStorage(Context context, String folderName) {
        if (!isExternalStorageWritable()) {
            String msg = context.getString(R.string.message_strorage_not_writable);
            Timber.e(msg);
            throw new IllegalStateException(msg);
        }
        File dir = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!dir.exists()) {
            Timber.i("Creating %s", dir.getAbsolutePath());
            dir.mkdirs(); // try to make it
        }
        if (!dir.isDirectory()) {
            String msg = "Directory " + dir.getAbsolutePath() + " does not exist.";
            Timber.e(msg);
            throw new IllegalStateException(msg);
        }
        return dir;
    }

    static public final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    static public boolean getWritePermission(Activity context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                Timber.w("Permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                context.requestPermissions(permissions, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    static public final int PERMISSIONS_REQUEST_CAMERA = 7;

    static public boolean getCameraPermission(Activity context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                Timber.w("Permission denied for CAMERA - requesting it");
                String[] permissions = {Manifest.permission.CAMERA};
                context.requestPermissions(permissions, PERMISSIONS_REQUEST_CAMERA);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    static public File getWalletFile(Context context, String aWalletName) {
        File walletDir = getWalletRoot(context);
        File f = new File(walletDir, aWalletName);
        Timber.d("wallet=%s size= %d", f.getAbsolutePath(), f.length());
        return f;
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static public void showKeyboard(Activity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(act.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
    }

    static public void hideKeyboard(Activity act) {
        if (act == null) return;
        if (act.getCurrentFocus() == null) {
            act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow((null == act.getCurrentFocus()) ? null : act.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    static public void showKeyboard(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    static public void hideKeyboardAlways(Activity act) {
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    static public String getDisplayAmount(long amount) {
        return getDisplayAmount(amount, 8);
    }

    static public String getDisplayAmount(long amount, int maxDecimals) {
        return getDisplayAmount(Wallet.getDisplayAmount(amount), maxDecimals);
    }

    // amountString must have '.' as decimal point
    private static String getDisplayAmount(String amountString, int maxDecimals) {
        int lastZero = 0;
        int decimal = 0;
        for (int i = amountString.length() - 1; i >= 0; i--) {
            if ((lastZero == 0) && (amountString.charAt(i) != '0')) lastZero = i + 1;
            // TODO i18n
            if (amountString.charAt(i) == '.') {
                decimal = i + 1;
                break;
            }
        }
        int cutoff = Math.min(Math.max(lastZero, decimal + 2), decimal + maxDecimals);
        return amountString.substring(0, cutoff);
    }

    static public String getFormattedAmount(double amount, boolean isXmr) {
        // at this point selection is XMR in case of error
        String displayB;
        if (isXmr) { // XMR
            long xmr = Wallet.getAmountFromDouble(amount);
            if ((xmr > 0) || (amount == 0)) {
                displayB = String.format(Locale.US, "%,.5f", amount);
            } else {
                displayB = null;
            }
        } else { // not XMR
            displayB = String.format(Locale.US, "%,.2f", amount);
        }
        return displayB;
    }

    static public Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    static private Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    static final int HTTP_TIMEOUT = 5000;

    static public String getUrl(String httpsUrl) {
        HttpsURLConnection urlConnection = null;
        try {
            URL url = new URL(httpsUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(HTTP_TIMEOUT);
            urlConnection.setReadTimeout(HTTP_TIMEOUT);
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());
            StringBuffer sb = new StringBuffer();
            final int BUFFER_SIZE = 512;
            char[] buffer = new char[BUFFER_SIZE];
            int length = in.read(buffer, 0, BUFFER_SIZE);
            while (length >= 0) {
                sb.append(buffer, 0, length);
                length = in.read(buffer, 0, BUFFER_SIZE);
            }
            return sb.toString();
        } catch (SocketTimeoutException ex) {
            Timber.w("C %s", ex.getLocalizedMessage());
        } catch (MalformedURLException ex) {
            Timber.e("A %s", ex.getLocalizedMessage());
        } catch (IOException ex) {
            Timber.e("B %s", ex.getLocalizedMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    static public void clipBoardCopy(Context context, String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clip);
    }

    static private Animation ShakeAnimation;

    static public Animation getShakeAnimation(Context context) {
        if (ShakeAnimation == null) {
            synchronized (Helper.class) {
                if (ShakeAnimation == null) {
                    ShakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake);
                }
            }
        }
        return ShakeAnimation;
    }

    static public HttpUrl getXmrToBaseUrl() {
        if ((WalletManager.getInstance() == null)
                || (WalletManager.getInstance().getNetworkType() != NetworkType.NetworkType_Mainnet)) {
            return HttpUrl.parse("https://test.xmr.to/api/v2/xmr2btc/");
        } else {
            return HttpUrl.parse("https://xmr.to/api/v2/xmr2btc/");
        }
    }

    static public void setMoneroHome(Context context) {
        try {
            String home = getStorage(context, HOME_DIR).getAbsolutePath();
            Os.setenv("HOME", home, true);
        } catch (ErrnoException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static public void initLogger(Context context) {
        if (BuildConfig.DEBUG) {
            initLogger(context, WalletManager.LOGLEVEL_DEBUG);
        }
        // no logger if not debug
    }

    // TODO make the log levels refer to the  WalletManagerFactory::LogLevel enum ?
    static public void initLogger(Context context, int level) {
        String home = getStorage(context, HOME_DIR).getAbsolutePath();
        WalletManager.initLogger(home + "/bittube", "bittube.log");
        if (level >= WalletManager.LOGLEVEL_SILENT)
            WalletManager.setLogLevel(level);
    }
}
