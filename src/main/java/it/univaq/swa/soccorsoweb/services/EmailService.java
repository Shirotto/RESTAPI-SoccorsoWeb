package it.univaq.swa.soccorsoweb.services;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servizio per l'invio di email
 * Versione modificata per scrivere su file di log invece di inviare email
 */
public class EmailService {
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());
    
    // Configurazione SMTP - mantenuta solo per retrocompatibilità
    private static final String SMTP_HOST = "smtp.example.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "noreply@soccorsoweb.it";
    private static final String SMTP_PASSWORD = "password_sicura";
    private static final String FROM_EMAIL = "noreply@soccorsoweb.it";
    private static final String FROM_NAME = "Soccorso Web";
    
    // URL base dell'applicazione per i link
    private static final String APP_BASE_URL = "http://localhost:8080/soccorso-web-services";
    
    // Path del file di log per le email
    private static final String EMAIL_LOG_FILENAME = "soccorsoweb-emails.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Invia una email (simulata scrivendo su file di log)
     * 
     * @param to Indirizzo email destinatario
     * @param subject Oggetto dell'email
     * @param htmlContent Contenuto HTML dell'email
     * @param importantInfo Informazioni importanti da loggare (es. link di convalida)
     * @return true se la scrittura è avvenuta con successo, false altrimenti
     */
    public boolean sendEmail(String to, String subject, String importantInfo) {
        try {
            // Scrive i dettagli dell'email in un file di log
            StringBuilder emailDetails = new StringBuilder();
            emailDetails.append("=== EMAIL SIMULATA ===\n");
            emailDetails.append("Data: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
            emailDetails.append("Da: ").append(FROM_NAME).append(" <").append(FROM_EMAIL).append(">\n");
            emailDetails.append("A: ").append(to).append("\n");
            emailDetails.append("Oggetto: ").append(subject).append("\n");
            
            // Aggiungi solo le informazioni importanti invece dell'HTML completo
            if (importantInfo != null && !importantInfo.isEmpty()) {
                emailDetails.append("Contenuto Importante:\n").append(importantInfo).append("\n");
            }
            
            emailDetails.append("====================\n\n");
            
            // Determina il percorso del file di log (nella directory home dell'utente)
            Path logFilePath = Paths.get(System.getProperty("user.home"), EMAIL_LOG_FILENAME);
            
            // Scrivi nel file di log (crea il file se non esiste, altrimenti appendi)
            Files.write(
                logFilePath, 
                emailDetails.toString().getBytes(StandardCharsets.UTF_8), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND
            );
            
            logger.info("Email simulata scritta nel log: " + logFilePath.toString());
            logger.info("Email a: " + to + ", Oggetto: " + subject);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore nella simulazione dell'invio email a: " + to, e);
            return false;
        }
    }
    
    /**
     * Invia l'email di convalida per una richiesta di soccorso
     * 
     * @param to Email del destinatario
     * @param nome Nome del richiedente
     * @param richiestaId ID della richiesta
     * @param validationToken Token di validazione
     * @return true se l'invio è avvenuto con successo, false altrimenti
     */
    public boolean sendValidationEmail(String to, String nome, Long richiestaId, String validationToken) {
        String subject = "Convalida la tua richiesta di soccorso";
        
        // Costruisci l'URL di convalida
        String validationUrl = APP_BASE_URL + "/convalida.html?id=" + richiestaId + "&token=" + validationToken;
        
        // Invece di registrare tutto l'HTML, registra solo le informazioni importanti
        String importantInfo = 
            "Gentile " + nome + ",\n\n" +
            "Per convalidare la tua richiesta di soccorso, utilizza questo link:\n" +
            validationUrl + "\n\n" +
            "ID Richiesta: " + richiestaId + "\n" +
            "Token di Validazione: " + validationToken;
        
        // Usa il metodo sendEmail modificato che scrive solo le informazioni essenziali
        return sendEmail(to, subject, importantInfo);
    }
}