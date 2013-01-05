package me.lfc;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import redis.clients.jedis.Jedis;

import java.net.URLEncoder;

/**
 * User: luofucong
 * Date: 13-1-1
 */
public class IcibaTranslator extends AbstractTranslator {

    public static final String URL = "http://dict-co.iciba.com/api/dictionary.php?w=";

    @Override
    public String call() throws Exception {
        String word = jedis.spop(StatisticsWords.TOTAL_WORDS_KEY);
        System.out.println("pop word: " + word);

        HttpClient httpClient = new DefaultHttpClient();
        String responseBody = null;
        try {
            String uri = URL + URLEncoder.encode(word, "UTF-8");
            HttpGet httpGet = new HttpGet(uri);
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

    public static void main(String[] args) throws Exception {
        Jedis _Jedis = new Jedis("127.0.0.1", 6379);
        IcibaTranslator icibaTranslator = new IcibaTranslator();
        icibaTranslator.setJedis(_Jedis);
        String res = icibaTranslator.call();
        System.out.println(res);
    }
}
