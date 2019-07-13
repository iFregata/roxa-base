/**
 * The MIT License
 * 
 * Copyright (c) 2018-2020 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Steven Chen
 *
 */
public class SysInfo {

	private NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
	private NumberFormat fmtDec = new DecimalFormat("###,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
	private NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));

	private OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

	private Map<String, String> jvmInfo = new LinkedHashMap<>();
	private Map<String, String> threadsInfo = new LinkedHashMap<>();
	private Map<String, String> memoryInfo = new LinkedHashMap<>();
	private Map<String, String> classesInfo = new LinkedHashMap<>();
	private Map<String, String> osInfo = new LinkedHashMap<>();

	public SysInfo collect() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

		jvmInfo.put("Java Virtual Machine", runtime.getVmName() + " version " + runtime.getVmVersion());
		jvmInfo.put("Version", System.getProperty("java.version"));
		jvmInfo.put("Vendor", runtime.getVmVendor());
		jvmInfo.put("Pid", getPid());
		jvmInfo.put("Uptime", durationText(runtime.getUptime()));

		try {
			Class<?> sunOS = Class.forName("com.sun.management.OperatingSystemMXBean");
			jvmInfo.put("Process CPU time", durationText(getValueAsLong(sunOS, "getProcessCpuTime") / 1000000));
			jvmInfo.put("Process CPU load", fmtDec.format(getValueAsDouble(sunOS, "getProcessCpuLoad")));
			jvmInfo.put("System CPU load", fmtDec.format(getValueAsDouble(sunOS, "getSystemCpuLoad")));
		} catch (Throwable t) {
		}

		try {
			Class<?> unixOS = Class.forName("com.sun.management.UnixOperatingSystemMXBean");
			jvmInfo.put("Open file descriptors", longText(getValueAsLong(unixOS, "getOpenFileDescriptorCount")));
			jvmInfo.put("Max file descriptors", longText(getValueAsLong(unixOS, "getMaxFileDescriptorCount")));
		} catch (Throwable t) {
		}
		jvmInfo.put("Total compile time",
				durationText(ManagementFactory.getCompilationMXBean().getTotalCompilationTime()));

		threadsInfo.put("Live threads", Integer.toString(threads.getThreadCount()));
		threadsInfo.put("Daemon threads", Integer.toString(threads.getDaemonThreadCount()));
		threadsInfo.put("Peak", Integer.toString(threads.getPeakThreadCount()));
		threadsInfo.put("Total started", Long.toString(threads.getTotalStartedThreadCount()));

		memoryInfo.put("Current heap size", printSizeInKb(mem.getHeapMemoryUsage().getUsed()));
		memoryInfo.put("Maximum heap size", printSizeInKb(mem.getHeapMemoryUsage().getMax()));
		memoryInfo.put("Committed heap size", printSizeInKb(mem.getHeapMemoryUsage().getCommitted()));
		memoryInfo.put("Pending objects", Integer.toString(mem.getObjectPendingFinalizationCount()));
		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			String val = "Name = '" + gc.getName() + "', Collections = " + gc.getCollectionCount() + ", Time = "
					+ durationText(gc.getCollectionTime());
			memoryInfo.put("Garbage collector", val);
		}

		classesInfo.put("Current classes loaded", longText(cl.getLoadedClassCount()));
		classesInfo.put("Total classes loaded", longText(cl.getTotalLoadedClassCount()));
		classesInfo.put("Total classes unloaded", longText(cl.getUnloadedClassCount()));

		osInfo.put("Name", os.getName() + " version " + os.getVersion());
		osInfo.put("Architecture", os.getArch());
		osInfo.put("Processors", Integer.toString(os.getAvailableProcessors()));
		try {
			osInfo.put("Total physical memory", printSizeInKb(getSunOsValueAsLong(os, "getTotalPhysicalMemorySize")));
			osInfo.put("Free physical memory", printSizeInKb(getSunOsValueAsLong(os, "getFreePhysicalMemorySize")));
			osInfo.put("Committed virtual memory",
					printSizeInKb(getSunOsValueAsLong(os, "getCommittedVirtualMemorySize")));
			osInfo.put("Total swap space", printSizeInKb(getSunOsValueAsLong(os, "getTotalSwapSpaceSize")));
			osInfo.put("Free swap space", printSizeInKb(getSunOsValueAsLong(os, "getFreeSwapSpaceSize")));
		} catch (Throwable t) {
		}
		return this;
	}

	public void printAll(OutputStream out) {
		PrintWriter pw = new PrintWriter(out, true);
		pw.println("JVM");
		int maxNameLen = 25;
		jvmInfo.entrySet().stream().forEach(e -> printValue(pw, e.getKey(), maxNameLen, e.getValue()));
		pw.println("Threads");
		threadsInfo.entrySet().stream().forEach(e -> printValue(pw, e.getKey(), maxNameLen, e.getValue()));
		pw.println("Memory");
		memoryInfo.entrySet().stream().forEach(e -> printValue(pw, e.getKey(), maxNameLen, e.getValue()));
		pw.println("Classes");
		classesInfo.entrySet().stream().forEach(e -> printValue(pw, e.getKey(), maxNameLen, e.getValue()));
		pw.println("Operating system");
		osInfo.entrySet().stream().forEach(e -> printValue(pw, e.getKey(), maxNameLen, e.getValue()));
	}

	public Map<String, String> getJvmInfo() {
		return this.jvmInfo;
	}

	public Map<String, String> getThreadsInfo() {
		return this.threadsInfo;
	}

	public Map<String, String> getMemoryInfo() {
		return this.memoryInfo;
	}

	public Map<String, String> getOsInfo() {
		return this.osInfo;
	}

	public Map<String, String> getClassesInfo() {
		return this.classesInfo;
	}

	private String getPid() {
		// In Java 9 the new process API can be used:
		// long pid = ProcessHandle.current().getPid();
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String[] parts = name.split("@");
		return parts[0];
	}

	protected String durationText(double uptime) {
		uptime /= 1000;
		if (uptime < 60) {
			return fmtD.format(uptime) + " seconds";
		}
		uptime /= 60;
		if (uptime < 60) {
			long minutes = (long) uptime;
			String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
			return s;
		}
		uptime /= 60;
		if (uptime < 24) {
			long hours = (long) uptime;
			long minutes = (long) ((uptime - hours) * 60);
			String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
			if (minutes != 0) {
				s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
			}
			return s;
		}
		uptime /= 24;
		long days = (long) uptime;
		long hours = (long) ((uptime - days) * 24);
		String s = fmtI.format(days) + (days > 1 ? " days" : " day");
		if (hours != 0) {
			s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
		}
		return s;
	}

	private long getSunOsValueAsLong(OperatingSystemMXBean os, String name) throws Exception {
		Method mth = os.getClass().getMethod(name);
		return (Long) mth.invoke(os);
	}

	private long getValueAsLong(Class<?> osImpl, String name) throws Exception {
		if (osImpl.isInstance(os)) {
			Method mth = osImpl.getMethod(name);
			return (Long) mth.invoke(os);
		}
		return -1;
	}

	private double getValueAsDouble(Class<?> osImpl, String name) throws Exception {
		if (osImpl.isInstance(os)) {
			Method mth = osImpl.getMethod(name);
			return (Double) mth.invoke(os);
		}
		return -1;
	}

	private String longText(long i) {
		return fmtI.format(i);
	}

	private String printSizeInKb(double size) {
		return fmtI.format((long) (size / 1024)) + " kbytes";
	}

	void printValue(PrintWriter out, String name, int pad, String value) {
		out.println(name + spaces(pad - name.length()) + "   " + value);
	}

	String spaces(int nb) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nb; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
}
