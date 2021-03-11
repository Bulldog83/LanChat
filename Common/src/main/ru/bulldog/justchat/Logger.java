package ru.bulldog.justchat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

	private final static SimpleDateFormat DATE_FORMAT;

	private final Class<?> owner;
	private final String prefix;

	public Logger(Class<?> owner) {
		this.owner = owner;
		this.prefix = "[" + owner.getSimpleName() + "]";
	}

	private void log(String message) {
		System.out.println(DATE_FORMAT.format(new Date()) + prefix + message);
	}

	public void info(String message) {
		log("[INFO]: " + message);
	}

	public void warn(String message) {
		log("[WARN]: " + message);
	}

	public void error(String message) {
		log("[ERROR]: " + message);
	}

	public void error(String message, Throwable ex) {
		log("[ERROR]: " + message + ": " + ex.getMessage());
		ex.printStackTrace(System.out);
	}

	public Class<?> getOwner() {
		return owner;
	}

	static {
		DATE_FORMAT = new SimpleDateFormat("[HH:mm:ss dd.MM.yyyy]");
	}
}
