package com.tme.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Autowired
	private ApplicationContext context;

	/*
	 * This test proves that the application can be loaded successful and that all @configurations and dependencies are there
	 */
	@Test
	public void contextLoads() throws Exception {
		assertThat(context).isNotNull();
	}
}
