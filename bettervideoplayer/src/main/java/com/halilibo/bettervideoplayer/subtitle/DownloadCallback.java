package com.halilibo.bettervideoplayer.subtitle;

import java.io.File;

public interface DownloadCallback {
    void onDownload(File file);
    void onFail(Exception e);
}
