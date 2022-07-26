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
 * @date 2022/6/25 10:35
 */
@Component
public class SynchronizationTask {
    private final Logger LOG = LoggerFactory.getLogger(SynchronizationTask.class);

    /**
     * 每小时40分钟同步上一个小时的刷验核酸数据
     */
//    @Scheduled(cron = "0 10 * * * ?")
    public void startSynchronize() {
        try {
            Calendar calendar1h40 = Calendar.getInstance();
            Calendar calendar40 = Calendar.getInstance();

            calendar1h40.add(Calendar.MINUTE, -40);
            calendar1h40.add(Calendar.HOUR, -1);
            calendar40.add(Calendar.MINUTE, -40);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String oldString = formatter.format(calendar1h40.getTime());
            String currentString = formatter.format(calendar40.getTime());

            LOG.info("本次查询日期段：" + oldString + " 至 " + currentString);
            File file = TaskUtil.createLogFile(calendar1h40, LOG);

            // 命令执行--同步支付宝核酸、微信核酸
            String baseShell1 = "python datax.py --jvm=\"-Xms3G -Xmx6G\" hbjkm_hsxx_sztx_zfb.json";
            String baseShell2 = "python datax.py --jvm=\"-Xms3G -Xmx6G\" hbjkm_hsxx_sztx_tx.json";
            String logPath = "/data/datax/execute_log/";
            String paramShell = String.format(" -p \"-Dstart_time='%s' -Dend_time='%s'\" >>%s 2>&1 &",
                    oldString, currentString, logPath + file.getName());
            String[] cmd1 = new String[]{"/bin/sh", "-c", baseShell1 + paramShell};
            String[] cmd2 = new String[]{"/bin/sh", "-c", baseShell2 + paramShell};
            Runtime rt = Runtime.getRuntime();
            Process p1 = rt.exec(cmd1);
            p1.waitFor();
            Process p2 = rt.exec(cmd2);
            p2.waitFor();
            p1.destroy();
            p2.destroy();
        } catch (Exception e) {
            LOG.error("执行错误:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 每天1点0分同步昨天的刷验与核酸数据
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    public void syncAll() {
        try {
            Calendar calendar1d1h = Calendar.getInstance();
            Calendar calendar0d1h = Calendar.getInstance();

            calendar1d1h.add(Calendar.HOUR, -25);
            calendar0d1h.add(Calendar.HOUR, -1);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String oldString = formatter.format(calendar1d1h.getTime());
            String currentString = formatter.format(calendar0d1h.getTime());

            LOG.info("本次查询日期段：" + oldString + " 至 " + currentString);
            File file = TaskUtil.createLogFile(calendar1d1h, LOG);

            // 命令执行--同步全量刷验信息，微信端核酸，支付宝端核酸
            String baseShell = "python datax.py --jvm=\"-Xms3G -Xmx6G\" t_hbjkm_pass_record_ds_all_ds.json";
            String baseShell1 = "python datax.py --jvm=\"-Xms3G -Xmx6G\" hbjkm_hsxx_sztx_zfb_all_ds.json";
            String baseShell2 = "python datax.py --jvm=\"-Xms3G -Xmx6G\" hbjkm_hsxx_sztx_tx_all_ds.json";
            String logPath = "/data/datax/execute_log/";
            String paramShell = String.format(" -p \"-Dstart_time='%s' -Dend_time='%s'\" >>%s 2>&1 &",
                    oldString, currentString, logPath + file.getName());
            String[] cmd = new String[]{"/bin/sh", "-c", baseShell + paramShell};
            String[] cmd1 = new String[]{"/bin/sh", "-c", baseShell1 + paramShell};
            String[] cmd2 = new String[]{"/bin/sh", "-c", baseShell2 + paramShell};
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd);
            p.waitFor();
            Process p1 = rt.exec(cmd1);
            p1.waitFor();
            Process p2 = rt.exec(cmd2);
            p2.waitFor();
            p.destroy();
            p1.destroy();
            p2.destroy();
        } catch (Exception e) {
            LOG.error("执行错误:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
