package implementations;

import java.util.ArrayDeque;
import java.util.Queue;

/*
 * Responsibility - 
 * Blocks the caller of enqueue if there is no more capacity in it
 * Blocks the caller of dequeue if there are no more items in it
 * 
 * Signals the blocked caller of enqueue when space is available
 * Signals the blocked caller of deque when items are available
 */

public class BlockingQueue<T> {
	Queue<T> queue;
	int capacity;
	
	public BlockingQueue(int capacity) {
		this.capacity = capacity;
		queue = new ArrayDeque<T>(capacity);
	}
	
	public synchronized void offer(T o) throws InterruptedException {
		while(queue.size() == this.capacity) {
			this.wait();
		}
		queue.offer(o);
		this.notifyAll();
	}
	
	public synchronized T poll() throws InterruptedException {
		while(queue.isEmpty()) {
			this.wait();
		}
		T ret = queue.poll();
		this.notifyAll();
		return ret;
	}
	
	//size can change when other is trying to modify the queue concurrently
	public synchronized int size() {
		return queue.size();
	}
	
	public int capacity() {
		return this.capacity;
	}
	
	//since size can change when other thread is trying to modify queue concurrently
	public synchronized boolean isEmpty() {
		return queue.size()==0;
	}
}
