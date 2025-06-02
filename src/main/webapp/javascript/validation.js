/**
 * validation.js
 * Validazione e convalida delle richieste
 */

function convalidaRichiesta() {
    const richiestaId = $('#richiestaId').val().trim();
    const validationToken = $('#validationToken').val().trim();
    
    // Validazione input
    if (!richiestaId || !validationToken) {
        showConvalidaResult('Errore: Tutti i campi sono obbligatori', 'error');
        return;
    }
    
    if (isNaN(richiestaId) || parseInt(richiestaId) <= 0) {
        showConvalidaResult('Errore: ID richiesta deve essere un numero valido', 'error');
        return;
    }
    
    // Disabilita il pulsante durante la richiesta
    $('#confermaConvalida').prop('disabled', true).text('⏳ Convalidando...');
    
    // Chiamata API di convalida
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${richiestaId}/convalida?token=${encodeURIComponent(validationToken)}`,
        method: 'POST',
        contentType: 'application/json',
        success: function(response) {
            let message = 'Richiesta convalidata con successo!';
            
            if (response.message) {
                message = response.message;
            }
            
            if (response.data && response.data.statoRichiesta) {
                message += ` Nuovo stato: ${response.data.statoRichiesta}`;
            }
            
            showConvalidaResult(message, 'success');
            
            // Reset del form dopo successo
            setTimeout(() => {
                $('#formConvalidaRichiesta')[0].reset();
                $('#convalidaRichiestaResult').fadeOut();
            }, 3000);
        },
        error: function(xhr) {
            let errorMessage = 'Errore nella convalida della richiesta';
            
            try {
                const errorResponse = JSON.parse(xhr.responseText);
                if (errorResponse.message) {
                    errorMessage = errorResponse.message;
                } else if (errorResponse.error) {
                    errorMessage = errorResponse.error;
                }
            } catch (e) {
                // Se non è JSON, usa il messaggio di default
                if (xhr.status === 404) {
                    errorMessage = 'Richiesta non trovata';
                } else if (xhr.status === 400) {
                    errorMessage = 'Token di validazione non valido o richiesta già convalidata';
                } else if (xhr.status === 500) {
                    errorMessage = 'Errore interno del server';
                }
            }
            
            showConvalidaResult(errorMessage, 'error');
        },
        complete: function() {
            // Riabilita il pulsante
            $('#confermaConvalida').prop('disabled', false).text('✅ Convalida Richiesta');
        }
    });
}

function showConvalidaResult(message, type) {
    const $result = $('#convalidaRichiestaResult');
    
    // Rimuovi classi precedenti
    $result.removeClass('success error warning info');
    
    // Aggiungi la classe appropriata
    $result.addClass(type);
    
    // Imposta il messaggio con icona appropriata
    let icon = '';
    switch (type) {
        case 'success':
            icon = '✅ ';
            break;
        case 'error':
            icon = '❌ ';
            break;
        case 'warning':
            icon = '⚠️ ';
            break;
        case 'info':
            icon = 'ℹ️ ';
            break;
    }
    
    $result.html(icon + message).fadeIn();
}

// Esportiamo le funzioni come oggetto
const Validation = {
    convalidaRichiesta,
    showConvalidaResult
};