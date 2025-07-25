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

package org.springframework.cloud.client.discovery.health;

import org.springframework.boot.health.contributor.Health;

/**
 * A health indicator interface specific to a DiscoveryClient implementation.
 *
 * @author Spencer Gibb
 */
public interface DiscoveryHealthIndicator {

	String getName();

	/**
	 * @return An indication of health.
	 */
	Health health();

}
