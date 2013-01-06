package me.lfc;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.io.SAXReader;
import redis.clients.jedis.Jedis;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * User: LuoFucong
 * Date: 12-12-31
 */
public class StatisticsWords {

    public static final String TOTAL_WORDS_FREQUENCY_KEY = "TOTAL_WORDS_FREQUENCY";

    public static final String TOTAL_WORDS_KEY = "TOTAL_WORDS";

    private static final String INIT_WORD = "INIT-WORD";

    private Jedis jedis;

    private Set<String> excludedWords;

    public StatisticsWords(Jedis jedis) throws IOException {
        this.jedis = jedis;
        if (!jedis.exists(TOTAL_WORDS_FREQUENCY_KEY)) {
            jedis.zadd(TOTAL_WORDS_FREQUENCY_KEY, 0, INIT_WORD);
        }
        if (!jedis.exists(TOTAL_WORDS_KEY)) {
            jedis.sadd(TOTAL_WORDS_KEY, INIT_WORD);
        }

        excludedWords = new HashSet<String>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream("ExcludedWords");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            String[] split = line.split(" ");
            for (String word : split) {
                excludedWords.add(word.trim());
            }
        }
    }

    public void Statistics(String filePath) throws IOException {
        PDFTextReader reader = new PDFTextReader();
        int count = 777;
        int page = 1;
        do {
            try {
                String returnedStr = reader.read(filePath, page, page);
                if (StringUtils.isNotBlank(returnedStr)) {
                    String[] split = returnedStr.split(" ");
                    l:
                    for (String s : split) {
                        String word = s.trim();
                        word = word.toLowerCase();
                        char[] chars = word.toCharArray();
                        for (char c : chars) {
                            if (c < 'a' || c > 'z') {
                                continue l;
                            }
                        }
                        word = word.replaceAll("([a-z]+)[?:!.,;]*", "$1");
                        if (StringUtils.isNotBlank(word) && !StringUtils.isNumeric(word)
                                && !excludedWords.contains(word)) {
                            jedis.zincrby(TOTAL_WORDS_FREQUENCY_KEY, 1, word);
                            jedis.sadd(TOTAL_WORDS_KEY, word);
                        }
                    }
                }
                page += 1;
            } catch (OverPageException e) {
                System.out.println("Total page num: " + page);
                break;
            }
        } while (count-- > 0);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, ParserConfigurationException {
        Jedis _Jedis = new Jedis("127.0.0.1", 6379);
//        StatisticsWords statisticsWords = new StatisticsWords(_Jedis);
//        statisticsWords.Statistics("E:\\A Dance With Dragons.pdf");

//        BingTranslatorService bingTranslatorService = new BingTranslatorService();

        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        int expiredSec = 0;
//        long beginTime = 0;
        do {
//            if (System.currentTimeMillis() - beginTime > expiredSec) {
//                bingTranslatorService.getBingToken();
//                expiredSec = Integer.parseInt(bingTranslatorService.accessToken.getExpires_in()) * 1000;
//                beginTime = System.currentTimeMillis();
//            }

//            AbstractTranslator task = new BingTranslator(bingTranslatorService.accessToken);
            AbstractTranslator task = new IcibaTranslator();
            task.setJedis(_Jedis);
            ((IcibaTranslator) task).setReader(new SAXReader());

            Future<String> submit = executorService.submit(task);
            System.out.println(submit.get());
        } while (_Jedis.scard(StatisticsWords.TOTAL_WORDS_KEY) > 0);
    }
}
