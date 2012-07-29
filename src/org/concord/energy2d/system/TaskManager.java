package org.concord.energy2d.system;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* 
 * @author Charles Xie
 */

public abstract class TaskManager implements Runnable {

	private List<Task> taskPool;
	private int indexOfStep;
	private List<Task> tasksToRemove, tasksToAdd;
	private TaskManagerView view;

	public TaskManager() {
		taskPool = Collections.synchronizedList(new ArrayList<Task>());
		tasksToAdd = Collections.synchronizedList(new ArrayList<Task>());
		tasksToRemove = Collections.synchronizedList(new ArrayList<Task>());
		view = new TaskManagerView(this);
	}

	/** remove all the tasks in the pool */
	public void clear() {
		taskPool.clear();
	}

	/** remove all the non-system tasks */
	public void removeAllNonSystemTasks() {
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (!t.isSystemTask())
					remove(t);
			}
		}
		processPendingRequests();
	}

	/** get customer tasks */
	public List<Task> getCustomTasks() {
		List<Task> list = new ArrayList<Task>();
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (!t.isSystemTask())
					list.add(t);
			}
		}
		return list;
	}

	/** get the running index of step */
	public int getIndexOfStep() {
		return indexOfStep;
	}

	public void setIndexOfStep(int i) {
		indexOfStep = i;
	}

	/** add a task to the task pool. */
	public void add(Task t) {
		if (t == null)
			throw new IllegalArgumentException("cannot be null.");
		if (tasksToAdd.contains(t))
			return;
		tasksToAdd.add(t);
	}

	/** remove a task from the task pool. */
	public void remove(Task t) {
		if (t == null)
			throw new IllegalArgumentException("cannot be null.");
		if (tasksToRemove.contains(t))
			return;
		tasksToRemove.add(t);
	}

	/** return true if the task pool contains the specified task */
	public boolean contains(Task t) {
		return taskPool.contains(t);
	}

	public boolean containsName(String name) {
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (t.getName().equals(name))
					return true;
			}
		}
		return false;
	}

	public boolean toBeAdded(Task t) {
		return tasksToAdd.contains(t);
	}

	public boolean hasTaskToAdd() {
		return !tasksToAdd.isEmpty();
	}

	public boolean toBeRemoved(Task t) {
		return tasksToRemove.contains(t);
	}

	public boolean hasTaskToRemove() {
		return !tasksToRemove.isEmpty();
	}

	/** return a task by name. */
	public Task getTaskByName(String s) {
		synchronized (taskPool) {
			for (Task t : taskPool) {
				if (t.getName().equals(s))
					return t;
			}
		}
		return null;
	}

	/** send the script to the model supported by this Job. */
	public abstract void runScript(String script);

	/** notify the model supported by this Job that the tasks have changed. */
	public abstract void notifyChange();

	/**
	 * execute the listed jobs sequentially. Do not synchronize this iterator block, or it may freeze (it froze on Linux), because we cannot 100% rule out that a task to be execute may call something that should have been called in the event thread.
	 */
	protected void execute() {
		processPendingRequests();
		Task task = null;
		try { // it probably won't hurt much not to synchronize this iterator
			for (Iterator<Task> it = taskPool.iterator(); it.hasNext();) {
				task = it.next();
				if (task == null || !task.isEnabled())
					continue;
				if (task.isCompleted()) {
					remove(task);
				}
				if (task.getInterval() <= 1) {
					task.execute();
				} else {
					if (indexOfStep % task.getInterval() == 0)
						task.execute();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void processPendingRequests() {
		if (!tasksToAdd.isEmpty()) {
			synchronized (tasksToAdd) {
				for (Task l : tasksToAdd)
					addJob(l);
			}
			tasksToAdd.clear();
		}
		if (!tasksToRemove.isEmpty()) {
			synchronized (tasksToRemove) {
				for (Task l : tasksToRemove)
					removeJob(l);
			}
			tasksToRemove.clear();
		}
	}

	private void removeJob(final Task l) {
		if (!contains(l))
			return;
		taskPool.remove(l);
		if (EventQueue.isDispatchThread()) {
			view.removeRow(l);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					view.removeRow(l);
				}
			});
		}
	}

	private void addJob(final Task l) {
		if (contains(l))
			return;
		if (taskPool.isEmpty()) {
			taskPool.add(l);
		} else {
			synchronized (taskPool) {
				int m = -1;
				int n = taskPool.size();
				for (int i = 0; i < n; i++) {
					if (l.getPriority() > taskPool.get(i).getPriority()) {
						m = i;
						break;
					}
				}
				if (m == -1) {
					taskPool.add(l);
				} else {
					taskPool.add(m, l);
				}
			}
		}
		if (EventQueue.isDispatchThread()) {
			view.insertRow(l);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					view.insertRow(l);
				}
			});
		}
	}

	public void setInitTaskAction(Runnable r) {
		view.setInitTaskAction(r);
	}

	public void show(final Window owner) {
		if (EventQueue.isDispatchThread()) {
			view.show(owner);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					view.show(owner);
				}
			});
		}
	}

	public String toString() {
		return taskPool.toString();
	}

}