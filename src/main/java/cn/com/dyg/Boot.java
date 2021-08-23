package cn.com.dyg;

import cn.com.dyg.dto.ResultVO;
import cn.com.dyg.dto.UploadRecordDTO;
import cn.com.dyg.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.*;

public class Boot {
    public static void main(String[] args) throws IOException {
        System.out.println("开始执行");
        //读取配置文件
        InputStream is1 = Boot.class.getResourceAsStream("/pdf.properties");
        BufferedReader bf = new BufferedReader(new InputStreamReader(is1, "UTF-8"));
        Properties pp1 = new Properties();
        pp1.load(bf);
        //要扫描的文件目录，用分号作分割
        String path = pp1.getProperty("filePath");
        //扫描并上传后要移动到目标目录
        String endPath = pp1.getProperty("endPath") + File.separator;
        bf.close();
        is1.close();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        InputStream is2 = null;
        //判断正式环境和测试环境
        if (judgeIsNormal(hostAddress)) {
            is2 = Boot.class.getResourceAsStream("/normal.properties");
        } else {
            is2 = Boot.class.getResourceAsStream("/test.properties");
        }
        Properties pp2 = new Properties();
        pp2.load(is2);
        is2.close();

        //获取http连接池
        CloseableHttpClient httpClient = HttpClientRequest.newInstance().getHttpClient();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(() -> {
            //扫描路径下所有pdf
            checkAndUpload(path, endPath, httpClient, pp2, hostAddress);
        }, 0, 2, TimeUnit.MINUTES);


    }

    private static void checkAndUpload(String path, String endPath, CloseableHttpClient httpClient, Properties properties, String hostAddress) {
        String file_url = properties.getProperty("FILE_URL");
        String f_query_url = properties.getProperty("F_QUERY_URL");
        String f_insert_url = properties.getProperty("F_INSERT_URL");
        String s_insert_url = properties.getProperty("S_INSERT_URL");
        String appid = properties.getProperty("appid");
        String secret = properties.getProperty("secret");
        //扫描路径下所有pdf
        ConcurrentLinkedQueue<File> allPdf = ScanUtil.folderMethod2(path);
        if (allPdf.isEmpty()) {
            return;
        }
        //签名
        byte[] encrypted = AesCBCUtil.AES_CBC_Encrypt(AesCBCUtil.PASSWORD.getBytes(), AesCBCUtil.KEY.getBytes(), AesCBCUtil.IV.getBytes());
        String sign = null;
        try {
            sign = AesCBCUtil.encryptBASE64(encrypted);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        //创建固定大小的线程池
        ExecutorService exec = Executors.newFixedThreadPool(3);
        while (!allPdf.isEmpty()) {
            String finalSign = sign;
            exec.execute(() -> {
                File file = allPdf.poll();
                if (file == null) {
                    return;
                }
                try {
                    String absolutePath = file.getAbsolutePath();
                    UploadRecordDTO dto = new UploadRecordDTO();
                    dto.setAbsolutePath(absolutePath);
                    dto.setHostIP(hostAddress);
                    dto.setSign(finalSign);
                    String jsonString = JSONObject.toJSONString(dto);
                    //调用查询接口
                    String retString = HttpClientUtil.sendPost(jsonString, httpClient, f_query_url);
                    if (retString == null) {
                        return;
                    }
                    String retStr = JSON.parse(retString).toString();
                    ResultVO resultVO = JSONObject.parseObject(retStr, ResultVO.class);
                    if (resultVO.getStatus().equals("200")) {
                        //调用上传接口
                        String fileId = AttachServiceUtil.upload(file, httpClient, file_url, appid, secret);
                        if (fileId == null) {
                            return;
                        }
                        dto.setFileID(fileId);
                        dto.setFileType("pdf");
                        dto.setSign(finalSign);
                        String jsonString2 = JSONObject.toJSONString(dto);
                        //调用插入接口
                        HttpClientUtil.sendPost(jsonString2, httpClient, f_insert_url);
                        HttpClientUtil.sendPost(jsonString2, httpClient, s_insert_url);
                    }
                    //结束后移动文件
                    fileMoveTo(absolutePath, endPath);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
        exec.shutdown();
    }

    private static void fileMoveTo(String startPath, String endPath) {
        File startFile = new File(startPath);
        File tmpFile = new File(endPath);//获取文件夹路径
        if (!tmpFile.exists()) {//判断文件夹是否创建，没有创建则创建新文件夹
            tmpFile.mkdirs();
        }
        startFile.renameTo(new File(endPath + startFile.getName()));
    }

    private static boolean judgeIsNormal(String ip) {
        if (ip.startsWith("192.168.20.") || ip.startsWith("172.16.49.")) {
            return true;
        } else {
            return false;
        }
    }
}
