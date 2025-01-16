package com.project.yogerOrder;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S", defaultLockAtLeastFor = "PT10S")
@SpringBootApplication
@ConfigurationPropertiesScan
public class YogerOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(YogerOrderApplication.class, args);
	}

}
