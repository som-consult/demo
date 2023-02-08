/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibm.test.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * A simple demo for cron-job like tasks that get executed regularly. It also
 * demonstrates how property values can be set. Users can set the property
 * values in /system/console/configMgr
 */
@ObjectClassDefinition(name = "Multi Scheduler Demo Service", description = "Demo for multi-scheduler")
public @interface MultiSchedulerConfiguration {

	@AttributeDefinition(name = "Concurrent task", description = "Whether or not to schedule this task concurrently", type = AttributeType.BOOLEAN)
	boolean isSchedulerConcurrent() default true;

	// BUK Site
	@AttributeDefinition(name = "BUK Cron Job Expression", description = "Cron Expression for BUK scheduler", type = AttributeType.STRING)
	String schedulerExpressionBuk() default "0 0/55 11 * * ?";

	@AttributeDefinition(name = "BUK Report Generation AEM DAM Path", description = "BUK Report Path", type = AttributeType.STRING)
	String schedulerReportPathBuk() default "/content/dam/barclays/reports/buk";

	@AttributeDefinition(name = "Enabled", description = "Check the box to enable the BUK scheduler", type = AttributeType.BOOLEAN)
	boolean isBukEnabled() default true;

	// Barclay Cards Site
	@AttributeDefinition(name = "BCards Cron Job Expression", description = "Cron Expression for BCards scheduler", type = AttributeType.STRING)
	String schedulerExpressionBcard() default "0 0/58 11 * * ?";

	@AttributeDefinition(name = "BCards Report Generation AEM DAM Path", description = "BCards Report Path", type = AttributeType.STRING)
	String schedulerReportPathBcard() default "/content/dam/barclays/reports/bcards";

	@AttributeDefinition(name = "Enabled", description = "Check the box to enable the BCARDS scheduler", type = AttributeType.BOOLEAN)
	boolean isBcardEnabled() default true;
}