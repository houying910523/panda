package com.mv.data.panda.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * author: houying
 * date  : 18-1-11
 * desc  :
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private Utils(){}

    public static File createTempDir() {
        File tmp = Files.createTempDir();
        tmp.deleteOnExit();
        return tmp;
    }

    public static String extractSparkSubmitCmd(File shell) {
        try {
            StringBuilder sb = new StringBuilder();
            boolean start = false;
            for(String line: Files.readLines(shell, Charsets.UTF_8)) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("spark-submit")) {
                    start = true;
                }
                if (start) {
                    sb.append(line).append(" ");
                }
            }
            if (start) {
                return sb.toString();
            }
        } catch (IOException e) {
            logger.error("打开文件失败: {}", shell.getPath(), e);
        }
        return null;
    }

    public static <T> T listToOne(List<T> list) {
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public static String convertToEmail(String usernames) {
        return Arrays.stream(usernames.split(","))
                .map(username -> username + "@mobvista.com")
                .reduce((v1, v2) -> v1 + "," + v2)
                .get();
    }

}
