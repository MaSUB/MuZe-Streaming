
// Inspired by the work of Arthur Assuncao at http://thiscouldbebetter.wordpress.com/2011/07/04/pausing-an-mp3-file-using-jlayer/

package RTSPtest;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.PlaybackListener;



// Music player that uses JLayer library to implement play and pause of music

public class Mp3Player implements Runnable{
	protected String filePath;
	protected JLayerPlayerPausable player;
	protected Thread playerThread;
	protected String namePlayerThread = "AudioPlayerThread";
	private JLayerPlayerPausable.PlaybackAdapter playbackListener = new JLayerPlayerPausable.PlaybackAdapter();

	// Constructor, Creates a music player with a filePath to the mp3file
	public Mp3Player(String filePath){
		this.filePath = filePath;
	}
	
	// Constructor, Creates a player with file path to the mp3file and a name for the PlayerThread
	public Mp3Player(String filePath, String namePlayerThread){
		this.filePath = filePath;
		this.namePlayerThread = namePlayerThread;
	}

	// Starts playing music if it hasn't been started, resumes if paused or stopped.
	public void play(){
		if (this.player == null){
			this.playerInitialize();
		}
		else if(!this.player.isPaused() || this.player.isComplete() || this.player.isStopped()){
			this.stop();
			this.playerInitialize();
		}
		this.playerThread = new Thread(this, namePlayerThread);
		this.playerThread.setDaemon(true);

		this.playerThread.start();
	}

	// Pause music playback
	public void pause(){
		if (this.player != null){
			this.player.pause();

			if(this.playerThread != null)
				this.playerThread = null;
			
		}
	}

	// Alternate paly and pause of music
	public void pauseToggle(){
		if (this.player != null){
			if (this.player.isPaused() && !this.player.isStopped()){
				this.play();
			}
			else{
				this.pause();
			}
		}
	}

	// Stop the playing of music
	public void stop(){
		if (this.player != null){
			this.player.stop();

			if(this.playerThread != null){
				this.playerThread = null;
			}
		}
	}
	
	// Verify if the music is completed
	public boolean isComplete(){
		return this.player.isComplete();
	}	
	// Initialize the music player
	protected void playerInitialize(){
		try {
			this.player = new JLayerPlayerPausable(this.filePath);
			this.player.setPlaybackListener(this.playbackListener);
		}
		catch (JavaLayerException e) {
			e.printStackTrace();
		}
	}

	// IRunnable members
	@Override
	public void run(){
		try{
			this.player.resume();
		}
		catch (javazoom.jl.decoder.JavaLayerException ex){
			ex.printStackTrace();
		}
	}
}