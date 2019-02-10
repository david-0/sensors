package org.sensors.logic;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Hello world!
 *
 */
public class App {
	private static final String WLAN_BUTTON = "wlan-button";
	private static final String LED_BUTTON = "led-button";

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sensor-application");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		StreamsBuilder builder = new StreamsBuilder();
		KStream<String, String> events = builder.stream("events");
		events.filter((k, v) -> ledButtonCheck(k, v) || wlanButtonCheck(k, v))//
				.map((k, v) -> updateSettings(k, v)) //
				.to("settings", Produced.with(Serdes.String(), Serdes.String()));

		KafkaStreams streams = new KafkaStreams(builder.build(), props);
		streams.start();
	}

	private static KeyValue<String, String> updateSettings(String k, String v) {
		if (LED_BUTTON.equals(k)) {
		}
		// TODO Auto-generated method stub
		return null;
	}

	private static boolean wlanButtonCheck(String k, String v) {
		return LED_BUTTON.equals(k);
	}

	private static boolean ledButtonCheck(String k, String v) {
		return WLAN_BUTTON.equals(k);
	}
}
