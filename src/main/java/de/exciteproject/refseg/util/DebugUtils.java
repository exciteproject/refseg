package de.exciteproject.refseg.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import com.sun.management.UnixOperatingSystemMXBean;

@SuppressWarnings("restriction")
public class DebugUtils {

    /**
     * count currently open file descriptors
     * 
     * @return
     */
    public static long getOpenFileDescriptorCount() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof UnixOperatingSystemMXBean) {
            return ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
        } else {
            return -1;
        }
    }
}
