package me.lfc;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URLEncoder;
import java.util.concurrent.Callable;

/**
 * User: luofucong
 * Date: 12-12-31
 */
@Deprecated
public class BingTranslator extends AbstractTranslator {

    private BingAccessToken accessToken;

    public BingTranslator(BingAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String call() {
        String word = jedis.spop(StatisticsWords.TOTAL_WORDS_KEY);
        System.out.println("pop word: " + word);

        HttpClient httpClient = new DefaultHttpClient();
        String responseBody = null;
        try {
            String uri = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text="
                    + URLEncoder.encode(word, "UTF-8") + "&from=en&to=zh-CHS";
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Authorization", accessToken.getHeadValue());
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            responseBody = httpClient.execute(httpGet, responseHandler);
        } catch (Exception e) {
            jedis.sadd(StatisticsWords.TOTAL_WORDS_KEY, word);
            System.out.println("Exception occurred: " + e + ". Re-Add to db: " + word);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseBody;
    }
}
