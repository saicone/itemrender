package com.saicone.item.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class MavenMirror {

    private static final String IP_API = "http://ip-api.com/json/";

    public static final String AMERICA = "https://maven-central.storage-download.googleapis.com/maven2/";
    public static final String EUROPE = "https://maven-central-eu.storage-download.googleapis.com/maven2/";
    public static final String ASIA = "https://maven.aliyun.com/repository/central/";

    public static final String DEFAULT = AMERICA;

    @SuppressWarnings("deprecation")
    private static final JsonParser PARSER = new JsonParser(); // Compatibility with older Gson versions

    private static String cached;

    @NotNull
    @SuppressWarnings("deprecation")
    public static String get() throws IOException {
        if (cached != null) {
            return cached;
        }

        HttpURLConnection con = (HttpURLConnection) new URL(IP_API).openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0");
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        con.setRequestMethod("GET");

        String code = null;
        try (Reader reader = new InputStreamReader(new BufferedInputStream(con.getInputStream()))) {
            final JsonElement element = PARSER.parse(reader);
            if (element.isJsonObject()) {
                final JsonElement countryCode = element.getAsJsonObject().get("countryCode");
                if (countryCode != null) {
                    code = countryCode.getAsString();
                }
            }
        }
        if (code == null) {
            cached = DEFAULT;
            return cached;
        }

        switch (code.toUpperCase()) {
            // America
            case "US":
            case "CA":
            case "MX":
            case "BR":
            case "AR":
            case "CL":
            case "CO":
            case "PE":
            case "VE":
            case "EC":
            case "GT":
            case "CU":
            case "BO":
            case "DO":
            case "HN":
            case "PY":
            case "NI":
            case "SV":
            case "CR":
            case "PA":
            case "UY":
            case "JM":
            case "HT":
            case "BS":
            case "BZ":
            case "GY":
            case "SR":
            case "TT":
            case "AG":
            case "BB":
            case "GD":
            case "KN":
            case "LC":
            case "VC":
                cached = AMERICA;
                break;
            // Europe
            case "DE":
            case "FR":
            case "NL":
            case "GB":
            case "IT":
            case "ES":
            case "PL":
            case "SE":
            case "NO":
            case "FI":
            case "DK":
            case "BE":
            case "CH":
            case "AT":
            case "IE":
            case "PT":
            case "GR":
            case "CZ":
            case "HU":
            case "SK":
            case "RO":
            case "BG":
            case "HR":
            case "SI":
            case "EE":
            case "LV":
            case "LT":
            case "LU":
            case "MT":
            case "IS":
            case "AL":
            case "ME":
            case "MK":
            case "RS":
            case "BA":
            case "UA":
            case "MD":
            case "BY":
                cached = EUROPE;
                break;
            // Asia
            case "CN":
            case "JP":
            case "KR":
            case "IN":
            case "SG":
            case "TH":
            case "VN":
            case "PH":
            case "MY":
            case "ID":
            case "PK":
            case "BD":
            case "LK":
            case "NP":
            case "KH":
            case "MM":
            case "MN":
            case "HK":
            case "TW":
            case "IR":
            case "IQ":
            case "SA":
            case "AE":
            case "IL":
            case "JO":
            case "QA":
            case "KW":
            case "OM":
            case "BH":
            case "YE":
            case "SY":
            case "KZ":
            case "UZ":
            case "TM":
            case "TJ":
            case "KG":
            case "AF":
            case "AZ":
            case "AM":
            case "GE":
                cached = ASIA;
                break;
            default:
                cached = DEFAULT;
                break;
        }
        return cached;
    }

    @NotNull
    public static String getOrDefault(@NotNull String def) {
        try {
            return get();
        } catch (IOException e) {
            return def;
        }
    }

    @NotNull
    public static CompletableFuture<String> getAsync() {
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> getOrDefault(DEFAULT));
    }
}
