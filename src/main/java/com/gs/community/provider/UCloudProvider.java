package com.gs.community.provider;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.auth.ObjectAuthorization;
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import cn.ucloud.ufile.http.OnProgressListener;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class UCloudProvider {
    @Value("${ucloud.ulife.public-key}")
    private String publicKey;

    @Value("${ucloud.ulife.private-key}")
    private String privateKey;

    @Value("${ucloud.ulife.bucketName}")
    private String bucketName;

    @Value("${ucloud.ulife.region}")
    private String region;

    @Value("${ucloud.ulife.proxySuffix}")
    private String proxySuffix;

    @Value("${ucloud.ulife.expires}")
    private Integer expires;

    public String upload(InputStream fileStream, String mimeType, String fileName) {
        String generatedFileName;
        String[] filePaths = fileName.split("\\.");
        if (filePaths.length > 1) {
            generatedFileName = UUID.randomUUID().toString() + "." + filePaths[filePaths.length - 1];
        } else {
            return null;
        }
        String imgUrl = uploadtoBucket(fileStream, mimeType, generatedFileName);
        return imgUrl;
    }

    public String uploadAvatar(InputStream inputStream, String contentType, String fileName) {
        String avatarUrl = uploadtoBucket(inputStream, contentType, fileName);
        return avatarUrl;
    }

    /*public String uploadAvatar2(InputStream inputStream, String contentType, User user, String fileName, Long contentLength) {
        String initUrl = uploadtoBucket2(inputStream, "avatar", contentType, user, fileName, contentLength);
        String avatarUrl = null;
        initUrl = initUrl + "?imageMogr2/scrop/168x168";
        avatarUrl = uploadUrltoBucket(initUrl, "avatar", contentType, user, fileName);
        initUrl = initUrl + "?imageMogr2/crop/168x168/gravity/center";
        avatarUrl = uploadUrltoBucket(initUrl, "avatar", contentType, user, fileName);
        return avatarUrl;
    }*/

    public String uploadtoBucket(InputStream inputStream, String contentType, String fileName) {
        try {
            ObjectAuthorization objectAuthorization = new UfileObjectLocalAuthorization(publicKey, privateKey);
            ObjectConfig config = new ObjectConfig(region, proxySuffix);
            PutObjectResultBean response = UfileClient.object(objectAuthorization, config)
                    .putObject(inputStream, contentType)
                    .nameAs(fileName)
                    .toBucket(bucketName)
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                        }
                    })
                    .execute();
            if (response != null && response.getRetCode() == 0) {
                String url = UfileClient.object(objectAuthorization, config)
                        .getDownloadUrlFromPrivateBucket(fileName, bucketName, expires)
                        .createUrl();
                return url;
            } else {
                throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
            }
        } catch (UfileClientException e) {
            e.printStackTrace();
            throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
        } catch (UfileServerException e) {
            e.printStackTrace();
            throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /*public String uploadtoBucket2(InputStream inputStream, String fileType, String contentType, User user, String fileName, Long contentLength) {
        String key = "upload/user/" + user.getId() + "/" + fileType + "/" + fileName;
        try {
            ObjectAuthorization objectAuthorization = new UfileObjectLocalAuthorization(publicKey, privateKey);
            ObjectConfig config = new ObjectConfig(region, proxySuffix);
            PutObjectResultBean response = UfileClient.object(objectAuthorization, config)
                    .putObject(inputStream, contentType)
                    .nameAs(fileName)
                    .toBucket(bucketName)
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(long bytesWritten, long contentLength) {
                        }
                    })
                    .execute();
            if (response != null && response.getRetCode() == 0) {
                String url = UfileClient.object(objectAuthorization, config)
                        .getDownloadUrlFromPrivateBucket(fileName, bucketName, expires)
                        .createUrl();
                return objecturl + key;
            } else {
                throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
            }
        } catch (UfileClientException e) {
            e.printStackTrace();
            throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
        } catch (UfileServerException e) {
            e.printStackTrace();
            throw new CustomizeException(CustomizeErrorCode.FILE_UPLOAD_FAIL);
        }
    }*/

    /*private String uploadUrltoBucket(String initUrl, String fileType, String contentType, User user, String fileName) {
        URL url = null;
        String finalUrl = null;
        InputStream fileStream = null;
        try {
            url = new URL(initUrl);
            URLConnection con = url.openConnection();
            //设置请求超时为5s
            con.setConnectTimeout(5 * 1000);
            // 输入流
            fileStream = con.getInputStream();
            finalUrl = uploadtoBucket2(fileStream, fileType, contentType, user, fileName, Long.valueOf(con.getContentLength()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//解决流资源未释放的问题
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalUrl;
    }*/
}
