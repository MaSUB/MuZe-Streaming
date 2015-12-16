/*
 *	Inspired by the work of Arthur Assuncao and Paulo Vitor
 */

package RTSPtest;

/* *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *   
 *   Original by: http://thiscouldbebetter.wordpress.com/2011/07/04/pausing-an-mp3-file-using-jlayer/
 *   Last modified: 21-jul-2012 by Arthur Assuncao 
 *----------------------------------------------------------------------
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

//use with JLayerPausableTest
/* Player that uses a library to allow play and pause
 * @author devon
 */
public class JLayerPlayerPausable{
	// This class is loosely based on javazoom.jl.player.AdvancedPlayer.

	protected java.net.URL urlToStreamFrom;
	protected String audioPath;
	protected Bitstream bitstream;
	protected Decoder decoder;
	protected AudioDevice audioDevice;
	protected boolean closed;
	protected boolean complete;
	protected boolean paused;
	protected boolean stopped;
	protected PlaybackListener listener;
	protected int frameIndexCurrent;
	private final int lostFrames = 52; //some fraction of a second of the sound gets "lost" after every pause. 52 in original code

	// Create a player with the file name of an mp3 file
	public JLayerPlayerPausable(URL urlToStreamFrom) throws JavaLayerException{
		this.urlToStreamFrom = urlToStreamFrom;
		this.listener = new PlaybackAdapter();
	}
	
	// Create a player with a specified address
	public JLayerPlayerPausable(String audioPath) throws JavaLayerException{
		this.audioPath = audioPath;
		this.listener = new PlaybackAdapter();
	}

	// Sets a listener for player events
	public void setPlaybackListener(PlaybackListener newPlaybackListener) throws NullPointerException{
		if(newPlaybackListener != null){
			this.listener = newPlaybackListener;
		}
		else{
			throw new NullPointerException("PlaybackListener is null");
		}
	}
	
	// Returns the actual size fo the file
	protected long getFileSize(){
		if(this.audioPath != null){
			return new File(this.audioPath).length();
		}
		else if(this.urlToStreamFrom != null){
			URLConnection conexaoURL;
			try {
				conexaoURL = this.urlToStreamFrom.openConnection();
			}
			catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
			return conexaoURL.getContentLength();
		}
		return -1;
	}

	// Returns the audio input stream
	protected InputStream getAudioInputStream() throws IOException{
		if(this.audioPath != null){
			return new FileInputStream(this.audioPath);
		}
		else if(this.urlToStreamFrom != null){
			this.urlToStreamFrom.openStream();
		}
		return null;
	}

	// Play music
	public boolean play() throws JavaLayerException{
		return this.play(0);
	}

	// Play music, with specific index to beginning of desired frame
	public boolean play(int frameIndexStart) throws JavaLayerException {
		return this.play(frameIndexStart, -1, lostFrames);
	}

	// Play music, with specific range of frames to play
	public boolean play(int frameIndexStart, int frameIndexFinal, int correctionFactorInFrames) throws JavaLayerException{
		try {
			this.bitstream = new Bitstream(this.getAudioInputStream());
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		this.audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
		this.decoder = new Decoder();
		this.audioDevice.open(this.decoder);

		boolean shouldContinueReadingFrames = true;

		this.paused = false;
		this.stopped = false;
		this.frameIndexCurrent = 0;

		while (shouldContinueReadingFrames == true && this.frameIndexCurrent < frameIndexStart - correctionFactorInFrames){
			shouldContinueReadingFrames = this.skipFrame();
			this.frameIndexCurrent++;
		}

		if (this.listener != null) {
			this.listener.playbackStarted(new PlaybackEvent(this, PlaybackEvent.EventType.Instances.Started, this.audioDevice.getPosition()));
		}

		if (frameIndexFinal < 0){
			frameIndexFinal = Integer.MAX_VALUE;
		}

		while (shouldContinueReadingFrames == true && this.frameIndexCurrent < frameIndexFinal){
			if (this.paused || this.stopped){
				shouldContinueReadingFrames = false;    
				try{
					Thread.sleep(1);
				}
				catch (Exception ex){
					ex.printStackTrace();
				}
			}
			else{
				shouldContinueReadingFrames = this.decodeFrame();
				this.frameIndexCurrent++;
			}
		}

		// last frame, ensure all data flushed to the audio device.
		if (this.audioDevice != null && !this.paused){
			this.audioDevice.flush();

			synchronized (this){
				this.complete = true;
				this.close();
			}

			// report to listener
			if (this.listener != null) {
				int audioDevicePosition = -1;
				if(this.audioDevice != null){
					audioDevicePosition = this.audioDevice.getPosition();
				}
				else{
					//throw new NullPointerException("attribute audioDevice in " + this.getClass() + " is NULL");
				}
				PlaybackEvent playbackEvent = new PlaybackEvent(this, PlaybackEvent.EventType.Instances.Stopped, audioDevicePosition);
				this.listener.playbackFinished(playbackEvent);
			}
		}

		return shouldContinueReadingFrames;
	}

	// Continue music playback
	public boolean resume() throws JavaLayerException{
		return this.play(this.frameIndexCurrent);
	}

	// End the playback of music
	public synchronized void close(){
		if (this.audioDevice != null){
			this.closed = true;

			this.audioDevice.close();

			this.audioDevice = null;

			try{
				this.bitstream.close();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

	// Decodes the music frame
	protected boolean decodeFrame() throws JavaLayerException{
		boolean returnValue = false;
		if(this.stopped){ //nothing for decode
			return false;
		}

		try{
			if (this.audioDevice != null){
				Header header = this.bitstream.readFrame();
				if (header != null){
					// sample buffer set when decoder constructed
					SampleBuffer output = (SampleBuffer) this.decoder.decodeFrame(header, this.bitstream);

					synchronized (this){
						if (this.audioDevice != null){
							this.audioDevice.write(output.getBuffer(), 0, output.getBufferLength());
						}
					}

					this.bitstream.closeFrame();
					returnValue = true;
				}
				else{
					System.err.println("End of file"); //end of file
					//this.stop();
					returnValue = false;
				}
			}
		}
		catch (RuntimeException ex){
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}
		return returnValue;
	}

	// Pauses the playback of music
	public void pause(){
		if(!this.stopped){
			this.paused = true;
			if (this.listener != null) {
				this.listener.playbackPaused(new PlaybackEvent(this, PlaybackEvent.EventType.Instances.Paused, this.audioDevice.getPosition()));
			}
			this.close();
		}
	}

	// Skip bits of the header
	protected boolean skipFrame() throws JavaLayerException{
		boolean returnValue = false;
		Header header = this.bitstream.readFrame();

		if (header != null) {
			this.bitstream.closeFrame();
			returnValue = true;
		}

		return returnValue;
	}

	// Stop playback of music
	public void stop(){
		if(!this.stopped){
			if(!this.closed){
				this.listener.playbackFinished(new PlaybackEvent(this, PlaybackEvent.EventType.Instances.Stopped, this.audioDevice.getPosition()));
				this.close();
			}
			else if(this.paused){
				int audioDevicePosition = -1; //this.audioDevice.getPosition(), audioDevice is null
				this.listener.playbackFinished(new PlaybackEvent(this, PlaybackEvent.EventType.Instances.Stopped, audioDevicePosition));
			}
			this.stopped = true;
		}
	}

	// Return whether or not the player is closed or not	 
	public boolean isClosed() {
		return closed;
	}
	
	// Return whether or not the music has finished playing or not
	public boolean isComplete() {
		return complete;
	}

	// Return if the music is paused or not
	public boolean isPaused() {
		return paused;
	}

	// Return if the music is stopped or not
	public boolean isStopped() {
		return stopped;
	}
	
	// inner classes
	// Classes for the execution of music playing
	public static class PlaybackEvent{
		private JLayerPlayerPausable source;
		private EventType eventType;
		private int frameIndex;

		// Create an instance of the playback event
		public PlaybackEvent(JLayerPlayerPausable source, EventType eventType, int frameIndex){
			this.source = source;
			this.eventType = eventType;
			this.frameIndex = frameIndex;
		}
		
		// Return an referent to the object where the event occured from
		public JLayerPlayerPausable getSource() {
			return source;
		}

		// Return the type of event
		public EventType getEventType() {
			return eventType;
		}

		// Return the Index of the frame
		public int getFrameIndex() {
			return frameIndex;
		}

		// Class with the types of events
		public static class EventType{
			protected String name;

			// Creates an instance of the type of event			 
			public EventType(String name){
				this.name = name;
			}
			
			// Return the type of event
			public String getName() {
				return name;
			}

			// Class with the instances of the different types of events
			public static class Instances{
				public static EventType Started = new EventType("Started");
				public static EventType Paused = new EventType("Paused");
				public static EventType Stopped = new EventType("Stopped");
			}
		}
	}

	// Class with the implementation of the events
	public static class PlaybackAdapter implements PlaybackListener{

       
		@Override
		public void playbackStarted(PlaybackEvent event){
			System.err.println("Playback started");
		}
		@Override
		public void playbackPaused(PlaybackEvent event){
			System.err.println("Playback paused");
		}
		@Override
		public void playbackFinished(PlaybackEvent event){
			System.err.println("Playback stopped");
		}
	}

	// Interface for event handlers
	public static interface PlaybackListener{
		// Method to deal with the playbackStarted event
		public void playbackStarted(PlaybackEvent event);
		// Method to deal with the playbackPaused event
		public void playbackPaused(PlaybackEvent event);
		// Method to deal with the playbackFinished event
		public void playbackFinished(PlaybackEvent event);
	}
}