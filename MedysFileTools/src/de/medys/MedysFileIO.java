package de.medys;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.FileWriterWithEncoding;


/**
 * MedyysFileIO-Klasse dient f&uuml;r Input/Output-Operationen auf Dateien.
 * <br>
 * <br>
 * Eine Datei kann im Vorfeld existieren und ihre Referenz kann durch die Angaben des Verzeichnispfads und Dateinamen zur einer Datei
 * festgelegt oder durch den passenden Konstruktor aufgerufen werden.
 * <br><br> 
 * Der Default-Arbeitsverzeichnis ist das Home-Verzeichnis eines Benutzers {User.Home} = Desktop.
 * <br><br>
 * Dieses Arbeitsverzeichnis wird in jedem Konstruktor dieser Klasse prim&auml;r fesgelegt und kann f&uuml;r 
 * Anwendungsf&auml;lle, die diese Pfadangabe ben&ouml;tigen, durch {@link #gibArbeitsverzeichnis()} erfragt werden.
 * <br><br>
 * Man kann das Arbeitsverzeichnis nach der Erzeugung einer Instanz von {@link MedysFileIO} durch
 * {@link #setzeArbeitsverzeichnisPfad(String)} &auml;ndern.
 * <br><br>
 * <u>Logging</u>
 * <blockquote>
 * Intern kommt für das Logging die Klasse {@link MedysLogger} zum Einsatz, wenn man die Methode 
 * {@link #starteLogger()} nach einem Konstruktoraufruf dieser Klasse aufrufen wird.
 * </blockquote>
 * 
 * @author Hayri Emrah Kayaman, W&uuml;lfrath 2016
 *
 */
public class MedysFileIO {
	
	// Instanzvariablen
	//
	private String pathToFile;
	private String fileName;
	private String fileFormat; // aka file-extension
	private String folderName;
	
	private String arbeitsverzeichnis;
	
	// Hilfsklassen
	//
	private static MedysLogger medysLogger = new MedysLogger();;
	private static StringBuilder sb = new StringBuilder();
	
	
	protected FileHandler fileHandler;
	
	protected Formatter formatter;
	
	/**
	 * Erzeugt eine neue Instanz von {@link MedysFileIO}
	 * und setzt intern die Verzeichnispfadangabe zum Desktop eines Benutzers
	 * als Arbeitsverzeichnis.
	 * <br>
	 * <br>
	 * Man kann mittels {@link #setzeArbeitsverzeichnisPfad(String)} das Arbeitsverzeichnis neu setzen.
	 * <br>
	 */
	public MedysFileIO()
	{
		arbeitsverzeichnis = gibBenutzerDesktopPfad();
	}
	
	/**
	 * Erzeugt eine neue Instanz von {@link MedysFileIO}
	 * und hinterlegt die Verzeichnispfadangabe zu einer Datei, sowie den
	 * Namen dieser Datei.
	 * <br><br>
	 * Das Arbeitsverzeichnis wird intern auf den Desktop des Benutzers eingestellt
	 * und kann später mittels {@link #setzeArbeitsverzeichnisPfad(String)} ersetzt werden.
	 * 
	 * @param pathToFile Verzeichnispfadangabe zu einer Datei
	 * @param fileName der Dateiname
	 */
	public MedysFileIO(String pathToFile, String fileName)
	{	
		pathToFile = gibValidenOrdnerPfad(pathToFile);
		
		if(existiert(pathToFile))
		{
			setzeVerzeichnispfadZurDatei(pathToFile);
			
		}
		
		setzeDateiName(fileName);
		
		// default setzen
		//
		arbeitsverzeichnis = gibValidenOrdnerPfad(gibBenutzerDesktopPfad());
	}
	
	/**
	 * Erzeugt eine neue Instanz von {@link MedysFileIO}
	 * und hinterlegt die Verzeichnispfadangabe zu einer Datei, sowie den
	 * Namen dieser Datei.
	 * <br><br>
	 * Der dritte Parameter dieses Konstruktor dient zur Festlegung des Dateiformats der Datei.
	 * <br><br>
	 * Das Arbeitsverzeichnis wird intern auf den Desktop des Benutzers eingestellt
	 * und kann später durch {@link #setzeArbeitsverzeichnisPfad(String)} ge&auml;ndert werden.
	 * 
	 * @param pathToFile die absolute Pfadangabe zum &uuml;bergeordneten Verzeichnis eines Dateinamen
	 * @param fileName der Name einer Datei
	 * @param fileFormat das Dateiformat einer Datei
	 */
	public MedysFileIO(String pathToFile, String fileName, String fileFormat)
	{
		setzeVerzeichnispfadZurDatei(gibValidenOrdnerPfad(pathToFile));
		setzeDateiName(fileName);
		setzeDateiFormat(fileFormat);
		
		arbeitsverzeichnis = gibValidenOrdnerPfad(gibBenutzerDesktopPfad());
	}
	
	/**
	 * Fassadenmethode - ruft intern {@link MedysLogger#initLogger(Object)} auf 
	 */
	public void starteLogger()
	{
		if(medysLogger != null)
		{
			medysLogger.initLogger(this);
		}
	}
	
	/**
	 * Vergibt einer existierenden Datei (nicht Verzeichnis) einen
	 * anderen Dateinamen
	 * 
	 * @param dateiOrdner Verzeichnispfadangabe zum Ordner in der sich die Datei befindet
	 * @param dateiName der Name der Datei, die umbennant werden soll
	 * @param neuerName der neue Name der Datei
	 */
	public static void benenneUm(String dateiOrdner, String dateiName, String neuerName)
	{
		if(existiert(dateiOrdner, dateiName))
		{
			File datei = gibDatei(dateiOrdner, dateiName);
			
			datei.renameTo(new File(dateiOrdner, neuerName));
		}
	}
	
	/**
	 * Entfernt eine Datei, die unter einer absoluten Verzeichnispfadangabe existieren mu&szlig;.
	 * <br><br>
	 * <u>Wichtig</u>
	 * <blockquote>
	 * 	Hier wird <u>kein Verzeichnis</u> entfernt.<br>
	 *  Bitte nutzen Sie die Funktion {@link #loescheVerzeichnis(File)}, um ein Verzeichnis zu entfernen! 
	 * </blockquote>
	 * 
	 * @param absPathToFile absolute, existierende Verzeichnispfadangabe zur Datei
	 * @return <code>true</code> wenn die Datei existiert und entfernt wurde, sonst <code>false</code>
	 * @throws Exception wenn die Datei nicht existiert oder nicht entfernt werden konnte
	 */
	public static boolean entferneDatei(String absPathToFile) throws Exception
	{
		boolean entferntStat = false;
		
		if(existiert(absPathToFile))
		{
			File file = gibDatei(absPathToFile);
			
			file.delete();
			
			entferntStat = true;
		}
		
		return entferntStat;
	}
	
	/**
	 * Entfernt eine Datei aus dem angegebenen Ordner.<br><br>
	 * 
	 * <u>Wichtig</u>
	 * <blockquote>
	 * 	Hier wird <u>kein Verzeichnis</u> entfernt.<br>
	 *  Bitte nutzen Sie die Funktion {@link #loescheVerzeichnis(File)}, um ein Verzeichnis zu entfernen! 
	 * </blockquote>
	 * 
	 * @param dateiOrdner Verzeichnispfadangabe zum &uuml;bergeordneten Ordner einem Dateinamen
	 * @param dateiName der Name einer Datei
	 * @return <code>true</code> wenn die Datei entfernt wurde, sonst <code>false</code>
	 */
	public static boolean entferneDatei(String dateiOrdner, String dateiName)
	{
		boolean entferntStat = false;
		
		File file = new File(dateiOrdner, dateiName);
		
		if(existiert(file))
		{
			file.delete();
			entferntStat = true;
		}
		
		return entferntStat;
	}
	
	/**
	 * Erstellt ein Verzeichnis an einer angegebenen Verzeichnispfadangabe (URI).<br><br>
	 * 
	 * @param absVerzeichnispfad 
	 * 			die absolute Verzeichnispfadangabe, wo das Verzeichnis erstellt werden soll
	 * @return <code>true</code> wenn das Verzeichnis erstellt werden konnte, <code>false</code>
	 * 			wenn das Verzeichnis bereits existiert oder eine IOException ausgel&ouml;st wurde
	 * @throws IOException wenn die Pfadangabe ung&uuml;tig ist oder das Verzeichnis nicht erzeugt werden konnte 
	 * 			eventuell durch gegebene Zugriffsrechte auf die Pfadangabe oder auf das &uuml;bergeordnete Verzeichnis 
	 * 			(wenn existent)
	 */
	public static boolean erstelleVerzeichnis(String absVerzeichnispfad) throws IOException
	{
		boolean status = false;
		
		// folgende prüft auch ob das Verzeichnis existiert und ein Verzeichnis darstellt
		//
		if(!istVerzeichnisUnterAngabe(absVerzeichnispfad))
		{
			File verz = new File(absVerzeichnispfad);
			verz.mkdirs();
			status = true;
		}
		
		return status;
	}
	
	/**
	 * Er&auml;lt ein {@link java.io.File}-Objekt, so da&szlig;, wenn das Objekt nicht NULL ist, 
	 * durch seine {@link java.io.File#exists()}-Funktion gepr&uuml;ft werden kann, ob 
	 * diese File-Datei existiert oder nicht
	 * 
	 * @param file die Datei, dessen Existenz geprüft wird
	 * @return <code>true</code> wenn es exisitiert, sonst <code>false</code>
	 */
	public static boolean existiert(File file)
	{
		boolean status = false;
		
		if(file != null)
		{
			status = file.exists();
		}
		return status;
	}
	
	/**
	 * pr&uuml;ft, ob eine Datei unter der Parameterangabe
	 * exisitiert oder nicht
	 * 
	 * @param absolutePfadangabeZurDatei die absoulte Verzeichnispfadangabe zur Datei
	 * @return <code>true</code> wenn eine Datei unter der Pfadangabe existiert, sonst <code>false</code>
	 */
	public static boolean existiert(String absolutePfadangabeZurDatei)
	{
		boolean status = false;
		
		if(absolutePfadangabeZurDatei != null)
		{
			File file = new File(absolutePfadangabeZurDatei);
			
			if(existiert(file))
			{
				status = true;
			}
		}
		
		return status;
	}
	
	/**
	 * pr&uuml;ft, ob eine Datei unter den gegebenen Parameterangaben 
	 * exisitiert oder nicht
	 * 
	 * @param pathToFile die Verzeichnispfadangabe zu der Datei oder Ordner
	 * @param fileName der Name einer Datei oder eines Verzeichnisses
	 * @return <code>true</code> wenn die Datei existiert, sonst <code>false</code>
	 */
	public static boolean existiert(String pathToFile, String fileName)
	{
		boolean status = false;
		
		if(existiert(pathToFile) && (fileName != null))
		{
			if(istVerzeichnisUndExistiert(pathToFile))
			{
				File file = new File(pathToFile, fileName);
				
				if(existiert(file.getAbsolutePath()))
				{
					status = true;
				}
			}
		}
		return status;
	}
	
	/**
	 * pr&uuml;ft, ob eine Datei existiert und kein Verzeichnis ist.
	 * <br>
	 * @param file die Datei, die zu &uuml;berpr&uuml;fen gilt 
	 * 		  und kein Verzeichnis darstellt
	 * @return <b>true</b> wenn existiert und kein Verzeichnis ist, 
	 *         sonst <b>false</b> 
	 */
	public static boolean istDateiKeinVerzeichnis(File file)
	{
		boolean status = false;
		
		if(existiert(file))
		{
			if (!file.isDirectory())
			{
				status = true;
			}
		}
		return status;
	}
	
	/**
	 * Pr&uuml;f, ob der intern genutzte {@link MedysLogger} bereit ist, um 
	 * Log-Events oder Nachrichten aufzunehmen.
	 * 
	 * @return <code>true</code> wenn bereit ist, sonst <code>false</code>, da die Methode 
	 * {@link #starteLogger()} nicht aufgerufen wurde
	 */
	public static boolean istLoggingGestartet()
	{
		return medysLogger != null ? medysLogger.istLoggingGestartet() : false;
	}
	
	/**
	 * pr&uuml;ft, ob der vollst&auml;ndige Name einer Datei ein PDF-Dokument darstellt<br><br>
	 * 
	 * <u>Der vollst&auml;ndige Name einer Datei setzt sich wie folgt zusammen</u>
	 * <blockquote>
	 * 	<code>&lt;Dateiname&gt;.&lt;Dateiendung&gt;</code><br><br>
	 *  wobei <i>.&lt;Dateiendung&gt;</i> die Dateiendung festlegt
	 * </blockquote>
	 * 
	 * @param dateiName der Name der Datei
	 * @return <b>true</b> wenn die Datei mit &quot;.pdf&quot; endet, 
	 * 		   sonst <b>false</b>
	 * @see #pruefeDateiEndung(String, String)
	 */
	public static boolean istPdfDokument(String dateiName) 
	{
		boolean validState = false;
		
		if (pruefeDateiEndung(dateiName, ".pdf"))
		{
			validState = true;
		}
		
		return validState;
	}

	/**
	 * Pr&uuml;ft, ob unter der angegebenen Verzeichnispfadangabe sich ein Verzeichnis befindet.
	 * 
	 * @param verzeichnispfad
	 * 			die absolute Verzeichnispfadangabe zu einem Verzeichnis in einem Betriebssystem
	 * 
	 * @return <code>true</code> wenn die Pfadnagabe auf ein Verzeichnis verweist, <code>false</code> wenn  
	 * 			die Pfadangabe nicht auf ein Verzeichnis verweist
	 */
	public static boolean istVerzeichnisUnterAngabe(String verzeichnispfad)
	{
		boolean status  = false;
		
		if(existiert(verzeichnispfad))
		{
			File file = new File(verzeichnispfad);
			
			if(!istDateiKeinVerzeichnis(file))
			{
				status = true;
			}
		}
		return status;
	}
	
	/**
	 * pr&uuml;ft anhand einer Verzeichnispfadangabe, ob am Ende der Angabe
	 * ein Verzeichnis existiert.
	 * <br>
	 * @param verzeichnisPfad eine absolute Verzeichnispfasdangabe
	 * @return <b>true</b> wenn das Verzeichnis anhand der Pfadangabe existiert,
	 * 		   sonst <b>false</b> 
	 */
	public static boolean istVerzeichnisUndExistiert(String verzeichnisPfad)
	{
		return existiert(verzeichnisPfad)
				? istVerzeichnisUndExistiert(new File(verzeichnisPfad))
				: false;
	}
	
	/**
	 * pr&uuml;ft, ob eine Datei ein Verzeichnis ist (Existenzpr&uuml;ng beinhaltend).
	 * <br>
	 * @param file die Datei, die eine Verzeichnisstruktur darstellt
	 * @return <b>true</b> wenn existiert und eine Verzeichnis ist,
	 *         sonst <b>false</b>
	 */
	public static boolean istVerzeichnisUndExistiert(File file) 
	{
		return file != null ? file.isDirectory() : false;
	}
	
	/**
     * pr&uuml;ft, ob der vollst&auml;ndige Name einer Datei ein XML-Dokument darstellt<br><br>
     * 
     * <u>Der vollst&auml;ndige Name einer Datei setzt sich wie folgt zusammen</u>
     * <blockquote>
     * 	<code>&lt;Dateiname&gt;.&lt;Dateiendung&gt;</code><br><br>
     *  wobei <i>.&lt;Dateiendung&gt;</i> die Dateiendung festlegt
     * </blockquote>
     * 
     * @param dateiName der Name der Datei
     * @return <b>true</b> wenn die Datei mit &quot;.xml&quot; endet, 
     * 		   sonst <b>false</b>
     * @see #pruefeDateiEndung(String, String)
     */
    public static boolean istXMLDokument(String dateiName) 
    {
		boolean validState = false;
		
		if (pruefeDateiEndung(dateiName, ".xml"))
		{
			validState = true;
		}
		
		return validState;
    }
    
    /**
     * pr&uuml;ft, ob der vollst&auml;ndige Name einer Datei einer Dateiendung entspricht<br><br>
     * 
     * <u>Der vollst&auml;ndige Name einer Datei setzt sich wie folgt zusammen</u>
     * <blockquote>
     * 	<code>&lt;Dateiname&gt;.&lt;Dateiendung&gt;</code><br><br>
     *  wobei <i>.&lt;Dateiendung&gt;</i> die Dateiendung festlegt
     * </blockquote>
     * 
     * @param dateiName der Name einer Datei
     * @param dateiEndung Dateiendung oder Format der Datei
     * @return TRUE wenn Dateiname auf Endung endet, sonst FALSE
     */
    public static boolean pruefeDateiEndung(String dateiName, String dateiEndung)
    {
		return (dateiName != null) && (dateiEndung != null)
				? dateiName.endsWith(dateiEndung) ? true : false
				: false;
    }
    
    /**
	 * L&auml;dt eine Bilddatei
	 * 
	 * @param pathToPicture absoulte Verzeichnispfadangabe zum Ordner einer Bilddatei
	 * @param pictureName der Name der Bilddatei
	 * @return ein java.awt.Image-Object das die Bilddatei repr&auml;sentiert
	 * @throws IOException wenn die Bilddatei nicht geladen werden kann 
	 * @throws FileNotFoundException wenn die Bilddatei nicht existiert 
	 */
	public static BufferedImage ladeBild(String pathToPicture, String pictureName) throws IOException,FileNotFoundException
	{
		return ImageIO.read(new File(pathToPicture, pictureName));
	}
	
	/**
	 * Legt eine neue Datei an und <u>kein neues Verzeichnis</u>!!<br><br>
	 * 
	 * Wenn man ein Verzeichnis als neue Datei anlegen will, dann sollte man {@link #legeDateiAn(String, boolean)} benutzen!
	 * 
	 * @param absolutePfadangabe eine absolute Verzeichnispfadnagabe zu einer Datei, die angelegt werden soll
	 * @return <i>java.io.File</i> wenn die Datei angelegt wurde, sonst IOException versucht wird ein Verzeichnis anzulegen
	 * @throws IOException wenn die absolute Pfadangabe auf ein Verzeichnis verweist, und nicht auf die Erstellung einer einzelnen Datei
	 */
	public static File legeDateiAn(String absolutePfadangabe) throws IOException
	{		
		File file = new File(absolutePfadangabe);
		
		if (file.exists())
		{
			if (istDateiKeinVerzeichnis(file))
			{
				file.delete();
			}
			else
			{
				sb = loeschStringBuilderInhalt(sb);
				
				sb.append("IOException aus MedysFileIO.legeDateiAn(String)\nDie Pfadangabe ").append(absolutePfadangabe)
				.append(" referenziert auf ein Verzeichnis und nicht auf eine Datei!").append("\n");
				
				throw new IOException(sb.toString());
			}
		}
		FileWriter fw = new FileWriter(file);
		fw.close();
		
		return file;
	}
	
	/**
	 * Legt eine neue Datei oder ein neues Verzeichnis a.n
	 * 
	 * @param absolutePfadangabe eine absolute Verzeichnispfadnagabe zu einer Datei, die angelegt werden soll
	 * @param alsVerzeichnis
	 * 			<code>true</code> wenn ein Verzeichnis erzeugt werden soll, <code>false</code> um eine leere Datei zu erzeugen
	 * @return <i>java.io.File</i> wenn die Datei angelegt wurde, sonst Exception
	 * @throws IOException wenn unter der gegebenen Verzeichnispfadangabe die Datei oder das Verzeichnis nicht angelegt werden konnte
	 */
	public static File legeDateiAn(String absolutePfadangabe, boolean alsVerzeichnis) throws IOException
	{
		File file = new File(absolutePfadangabe);
		
		if(file.exists())
		{
			if(istDateiKeinVerzeichnis(file))
			{
				if(!file.delete())
				{
					sb = loeschStringBuilderInhalt(sb);
					
					sb.append("IOException aus MedysFileIO.legeDateiAn(String, boolean)\nKonnte die vorherige Datei unter ")
					.append(absolutePfadangabe).append(" nicht löschen!").append("\n");
					
					throw new IOException(sb.toString());
				}
				else
				{
					// eine einzelne Datei erzeugen 
					FileWriter fw = new FileWriter(file);
					fw.close();
				}
			}
			if(alsVerzeichnis)
			{
				if(!MedysFileIO.loescheVerzeichnis(file))
				{
					sb = loeschStringBuilderInhalt(sb);
					
					sb.append("IOException aus MedysFileIO.legeDateiAn(String, boolean)\nKonnte das Verzeichnis unter ")
					.append(absolutePfadangabe).append(" nicht löschen!").append("\n");
					
					throw new IOException(sb.toString());
				}
				else
				{
					file.mkdirs();
				}
			}
		}
		else
		{
			if(alsVerzeichnis)
			{
				file.mkdirs();
			}
			else {
				MedysFileIO.legeDateiAn(file.getAbsolutePath());
			}
			
		}
		return file;
	}
	
	/**
	 * Legt eine neue Datei an
	 * 
	 * @param dateiPfad der Speicherort/das Verzeichnis der Datei
	 * @param dateiName der Name der Datei
	 * @return <i>java.io.File</i> wenn die Datei angelegt wurde, 
	 * 		   sonst <i>NULL</i>
	 * @throws IOException wenn unter der gegebenen Verzeichnispfadangabe die Datei nicht angelegt werden konnte
	 */
	public static File legeDateiAn(String dateiPfad, String dateiName) throws IOException
	{
		File file = null;
		
		if(existiert(dateiPfad) && (dateiName != null))
		{
			String dateiMitPfad = gibValidenOrdnerPfad(dateiPfad) + dateiName;
			
			file = new File(dateiMitPfad);
			
			FileWriter fw = null;
			
			if(istDateiKeinVerzeichnis(file))
			{
				file.delete();
			}
			
			try
			{
				fw = new FileWriter(file);
				
				fw.close();
			}
			catch(IOException creationFailed)
			{
				sb = loeschStringBuilderInhalt(sb);
				
				sb.append("Exception aus MedysFileIO.legeDateiAn\nDie Datei ").append(dateiName).append(" konnte im Verzeichnis ")
				.append(dateiPfad).append(" nicht angelegt werden!\n").append(creationFailed.toString()).append("\n");
				
				throw new IOException(sb.toString());
			}
		}
		return file;
	}

	/**
	 * Liefert ein File-Objekt, das unter einer ansoluten Verzeichnispfadangabe
	 * zu finden sein muss
	 * 
	 * @param absolutePfadangabeZurDatei die absoulte Verzeichnispfadangabe zu einer Datei
	 * @return ein {@link java.io.File}-Objekt, das diese Datei repr&auml;sentiert
	 */
	public static File gibDatei(String absolutePfadangabeZurDatei)
	{
		File file = null;
		
		if(existiert(absolutePfadangabeZurDatei))
		{
			file = new File(absolutePfadangabeZurDatei);
		}
		return file;
	}
	
	/**
	 * Liefert die Datei aus einem angegebenen Ordner
	 * <br><br>
	 * Eine  Datei kann ein Verzeichnis oder Daten-Datei sein
	 * 
	 * @param pathToFile der absolute Pfad zum Ordner
	 * @param fileName der Name der Datei
	 * @return die Datei aus dem Ordner falls sie existiert, sonst NULL
	 * @throws NullPointerException wenn der absolute Pfad nicht gegeben ist 
	 */
	public static File gibDatei(String pathToFile, String fileName) 
	{
		String folder = gibValidenOrdnerPfad(pathToFile);
		
		File file = null;
		
		if(existiert(folder))
		{
			file = new File(folder + fileName);
		}
		return file;
	}
	
	/**
	 * Liefert die Bytes des Inhalts einer Datei
	 * 
	 * @param pathToFile der Ordner der Datei (Verzeichnispfad ohne Dateiname)
	 * @param fileName der Name der Datei
	 * @return der Inhalt einer Datei in einem ByteArray
	 * @throws Exception wenn die Datei nicht ausgelesen werden konnte
	 */
	public static byte[] gibDateiInhalt(String pathToFile, String fileName) throws Exception
	{
		byte[] documentInBytes = new byte[0];
		
		try
		{
			File file = gibDatei(pathToFile, fileName);
	
			if(istDateiKeinVerzeichnis(file))
			{
				documentInBytes = gibDateiInhalt(file);
			}
		}
		catch(IOException ioExcep)
		{
			throw new IOException("IOException aus MedysFileIO.gibDateiInhalt()\n" + ioExcep.getMessage() + "\n");
		}
		return documentInBytes;
	}
	
	/**
     * Liest den Inhalt einer Datei aus 
     * und liefert diesen in einem ByteArray zur&uuml;ck.

     * 
     * @param datei die Datei, die ausgelesen werden soll
     * @return der Inhalt der Datei in Bytes
     * @throws Exception Fehler beim Lesen oder Schreiben auf der Datei
     */
    public static String gibDateiInhaltToString(File datei) throws Exception
    {
    		String content = null;
    	
		try
		{
			FileInputStream fis = new FileInputStream(datei);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			moveStreamData(fis, baos);
			
			content = baos.toString();
		}
		catch (Exception e)
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("Exception aus MedysFileIO.gibDateiInhalt()\n").append("Kein Dokumentinhalt vorhanden!\n")
			.append(e.toString()).append("\n");
			
			throw new Exception(sb.toString());
		}
		return content;
	}
    
	/**
     * Liest den Inhalt einer Datei aus 
     * und liefert diesen in einem ByteArray zur&uuml;ck.

     * 
     * @param datei die Datei, die ausgelesen werden soll
     * @return der Inhalt der Datei in Bytes
     * @throws Exception Fehler beim Lesen oder Schreiben auf der Datei
     */
	public static byte[] gibDateiInhalt(File datei) throws Exception
	{
		byte[] content = new byte[0];
		
		try
		{
			FileInputStream fis = new FileInputStream(datei);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			moveStreamData(fis, baos);
			
			content = baos.toByteArray();
		}
		catch (Exception e)
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("Exception aus MedysFileIO.gibDateiInhalt()\n").append("Kein Dokumentinhalt vorhanden!\n")
			.append(e.toString()).append("\n");
			
			throw new Exception(sb.toString());
		}
		return content;
	}
    
	/**
     * Liest den Inhalt einer Datei aus dem Dateistrom (fis) aus 
     * und liefert diesen in einem ByteArray zur&uuml;ck.
     * 
     * @param fis FileInputStream, das zu einer Datei verbunden ist
     * @return der Inhalt der Datei in Bytes
     * @throws Exception Fehler beim Lesen oder Schreiben der Datei
     */
	public static byte[] gibDateiInhalt(FileInputStream fis) throws Exception
	{
		byte[] content = new byte[0];
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			int copyBufferLen = 2000;
			
			byte[] copyBuffer = new byte[copyBufferLen];
			
			while (true)
			{
				int bytesRead = fis.read(copyBuffer);
				
				if (0 > bytesRead)
				{
					break;
				}
				baos.write(copyBuffer, 0, bytesRead);
			}
			content = baos.toByteArray();
			
			fis.close();
			baos.close();
		}
		catch (Exception e)
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("Exception aus MedysFileIO.gibDateiInhalt(FileInutStream):").append("\n")
			.append("Kein Dokumentinhalt vorhanden!\n").append(e.toString()).append("\n");
			
			throw new Exception(sb.toString());
		}
		return content;
	}

    /**
     * Liefert die intern genutzte Instanz einer MedysLogger-Klasse
     * 
     * @return eine Instanz von {@link MedysLogger} 
     */
    public static MedysLogger gibMedysLogger()
    {
    		return medysLogger;
    }
    
    /**
	 * Liefert einen g&uuml;ltigen Verzeichnispfad einer Datei aus dem Dateisystem mit der Endung &quot;/&quot;
	 * 
	 * @param folderPath der urspr&uuml;gliche Verzeichnispfad zu einer Datei
	 * @return die system-valide Verzeichnispfad angabe, falls das Verzeichnis 
	 * 		   existiert, sonst NULL
	 * @see java.net.URI
	 */
	public static String gibValidenOrdnerPfad(String folderPath)
	{
		String rtValue = "";
		
		if(folderPath != null) 
		{
			File folder = new File(folderPath);
		
			if (folder.isDirectory()) 
			{
				if(folderPath.endsWith("/"))
				{
					rtValue = folderPath;
				}
				else
				{
					rtValue = folderPath + "/";
				}
			}
		}
		return rtValue;
	}
	
	/**
	 * Liefert dem URI-Schema &quot;prefixUriSchema&quot; vorangestellten
	 * Verzeichnispfad einer Datei aus dem Dateisystem mit der Endung &quot;/&quot;
	 * 
	 * @param folderPath der urspr&uuml;gliche Verzeichnispfad zu einer Datei
	 * @param prefixUriSchema mit einer Pr&auml;fixangabe gemäß {@link java.net.URI} f&uuml;r <b>Dateipfadangaben</b>
	 * @return die system-valide Verzeichnispfad angabe, falls das Verzeichnis 
	 * 		   existiert, sonst NULL
	 * @see java.net.URI
	 */
	public static String gibValidenOrdnerPfad(String folderPath, String prefixUriSchema)
	{
		String rtValue = "";
		
		if(folderPath != null) 
		{
			File folder = new File(folderPath);
		
			if (folder.isDirectory()) 
			{
				if(folderPath.endsWith("/"))
				{
					rtValue = folderPath;
				}
				else
				{
					rtValue = folderPath + "/";
				}
			}
		}
		
		if(prefixUriSchema != null)
		{
			if(prefixUriSchema.toLowerCase().equals("file:///"))
			{
				rtValue = erhalteFileSchemaKonformeURIVon(rtValue);
			}
		}
		return rtValue;
	}
	
	/**
	 * Legt ein neues Verzeichnis in einem Verzeichnis an.<br><br>
	 * 
	 * <u>Information</u>
	 * 
	 * @param elternVerzeichnis
	 * 			das &uuml;bergeordnete Verzeichnis, in der das neue Verzeichnis angelegt werden
	 */
	
	/**
	 * L&ouml;scht eine Verzeichnis-Objekt (ein Verzeichnis/Ordner im Dateisystem) mit seinem gesamten Inhalt
	 *  
	 * @param verzeichnis das Verzeichnis oder der Ordner, das gel&ouml;scht werden soll 
	 * @return <code>true</code> wenn das angegebene Verzeichnis gel&ouml;scht werden konnte, <code>false</code> wenn nicht
	 * @throws IOException wenn das Verzeichnis oder eine Datei aus diesem Verzeichnis nicht gel&ouml;scht werden konnte 
	 */
	public static boolean loescheVerzeichnis(File verzeichnis) throws IOException
	{
		if(verzeichnis != null)
		{
			if(verzeichnis.exists())
			{
		        File[] dateien = verzeichnis.listFiles();
		        
		        if(dateien != null)
		        {
		            for(File datei : dateien)
		            {
		                if(datei.isDirectory())
		                {
		                		loescheVerzeichnis(datei);
		                }
		                else {
		                    datei.delete();
		                }
		            }
		        }
		    }
		}
	    
	    return verzeichnis.delete();
	}
	
	/**
	 * Entfernt eine Datei in einem Ordner.<br><br>
	 * 
	 * Existiert die Datei nicht,so wird keine Operation bzw.
	 * Meldung durchgef&uuml;hrt.<br>
	 * 
	 * <blockquote>
	 * Diese Funktion leistet das gleiche wie {@link #entferneDatei(String)} oder {@link #entferneDatei(String, String)} 
	 * mit dem <i>feinen Unterschied</i>, da&szlig; hier intern mittels {@link java.nio.file.Paths} und {@link java.nio.file.Path} 
	 * gearbeitet wird!
	 * </blockquote>
	 * 
	 * <u><b>Wichtig</b></u>
	 * <blockquote>
	 * 	Hier wird eine einzelne Datei aus einem Ordner gel&ouml;scht (und nicht der Ordner selber) <br>
	 * 	Bitte nutzen Sie {@link #loescheVerzeichnis(File)} um ein <u>Verzeichnis</u> zu l&ouml;schen!!
	 * </blockquote>
	 * 
	 * @param ordnerPfadangabe Verzeichnispfadangabe zum Ordner der Datei
	 * @param dateiname der Name der Datei, ide entfernt werden soll
	 * @return TRUE, wenn die Datei erfolgreich gelöscht wurde, sonst FALSE
	 * @throws IOException wenn der Verzeichnispfad nicht existiert oder der Zugriff nicht gew&auml;hrt ist
	 * @throws URISyntaxException wenn der Verzeichnispfad keine g&uuml;tige Verzeichnispfad-URI-Angabe ist
	 */
	public static boolean loescheDatei(String ordnerPfadangabe, String dateiname) 
			throws IOException,URISyntaxException
	{
		Path path = 
				Paths.get(
						new URI(
								gibValidenOrdnerPfad(ordnerPfadangabe, "file:///") 
								+ dateiname));
		
		return Files.deleteIfExists(path);
	}
	
	/**
     * Moves stream data from input stream to output stream. Will copy the complete stream.
     * 
     * @param is input stream
     * @param os output stream
     * @throws IOException Fehler beim Lesen oder Schreiben
     */
    public static void moveStreamData(InputStream is, OutputStream os) 
    		throws IOException
    {
        int copyBufferLen = 2000;
        byte[] copyBuffer = new byte[copyBufferLen];

        while (true)
        {
            int bytesRead = is.read(copyBuffer);
            if (0 > bytesRead)
            {
                break;
            }
            os.write(copyBuffer, 0, bytesRead);
        }
    }
	
    /**
     * Kopiert eine Datei in ein Zielverzeichnis.
     * <br><br>
     * Der Kopiervorgang wird abgebrochen wenn die Datei bereits existiert.
     * <br><br>
     * Daher sollte man vorher mit {@link #existiert(String, String)} die Datei pr&uuml;fen.
     * 
     * @param zielVerzeichnis das Verzeichnis in das eine Datei kopiert werden soll
     * @param datei die Datei (java.io.File-Objekt), die kopiert werden soll
     * @throws Exception wenn ein Fehler beim Kopieren auftritt
     */
	public static void kopiereDatei(String zielVerzeichnis, File datei) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		
		if (existiert(zielVerzeichnis) && existiert(datei))
		{
			String dateiName = datei.getName();
			
			if (!existiert(zielVerzeichnis, dateiName))
			{
				try
				{
					File tmp = legeDateiAn(zielVerzeichnis, dateiName);
					
					byte[] inhalt = gibDateiInhalt(datei.getParent(), dateiName);
					
					if (inhalt.length > 0)
					{
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp));
						
						int i = 0;
						
						while (i < inhalt.length)
						{
							bos.write(inhalt[i]);
							i++;
						}
						bos.flush();
						bos.close();
					}
				}
				catch (Exception ioexec)
				{
					sb = loeschStringBuilderInhalt(sb);
					
					sb.append("IOException aus MedysFileIO#kopiereDatei(String,String)").append("\n\nKonnte die Datei")
							.append(datei.getName()).append("nicht in das" + "Verzeichnis ").append(zielVerzeichnis)
							.append(" kopieren.\n\n").append(ioexec.getMessage());
					
					throw new Exception(sb.toString());
				}
				
			}
			else
			{
				// lösche und versuche kopie erneut
				//
				if (entferneDatei(zielVerzeichnis, datei.getName()))
				{
					kopiereDatei(zielVerzeichnis, datei);
				}
			}
		}
		else
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("Exeption aus MedysFileIO#kopiereDatei(String,String)")
			.append("\n").append("Entweder das Zielverzeichnis oder die Datei, die kopiert werden soll existiert nicht!")
			.append("\n");
			
			throw new Exception(sb.toString());
		}
	}
    
    /**
     * Kopiert eine Datei aus seinem Dateiordner in ein Zielverzeichnis.
     * <br><br>
     * Der Kopiervorgang wird abgebrochen wenn die Datei im Zielverzeichnis 
     * bereits existiert.
     * <br><br>
     * Daher sollte man vorher mit {@link #existiert(String, String)} die Datei pr&uuml;fen
     * 
     * @param zielVerzeichnis das Verzeichnis in das eine Datei kopiert werden soll
     * @param dateiOrdnerpfad die Pfadangabe des Ordners, in der die Datei sich befindet
     * @param dateiName der Name der Datei, die kopiert werden soll
     * @throws Exception wenn ein Fehler beim Kopieren auftritt
     */
	public static void kopiereDatei(String zielVerzeichnis, String dateiOrdnerpfad, String dateiName) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		
		if (existiert(zielVerzeichnis) && (dateiName != null))
		{
			if (existiert(dateiOrdnerpfad, dateiName))
			{
				if (istVerzeichnisUndExistiert(zielVerzeichnis))
				{
					if (!existiert(zielVerzeichnis, dateiName))
					{
						try
						{
							File tmp = legeDateiAn(zielVerzeichnis, dateiName);
							
							byte[] inhalt = gibDateiInhalt(dateiOrdnerpfad, dateiName);
							
							if (inhalt.length > 0)
							{
								
								BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp));
								
								int i = 0;
								
								while (i < inhalt.length)
								{
									bos.write(inhalt[i]);
									i++;
								}
								bos.flush();
								bos.close();
							}
						}
						catch (IOException ioexec)
						{
							sb = loeschStringBuilderInhalt(sb);
							
							sb.append("IOException aus MedysFileIO#kopiereDatei(String,String)")
									.append("\n\nKonnte die Datei").append(dateiName)
									.append("nicht in das Verzeichnis ").append(zielVerzeichnis)
									.append(" kopieren.\n\n").append(ioexec.getMessage());
							
							throw new Exception(sb.toString());
						}
						
					}
					else
					{
						// lösche und versuche kopie erneut
						//
						if (entferneDatei(zielVerzeichnis, dateiName))
						{
							kopiereDatei(zielVerzeichnis, dateiOrdnerpfad, dateiName);
						}
					}
				}
			}
		}
		else
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("Exeption aus MedysFileIO#kopiereDatei(String,String)")
			.append("\n").append("Entweder das Zielverzeichnis oder die Datei, die kopiert werden soll existiert nicht!")
			.append("\n");
			
			throw new Exception(sb.toString());
		}
	}
    
	/**
	 * Legt das aktuelle Arbeitsverzeichnis fest, falls es existiert
	 * 
	 * @param arbeitsverzeichnis die absolute Pfadangabe zur einem Arbeitsverzeichnis
	 */
	public void setzeArbeitsverzeichnisPfad(String arbeitsverzeichnis)
	{
		if(existiert(arbeitsverzeichnis))
		{
			this.arbeitsverzeichnis = arbeitsverzeichnis;
		}
	}
	
	/**
	 * Liefert das durch {@link #setzeArbeitsverzeichnisPfad(String)} festgelegte 
	 * Arbeitverzeichnis zur&uuml;ck.
	 * 
	 * @return wenn vorhanden, die Verzeichnispfadangabe zum festgelegten Arbeitsverzeichnis, 
	 * 			sonst NULL 
	 */
	public String gibArbeitsverzeichnis()
	{
		return arbeitsverzeichnis;
	}
	
	/**
	 * @return die absoulte Verzeichnispfadangabe zum aktuellen Benutzerverzeichnis 
	 * des Betriebssystems zur&uuml;ck
	 */
	public static final String gibBenutzerDesktopPfad()
	{
		return System.getProperty("user.home") + File.separator + "Desktop";
	}
	
	/**
	 * Liefert eine {@link java.net.URI} konforme Darstellung f&uuml;r die Anwendung 
	 * einer Verzeichnispfadangabe, zum Beispiel in {@link java.nio.file.Paths#get(URI)}-Methode.<br><br>
	 * 
	 * Die Konformit&auml;t wird dadurch erreicht, da&szlig; in einer gegebenen Pfadangabe 
	 * das &quot;Pr&auml;fix&quot; <i>URI-Schema</i> vorhanden sein mu&szlig;.<br><br>
	 * 
	 * <b><u>Pr&auml;fix (oder auch URI-Schema) k&ouml;nnen sein:</u></b>
	 * <blockquote>
	 * 	<code>file:/// - Schema f&uuml;r Verzeichnisse und Dateien</code><br>
	 *  <code>http://  - URL-Ressourcen f&uuml;r hierarchische URI-Angaben</code><br><br>
	 *  f&uuml;r n&auml;here Informationen {@link java.net.URI}
	 * </blockquote>
	 *
	 * Hier wird prim&auml;r das URI-Schema &quot;file:///&quot; in einer gegebene Verzeichnipfadangabe 
	 * abgefragt. <br><br>
	 * 
	 * Wenn die Verzeichnispfadangabe dieses Schema nicht besitzt, so wird die Verzeichnispfadangabe mit 
	 * diesem Pr&auml;fix URI-Konform konkateniert, d.h. zum Beispiel: <br><br>
	 * wenn eine Verzeichnispfadangabe mit &quot;/&quot; beginnt, so wird diese durch &quot;file:///&quot; ersetzt!
	 * 
	 * @param pfadangabe die Verzeichnispfadangabe, die mit dem URI-Schema konkateniert werden soll
	 * @return eine URI-konforme FileSchema-Pfadangabe, sonst NULL, wenn die Pfadangabe NULL ist
	 * @see java.net.URI
	 * @see java.nio.file.Paths
	 */
	public static String erhalteFileSchemaKonformeURIVon(String pfadangabe)
	{
		String schema = "file:///";
		
		if(pfadangabe != null)
		{
			if(!pfadangabe.startsWith(schema))
			{
				if (pfadangabe.startsWith("/"))
				{	
					pfadangabe = pfadangabe.substring(1, pfadangabe.length());
				}
				
				schema = schema + pfadangabe;
			}
		}
		else
		{
			schema = null;
		}
		return schema;
	}
	/**
	 * Liefert eine Verzeichnispfadangabe zu einer Datei,
	 * falls die &uuml;bergebenen Zeichenkette eine absolute Verzeichnispfadangabe
	 * zu einer Datei ist.
	 * <br><br>
	 * Eine absolute Verzeichnispfadangabe ist dann gegeben, wenn der Ordner in
	 * dem &uuml;bergebenen Parameter exisitiert und der Inhalt sich wie folgt 
	 * aufgebauen w&uuml;rde:
	 * <br><br>
	 * absolutePathToFile = c:/users/test/desktop/testdoc.pdf
	 * <br><br>
	 * <b>Ergebnis</b><br><br>
	 * Return-Wert = c:/users/test/desktop
	 * 
	 * @param absolutePfadangabeZurDatei die absolute Verzeichnispfadangabe zu einer Datei
	 * @return die absolute Verzeichnispfadangabe zum Ordner einer Datei,
	 * 		wenn der Ordner existiert, sonst NULL
	 */
	public String gibVerzeichnispfadZuOrdnerVon(String absolutePfadangabeZurDatei)
	{
		return existiert(absolutePfadangabeZurDatei) 
				?
				gibDatei(absolutePfadangabeZurDatei).getParent()
				: null;
	}
	
	
	// Setter und Getter zu den Instanzvariablen
	//
	
	/**
	 * Liefert die Verzeichnispfadangabe zu durch {}@link {@link #setzeVerzeichnispfadZurDatei(String)} 
	 * festgelegten Datei zur&uuml;ck.
	 * <br><br>
	 * Diese Angabe wird erhalten, wenn man entweder durch die Konstruktoren
	 * {@link #MedysFileIO(String, String)}, {@link #MedysFileIO(String,String,String)} 
	 * vorher genutzt hat oder eine MedysFileIO-Instanz erstellt und 
	 * {@link #setzeVerzeichnispfadZurDatei(String)} danach aufgerufen hat.
	 * 
	 * @return die Verzeichnispfadangabe zu einer festgelegten Datei
	 */
	public String gibVerzeichnispfadZurDatei() {
		return pathToFile;
	}

	/**
	 * Legt den Verzeichnispfad zu einer Datei fest
	 * 
	 * @param pathToFile Verzeichnispfadangabe zu einer Datei
	 */
	public void setzeVerzeichnispfadZurDatei(String pathToFile) 
	{	
		if(existiert(pathToFile))
		{
			File file = gibDatei(pathToFile);
			
			if(istVerzeichnisUndExistiert(file))
			{
				setzeOrdnerNameDerDatei(pathToFile);
				
				this.pathToFile = pathToFile;
			}
		}
	}
	
	/**
	 * Schreibt einen Inhalt in eine <b>existierende</b> Datei mit einem vorgegebenen Zeichensatz.<br><br>
	 * 
	 * Diese Klasse nutzt {@link FileWriterWithEncoding} um einen FileWriter mit Encoding (Zeichensatz-Angabe) einzusetzen!<br><br>
	 * 
	 * n&auml;here Infos zu den Java-supported Zeichens&auml;tzen unter
	 * <blockquote>
	 * 	<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html">supported Endcoings ab Java 7</a>
	 * </blockquote>
	 * 
	 * Dabei gelten die &quot;Charset-Namen&quot; aus der Spalte <i>Canonical Name for <code>java.nio</code> API</i><br><br>
	 * 
	 * <u>Schreib/-Inhaltsregeln</u>
	 * <blockquote>
	 * 		<ul>
	 * 			<li>	Der Inhalt kann bin&auml;r oder aus Zeichen bestehen.</li>
	 * 			<li>Wenn der Inhalt bin&auml;r ist oder ein byte-Array ist, so sollte der 
	 * 				Inhalt vorher in ein String-Objekt &uuml;berf&uuml;hrt werden</li>
	 * 			<li>	Ist die Datei nicht existent, so mu&szlig; sie vorher durch 
	 * 				{@link #legeDateiAn(String, String)} angelegt werden</li>
	 * 			<li>ist die Option &quot;alsAnhang&quot; <code>true</code>, so wird an das 
	 * 				Dateiende geschrieben. Wenn die Datei leer ist, an den Anfang</li>
	 *		</ul>
	 * </blockquote>
	 * 
	 * @param pfadZurDatei die absolute Pfadangabe zu einer Datei (inl. Dateiname und Endung)
	 * @param inhalt der Inhalt, der geschrieben werden soll
	 * @param zeichensatz der Zeichensatz, der hier anngewendet werden soll
	 * @param alsAnhang ob der Inhalt ans Dateiende angeh&auml;ngt werden soll oder nicht
	 * @throws IOException wenn die existierende Datei nicht beschrieben werden kann
	 */
	public static void schreibInDatei(String pfadZurDatei, String inhalt, String zeichensatz, boolean alsAnhang) throws IOException
	{
		StringBuilder sb  = new StringBuilder();
		
		if(inhalt != null)
		{
			if(existiert(pfadZurDatei))
			{
				File file = gibDatei(pfadZurDatei);

				FileWriterWithEncoding fw = new FileWriterWithEncoding(file, zeichensatz, alsAnhang); // häng Inhalt ans Dateiende an
					
				fw.write(inhalt);
					
				fw.flush();
					
				fw.close();
			}
			else
			{
				sb.append(" aus MedysFileIO.schreibInDatei\nUnter der Pfadangabe ").append(pfadZurDatei).append(" existiert keine Datei")
				.append(" die beschrieben werden kann");
				
				if(istLoggingGestartet())
				{
					medysLogger.logConfigMessage(Level.INFO, sb.toString());
				}
				MedysLogger.printMessgeOnConsole("INFO\n" + sb.toString());
				
			}
		}
		else
		{
			sb = loeschStringBuilderInhalt(sb);
			
			sb.append("IOException aus MedysFileIO.schreibInDatei\nDer Inhalt ist NULL.\nBitte geben Sie einen Inhalt an!");
				
			throw new IOException(sb.toString());
		}
	}
	
	/**
	 * Schreibt einen Inhalt in eine <b>existierende</b> Datei.<br><br>
	 * 
	 * Der Inhalt der datei wird in dem aktuellen Zeichnsatz desjeweiligen Betriebssystems beschrieben.<br><br>
	 * 
	 * Benutzen Sie {@link #schreibInDatei(String, String, String, boolean)}, 
	 * wenn der Inhalt mit einem anderen Zeichnsatz beschreiben werden soll.<br><br>
	 * 
	 * <u>Schreib/-Inhaltsregeln</u>
	 * <blockquote>
	 * 		<ul>
	 * 			<li>	Der Inhalt kann bin&auml;r oder aus Zeichen bestehen.</li>
	 * 			<li>Wenn der Inhalt bin&auml;r ist oder ein byte-Array ist, so sollte der 
	 * 				Inhalt vorher in ein String-Objekt &uuml;berf&uuml;hrt werden</li>
	 * 			<li>	Ist die Datei nicht existent, so mu&szlig; sie vorher durch 
	 * 				{@link #legeDateiAn(String, String)} angelegt werden</li>
	 * 			<li>ist die Option &quot;alsAnhang&quot; <code>true</code>, so wird an das 
	 * 				Dateiende geschrieben. Wenn die Datei leer ist, an den Anfang</li>
	 *		</ul>
	 * </blockquote>
	 * 
	 * @param pfadZurDatei die absolute Pfadangabe zu einer Datei (inl. Dateiname und Endung)
	 * @param inhalt der Inhalt, der geschrieben werden soll
	 * @param alsAnhang ob der Inhalt ans Dateiende angeh&auml;ngt werden soll oder nicht
	 * @throws IOException wenn die existierende Datei nicht beschrieben werden kann
	 */
	public static void schreibInDatei(String pfadZurDatei, String inhalt, boolean alsAnhang) throws IOException
	{
		StringBuilder sb  = new StringBuilder();
		
		if(inhalt != null)
		{
			if(existiert(pfadZurDatei))
			{
				File file = gibDatei(pfadZurDatei);

				FileWriter fw = new FileWriter(file, alsAnhang); // häng Inhalt ans Dateiende an
					
				fw.write(inhalt);
					
				fw.flush();
					
				fw.close();
			}
			else
			{
				sb = loeschStringBuilderInhalt(sb);
				sb.append("IOException aus MedysFileIO.schreibInDatei\nUnter der Pfadangabe ").append(pfadZurDatei).append(" existiert keine Datei")
				.append(" die beschrieben werden kann");
				
				if(istLoggingGestartet())
				{
					medysLogger.logConfigMessage(Level.INFO, sb.toString());
				}
				MedysLogger.printMessgeOnConsole("INFO\n" + sb.toString());
				
			}
		}
		else
		{
			sb.append(" aus MedysFileIO.schreibInDatei\nDer Inhalt ist NULL.\nBitte geben Sie einen Inhalt an!");
				
			if (istLoggingGestartet())
			{
				medysLogger.logConfigMessage(Level.WARNING, sb.toString());
			}
			MedysLogger.printMessgeOnConsole("Warnung " + sb.toString());
		}
	}
	
	/**
	 * Liefert den Ordnernamen eines Ordners zur&uuml;ck, wenn dieser vorher durch 
	 * {@link #setzeOrdnerNameDerDatei(String)} fetgelegt wurde. 
	 * 
	 * @return wenn vorhanden, der festgelegte Ordnername eines Ordners, sonst NULL
	 */
	public String gibOrdnerNameDerDatei()
	{
		return folderName;
	}

	/**
	 * Legt den Namen des &uuml;bergeordneten Ordners einer Datei fest
	 * 
	 * @param ordnerNameDerDatei der Name des &uuml;bergeordneten Ordners einer Datei
	 */
	public void setzeOrdnerNameDerDatei(String ordnerNameDerDatei)
	{
		folderName = ordnerNameDerDatei;
	}
	
	/**
	 * Liefert den durch {@link #setzeDateiName(String)} fetgelegten Dateinamen zur&uuml;ck
	 * 
	 * @return wenn vorher festgelegt, der Name einer Datei, sonst NULL
	 */
	public String gibDateiName() {
		return fileName;
	}

	/**
	 * Legt den Namen einer Datei fest
	 * 
	 * @param fileName der Name der Datei
	 */
	public void setzeDateiName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setzeDateiNameAusPfad(String absolutePfadangabe)
	{
		File datei = gibDatei(absolutePfadangabe);
		
		if(istDateiKeinVerzeichnis(datei))
		{
			fileName = datei.getName();
		}
	}

	/**
	 * Liefert das durch {@link #setzeDateiFormat(String)} Dateiformat zur&uuml;ck
	 * 
	 * @return wenn vorher festgelegt, das Dateiformat einer Datei, sonst NULL
	 */
	public String gibDateiFormat() {
		return fileFormat;
	}

	/**
	 * Legt das Dateiformat einer Datei fest.<br><br>
	 * 
	 * Ein Dateiformat ist das Format unter dem eine Datei abgespeichert wird!
	 * 
	 * @param fileFormat das Dateiformat einer Datei
	 */
	public void setzeDateiFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	
	/**
	 * Liefert die absolute Verzeichnispfadangabe/Speicherort im Dateisystem zu 
	 * einem gegebenen Objekt, das eine Java-Klasse sein muss.
	 * 
	 * @param classObj die <b>Instanz</b> einer Java-Klasse, von der der Speicherort ermittelt werden soll
	 * @return absolute Pfadangabe zu dieser Java-Klasse, sonst NULL wenn classObj
	 * 			kein Class-Object ist
	 */
	public static String getExecutionPath(Object classObj)
	{
		String absolutePath = null;
		
		if(classObj != null)
		{
			// DEBUG ONLY
//			Class<? extends Object> classX = classObj.getClass();
//			ProtectionDomain protDom = classX.getProtectionDomain();
//			CodeSource codeSrc = protDom.getCodeSource();
//			URL location = codeSrc.getLocation();
//			absolutePath = location.getPath();
			
			ProtectionDomain protDom = classObj.getClass().getProtectionDomain();
			
			if(protDom != null)
			{
				CodeSource codeSrc = protDom.getCodeSource();
				
				if(codeSrc != null)
				{
					URL location = codeSrc.getLocation();
					
					if(location != null)
					{
						absolutePath = location.getPath();
						
						if(existiert(absolutePath))
						{
							absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
							
							if(absolutePath != null)
							{
								absolutePath = absolutePath.replaceAll("%20"," ");
								
								if(absolutePath.startsWith(File.pathSeparator))
								{
									absolutePath  = absolutePath.substring(1, absolutePath.length());
								}
							}
						}
					}
				}
			}
			
//			if(absolutePath != null)
//			{
//				if(absolutePath.startsWith(File.pathSeparator))
//				{
//					absolutePath  = absolutePath.substring(1, absolutePath.length());
//				}
//			}
		}
		
	    return absolutePath;
	}
	
	/*
	 * L&ouml;scht den bisherigen aufgenommenen Inhalt in einer StringBuilder-Instanz.<br><br>
	 * 
	 * @param stringBuilder die Instanz eines StringBuilders
	 * @return einen leeren StringBuilder, wenn der Eingangsparameter NICHT NULL ist, sonst NULL
	 */
	private static StringBuilder loeschStringBuilderInhalt(StringBuilder stringBuilder)
	{
		StringBuilder sb = null;
		
		if(stringBuilder != null)
		{
			if(stringBuilder.capacity() > 0)
			{
				stringBuilder.delete(0, stringBuilder.capacity() - 1);
				
				sb = stringBuilder;
			}
		}
		return sb;
	}
}
