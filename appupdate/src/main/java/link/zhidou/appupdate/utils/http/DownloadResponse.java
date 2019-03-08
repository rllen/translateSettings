package link.zhidou.appupdate.utils.http;

import java.io.InputStream;

/**
 * created by yue.gan 18-7-18
 */
public class DownloadResponse {

    private InputStream inputStream;

    private long contentLength;

    public DownloadResponse (InputStream in, long contentLength) {
        this.inputStream = in;
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getContentLength() {
        return contentLength;
    }
}
