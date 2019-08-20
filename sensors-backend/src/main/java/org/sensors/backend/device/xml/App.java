package org.sensors.backend.device.xml;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class App {

	private ObjectMapper mapper;

	public App() {
		mapper = new ObjectMapper();
		mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, As.WRAPPER_OBJECT);
		mapper.addMixIn(TestParent.class, TestParentMixin.class);
		mapper.addMixIn(ATest.class, ATestMixin.class);
		mapper.addMixIn(BTest.class, BTestMixin.class);
	}

	public String writeToString() throws JsonProcessingException {
		TestParent parent = new TestParent(Arrays.asList(new ATest(1, "A"), new BTest(2, "desc")));
		return mapper.writeValueAsString(parent);
	}

	public TestParent readFromString(String value) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(value, TestParent.class);
	}

	public static void main(String[] args) throws IOException {
		App app = new App();
		String json = app.writeToString();
		System.out.println(json);
		TestParent readFromString = app.readFromString(json);
		System.out.println(readFromString);
	}

}
