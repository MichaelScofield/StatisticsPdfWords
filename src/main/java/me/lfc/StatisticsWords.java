package me.lfc;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * User: LuoFucong
 * Date: 12-12-31
 * Time: 下午12:07
 */
public class StatisticsWords {

    private static final String ZSET_KEY = "TOTAL_WORDS";

    private static final String INIT_WORD = "INIT-WORD";

    private Jedis jedis;

    private Set<String> excludedWords;

    public StatisticsWords(String host, int port) throws IOException {
        jedis = new Jedis(host, port);
        if (!jedis.exists(ZSET_KEY)) {
            jedis.zadd(ZSET_KEY, 0, INIT_WORD);
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
                        if (StringUtils.isNotBlank(word) && !StringUtils.isNumeric(word) && !excludedWords.contains(word)) {
                            jedis.zincrby(ZSET_KEY, 1, word);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        StatisticsWords statisticsWords = new StatisticsWords("127.0.0.1", 6379);
        statisticsWords.Statistics("E:\\A Dance With Dragons.pdf");
    }
}
