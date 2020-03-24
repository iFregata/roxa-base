/**
 * The MIT License
 * 
 * Copyright (c) 2016 Shell Technologies PTY LTD
 *
 * You may obtain a copy of the License at
 * 
 *       http://mit-license.org/
 *       
 */
package io.roxa.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * @author steven
 *
 */
public class QRCodes {

	public static void qrCode(String content, OutputStream out, int size) throws WriterException, IOException {
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
		int CrunchifyWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < CrunchifyWidth; i++) {
			for (int j = 0; j < CrunchifyWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		ImageIO.write(image, "png", out);

	}

	public static void qrCodeWithLogoAndLabel(String content, String iconPath, String labelText, OutputStream out,
			int size) throws WriterException, IOException {
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
		int CrunchifyWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < CrunchifyWidth; i++) {
			for (int j = 0; j < CrunchifyWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		image = decorateIconAndLabel(image, size, iconPath, labelText);
		ImageIO.write(image, "png", out);
	}

	public static String imgDataUrl(String content, String labelText, int size) throws WriterException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String _labelText = Strings.emptyAsNull(labelText);
		if (_labelText == null) {
			_labelText = String.valueOf(System.currentTimeMillis());
		}
		qrCodeWithLabel(content, _labelText, bos, size);
		String imgBase64Encode = Codecs.asBase64(bos.toByteArray());
		return String.format("data:image/%s;base64,%s", "png", imgBase64Encode);
	}

	public static void qrCodeWithLabel(String content, String labelText, OutputStream out, int size)
			throws WriterException, IOException {
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
		int CrunchifyWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < CrunchifyWidth; i++) {
			for (int j = 0; j < CrunchifyWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		image = appendLabel(image, size, labelText);
		ImageIO.write(image, "png", out);
	}

	public static void qrCodeWithLogo(String content, OutputStream out, int size, String iconPath, int scaleSize)
			throws WriterException, IOException {
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
		int CrunchifyWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < CrunchifyWidth; i++) {
			for (int j = 0; j < CrunchifyWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		image = nestIcon(image, size, iconPath, scaleSize);
		ImageIO.write(image, "png", out);

	}

	public static void qrCode(String content, OutputStream out) throws WriterException, IOException {
		qrCode(content, out, 250);
	}

	protected static BufferedImage appendLabel(BufferedImage source, int qrcodeSize, String labelText)
			throws IOException {
		// String[] fontFamilys =
		// GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		// logger.info("Font Familys:{}", Arrays.toString(fontFamilys));
		Font font = new Font("Serif", Font.BOLD, 12);
		int sw = source.getWidth();
		int sh = source.getHeight();

		BufferedImage combined = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) combined.getGraphics();
		g2.drawImage(source, 0, 0, null);
		g2.setFont(font);
		FontRenderContext frc = g2.getFontRenderContext();
		Rectangle2D boundsLabel = font.getStringBounds(labelText, frc);
		int wText = Math.min((int) boundsLabel.getWidth(), sw);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		int rX = (sw - wText) / 2;
		int rY = sh - 4;
		g2.setColor(Color.BLACK);
		g2.drawString(labelText, rX, rY);
		return combined;
	}

	protected static BufferedImage decorateIconAndLabel(BufferedImage source, int qrcodeSize, String iconPath,
			String labelText) throws IOException {
		Image iconFile = ImageIO.read(new File(iconPath));
		int scaleSize = (int) (qrcodeSize / 6.25);
		Image scaledIcon = iconFile.getScaledInstance(scaleSize, scaleSize, Image.SCALE_SMOOTH);
		BufferedImage iconImageBuf = new BufferedImage(scaleSize, scaleSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = iconImageBuf.getGraphics();
		g.drawImage(scaledIcon, 0, 0, null);
		g.dispose();

		int deltaHeight = source.getHeight() - iconImageBuf.getHeight();
		int deltaWidth = source.getWidth() - iconImageBuf.getWidth();

		BufferedImage combined = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) combined.getGraphics();
		g2.drawImage(source, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2.drawImage(iconImageBuf, Math.round(deltaWidth / 2), Math.round(deltaHeight / 2), null);

		return appendLabel(combined, qrcodeSize, labelText);
	}

	protected static BufferedImage nestIcon(BufferedImage source, int qrcodeSize, String iconPath, int scaleSize)
			throws IOException {
		Image iconFile = ImageIO.read(new File(iconPath));
		Image scaledIcon = iconFile.getScaledInstance(scaleSize, scaleSize, Image.SCALE_SMOOTH);
		BufferedImage iconImageBuf = new BufferedImage(scaleSize, scaleSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = iconImageBuf.getGraphics();
		g.drawImage(scaledIcon, 0, 0, null);
		g.dispose();

		int deltaHeight = source.getHeight() - iconImageBuf.getHeight();
		int deltaWidth = source.getWidth() - iconImageBuf.getWidth();

		BufferedImage combined = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) combined.getGraphics();
		g2.drawImage(source, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2.drawImage(iconImageBuf, Math.round(deltaWidth / 2), Math.round(deltaHeight / 2), null);

		return combined;
	}

}
