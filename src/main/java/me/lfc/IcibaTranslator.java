package me.lfc;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: luofucong
 * Date: 13-1-1
 */
public class IcibaTranslator extends AbstractTranslator {

    public static final String URL = "http://dict-co.iciba.com/api/dictionary.php?w=";

    private SAXReader reader;

    private AtomicInteger wordIndex;

    @Override
    @SuppressWarnings("unchecked")
    public String call() throws Exception {
//        String word = jedis.spop(StatisticsWords.TOTAL_WORDS_KEY);
        int index = wordIndex.get();
        if (index >= jedis.zcard(StatisticsWords.TOTAL_WORDS_FREQUENCY_KEY)) {
            return "";
        }
        String word = jedis.zrevrange(StatisticsWords.TOTAL_WORDS_FREQUENCY_KEY, index, index).iterator().next();
        int frequency = jedis.zscore(StatisticsWords.TOTAL_WORDS_FREQUENCY_KEY, word).intValue();
        System.out.println(index + " pop word: " + word + " " + frequency);

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
        return word + ":\n" + frequency + "\n" + result.toString();
    }

    public void setReader(SAXReader reader) {
        this.reader = reader;
    }

    public void setWordIndex(AtomicInteger wordIndex) {
        this.wordIndex = wordIndex;
    }
}
