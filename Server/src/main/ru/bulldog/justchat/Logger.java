package ru.bulldog.justchat;

public class Logger {

	private final Class<?> owner;
	private final String prefix;

	public Logger(Class<?> owner) {
		this.owner = owner;
		this.prefix = "[" + owner.getSimpleName() + "]";
	}

	public void info(String message) {
		System.out.println(prefix + "[INFO]: " + message);
	}

	public void warn(String message) {
		System.out.println(prefix + "[WARN]: " + message);
	}

	public void error(String message) {
		System.out.println(prefix + "[ERROR]: " + message);
	}

	public void error(String message, Throwable ex) {
		System.out.println(prefix + "[ERROR]: " + message + ": " + ex.getMessage());
	}

	public Class<?> getOwner() {
		return owner;
	}
}
