package me.lfc;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import redis.clients.jedis.Jedis;

import java.net.URLEncoder;
import java.util.List;

/**
 * User: luofucong
 * Date: 13-1-1
 */
public class IcibaTranslator extends AbstractTranslator {

    public static final String URL = "http://dict-co.iciba.com/api/dictionary.php?w=";

    private SAXReader reader;

    @Override
    @SuppressWarnings("unchecked")
    public String call() throws Exception {
        String word = jedis.spop(StatisticsWords.TOTAL_WORDS_KEY);
        System.out.println("pop word: " + word);

        HttpClient httpClient = new DefaultHttpClient();
        StringBuilder result = new StringBuilder();
        try {
            String uri = URL + URLEncoder.encode(word, "UTF-8");
            HttpGet httpGet = new HttpGet(uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            Document document = reader.read(entity.getContent());
            Element rootElement = document.getRootElement();
            List<Element> posElements = rootElement.elements("pos");
            List<Element> acceptationElements = rootElement.elements("acceptation");
            for (int i = 0; i < posElements.size(); i++) {
                result.append(posElements.get(i).getText()).append(acceptationElements.get(i).getText());
            }
        } catch (Exception e) {
            jedis.sadd(StatisticsWords.TOTAL_WORDS_KEY, word);
            System.out.println("Exception occurred: " + e + ". Re-Add to db: " + word);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return result.toString();
    }

    public void setReader(SAXReader reader) {
        this.reader = reader;
    }

    public static void main(String[] args) throws Exception {
        Jedis _Jedis = new Jedis("127.0.0.1", 6379);
        IcibaTranslator icibaTranslator = new IcibaTranslator();
        icibaTranslator.setJedis(_Jedis);
        icibaTranslator.setReader(new SAXReader());
        String res = icibaTranslator.call();
        System.out.println(res);
    }
}
