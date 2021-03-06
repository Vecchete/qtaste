package com.qspin.qtaste.testapi.impl.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.qspin.qtaste.testapi.api.LinuxProcess;
import com.qspin.qtaste.testapi.api.ProcessStatus;
import com.qspin.qtaste.testsuite.QTasteException;
import com.qspin.qtaste.testsuite.QTasteTestFailException;
import com.qspin.qtaste.util.OS;

public class LinuxProcessImpl extends ProcessImpl implements LinuxProcess {

    public LinuxProcessImpl(String pInstanceId) throws QTasteException {
        super(pInstanceId);
        if (OS.getType() != OS.Type.LINUX) {
            throw new QTasteTestFailException("Cannot create a Linux process on a non Linux operating system.");
        }
    }

    @Override
    public void start() throws QTasteException {
        super.start();
        mPid = searchPid();
    }

    @Override
    public void terminate() throws QTasteException {
        if (getStatus() == ProcessStatus.RUNNING) {
            killProcessWithSignal(-9);
        }
    }

    @Override
    public void killProcess() throws QTasteException {
        killProcessWithSignal(15);
    }

    @Override
    public void killProcessWithSignal(int pSignal) throws QTasteException {
        if (getStatus() != ProcessStatus.RUNNING) {
            throw new QTasteTestFailException("Unable to stop a non running process.");
        }
        try {
            String command = "kill ";
            if (pSignal > 0) {
                command += "-" + pSignal + " ";
            }
            if (getPid() == -1) {
                throw new QTasteTestFailException("Cannot kill a process if the pid cannot be found!");
            }

            command += getPid();
            LOGGER.trace("Kill the process " + getInstanceId() + " with the command : " + command);
            Runtime.getRuntime().exec(command);
            Thread.sleep(10000);
            LOGGER.trace("Process " + getInstanceId() + " status : " + getStatus());
            if (getStatus() != ProcessStatus.STOPPED) {
                throw new QTasteTestFailException("The process " + getInstanceId() + " is still running.");
            }
        } catch (IOException | InterruptedException pException) {
            LOGGER.error("Unable to kill the process " + getInstanceId() + " : " + pException.getMessage(), pException);
        }
    }

    @Override
    public synchronized int getPid() throws QTasteException {
        if (getStatus() != ProcessStatus.RUNNING) {
            throw new QTasteException("Invalide state. Cannot retrieve tha pid of a non running process.");
        }
        if (mPid == -1) {
            mPid = searchPid();
        }
        return mPid;
    }

    /**
     * Searches the process'identifier of the current process. If none found, return -1.<br>
     * Only available for Unix process.
     *
     * @return the process'identifier or -1 if none found.
     */
    protected synchronized int searchPid() {
        //rebuild the process command
        String cmd = "";
        for (int i = 0; i < mParameters.length; i++) {
            if (i > 0) {
                cmd += " ";
            }
            cmd += mParameters[i];
        }

        List<String> lines = new ArrayList<>();
        //use ps command to list all processes and filter on the process command
        try {
            java.lang.Process myProcess = Runtime.getRuntime().exec("ps -eo pid,command");
            try (BufferedReader stdout = new BufferedReader(new InputStreamReader(myProcess.getInputStream()))) {
                String line;
                while ((line = stdout.readLine()) != null) {
                    if (line.contains(cmd) && !lines.contains(line)) {
                        LOGGER.info("line found for the process : " + line);
                        lines.add(line);
                    }
                }
            }
            myProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        //get the last process and retrieve the pid (first value of the line)
        if (lines.size() >= 1) {
            String line = lines.get(lines.size() - 1).trim();
            return Integer.parseInt(line.split(" ")[0]);
        } else {
            LOGGER.warn("unable to find the process pid");
            return -1;
        }
    }

    protected int mPid;
}
