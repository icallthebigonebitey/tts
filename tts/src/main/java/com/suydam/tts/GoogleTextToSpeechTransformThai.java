package com.suydam.tts;

public class GoogleTextToSpeechTransformThai extends GoogleTextToSpeechTransform {

	/**
	 * 
	 */
	public GoogleTextToSpeechTransformThai() {
		this(0);
	}
	
	/**
	 * 
	 * @param delayMS number of milliseconds to inject for pauses
	 */
	public GoogleTextToSpeechTransformThai(int delayMS) {
		super(delayMS);
		
		// standalone consonants
		registerRule(new TransformRule("บ", true, "บ ใบไม"));
		registerRule(new TransformRule("น", true, "นอ หนู"));
		registerRule(new TransformRule("ถ", true, "ท่อ ถุง"));
		registerRule(new TransformRule("ผ", true, "พ่อ ผึ้ง"));
		registerRule(new TransformRule("ธ", true, "ท่อ ธง"));
		registerRule(new TransformRule("ฅ", true, "ขอ คน"));
		registerRule(new TransformRule("ฌ", true, "ชอ เฌอ"));
		
		// standalone vowels
		
	}
	
	public static void main(String[] args) {
		TextToSpeechTransform g = new GoogleTextToSpeechTransformThai();
		System.out.println(g.transform("to stay"));
		System.out.println(g.transform("to stay / to be at"));
		System.out.println(g.transform("to stay / to be at / like spanish estar"));
		System.out.println(g.transform("บ"));
		System.out.println(g.transform("ชอบ"));
		System.out.println(g.transform("น"));
		System.out.println(g.transform("นอน"));
	}
}
