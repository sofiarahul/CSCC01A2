package ca.utoronto.utm.mcs;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = PostModule.class)
public interface PostComponent {
	
	Post getPost();

}
