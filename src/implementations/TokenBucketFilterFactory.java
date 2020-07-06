package implementations;

//Factory class to create TokenBucketFilter objects
//this class creates TokenBucketFilter objects that are thread safe
//Responsibility
//provide static method to return TokenBucketFilter object
public class TokenBucketFilterFactory {
	
	private static class MultiThreadedTokenBucketFilter implements TokenBucketFilter{
		
		private int availableTokens;
		private final int MAX_TOKENS;
		private final int GENERATION_RATE;
		
		private MultiThreadedTokenBucketFilter(int capacity) {
			this.availableTokens = 0;
			this.MAX_TOKENS = capacity;
			this.GENERATION_RATE = 1000; //in milli seconds
		}
		
		private void initTokenGenerator() {
			Thread generator = new Thread( () -> this.tokenGenerator() );
			generator.setName("Token Generator thread");
			generator.setDaemon(true);		//low priority thread to generate tokens, don't stop jvm from exiting
		}
		
		private void tokenGenerator() {
			while(true) {
				synchronized(this) {
					if(availableTokens < MAX_TOKENS)
						availableTokens++;
					this.notifyAll();
				}
				try {
					Thread.sleep(GENERATION_RATE);
				} catch(InterruptedException e) {
					System.out.println(" token Generation interrupted");
				}
			}
		}
		
		@Override
		public void getToken() {
			
			synchronized(this) {
				while(availableTokens == 0) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						System.out.println("wait for token interrupted ! thread = "+Thread.currentThread().getName());
					}
				}
				
				availableTokens--;
				System.out.println("Got token from TokenBucket at "+System.currentTimeMillis());
			}
		}
		
	}
	
	public static TokenBucketFilter createTokenBucketFilter(int capacity) {
		TokenBucketFilter o = new MultiThreadedTokenBucketFilter(capacity);
		
		((MultiThreadedTokenBucketFilter) o).initTokenGenerator();
		
		return o;
		
	}
}

