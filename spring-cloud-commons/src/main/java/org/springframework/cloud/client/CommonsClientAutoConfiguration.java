/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.httpservice.CircuitBreakerRestClientHttpServiceGroupConfigurer;
import org.springframework.cloud.client.circuitbreaker.httpservice.CircuitBreakerWebClientHttpServiceGroupConfigurer;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicator;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.DiscoveryCompositeHealthContributor;
import org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Cloud Commons Client.
 *
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @author Omer Naci Soydemir
 */
@Configuration(proxyBeanMethods = false)
public class CommonsClientAutoConfiguration {

	@ConditionalOnClass({ CircuitBreaker.class, RestClientHttpServiceGroupConfigurer.class })
	@ConditionalOnBean(CircuitBreakerFactory.class)
	@ConditionalOnProperty(value = "spring.cloud.circuitbreaker.http-services.enabled", havingValue = "true",
			matchIfMissing = true)
	@Configuration(proxyBeanMethods = false)
	protected static class CircuitBreakerInterfaceClientsAutoConfiguration {

		@Bean
		public CircuitBreakerRestClientHttpServiceGroupConfigurer circuitBreakerRestClientConfigurer(
				CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
			return new CircuitBreakerRestClientHttpServiceGroupConfigurer(circuitBreakerFactory);
		}

	}

	@ConditionalOnClass({ CircuitBreaker.class, ReactiveCircuitBreaker.class,
			WebClientHttpServiceGroupConfigurer.class })
	@ConditionalOnBean({ CircuitBreakerFactory.class, ReactiveCircuitBreakerFactory.class })
	@ConditionalOnProperty(value = "spring.cloud.circuitbreaker.reactive-http-services.enabled", havingValue = "true",
			matchIfMissing = true)
	@Configuration(proxyBeanMethods = false)
	protected static class ReactiveCircuitBreakerInterfaceClientsAutoConfiguration {

		@Bean
		public CircuitBreakerWebClientHttpServiceGroupConfigurer circuitBreakerWebClientConfigurer(
				ReactiveCircuitBreakerFactory<?, ?> reactiveCircuitBreakerFactory,
				CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
			return new CircuitBreakerWebClientHttpServiceGroupConfigurer(reactiveCircuitBreakerFactory,
					circuitBreakerFactory);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(HealthIndicator.class)
	@EnableConfigurationProperties(DiscoveryClientHealthIndicatorProperties.class)
	@ConditionalOnBean(DiscoveryClient.class)
	@ConditionalOnDiscoveryEnabled
	@ConditionalOnBlockingDiscoveryEnabled
	protected static class DiscoveryLoadBalancerConfiguration {

		@Bean
		@ConditionalOnDiscoveryHealthIndicatorEnabled
		public DiscoveryClientHealthIndicator discoveryClientHealthIndicator(
				ObjectProvider<DiscoveryClient> discoveryClient, DiscoveryClientHealthIndicatorProperties properties) {
			return new DiscoveryClientHealthIndicator(discoveryClient, properties);
		}

		@Bean
		@ConditionalOnProperty(value = "spring.cloud.discovery.client.composite-indicator.enabled",
				matchIfMissing = true)
		@ConditionalOnBean({ DiscoveryHealthIndicator.class })
		public DiscoveryCompositeHealthContributor discoveryCompositeHealthContributor(
				List<DiscoveryHealthIndicator> indicators) {
			return new DiscoveryCompositeHealthContributor(indicators);
		}

		@Bean
		public HasFeatures springCloudCommonsFeatures() {
			return HasFeatures.abstractFeatures(DiscoveryClient.class, LoadBalancerClient.class);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Endpoint.class)
	@ConditionalOnProperty(value = "spring.cloud.features.enabled", matchIfMissing = true)
	protected static class ActuatorConfiguration {

		@Autowired(required = false)
		private List<HasFeatures> hasFeatures = new ArrayList<>();

		@Bean
		@ConditionalOnAvailableEndpoint
		public FeaturesEndpoint featuresEndpoint() {
			return new FeaturesEndpoint(this.hasFeatures);
		}

	}

}
