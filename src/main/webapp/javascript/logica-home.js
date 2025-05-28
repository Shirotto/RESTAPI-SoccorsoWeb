$(document).ready(function() {
    // Verifica se l'utente è autenticato
    checkAuthentication();

    // Event handlers
    $('#logoutBtn').on('click', function(e) {
        e.preventDefault();
        logout();
    });

    // Apri modale per inserimento richiesta
    $('#btn-insert-request').on('click', function(e) {
        e.preventDefault();
        openModalRichiesta();
    });

    // Chiudi modale
    $('#closeModal, #cancellaRichiesta').on('click', function(e) {
        e.preventDefault();
        closeModalRichiesta();
    });

    // Chiudi modale cliccando fuori
    $('#modalRichiesta').on('click', function(e) {
        if (e.target === this) {
            closeModalRichiesta();
        }
    });

    // Submit form richiesta
    $('#formRichiesta').on('submit', function(e) {
        e.preventDefault();
        inviaRichiestaSoccorso();
    });
});

// Verifica autenticazione
function checkAuthentication() {
    const token = sessionStorage.getItem('authToken');
    
    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

    // Verifica validità token
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/auth/verify',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        error: function() {
            alert('Sessione scaduta. Effettua nuovamente il login.');
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('userInfo');
            window.location.href = 'index.html';
        }
    });
}

// Funzione per il logout
function logout() {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('userInfo');
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/auth/logout',
        type: 'POST',
        success: function() {
            window.location.href = 'index.html';
        },
        error: function() {
            window.location.href = 'index.html';
        }
    });
}

// Apri modale richiesta
function openModalRichiesta() {
    // Pulisci il form
    $('#formRichiesta')[0].reset();
    
    // Preleva info utente dal sessionStorage
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}');
    
    // Pre-compila alcuni campi se disponibili
    if (userInfo.nome && userInfo.cognome) {
        $('#richiedente').val(userInfo.nome + ' ' + userInfo.cognome);
    }
    if (userInfo.email) {
        $('#email').val(userInfo.email);
    }
    if (userInfo.telefono) {
        $('#telefono').val(userInfo.telefono);
    }
    
    // Mostra il modale
    $('#modalRichiesta').fadeIn(300);
}

// Chiudi modale richiesta
function closeModalRichiesta() {
    $('#modalRichiesta').fadeOut(300);
}

// Invia richiesta di soccorso
function inviaRichiestaSoccorso() {
    const token = sessionStorage.getItem('authToken');
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}');

    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

    // Raccogli i dati dal form
    const richiestaData = {
        usersId: userInfo.id ? userInfo.id.toString() : null,
        richiedente: $('#richiedente').val().trim(),
        descrizione: $('#descrizione').val().trim(),
        indirizzo: $('#indirizzo').val().trim(),
        telefonoContattoRichiesta: $('#telefono').val().trim(),
        emailContattoRichiesta: $('#email').val().trim()
    };

    // Validazioni lato client
    if (!richiestaData.descrizione) {
        alert('La descrizione dell\'emergenza è obbligatoria.');
        $('#descrizione').focus();
        return;
    }

    if (richiestaData.descrizione.length < 10) {
        alert('La descrizione deve essere di almeno 10 caratteri.');
        $('#descrizione').focus();
        return;
    }

    if (!richiestaData.indirizzo) {
        alert('L\'indirizzo dell\'emergenza è obbligatorio.');
        $('#indirizzo').focus();
        return;
    }

    // Validazione telefono se inserito
    if (richiestaData.telefonoContattoRichiesta && 
        !/^[0-9]{10}$/.test(richiestaData.telefonoContattoRichiesta)) {
        alert('Il numero di telefono deve contenere esattamente 10 cifre.');
        $('#telefono').focus();
        return;
    }

    // Validazione email se inserita
    if (richiestaData.emailContattoRichiesta && 
        !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(richiestaData.emailContattoRichiesta)) {
        alert('Inserire un indirizzo email valido.');
        $('#email').focus();
        return;
    }

    // Disabilita il bottone per evitare doppi invii
    $('#confermaRichiesta').prop('disabled', true).text('Invio in corso...');

    // Invio della richiesta
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/richieste',
        type: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        data: JSON.stringify(richiestaData),
        success: function(response) {
            alert('✅ Richiesta di soccorso inviata con successo!\n' +
                  'ID Richiesta: ' + (response.id || 'N/A') + '\n' +
                  'Stato: NUOVA\n\n' +
                  'La tua richiesta è stata registrata e sarà processata al più presto.');
            
            closeModalRichiesta();
            
            // Opzionale: mostra i dettagli della richiesta creata
            console.log('Richiesta creata:', response);
        },
        error: function(xhr, status, error) {
            let errorMessage = 'Errore nell\'invio della richiesta.';
            
            if (xhr.responseJSON && xhr.responseJSON.error) {
                errorMessage = xhr.responseJSON.error;
            } else if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            } else if (xhr.status === 401) {
                errorMessage = 'Sessione scaduta. Effettua nuovamente il login.';
                sessionStorage.removeItem('authToken');
                sessionStorage.removeItem('userInfo');
                window.location.href = 'index.html';
                return;
            } else if (xhr.status === 400) {
                errorMessage = 'Dati non validi. Controlla i campi inseriti.';
            } else if (xhr.status === 500) {
                errorMessage = 'Errore del server. Riprova più tardi.';
            }
            
            alert('❌ ' + errorMessage);
            console.error('Errore invio richiesta:', xhr.responseText);
        },
        complete: function() {
            // Riabilita il bottone
            $('#confermaRichiesta').prop('disabled', false).text('🆘 Invia Richiesta');
        }
    });
}

// Funzione di utilità per formattare le date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('it-IT', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}