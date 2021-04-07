package ru.bulldog.justchat;

import org.apache.logging.log4j.LogManager;

public class Logger {

	private final static org.apache.logging.log4j.Logger SYSTEM_LOGGER;
	private final static org.apache.logging.log4j.Logger CHAT_LOGGER;

	public static Logger getLogger(Class<?> owner) {
		return new Logger(owner);
	}

	private final org.apache.logging.log4j.Logger classLogger;

	private Logger(Class<?> owner) {
		this.classLogger = LogManager.getLogger(owner);
	}

	public void debug(String message) {
		classLogger.debug(message);
	}

	public void info(String message) {
		classLogger.info(message);
	}

	public void warn(String message) {
		classLogger.warn(message);
	}

	public void warn(String message, Throwable ex) {
		classLogger.warn(message, ex);
	}

	public void error(String message) {
		classLogger.error(message);
	}

	public void error(String message, Throwable ex) {
		classLogger.error(message, ex);
	}

	public static void logChat(String from, String to, String message) {
		CHAT_LOGGER.info(String.format("[%s][%s] %s", from, to, message));
	}

	public static void logInfo(String message) {
		SYSTEM_LOGGER.info(message);
	}

	public static void logWarn(String message) {
		SYSTEM_LOGGER.warn(message);
	}

	public static void logWarn(String message, Throwable ex) {
		SYSTEM_LOGGER.warn(message, ex);
	}

	public static void logError(String message) {
		SYSTEM_LOGGER.error(message);
	}

	public static void logError(String message, Throwable ex) {
		SYSTEM_LOGGER.error(message, ex);
	}

	static {
		SYSTEM_LOGGER = LogManager.getLogger("system");
		CHAT_LOGGER = LogManager.getLogger("chat");
	}
}
