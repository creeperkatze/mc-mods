package dev.creeperkatze.resume;

import dev.creeperkatze.resume.config.ResumeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Resume {
	public static final String MOD_ID = "resume";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private Resume() {
	}

	public static void init() {
		ResumeConfig.HANDLER.load();
	}

	public static void onJoin(Place place) {
		Screenshots.onJoin(place);
		LoadingBackground.onJoin(place);
	}

	public static ResumeConfig config() {
		return ResumeConfig.HANDLER.instance();
	}
}
