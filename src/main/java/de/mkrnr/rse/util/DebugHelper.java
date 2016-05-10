package de.mkrnr.rse.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import com.sun.management.UnixOperatingSystemMXBean;

@SuppressWarnings("restriction")
public class DebugHelper {

    public static long getOpenFileDescriptorCount() {
	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
	if (os instanceof UnixOperatingSystemMXBean) {
	    return ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
	} else {
	    return -1;
	}

    }
}
