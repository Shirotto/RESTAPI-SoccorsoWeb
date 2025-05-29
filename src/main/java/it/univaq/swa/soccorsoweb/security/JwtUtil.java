package it.univaq.swa.soccorsoweb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import it.univaq.swa.soccorsoweb.model.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import javax.crypto.SecretKey;
import java.util.Date;

@ApplicationScoped
public class JwtUtil {
    
    // Chiave segreta per firmare i token 
    private static final String SECRET_KEY = "qlx1<`D1jI6AMnKZ<0r_4(*P.{]'3iLmDl6[q(MW=@kD07p>n}7.1S^A^gePUqs";
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    // Durata del token: 24 ore
    private static final long EXPIRATION_TIME = 86400000; // 24 ore 
    
    /**
     * Genera un token JWT per l'utente (versione originale)
     */
    public String generateToken(String email, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setSubject(email) // Subject del token (email dell'utente)
                .claim("userId", userId) // Claim personalizzato con l'ID utente
                .setIssuedAt(now) // Data di emissione
                .setExpiration(expiryDate) // Data di scadenza
                .signWith(key, SignatureAlgorithm.HS256) // Firma con chiave segreta
                .compact();
    }
    
    /**
     * Genera un token JWT per l'utente con ruolo
     */
    public String generateToken(String email, Long userId, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setSubject(email) // Subject del token (email dell'utente)
                .claim("userId", userId) // Claim personalizzato con l'ID utente
                .claim("role", role.toString()) // Claim per il ruolo utente
                .setIssuedAt(now) // Data di emissione
                .setExpiration(expiryDate) // Data di scadenza
                .signWith(key, SignatureAlgorithm.HS256) // Firma con chiave segreta
                .compact();
    }
    
    /**
     * Estrae l'email dal token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser() 
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * Estrae l'ID utente dal token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()  
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("userId", Long.class);
    }
    
    /**
     * Estrae il ruolo utente dal token
     */
    public UserRole getUserRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()  
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String roleString = claims.get("role", String.class);
            return roleString != null ? UserRole.valueOf(roleString) : UserRole.UTENTE;
        } catch (Exception e) {
            return UserRole.UTENTE; // Default fallback
        }
    }
    
    /**
     * Verifica se il token è valido
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()  
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Verifica se il token è scaduto
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}