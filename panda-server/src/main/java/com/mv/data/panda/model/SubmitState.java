package com.mv.data.panda.model;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
public class SubmitState {
    public static final int SUBMITTED = 0;
    public static final int ACCEPTED = 1;
    public static final int RUNNING = 2;
    public static final int FINISHED = 3;
    public static final int FAILED = 4;
    public static final int KILLED = 5;

    private static String[] array = new String[]{"SUBMITTED", "ACCEPTED", "RUNNING", "FINISHED", "FAILED", "KILLED"};

    public static String valueOf(int state) {
        return state < array.length ? array[state]:null;
    }

    public static int valueOf(String state) {
        switch (state) {
        case "ACCEPTED": return ACCEPTED;
        case "RUNNING": return RUNNING;
        case "FINISHED": return FINISHED;
        case "FAILED": return FAILED;
        case "KILLED": return KILLED;
        default: throw new IllegalArgumentException("不可识别的状态" + state);
        }
    }
}
