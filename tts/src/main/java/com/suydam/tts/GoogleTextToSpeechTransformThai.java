package com.suydam.tts;

public class GoogleTextToSpeechTransformThai extends GoogleTextToSpeechTransform {
	
	/*
	 * 
	 * Google TTS not correctly handling certain vowels and consonants
	 * filed with this form: https://issuetracker.google.com/issues/new?component=451645&template=1161335
	 * 
	 * google issue: 244970396
	 * - บ ("baw") should be something like "Baaw bai-máai"
	 * - น ("nalika") should be something like  "Naaw nǔu"
	 * - ผ ("haw hooo") should be something like "Phǎaw phûeng"
	 * - ถ ("haw  thǔng") should be something like "Thǎaw thǔng"
	 * - ฅ ("haw khon") should be something like "Khaaw khon"
	 * - ธ ("haw thong") should be something like "Thaaw thong"
	 * - ฌ ("taw thoo") - should be something like "Chaaw cher"
	 * - ฤ ("raw Rúe") should be something like "Rúe"
	 * - แ ("beh") should be something like "Sa-ra ehhh" (long ae)
	 * - ฦ ("law Lúe") should be something like "Lúe"
	 * 
	 */

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
		
		// fix pronunciation of several standalone consonants
		registerRule(new TransformRule("บ", true, "บ ใบไม"));
		registerRule(new TransformRule("น", true, "นอ หนู"));
		registerRule(new TransformRule("ถ", true, "ท่อ ถุง"));
		registerRule(new TransformRule("ผ", true, "พ่อ ผึ้ง"));
		registerRule(new TransformRule("ธ", true, "ท่อ ธง"));
		registerRule(new TransformRule("ฅ", true, "ขอ คน"));
		registerRule(new TransformRule("ฌ", true, "ชอ เฌอ"));
		
		// fix pronunciation of "sah-rah"
		registerRule(new TransformRule("สระ", false, "สะระ"));
		
		// fix pronunciation of several standalone vowels
		
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
