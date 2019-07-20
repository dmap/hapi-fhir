package ca.uhn.fhir.jpa.subscription.module.config;

import ca.uhn.fhir.jpa.model.sched.ISchedulerService;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamProvider;
import ca.uhn.fhir.jpa.subscription.module.cache.ISubscriptionProvider;
import org.springframework.context.annotation.*;

import static org.mockito.Mockito.mock;

@Configuration
@Import(TestSubscriptionConfig.class)
public class TestSubscriptionDstu3Config extends SubscriptionDstu3Config {
	@Bean
	@Primary
	public ISearchParamProvider searchParamProvider() {
		return new MockFhirClientSearchParamProvider();
	}

	@Bean
	@Primary
	public ISubscriptionProvider subsriptionProvider() {
		return new MockFhirClientSubscriptionProvider();
	}

	@Bean
	public ISchedulerService schedulerService() {
		return mock(ISchedulerService.class);
	}

}
