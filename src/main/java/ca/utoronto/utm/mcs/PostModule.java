package ca.utoronto.utm.mcs;

import dagger.Module;
import dagger.Provides;


@Module
public class PostModule {
	
	private static Post post;
	
	@Provides public Post providePost() {
		
		post = new Post();
		return post;
		
	}
	
}
