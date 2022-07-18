package org.jisho.textosJapones.util.similarity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageHistogram {

	final static public double SIMILAR = 1; // Images close to 1 are very similar
	final static public double NOT_SIMILAR = 0; // Images close to 0 are not similar
	// Expected threshold for the image to be similar, where above it will be equal
	final static public double LIMIAR = 0.7;

	private int redBins;
	private int greenBins;
	private int blueBins;

	public ImageHistogram() {
		redBins = greenBins = blueBins = 4;
	}

	public float[] generate(InputStream is) throws IOException {
		return filter(ImageIO.read(is));
	}

	private float[] filter(BufferedImage src) {
		int width = src.getWidth();
		int height = src.getHeight();

		int[] inPixels = new int[width * height];
		float[] histogramData = new float[redBins * greenBins * blueBins];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		int redIdx = 0, greenIdx = 0, blueIdx = 0;
		int singleIndex = 0;
		float total = 0;
		for (int row = 0; row < height; row++) {
			int tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				redIdx = (int) getBinIndex(redBins, tr, 255);
				greenIdx = (int) getBinIndex(greenBins, tg, 255);
				blueIdx = (int) getBinIndex(blueBins, tb, 255);
				singleIndex = redIdx + greenIdx * redBins + blueIdx * redBins * greenBins;
				histogramData[singleIndex] += 1;
				total += 1;
			}
		}

		for (int i = 0; i < histogramData.length; i++) {
			histogramData[i] = histogramData[i] / total;
		}

		return histogramData;
	}

	private float getBinIndex(int binCount, int color, int colorMaxValue) {
		float binIndex = (((float) color) / ((float) colorMaxValue)) * ((float) binCount);
		if (binIndex >= binCount)
			binIndex = binCount - 1;
		return binIndex;
	}

	private int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
		int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
			return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
		return image.getRGB(x, y, width, height, pixels, 0, width);
	}

	public double match(File srcFile, File canFile) throws IOException {
		float[] sourceData = this.filter(ImageIO.read(srcFile));
		float[] candidateData = this.filter(ImageIO.read(canFile));
		return calcSimilarity(sourceData, candidateData);
	}

	public double match(URL srcUrl, URL canUrl) throws IOException {
		float[] sourceData = this.filter(ImageIO.read(srcUrl));
		float[] candidateData = this.filter(ImageIO.read(canUrl));
		return calcSimilarity(sourceData, candidateData);
	}

	private double calcSimilarity(float[] sourceData, float[] candidateData) {
		double[] mixedData = new double[sourceData.length];
		for (int i = 0; i < sourceData.length; i++) {
			mixedData[i] = Math.sqrt(sourceData[i] * candidateData[i]);
		}

		// The values of Bhattacharyya Coefficient ranges from 0 to 1,
		double similarity = 0;
		for (int i = 0; i < mixedData.length; i++) {
			similarity += mixedData[i];
		}

		// The degree of similarity
		return similarity;
	}

	public boolean match(float[] sourceData, float[] candidateData) {
		if (sourceData == null || candidateData == null)
			return false;

		return calcSimilarity(sourceData, candidateData) >= LIMIAR;
	}

	public boolean match(float[] sourceData, float[] candidateData, double limiar) {
		if (sourceData == null || candidateData == null)
			return false;

		return calcSimilarity(sourceData, candidateData) >= limiar;
	}

	public double matchLimiar(float[] sourceData, float[] candidateData, double limiar) {
		if (sourceData == null || candidateData == null)
			return 0;

		return calcSimilarity(sourceData, candidateData);
	}
}