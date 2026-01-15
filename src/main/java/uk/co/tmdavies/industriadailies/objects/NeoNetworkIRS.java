package uk.co.tmdavies.industriadailies.objects;

import net.minecraft.world.entity.player.Player;
import uk.co.tmdavies.industriadailies.IndustriaDailies;
import uk.co.tmdavies.industriadailies.utils.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NeoNetworkIRS {

    private static final String LINE_END = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****"; // Change this string to a unique boundary
    private final String apiKey;

    public NeoNetworkIRS(String apiKey) {
        this.apiKey = apiKey;
    }

    public void giveMoney(Player target, int amount, String ref) {
        try {
            URL obj = new URL("https://irs.neonetwork.xyz/api/send");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(twoHyphens + boundary + LINE_END);
                wr.writeBytes("Content-Disposition: form-data; name=\"apikey\"" + LINE_END);
                wr.writeBytes(LINE_END);
                wr.writeBytes(this.apiKey + LINE_END);

                wr.writeBytes(twoHyphens + boundary + LINE_END);
                wr.writeBytes("Content-Disposition: form-data; name=\"to\"" + LINE_END);
                wr.writeBytes(LINE_END);
                wr.writeBytes(target.getName().getString() + LINE_END);

                wr.writeBytes(twoHyphens + boundary + LINE_END);
                wr.writeBytes("Content-Disposition: form-data; name=\"amount\"" + LINE_END);
                wr.writeBytes(LINE_END);
                wr.writeBytes(amount + LINE_END);

                wr.writeBytes(twoHyphens + boundary + LINE_END);
                wr.writeBytes("Content-Disposition: form-data; name=\"reference\"" + LINE_END);
                wr.writeBytes(LINE_END);
                wr.writeBytes(ref + LINE_END);

                wr.writeBytes(LINE_END);
                wr.writeBytes(twoHyphens + boundary + twoHyphens + LINE_END);
                wr.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            IndustriaDailies.LOGGER.info(response.toString());
        } catch (Exception exception) {
            IndustriaDailies.LOGGER.error("Unable complete request [{}] [{}] [{}]", target.getName().getString(), amount, ref);
            exception.printStackTrace();
        }
        target.sendSystemMessage(Utils.Chat("&a+£%s", amount));
        target.sendSystemMessage(Utils.Chat("&aRef: %s", ref));
    }

}
