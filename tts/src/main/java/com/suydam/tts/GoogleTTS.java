package com.suydam.tts;

import org.apache.commons.cli.*;

import javax.sound.sampled.*;

/* requires google-cloud-pubsub dependency in pom.xml
 * https://cloud.google.com/java/docs/reference/google-cloud-pubsub/latest/com.google.pubsub.v1.ProjectSubscriptionName
 * https://cloud.google.com/java/docs/reference/google-cloud-pubsub/latest/com.google.cloud.pubsub.v1.SubscriptionAdminClient
 * Would also need to create a Topic on project and a subscription for that Topic to run the code below
 * */
//import com.google.pubsub.v1.*;
//import com.google.cloud.pubsub.v1.*;


//Imports the Google Cloud client library
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
//import com.google.cloud.internal.PickFirstLoadBalancer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

/**
 * Hello world!
 *
 */
public class GoogleTTS implements LineListener
{
	private TextToSpeechClient textToSpeechClient;
	private AudioConfig audioConfig;
	boolean playCompleted;
	
    public static void main( String[] args )
    {
    	String verbiage = "งาน";
    	// งาน  ม
    	// "ในวันฝนพรำเธอคิดถึงกันบ้างไหมในตอนที่ไม่ได้เจอแล้วเธอนั้นเป็นอย่างไร"
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
        
        /*
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("TTS Quickstart", subscriptionId);
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            subscriptionAdminClient.getSubscription(subscriptionName); // Exception is thrown in this line
        } catch (Exception t) {
        	t.printStackTrace();
        	System.exit(1);
        }
        */
        
        try {
        	GoogleTTS client = new GoogleTTS();
        	//String resource = client.renderAsMP3(verbiage, language);
        	client.playAsWAV(verbiage, language);
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(1);
        }
     
        System.exit(0);
    }
    
    
    public GoogleTTS() throws IOException {
    	//LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    	textToSpeechClient = TextToSpeechClient.create();
    }
    
    
    private ByteString synthesize(String verbiage, String language) {
 
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
    	return response.getAudioContent();
    }
    
    public String renderAsMP3(String verbiage, String language) {
    	// Select the type of audio file you want returned
    	// https://cloud.google.com/text-to-speech/docs/reference/rpc/google.cloud.texttospeech.v1#google.cloud.texttospeech.v1.AudioEncoding
    	audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
    	
		ByteString audioContents = synthesize(verbiage, language);
		
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
    
    public void playAsWAV(String verbiage, String language) {
    	// Select the type of audio file you want returned
    	// https://cloud.google.com/text-to-speech/docs/reference/rpc/google.cloud.texttospeech.v1#google.cloud.texttospeech.v1.AudioEncoding
    	audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).build();
    	
    	ByteString audioContents = synthesize(verbiage, language);
    	InputStream inputStream = audioContents.newInput();
    	
    	try {
	    	AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
	    	
	    	AudioFormat audioFormat = audioStream.getFormat();
	    	DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
	    	
	    	Clip audioClip = (Clip) AudioSystem.getLine(info);
	    	audioClip.addLineListener(this);
	    	audioClip.open(audioStream);
	    	audioClip.start();
	    	
	    	while (!playCompleted) {
                // wait for the playback completes
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
             
            audioClip.close();
            audioStream.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    	
    }
    
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
         
        if (type == LineEvent.Type.STOP) {
            playCompleted = true;
            System.out.println("Playback completed.");
        }
    }
}

