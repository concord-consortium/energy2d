package org.concord.energy2d.system;

/**
 * @author Charles Xie
 */

public abstract class Task {

	public static final int PERMANENT = Integer.MAX_VALUE;

	private int lifetime = PERMANENT;
	private int interval = 10;
	private int minInterval = 1;
	private int maxInterval = 5000;
	private int minLifetime = 100;
	private int maxLifetime = 1000;
	private String name = "Unknown";
	private String description;
	private boolean enabled = true;
	private boolean completed;
	private boolean systemTask = true;
	private String script;
	private int priority = Thread.NORM_PRIORITY;

	public Task() {
		name += "@" + Long.toHexString(System.currentTimeMillis());
	}

	/**
	 * @param i
	 *            interval for executing this task
	 */
	public Task(int i) {
		this();
		setInterval(i);
	}

	/**
	 * @param i
	 *            interval for executing this task
	 * @param j
	 *            total steps for executing this task
	 */
	public Task(int i, int j) {
		this();
		setInterval(i);
		setLifetime(j);
	}

	public abstract void execute();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		enabled = b;
	}

	public void setSystemTask(boolean b) {
		systemTask = b;
	}

	public boolean isSystemTask() {
		return systemTask;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Task))
			return false;
		return ((Task) o).getName().equals(getName());
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public void setCompleted(boolean b) {
		completed = b;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setPriority(int i) {
		priority = i;
	}

	public int getPriority() {
		return priority;
	}

	public void setLifetime(int i) {
		lifetime = i;
	}

	public int getLifetime() {
		return lifetime;
	}

	public int getMinLifetime() {
		return minLifetime;
	}

	public void setMinLifetime(int i) {
		minLifetime = i;
	}

	public int getMaxLifetime() {
		return maxLifetime;
	}

	public void setMaxLifetime(int i) {
		maxLifetime = i;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int i) {
		interval = i;
	}

	public int getMinInterval() {
		return minInterval;
	}

	public void setMinInterval(int i) {
		minInterval = i;
	}

	public int getMaxInterval() {
		return maxInterval;
	}

	public void setMaxInterval(int i) {
		maxInterval = i;
	}

	public void setName(String s) {
		name = s;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String s) {
		description = s;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return getName() + ":" + getPriority();
	}

}