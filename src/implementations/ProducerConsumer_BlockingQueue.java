package implementations;

/*
 * Implementation of producer and Consumer problem using 
 * a blocking queue
 * 
 * Producer produces an item and adds it to the list
 * consumer consumes an item from the list
 * 
 */
public class ProducerConsumer_BlockingQueue {
	
	BlockingQueue<Integer> bq;
	
	ProducerConsumer_BlockingQueue() {
		this.bq = new BlockingQueue<>(20);
	}
	
	public static void main(String[] args) {
		ProducerConsumer_BlockingQueue o = new ProducerConsumer_BlockingQueue();
		
		Thread producer1 = new Thread(()-> {
			int i = 0;
			while(true) {
				try {
					System.out.println("Producer1 adding "+i+" to the queue");
					o.getBlockingQueue().offer( i++ );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread consumer1 = new Thread(()-> {
			while(true) {
				int v = -1;
				try {
					v = o.getBlockingQueue().poll().intValue();
					System.out.println("Consumer1 got "+v+" from the queue");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		Thread consumer2 = new Thread(()-> {
			while(true) {
				int v = -1;
				try {
					v = o.getBlockingQueue().poll().intValue();
					System.out.println("Consumer2 got "+v+" from the queue");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		producer1.start();
		consumer1.start();
		consumer2.start();
		
		try {
			producer1.join();
			consumer1.join();
			consumer2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("exiting main"); // called only in case of exceptions
	}
	
	
	BlockingQueue<Integer> getBlockingQueue() {
		return this.bq;
	}
}
