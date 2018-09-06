package de.medys;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Java-Logger implementierung f&uuml;r Medys-Anwendungen
 *
 * @author Hayri Emrah Kayaman, MEDYS GmbH W&uuml;lrath 2015
 */
public class MedysLogger {
	
	private Logger logger;
	
	private FileHandler fileHandler;
	
	private Formatter formatter;
	
	private String actualDateTime;
	
	private Object objToLog;
	
	private static String logFilePath;
	
	private boolean loggingGestartet;
	
	/**
	 * Erstellt eine neue Instanz von MedysLogger und 
	 * bereitet den lokalen Zeitstempel f&uuml;r den Namensanteil der 
	 * Log-Datei vor. <br><br>
	 * 
	 * Nach dem Konstruktoraufruf oder bei Vererbung nach dem Aufruf von &quot;super()&quot; 
	 * sollte durch die Methode {@link #initLogger(Object)} aufgerufen werden, um die jeweilige 
	 * Java-Klasse (Instanz der Klasse) zu loggen.<br><br>
	 * 
	 * @see java.time.LocalDateTime#now()
	 */
	public MedysLogger()
	{
		loggingGestartet = false;
		actualDateTime = LocalDateTime.now().toLocalTime().toString();
	}
	
	/**
	 * Liefert die absolute Verzeichnispfadangabe zur Log-Datei eines Objekts
	 * 
	 * @param classObj das Java-Objekt (Instanz einer Klasse), da&szlig; geloggt wurde
	 * @return absolute Verzeichnispfadangabe zu der Log-Datei
	 * @throws java.lang.NullPointerException - wenn die Log-Datei nicht existiert
	 */
	public String gibLogFilePfadVon(Object classObj) throws NullPointerException
	{
		String uriAngabe = MedysFileIO.getExecutionPath(classObj);
		
		File file = MedysFileIO.gibDatei(uriAngabe);
		
		if(MedysFileIO.istVerzeichnisUndExistiert(file))
		{
			if(uriAngabe.endsWith("/bin"))
			{
				uriAngabe = uriAngabe.substring(0,uriAngabe.length() - "/bin".length());
			}
			if(!uriAngabe.endsWith("/"))
			{
				uriAngabe = uriAngabe + "/";
			}
		}
		else if(MedysFileIO.istDateiKeinVerzeichnis(file))
		{
			uriAngabe = file.getAbsolutePath();
		}
		
		return uriAngabe;
	}
	
	/**
	 * Initialisiert den Logger, der die Log-Ereignisse eines Objekts auff&auml;ngt und in eine 
	 * entsprechende Log-Datei schreibt. <br><br>
	 * 
	 * @param logObjectClass die Objektklasse, die geloggt werden soll
	 */
	public void initLogger(Object logObjectClass) 
	{	
		try 
		{	
			if((logObjectClass != null) && !istLoggingGestartet())
			{
				String msg = "";
				
				File file = new File(MedysFileIO.getExecutionPath(logObjectClass));
				
				String logDateiName = null;
				
				if(MedysFileIO.istVerzeichnisUndExistiert(file))
				{
					logDateiName = "log-" + logObjectClass.getClass().getSimpleName() + ".txt";
				}
				
				if(logDateiName != null)
				{
					file = new File(file.getAbsolutePath(), logDateiName);
					
					try
					{					
						file = MedysFileIO.legeDateiAn(file.getAbsolutePath());
						
						if(MedysFileIO.existiert(file))
						{
							// weiter gehts
							
							logFilePath = file.getAbsolutePath();
							
							fileHandler = new FileHandler(logFilePath, true);
							
							formatter = new SimpleFormatter();
							
							fileHandler.setFormatter(formatter);
							
							logger = Logger.getLogger(logObjectClass.getClass().getSimpleName());
							
							logger.addHandler(fileHandler);
							
							logger.log(Level.INFO, "starte logging von " + logObjectClass.getClass().getSimpleName());
							
							loggingGestartet = true;
						}
						else
						{
							msg = "Warnung - Logging für " + logObjectClass.getClass().getSimpleName() 
									+ "konnte keine Log-Datei initialisiert werden";
							
							printMessgeOnConsole(msg);
						}
					}
					catch(IOException fileCreatErr)
					{
						msg = "Exception aus MedysLogger.initLogger\n" + fileCreatErr.getMessage();
						
						printMessgeOnConsole(msg);
						
						fileCreatErr.printStackTrace();
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Pr&uuml;ft, ob das Logging durch {@link #initLogger(Object)} vorher 
	 * gestartet wurde.
	 *  
	 * @return <code>true</code> wenn {@link #initLogger(Object)} vorher aufgerufen wurde, 
	 * 			sonst <code>false</code>
	 */
	public boolean istLoggingGestartet()
	{
		return loggingGestartet;
	}
	
	/**
	 * Loggt eine Nachricht in die festgelegte Log-Datei
	 * 
	 * @param level Log-Level
	 * @param message die Nachricht
	 * @see java.util.logging.Level für den Inhalt des &quot;level&quot;-Parameters
	 */
	public void logConfigMessage(String level, String message) 
	{
		if((level != null) && (message != null))
		{
			try
			{
				Level logLevel = Level.parse(level.toUpperCase());
				logger.log(logLevel, message);
			}
			catch(Exception wrongLog)
			{
				logger.log(Level.FINEST, "LOGGING_ERROR:" + wrongLog.getMessage());
			}
		}
	}
	
	/**
	 * Loggt eine Nachricht in die festgelegte Log-Datei
	 * 
	 * @param level Log-Level-Object
	 * @param message die Nachricht
	 * @see java.util.logging.Level f&uuml;r den Inhalt des &quot;level&quot;-Parameters
	 */
	public void logConfigMessage(Level level, String message) 
	{
		if((level != null) && (message != null))
		{
			try
			{
				logger.log(level, message);
			}
			catch(Exception wrongLog)
			{
				logger.log(Level.FINEST, "LOGGING_ERROR:" + wrongLog.getMessage());
			}
		}
	}
	
	/**
	 * Schreibt die Nachricht <i>message</i> auf die System-Konsole
	 * 
	 * @param message die Nachricht, die in einer System-Konsole angezeigt werden soll
	 */
	public static void printMessgeOnConsole(String message)
	{
		System.out.println(message);
	}
	
	/**
	 * Liefert den Inhalt des bisherigen Loggings auf der System-Console
	 */
	public static void printLogDateiToConsole()
	{
		try
		{
			System.out.println(gibLogDateiinhalt());
		}
		catch (Exception e)
		{
			printMessgeOnConsole("IOException in MedysException.printLogDateiToConsole\n"
					+ e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Liefert den Inhalt des bisherigen Loggings zur&uuml;ck
	 * 
	 * @return Inhalt der <i>intern festgelegten Log-Datei</i>, die durch {@link #logConfigMessage(Level, String)} oder   
	 * 			{@link #logConfigMessage(String, String)} zuvor sgesetzt wurde
	 * 
	 * @throws Exception Fehler beim auslesen der <i>intern festgelegten Log-Datei</i>
	 */
	public static String gibLogDateiinhalt() throws Exception
	{
		String inhalt = null;
		
		try
		{		
			inhalt = MedysFileIO.gibDateiInhaltToString(
					MedysFileIO.gibDatei(gibLogFilePfad()));
		}
		catch (Exception e)
		{
			printMessgeOnConsole("IOException in MedysException.gibLogDateiinhalt\n"
					+ e.getMessage());
			e.printStackTrace();
		}
		
		return inhalt;
	}
	
	/**
	 * Liefert den SimpleName-Wert eines Objekts
	 * 
	 * @param obtToLog das Objekt
	 * @return wenn <code>objToLog</code> nicht NULL ist, den SimpleName des Objekts als Log-Filename
	 * 			sonst <code>NULL</code>
	 */
	public String gibLogFilename(Object obtToLog)
	{
		return obtToLog != null ? obtToLog.getClass().getSimpleName() + actualDateTime + ".txt" : null;
	}
	
	/**
	 * Liefert die absoulte Pfadangabe zu der zuletzt initialisierten log-Datei 
	 * eines Objekts, da&szlig; vorher mit dem Aufruf {@link #initLogger(Object)} 
	 * gestartet wurde.
	 * 
	 * @return absolute Verzeichnispfadangabe zur letzten Log-Datei
	 */
	public static String gibLogFilePfad()
	{
		return logFilePath;
	}
	
	/**
	 * Liefert die Referenz auf das Objekt zur&uuml;ck, das durch {@link #initLogger(Object)} 
	 * zum Logging festgelegt wurde
	 * 
	 * @return das Objekt, da&szlig; geloggt wird, falls {@link MedysLogger} das Logging f&uuml;r dieses
	 * 			Objekt initialisiern konnte, sonst NULL
	 */
	public Object gibLogObject()
	{
		return objToLog;
	}
}
