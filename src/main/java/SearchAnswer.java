import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wtwei .
 * @date 2018/1/17 .
 * @time 17:36 .
 */
public class SearchAnswer {
    
    private static String[] notKeyWords = {"不是", "非", "不正确", "不包含", "不属于", "不与", "不能", "不可能"};
    private static String[] yesOrNoSplitKeyWords = {"是", "属于", "不可能是", "可能是", "包含", "不包含"};

    private static String baidu = "https://www.baidu.com/s?ie=utf-8&wd=";
    private static String sogou = "https://m.sogou.com/web/searchList.jsp?keyword=";
    
    public static String search(String question, List daixuan) {
        try{
            Thread.sleep(500);
            preProcessQuestion(question, daixuan);
            
            String url = sogou + question;
            OkHttpClient okHttpClient = getOkHttpClient();
            String html = okHttpClient.newCall(new Request.Builder()
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .addHeader("Cookie", "BIDUPSID=4BE06E9A3BE5F1C9EDC2657F1533EEF1; PSTM=1578899116; BAIDUID=4BE06E9A3BE5F1C92661DEA91A11E629:FG=1; BD_UPN=12314753; H_PS_PSSID=1425_21094_30490_26350_30503; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; H_PS_645EC=565bLz8zjcp3sBZd6lTWXvHQH3mZyEb9zh1vKLtCubSu64fu%2Fx3vbRnJT5Y; BDSVRTM=113")
                    .url(url).get().build()).execute().body().string();
            Document doc = Jsoup.parse(html);
            
            String result = doc.text();
            if (result.contains("百度安全验证")){
                System.err.println("百度搜索出错");
                return "";
            }
            
            if (daixuan.contains("正确")){
                System.err.println("  判断题搜索结果：   " + result.substring(result.indexOf(question)));
            }

            if (containsNoKeyWords(question)){
                System.err.println("包含否定词，建议选少数");
            }
            
            int count = 0;
            for (int i = 0; i < daixuan.size(); i++) {
                String option = daixuan.get(i).toString();
                
                count = wordCount(result, option);
                System.out.println( "["+ option + "] 统计： "+ count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
    
    private static void preProcessQuestion(String question, List<String> options){
        if (!options.contains("正确")){
            return;
        }

        try {
            for (String keyWord : yesOrNoSplitKeyWords) {
                if (question.lastIndexOf(keyWord) != -1){
                    options.add(question.substring(question.lastIndexOf(keyWord) + 1));
                    question = question.substring(0, question.lastIndexOf(keyWord));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    private static boolean containsNoKeyWords(String question){
        for (String notKeyWord : notKeyWords) {
            if (question.indexOf(notKeyWord) != -1){
                return true;
            }
        }
        return false;
    }
    
    private static int wordCount(String source, String keyWord){
        Pattern p = Pattern.compile(keyWord.toLowerCase());
        //使用Matcher进行各种查找替换操作  
        Matcher m = p.matcher(source.toLowerCase());
        int i = 0;
        while(m.find()){
            i++;
        }

        return i;
    }

    static Map<String, List<String>> cookieMap = new HashMap<>();
    private static OkHttpClient getOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            okHttpClient.setCookieHandler(new CookieHandler() {
                String newCookie;
                @Override
                public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
                    return cookieMap = requestHeaders;
                }

                @Override
                public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
                    responseHeaders = cookieMap;
                }
            });
            
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
        public static void main(String[] args) {
        search("一英尺等于多少英寸", Arrays.asList("10", "12"));
    }

}
