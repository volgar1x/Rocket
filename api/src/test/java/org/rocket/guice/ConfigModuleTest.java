package org.rocket.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.rocket.InjectConfig;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class ConfigModuleTest {
	Injector injector;
	Config config;

	@Before
	public void init() {
		config = ConfigFactory.empty()
			.withValue("lol", ConfigValueFactory.fromAnyRef(42));

		injector = Guice.createInjector(ConfigModule.of(config));
	}

	static class MyService {
		@InjectConfig("lol") int answerToLife;
	}

	@Test
	public void injectConfigValue() {
		MyService o = new MyService();
		injector.injectMembers(o);

		assertThat(o.answerToLife, equalTo(42));
	}

}
