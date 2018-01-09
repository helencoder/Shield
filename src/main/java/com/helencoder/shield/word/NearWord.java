package com.helencoder.shield.word;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * 形近字
 *
 * Created by helencoder on 2018/1/9.
 */
public class NearWord {
    Logger logger = LoggerFactory.getLogger(NearWord.class);
    public static void main(String[] args) throws Exception {
        // 控制HttpClient控制台输出
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        // 形近字结果搜集

        // 获取文本
        String word = "繁";
        // 获取对应的url
        String requestUrl = String.format("http://www.fantizi5.com/xingjinzi/json/%s.html", urlCode(word));
        // 获取对应的形近词结果
        String response = get(requestUrl);
        // 数据解析
        List<String> simList = new ArrayList<>();
        String[] imgArr = response.split("&");
        for (String str : imgArr) {
            String imgUrl = String.format("http://www.fantizi5.com/xingjinzi/xsz/%s/%s.png", str.substring(0, 2), str);
            String imgPath = String.format("data/img/%s.png", str);
            // 图片转存
            downloadImage(imgUrl, imgPath);
            // 图片解析
            simList.add(imgOcr(imgPath));
        }
        // 数据转存


    }

    /**
     * 中文url编码
     */
    private static String urlCode(String str) {
        String code = "";
        try {
            code = URLEncoder.encode(str, "utf-8")
                    .replaceAll("%", "")
                    .toLowerCase();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return code;
    }

    /**
     * Get请求(不带参数)
     *
     * @param url 请求的url地址
     * @return String 请求响应
     */
    public static String get(String url) {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String response = "";
        try {
            // 创建HttpGet
            HttpGet httpget = new HttpGet(url);
            // 执行get请求.
            CloseableHttpResponse httpResponse = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    response = EntityUtils.toString(entity, Consts.UTF_8);
                }
            } finally {
                httpResponse.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    /**
     * 图片下载
     */
    private static void downloadImage(String imgUrl, String storeFilePath) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            // 创建httpGet
            HttpGet httpget = new HttpGet(imgUrl);
            // 执行get请求.
            CloseableHttpResponse httpResponse = httpclient.execute(httpget);
            // 文件存储
            FileOutputStream output = new FileOutputStream(new File(storeFilePath));
            try {
                // 获取响应实体
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    try {
                        byte b[] = new byte[1024];
                        int j = 0;
                        while( (j = instream.read(b))!=-1){
                            output.write(b,0,j);
                        }
                        output.flush();
                        output.close();
                    } catch (IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    } catch (RuntimeException ex) {
                        // In case of an unexpected exception you may want to abort
                        // the HTTP request in order to shut down the underlying
                        // connection immediately.
                        httpget.abort();
                        throw ex;
                    } finally {
                        // Closing the input stream will trigger connection release
                        try { instream.close(); } catch (Exception ignore) {}
                    }
                }
            } finally {
                httpResponse.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 图片文字识别
     */
    private static String imgOcr(String imgpath) {
        File imageFile = new File(imgpath);
        if (!imageFile.exists()) {
            return "";
        }

        ITesseract instance = new Tesseract();
        instance.setDatapath("data/tessdata");//设置训练库的位置
        instance.setLanguage("chi_sim");//中文识别
        String result = "";
        try {
            result = instance.doOCR(imageFile);
        } catch (TesseractException ex) {
            ex.printStackTrace();
        }
        return result;
    }

}
