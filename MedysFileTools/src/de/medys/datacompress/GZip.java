package de.medys.datacompress;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

import de.medys.datadecompress.Unzip;

/**
 * Klasse zum Komprimieren von Daten zu GZip-Dateien und Dekomprimieren von GZip-Dateien in menschen-lesbare Zeichen.<br><br>
 * 
 * <u>Wichtig</u>
 * <blockquote>
 * Um Zip-Dateien (Windows) zu entpacken, 
 * mu&szlig; man statt dieser Klasse die Klasse {@link Unzip} benutzen!<br><br>
 * </blockquote>
 * siehe hierzu : <a href="http://www.oracle.com/technetwork/articles/java/compress-1565076.html">Oracle-erkl&auml;rung</a>
 * 
 * @author Hayri Emrah Kayaman, MEDYS GmbH, W&uuml;lfrath 2018
 *
 */
public class GZip
{
	/**
	 * Komprimiert gegebene Daten in einem gegebenen Zeichnsatz in 
	 * GZip-komprimierte Bytes
	 * 
	 * @param data die Eingangsdaten
	 * @param charset der Zeichensatz, in dem die eingangsdaten kodiert sind
	 * @return die GZip-komprimierten Daten der Eingagnsdaten in dem angegebenen Zeichensatz, 
	 * 			wenn die Eingangsparameter nicht <code>NULL</code> sind, sonst <code>NULL</code>
	 * @throws IOException wenn die Kompression auf die Daten nicht zugreifen konnte oder diese
	 * 			nicht beschreiben konnte
	 */
	public static byte[] compress(String data, Charset charset) throws IOException
	{
		byte[] compressed = null;
		
		if((data != null) && (charset != null))
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data.getBytes(charset.name()));
			gzip.close();
			compressed = bos.toByteArray();
			bos.close();
		}
		
		return compressed;
	}
	
	/**
	 * Dekomprimiert gegebene Daten und liefert daraufhin die Daten in menschen-lesbarer 
	 * Form als Zeichenkette in dem angegebenen Zeichensatz zur&uuml;ck. 
	 * 
	 * @param compressed die GZip-komprimierten Daten
	 * @param charset der Zeichensatz der zur&uuml;ckgelieferten Zeichenkette
	 * @return menschen-lesbare Zeichen der komprimierten Daten in dem angegebenen Zeichensatz, 
	 * 			wenn die Eingangsparameter nicht <code>NULL</code> sind, sonst <code>NULL</code>
	 * @throws IOException wenn die Dekompression auf die Daten nicht zugreifen konnte oder diese
	 * 			nicht auslesen konnte bzw. als Ausgabe produzieren konnte
	 */
	public static String decompress(byte[] compressed, Charset charset) throws IOException
	{
		String s = null;
		StringBuilder sb = null;
		if((compressed != null) && (charset != null))
		{
			sb = new StringBuilder();
			ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
			GZIPInputStream gis = new GZIPInputStream(bis);
			BufferedReader br = new BufferedReader(new InputStreamReader(gis, charset.name()));
			String line;
			while((line = br.readLine()) != null) 
			{
				sb.append(line);
			}
			br.close();
			gis.close();
			bis.close();
		}
		if(sb != null)
		{
			s = sb.toString();
		}
		return s;
	}
}

