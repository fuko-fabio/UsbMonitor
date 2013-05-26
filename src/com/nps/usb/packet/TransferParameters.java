package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 *
 */
public class TransferParameters {
	private static final int defaultStreamInSize = 16;
	private static final int defaultStreamOutSize = 48;
	private int customStreamInSize;
	private int customStreamOutSize;
	private int numberOfSeries;
	private int numberOfRepeats;
	
	/**
	 * Default constructor
	 * stream in = default input stream size 16
	 * stream out = default output stream size 48
	 * number of series = 1
	 * number of repeats = 1
	 */
	public TransferParameters() {
		customStreamInSize = defaultStreamInSize;
		customStreamOutSize = defaultStreamOutSize;
		numberOfSeries = 1;
		numberOfRepeats = 1;
	}
	
	/**
	 * @param streamInSize size of input stream
	 * @param streamOutSize size of output stream
	 */
	public TransferParameters(int streamInSize, int streamOutSize) {
		customStreamInSize = streamInSize;
		customStreamOutSize = streamOutSize;
		numberOfSeries = 1;
		numberOfRepeats = 1;
	}
	
	/**
	 * @param streamInSize size of input stream
	 * @param streamOutSize size of output stream
	 * @param series number of series
	 * @param repeats number of repeats
	 */
	public TransferParameters(int streamInSize, int streamOutSize, int series, int repeats) {
		customStreamInSize = streamInSize;
		customStreamOutSize = streamOutSize;
		numberOfSeries = series;
		numberOfRepeats = repeats;
	}
	
	/**
	 * @return size of input stream
	 */
	public int getStreamInSize(){
		return customStreamInSize;
	}
	
	/**
	 * @param size size of input stream
	 */
	public void setStreamInSize(int size){
		this.customStreamInSize = size;
	}
	
	/**
	 * @return size of output stream
	 */
	public int getStreamOutSize(){
		return customStreamOutSize;
	}
	
	/**
	 * @param size size of output stream
	 */
	public void setStreamOutSize(int size){
		this.customStreamOutSize = size;
	}
	
	/**
	 * @return default size of input stream
	 */
	public int getDefaultStreamInSize(){
		return defaultStreamInSize;
	}
	
	/**
	 * @return default size of output stream
	 */
	public int getDefaultStreamOutSize(){
		return defaultStreamOutSize;
	}
	
	/**
	 * @return number of series
	 */
	public int getNumberOfSeries(){
		return numberOfSeries;
	}
	
	/**
	 * @param series number of series
	 */
	public void setNumberOfSeries(int series){
		this.numberOfSeries = series;
	}
	
	/**
	 * @return number of repeats
	 */
	public int getNumberOfRepeats(){
		return numberOfRepeats;
	}
	
	/**
	 * @param repeats number of repeats
	 */
	public void setNumberOfRepeats(int repeats){
		this.numberOfRepeats = repeats;
	}
}
