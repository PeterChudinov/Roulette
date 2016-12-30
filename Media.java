package roulette;

public class Media {
	
	//byte arrays for both audio and video
	private byte[][] video;
	private byte[] audio;
	//type int
	private int type;
	static final int STREAM = 0, DISCONNECT = 1;
	
	public Media(byte[][] video, byte[] audio, int type) {
		this.video = video;
		this.audio = audio;
		this.type = type;
	}
	
	public void setVideo(byte[][] video) {
		this.video = video;
	}
	
	public void setAudio(byte[] audio) {
		this.audio = audio;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public byte[][] getVideo() {
		return video;
	}
	
	public byte[] getAudio() {
		return audio;
	}
	
	public int getType() {
		return type;
	}
	
}
