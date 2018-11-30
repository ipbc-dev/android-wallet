package com.bittube.wallet.network.models;

import com.bittube.wallet.util.RestoreHeight;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import timber.log.Timber;

public class OnlineWallet {

    private String name;

    private Long creation_date;

    private String address;

    private String seed;

    private String viewKey;

    private String SpendKey;


    public OnlineWallet(String name, JSONObject json) throws JSONException {
        this.name = name;
        this.address = json.getString("public_addr");
        this.seed = json.getString("seed");
        this.viewKey = json.getJSONObject("view").getString("sec");
        this.SpendKey = json.getJSONObject("spend").getString("sec");
        this.creation_date = getHeightFromCreationDate(json.optLong("creation_date", 0));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getViewKey() {
        return viewKey;
    }

    public void setViewKey(String viewKey) {
        this.viewKey = viewKey;
    }

    public String getSpendKey() {
        return SpendKey;
    }

    public void setSpendKey(String spendKey) {
        SpendKey = spendKey;
    }

    public Long getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Long creation_date) {
        this.creation_date = creation_date;
    }


    private long getHeightFromCreationDate(Long timestamp) {
        long height = 0;

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String restoreHeight = parser.format(cal.getTime());

        if (restoreHeight.isEmpty()) return -1;
        try {
            // is it a date?
            parser.setLenient(false);
            parser.parse(restoreHeight);
            height = RestoreHeight.getInstance().getHeight(restoreHeight);
        } catch (ParseException exPE) {
            try {
                // or is it a height?
                height = Long.parseLong(restoreHeight);
            } catch (NumberFormatException exNFE) {
                return -1;
            }
        }
        Timber.d("Using Restore Height = %d", height);
        return height;
    }
}
