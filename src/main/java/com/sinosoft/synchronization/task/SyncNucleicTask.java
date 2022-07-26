package com.sinosoft.synchronization.task;

import com.sinosoft.synchronization.util.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author hiYuzu
 * @version V1.0
 * @date 2022/7/26 15:48
 */
@Component
public class SyncNucleicTask {

    private final Logger LOG = LoggerFactory.getLogger(SyncNucleicTask.class);

    /**
     * 每小时10分钟同步上一个小时的核酸数据（给公安）
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void startSynchronize() {
        try {
            Calendar calendar1h10 = Calendar.getInstance();
            Calendar calendar10 = Calendar.getInstance();

            calendar1h10.add(Calendar.MINUTE, -10);
            calendar1h10.add(Calendar.HOUR, -1);
            calendar10.add(Calendar.MINUTE, -10);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String oldString = formatter.format(calendar1h10.getTime());
            String currentString = formatter.format(calendar10.getTime());

            LOG.info("本次查询日期段：" + oldString + " 至 " + currentString);
            File file = TaskUtil.createLogFile(calendar1h10, LOG);

            // 命令执行
            String baseShell1 = "python datax.py --jvm=\"-Xms3G -Xmx6G\" hbjkm_hbs_hsxx_ds.json";
            String logPath = "/data/datax/execute_log/";
            String paramShell = String.format(" -p \"-Dstart_time='%s' -Dend_time='%s'\" >>%s 2>&1 &",
                    oldString, currentString, logPath + file.getName());
            String[] cmd = new String[]{"/bin/sh", "-c", baseShell1 + paramShell};
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd);
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            LOG.error("执行错误:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
