package com.mv.data.panda;

import com.mv.data.panda.service.SparkSubmitShell;

/**
 * author: houying
 * date  : 18-2-6
 * desc  :
 */
public class SparkSubmitShellTest {
    public static void main(String[] args) throws InterruptedException {
        SparkSubmitShell shell = new SparkSubmitShell(1, "bash main.sh", "/home/houying/tmp/", "output.log");
        shell.setEnv("HADOOP_CONF_DIR", "/home/houying/tmp/hadoop");
        shell.start();
        System.out.println(shell.awaitCompletion(300000L));
        System.out.println(shell.getApplicationId());
        System.out.println(shell.getTrackingUrl());
        System.out.println(shell.getStartTime());
    }
}
