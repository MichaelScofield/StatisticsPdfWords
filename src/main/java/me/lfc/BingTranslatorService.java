package me.lfc;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: luofucong
 * Date: 12-12-31
 */
public class BingTranslatorService {

    public final String clientId;

    public final String clientSecret;

    public BingAccessToken accessToken;

    public BingTranslatorService() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream("BingTranslatorService.properties");
        properties.load(in);

        clientId = properties.getProperty("clientId");

        clientSecret = properties.getProperty("clientSecret");
    }

    public void getBingToken() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost httpPost = new HttpPost("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("scope", "http://api.microsofttranslator.com"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(entity.getContent(), writer, "UTF-8");
                accessToken = new ObjectMapper().readValue(writer.toString(), BingAccessToken.class);
                System.out.println("Getting Bing token success: " + accessToken.toString());
            } else {
                System.out.println("Error occurred when trying to get Bing token: " + response.getStatusLine());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
