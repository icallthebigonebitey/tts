package com.suydam.tts;

/*
 * This is a tricky class because it contains transforms that make
 * the es-US pronunciation approximate Swahili pronunciation!
 */
public class GoogleTextToSpeechTransformSwahili extends GoogleTextToSpeechTransform {

	/**
	 * 
	 */
	public GoogleTextToSpeechTransformSwahili() {
		this(0);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransformSwahili(int delayMS) {
		super(delayMS);
		
		// fix pronunciation of "lake"
		registerRule(new TransformRule("lake", false, "laque"));
		
	}
}
