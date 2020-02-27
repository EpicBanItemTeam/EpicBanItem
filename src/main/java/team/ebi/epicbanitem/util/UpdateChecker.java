package team.ebi.epicbanitem.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.plugin.meta.version.ComparableVersion;
import team.ebi.epicbanitem.EpicBanItem;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author The EpicBanItem Team
 */
@Singleton
public class UpdateChecker {
    private static String ORE_API = "https://ore.spongepowered.org/api/v2/";

    @Inject
    private Logger logger;

    public UpdateChecker() {

    }

    public void checkUpdate() {
        try {
            JsonParser jsonParser = new JsonParser();
            URL auth = new URL(ORE_API+ "authenticate");
            HttpURLConnection connection = (HttpURLConnection) auth.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json;charset=UTF-8");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", EpicBanItem.PLUGIN_ID + "/" + EpicBanItem.VERSION);
            JsonObject jsonObject;
            jsonObject = jsonParser.parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
            String session = jsonObject.get("session").getAsString();
            //
            URL url = new URL(ORE_API + "projects/" + EpicBanItem.PLUGIN_ID + "/versions?limit=1&offset=0");
            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Accept", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", EpicBanItem.PLUGIN_ID + "/" + EpicBanItem.VERSION);
            connection.setRequestProperty("Authorization", "OreApi session=" + session);
            connection.connect();
            jsonObject = new JsonParser().parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
            String lastVersion = jsonObject.get("result").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
            if (new ComparableVersion(lastVersion).compareTo(new ComparableVersion(EpicBanItem.VERSION)) > 0) {
                logger.warn("Found a new version {} , current {}.\nYou can get the new version at {}", lastVersion, EpicBanItem.VERSION, EpicBanItem.ORE);
            }
        } catch (Exception e) {
            logger.error("Failed to check update.", e);
        }
    }

}
