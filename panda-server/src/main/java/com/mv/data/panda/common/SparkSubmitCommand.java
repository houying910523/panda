package com.mv.data.panda.common;


import com.mv.data.panda.model.SubmitResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: houying
 * date  : 18-1-15
 * desc  :
 */
public class SparkSubmitCommand {
    private final static Pattern executorNumPtn = Pattern.compile("--num-executors\\s+([0-9]+)");
    private final static Pattern executorCoresPtn = Pattern.compile("--executor-cores\\s+([0-9]+)");
    private final static Pattern executorMemoryPtn = Pattern.compile("--executor-memory\\s+([^\\s]+)");
    private final String cmd;

    public SparkSubmitCommand(String cmd) {
        this.cmd = cmd.trim();
    }

    public int getExecutorNum() throws ParseException, NumberFormatException {
        Matcher matcher = executorNumPtn.matcher(cmd);
        if (!matcher.find()) {
            throw new ParseException("未指定--num-executors");
        }
        return Integer.valueOf(matcher.group(1));
    }

    public int getExecutorCores() throws ParseException, NumberFormatException {
        Matcher matcher = executorCoresPtn.matcher(cmd);
        if (!matcher.find()) {
            throw new ParseException("未指定--executor-cores");
        }
        return Integer.valueOf(matcher.group(1));
    }

    public String getExecutorMemory() throws ParseException {
        Matcher matcher = executorMemoryPtn.matcher(cmd);
        if (!matcher.find()) {
            throw new ParseException("未指定--executor-memory");
        }
        return matcher.group(1);
    }

    public String validate() {
        try {
            getExecutorNum();
            getExecutorCores();
            getExecutorMemory();
            return null;
        } catch (ParseException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "--executor-cores or --executor-num is not number";
        }
    }

    public SubmitResource getSubmitResource() {
        try {
            return new SubmitResource(getExecutorNum(), getExecutorCores(), getExecutorMemory());
        } catch (ParseException e) {
            return null;
        }
    }

}
