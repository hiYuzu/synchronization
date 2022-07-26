package com.sinosoft.synchronization.util;

import org.slf4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author hiYuzu
 * @version V1.0
 * @date 2022/7/26 15:49
 */
public class TaskUtil {
    public static File createLogFile(Calendar calendar1d30, Logger logger) throws Exception {
        // 日志时间
        SimpleDateFormat logFormatter = new SimpleDateFormat("yyyy-MM-dd");
        // 日志文件路径
        String logPathName = "/data/datax/execute_log/corn_logbus_clue_atta_log.";
        String logFileName = logPathName + logFormatter.format(calendar1d30.getTime());
        File file = new File(logFileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new Exception("日志文件创建失败");
            }
            logger.info("日志文件创建完成:" + file.getName());
        }
        return file;
    }
}
