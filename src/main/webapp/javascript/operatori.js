/**
 * operatori.js
 * Gestione degli operatori
 */

function caricaOperatoriLiberi() {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/operatori?disponibile=true',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(operatori) {
            const tbody = $('#tabellaOperatoriLiberi tbody');
            tbody.empty();
            
            if (operatori.length === 0) {
                tbody.append('<tr><td colspan="4">Nessun operatore libero al momento</td></tr>');
                return;
            }
            
            operatori.forEach(function(operatore) {
                tbody.append(`
                    <tr>
                        <td>${operatore.id}</td>
                        <td>${operatore.nome}</td>
                        <td>${operatore.cognome}</td>
                        <td>${operatore.ruolo}</td>
                    </tr>
                `);
            });
        },
        error: function(xhr) {
            alert('Errore nel caricamento operatori liberi: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function caricaTuttiOperatori() {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/operatori',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(operatori) {
            const tbody = $('#tabellaDettagliOperatori tbody');
            tbody.empty();
            
            if (operatori.length === 0) {
                tbody.append('<tr><td colspan="5">Nessun operatore trovato</td></tr>');
                return;
            }
            
            operatori.forEach(function(operatore) {
                const disponibile = operatore.disponibile ? '🟢 Sì' : '🔴 No';
                tbody.append(`
                    <tr>
                        <td>${operatore.id}</td>
                        <td>${operatore.nome}</td>
                        <td>${operatore.cognome}</td>
                        <td>${operatore.ruolo}</td>
                        <td>${disponibile}</td>
                    </tr>
                `);
            });
        },
        error: function(xhr) {
            alert('Errore nel caricamento operatori: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function missioniDellOperatore() {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/operatori',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(operatori) {
            const select = $('#selectOperatoreMissioni');
            select.empty().append('<option value="">Seleziona un operatore</option>');
            
            operatori.forEach(function(operatore) {
                select.append(`<option value="${operatore.id}">${operatore.nome} ${operatore.cognome} (${operatore.ruolo})</option>`);
            });
        },
        error: function(xhr) {
            alert('Errore nel caricamento operatori: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function caricaOperatoriLiberiSeparati() {
    const token = Auth.getAuthToken();

    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/operatori?disponibile=true',
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(data) {
            const $selectAutista = $('#selectAutista');
            const $selectCaposquadra = $('#selectCaposquadra');

            $selectAutista.empty().append('<option value="">Seleziona un autista</option>');
            $selectCaposquadra.empty().append('<option value="">Seleziona un caposquadra</option>');

            data.forEach(function(operatore) {
                const optionHtml = `<option value="${operatore.id}">${operatore.nome} ${operatore.cognome} - ${operatore.ruolo}</option>`;
                if (operatore.ruolo.toLowerCase() === 'autista') {
                    $selectAutista.append(optionHtml);
                } else if (operatore.ruolo.toLowerCase() === 'caposquadra') {
                    $selectCaposquadra.append(optionHtml);
                }
            });
        },
        error: function() {
            $('#selectAutista').empty().append('<option>Errore caricamento</option>');
            $('#selectCaposquadra').empty().append('<option>Errore caricamento</option>');
        }
    });
}

function caricaOperatoriLiberiPerMissione() {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/operatori?disponibile=true',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(operatori) {
            const selectAutista = $('#selectAutista');
            const selectCaposquadra = $('#selectCaposquadra');
            
            selectAutista.empty().append('<option value="">Seleziona un autista</option>');
            selectCaposquadra.empty().append('<option value="">Seleziona un caposquadra</option>');
            
            operatori.forEach(function(operatore) {
                const option = `<option value="${operatore.id}">${operatore.nome} ${operatore.cognome}</option>`;
                if (operatore.ruolo === 'AUTISTA') {
                    selectAutista.append(option);
                } else if (operatore.ruolo === 'CAPOSQUADRA') {
                    selectCaposquadra.append(option);
                }
            });
        },
        error: function(xhr) {
            console.error('Errore nel caricamento operatori liberi:', xhr);
        }
    });
}

// Esportiamo le funzioni come oggetto
const Operatori = {
    caricaOperatoriLiberi,
    caricaTuttiOperatori,
    missioniDellOperatore,
    caricaOperatoriLiberiSeparati,
    caricaOperatoriLiberiPerMissione
};