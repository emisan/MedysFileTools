package de.medys.datacompress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.medys.MedysFileIO;

/**
 * Klasse zum erstellen einer ZIP-Datei.<br><br>
 * 
 * <u>Wichtig</u>
 * <blockquote>
 * Um TAR-Dateien (Unix/Mac OSX) zu entpacken oder zu komprimieren, 
 * mu&szlig; man statt dieser Klasse die Klasse {@link TARZip} benutzen!<br><br>
 * 
 * Um GZip-Dateien zu entpacken oder zu komprimieren, 
 * mu&szlig; man statt dieser Klasse die Klasse {@link GZip} benutzen!<br><br>
 * </blockquote>
 * siehe hierzu : <a href="http://www.oracle.com/technetwork/articles/java/compress-1565076.html">Oracle-erkl&auml;rung</a>
 * 
 * @author Hayri Emrah Kayaman, MEDYS GmbH, W6uuml;lfrath 2018
 *
 */
public class Zip 
{

	private String verzeichnis;

	private String dateiName;
	
	private StringBuilder sb;
	
	public Zip()
	{
		sb = new StringBuilder();
	}
	
	public Zip(final String verzeichnis, final String dateiName)
	{
		sb = new StringBuilder();
		
		this.verzeichnis = verzeichnis;
		this.dateiName = dateiName;
	}
	
	/**
	 * Erstellt eine neue ZIP-Datei mit dem Dateinamen <code>&lt;datei-Name&gt;.zip</code> 
	 * in einem angegegebenen Verzeichnis.
	 * 
	 * @param datei
	 * 			die einzelne Datei, die gezippt werden soll
	 * @param zielVerzeichnis
	 * 			wo die gezippte Datei liegen soll
	 * @throws Exception wenn die Datei oder das Verzeichnis nicht exisitiert, ein Zugriff nicht klappt 
	 * 			oder der Inhalt nicht ausgelesen und gezippt werden konnte
	 */
	public void zipDatei(File datei, File zielVerzeichnis) throws Exception
	{
		if (MedysFileIO.istDateiKeinVerzeichnis(datei) 
			&& MedysFileIO.istVerzeichnisUndExistiert(zielVerzeichnis)) 
		{				
			if (datei.isFile())
			{
				String destination = zielVerzeichnis.getAbsolutePath() + File.separator + datei.getName() + ".zip";
				
				int BUFFER = 2048;
				
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(datei));
				
				ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination), BUFFER));
				
				ZipEntry zipEntry;
				
				// eine einzelne Datei zippen
				zipEntry = new ZipEntry(MedysFileIO.gibDateiInhaltToString(datei));
				zos.putNextEntry(zipEntry);
				
				byte[] dataBuffer = new byte[BUFFER];
				
				int count;
				
				while((count = bis.read(dataBuffer, 0, BUFFER)) != -1)
				{
					zos.write(dataBuffer, 0, count);
				}
				
				setDateiName(datei.getName() + ".zip");
				setVerzeichnis(MedysFileIO.gibValidenOrdnerPfad(zielVerzeichnis.getAbsolutePath()));
				
				bis.close();
				zos.close();
			}
			else 
			{
				sb.append("INFO aus de.medys.Zip.zipDatei(File, File)").append("\n")
				  .append("Die Datei ").append(datei.getName()).append(" ist ein Verzeichnis.")
				  .append("\n").append("Geben Sie bitte eine einzelne Datei an, um diese zu Zippen!")
				  .append("\n");
				
				throw new Exception(sb.toString());
			}
		}
	}
	
	/**
	 * Erstellt eine neue ZIP-Datei mit dem Verzeichnisnamen <code>&lt;verzeichnis-Name&gt;.zip</code> 
	 * in einem angegegebenen Verzeichnis.
	 * 
	 * @param verzeichnis
	 * 			das Verzeichnis, da&szlig; gezippt werden soll
	 * @param zielVerzeichnis
	 * 			wo das gezippte Verzeichnis liegen soll
	 * @throws Exception wenn die Datei oder das Verzeichnis nicht exisitiert, ein Zugriff nicht klappt 
	 * 			oder der Inhalt nicht ausgelesen und gezippt werden konnte
	 */
	public void zipVerzeichnis(File verzeichnis, File zielVerzeichnis) throws Exception
	{
		if (MedysFileIO.existiert(verzeichnis) 
			&& MedysFileIO.istVerzeichnisUndExistiert(zielVerzeichnis)) 
		{
			String destination = zielVerzeichnis.getAbsolutePath() + File.separator + verzeichnis.getName() + ".zip";
			
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));
			ZipEntry zipEntry;
							
			int BUFFER = 2048;
			byte[] dataBuffer = new byte[BUFFER];
			
			File[] files = verzeichnis.listFiles();
				
			if(files != null)
			{
				for(File file : files)
				{
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER);
					zipEntry = new ZipEntry(MedysFileIO.gibDateiInhaltToString(file));
					if (file.isDirectory())
					{
						zipVerzeichnis(file, zielVerzeichnis);
					}
					else
					{
						zos.putNextEntry(zipEntry);
						
						int count;
						
						while((count = bis.read(dataBuffer, 0, BUFFER)) != -1)
						{
							zos.write(dataBuffer, 0, count);
						}
						bis.close();
					}
				}
			}
			zos.close();
		}
		else
		{
			sb.append("Fehler aus de.medys.datacompress.Zip.zipInhalt(File, File)").append("Die Datei ")
					.append(zielVerzeichnis.getName()).append(" exisitiert nicht und muß ")
					.append("vorher in einem Verzeichnis erzeugt werden!").append("\n");
			
			throw new Exception(sb.toString());
		}
	}
	
	public String getVerzeichnis()
	{
		return verzeichnis;
	}

	public void setVerzeichnis(final String verzeichnis)
	{
		this.verzeichnis = verzeichnis;
	}

	public String getDateiName()
	{
		return dateiName;
	}

	public void setDateiName(final String dateiName)
	{
		this.dateiName = dateiName;
	}
	
	/**
	 * Liefert die zuletzt erstellte Zip-Datei
	 * 
	 * @return absolute File-Objekt Reference auf die zuletzt erstellte Zip-Datei, wenn keine vorhanden, dann NULL
	 */
	public File gibZipDatei()
	{
		File zipDatei = null;
		if((getVerzeichnis() != null) && (getDateiName() != null))
		{
			File delta = new File(getVerzeichnis(), getDateiName());
			
			if(MedysFileIO.istDateiKeinVerzeichnis(delta))
			{
				zipDatei = delta;
			}
		}
		return zipDatei;
	}
}
