package com.suydam.tts;

import org.apache.commons.cli.*;

//Imports the Google Cloud client library
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Hello world!
 *
 */
public class GoogleTTS 
{
	private TextToSpeechClient textToSpeechClient;
	private AudioConfig audioConfig;
	
    public static void main( String[] args )
    {
    	String verbiage = "ม";
    	String language = "th-TH";
    	
    	// define options
        Options options = new Options();
        options.addOption(new Option("v", "verbiage", true, "the verbiage to verbalize"));
        options.addOption(new Option("l", "language", true, "the language code to use"));
                
        CommandLineParser parser = new DefaultParser(); 
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null; 

        try {
            cmd = parser.parse(options, args);  //it will parse according to the options and parse option value
            
            if (cmd.hasOption("v")) {
            	verbiage = cmd.getOptionValue("verbiage");
            	System.out.println("Verbalizing: " + verbiage);
            }
            
            if (cmd.hasOption("l")) {
            	language = cmd.getOptionValue("language");
            	System.out.println("Language Code: " + language);
            }
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Quiz", options); 

            System.exit(1);
        } 

        System.out.println("Verbalizing: " + verbiage);
        System.out.println("Language Code: " + language);
        
        try {
        	GoogleTTS client = new GoogleTTS();
        	String resource = client.renderAsMP3(verbiage, language);
        	
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(1);
        }
     
        System.exit(0);
    }
    
    public GoogleTTS() throws IOException {
    	textToSpeechClient = TextToSpeechClient.create();
    	
    	// Select the type of audio file you want returned
    	audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
    }
    
    public String renderAsMP3(String verbiage, String language) {
    	// Set the text input to be synthesized
    	SynthesisInput input = SynthesisInput.newBuilder().setText(verbiage).build();
    	
    	// Build the voice request, specify the language code and the ssml voice gender
    	// ("neutral")
    	VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode(language).
	           setSsmlGender(SsmlVoiceGender.NEUTRAL).build();
    	
    	// Perform the text-to-speech request on the text input with the selected voice parameters and
	    // audio file type
    	SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
    	
    	// Get the audio contents from the response
    	ByteString audioContents = response.getAudioContent();

    	   
    	// Write the response to the output file.
    	String fileName = "output.mp3";
    	try (OutputStream out = new FileOutputStream(fileName)) {
    		out.write(audioContents.toByteArray());
    		out.close();
    	    System.out.println("Audio content written to file \"output.mp3\"");
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    	
    	return fileName;
    }
}

