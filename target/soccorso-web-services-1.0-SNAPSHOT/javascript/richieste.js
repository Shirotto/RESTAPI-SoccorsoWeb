/**
 * richieste.js
 * Gestione delle richieste di soccorso
 */

// Variabili condivise per la gestione delle richieste
let currentFilters = {};

function inviaRichiestaSoccorso() {
    const token = Auth.getAuthToken();
    const userInfo = Auth.getUserInfo();

    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

    const richiestaData = {
        usersId: userInfo.id ? userInfo.id.toString() : null,
        richiedente: $('#richiedente').val().trim(),
        descrizione: $('#descrizione').val().trim(),
        indirizzo: $('#indirizzo').val().trim(),
        telefonoContattoRichiesta: $('#telefono').val().trim(),
        emailContattoRichiesta: $('#email').val().trim()
    };

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

    if (richiestaData.telefonoContattoRichiesta && 
        !/^[0-9]{10}$/.test(richiestaData.telefonoContattoRichiesta)) {
        alert('Il numero di telefono deve contenere esattamente 10 cifre.');
        $('#telefono').focus();
        return;
    }

    if (richiestaData.emailContattoRichiesta && 
        !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(richiestaData.emailContattoRichiesta)) {
        alert('Inserire un indirizzo email valido.');
        $('#email').focus();
        return;
    }

    $('#confermaRichiesta').prop('disabled', true).text('Invio in corso...');

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
                  'Stato: PENDING_VALIDATION\n\n' +
                  'La tua richiesta è stata registrata e sarà processata al più presto.');
            
            UI.closeModalRichiesta();
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
        },
        complete: function() {
            $('#confermaRichiesta').prop('disabled', false).text('🆘 Invia Richiesta');
        }
    });
}

function caricaRichieste() {
    const token = Auth.getAuthToken();
    
    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

    $('#loadingRichieste').show();
    $('#tabellaRichieste').hide();
    $('#noRichieste').hide();

    let url = 'http://localhost:8080/soccorso-web-services/api/richieste';
    let params = new URLSearchParams();
    
    params.append('page', Pagination.getCurrentPage().toString());
    params.append('size', Pagination.getPageSize().toString());
    
    if (currentFilters.stato) {
        params.append('stato', currentFilters.stato);
    }
    
    if (currentFilters.periodo) {
        params.append('periodo', currentFilters.periodo);
    }
    
    if (params.toString()) {
        url += '?' + params.toString();
    }

    $.ajax({
        url: url,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            displayRichieste(response);
        },
        error: function(xhr, status, error) {
            $('#loadingRichieste').hide();
            
            if (xhr.status === 401) {
                alert('Sessione scaduta. Effettua nuovamente il login.');
                sessionStorage.removeItem('authToken');
                sessionStorage.removeItem('userInfo');
                window.location.href = 'index.html';
                return;
            }
            
            alert('Errore nel caricamento delle richieste: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function caricaRichiesteNonPositive() {
    const token = Auth.getAuthToken();
    
    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

    $('#loadingRichieste').show();
    $('#tabellaRichieste').hide();
    $('#noRichieste').hide();

    let url = 'http://localhost:8080/soccorso-web-services/api/richieste';
    let params = new URLSearchParams();
    
    params.append('page', Pagination.getCurrentPage().toString());
    params.append('size', Pagination.getPageSize().toString());
    params.append('stato', 'CHIUSA');
    
    if (params.toString()) {
        url += '?' + params.toString();
    }

    $.ajax({
        url: url,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            if (response && response.content) {
                const richiesteFiltrate = response.content.filter(function(richiesta) {
                    if (richiesta.livelloSuccesso) {
                        const livello = parseInt(richiesta.livelloSuccesso);
                        return !isNaN(livello) && livello < 5;
                    }
                    return false;
                });
                
                const responseFiltered = {
                    content: richiesteFiltrate,
                    totalElements: richiesteFiltrate.length,
                    totalPages: Math.ceil(richiesteFiltrate.length / Pagination.getPageSize()),
                    number: 0,
                    size: Pagination.getPageSize()
                };
                
                displayRichiesteNonPositive(responseFiltered);
            } else if (Array.isArray(response)) {
                const richiesteFiltrate = response.filter(function(richiesta) {
                    if (richiesta.statoRichiesta === 'CHIUSA' && richiesta.livelloSuccesso) {
                        const livello = parseInt(richiesta.livelloSuccesso);
                        return !isNaN(livello) && livello < 5;
                    }
                    return false;
                });
                
                displayRichiesteNonPositive(richiesteFiltrate);
            } else {
                displayRichiesteNonPositive([]);
            }
        },
        error: function(xhr, status, error) {
            $('#loadingRichieste').hide();
            
            if (xhr.status === 401) {
                alert('Sessione scaduta. Effettua nuovamente il login.');
                sessionStorage.removeItem('authToken');
                sessionStorage.removeItem('userInfo');
                window.location.href = 'index.html';
                return;
            }
            
            alert('Errore nel caricamento delle richieste: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function displayRichieste(richieste) {
    $('#loadingRichieste').hide();
    
    if (!richieste || richieste.length === 0) {
        $('#noRichieste').show();
        $('#tabellaRichieste').hide();
        Pagination.updatePaginationInfo(0, 0);
        return;
    }

    let richiesteData = richieste;
    if (richieste.content) {
        richiesteData = richieste.content;
        Pagination.setTotalItems(richieste.totalElements || richieste.length);
        Pagination.setTotalPages(richieste.totalPages || 1);
        Pagination.setCurrentPage((richieste.number || 0) + 1);
    } else {
        Pagination.setTotalItems(richieste.length);
        Pagination.setTotalPages(Math.ceil(Pagination.getTotalItems() / Pagination.getPageSize()));
    }

    const tbody = $('#bodyTabellaRichieste');
    tbody.empty();

    richiesteData.forEach(function(richiesta) {
        const row = createRichiestaRow(richiesta);
        tbody.append(row);
    });

    $('#tabellaRichieste').show();
    Pagination.updatePaginationInfo(richiesteData.length, Pagination.getTotalItems());
    Pagination.updatePaginationControls();
}

function displayRichiesteNonPositive(richieste) {
    $('#loadingRichieste').hide();
    
    let richiesteData = richieste;
    if (richieste.content) {
        richiesteData = richieste.content;
        Pagination.setTotalItems(richieste.totalElements || richieste.length);
        Pagination.setTotalPages(richieste.totalPages || 1);
        Pagination.setCurrentPage((richieste.number || 0) + 1);
    } else {
        richiesteData = richieste;
        Pagination.setTotalItems(richieste.length);
        Pagination.setTotalPages(Math.ceil(Pagination.getTotalItems() / Pagination.getPageSize()));
    }
    
    if (!richiesteData || richiesteData.length === 0) {
        $('#noRichieste').show();
        $('#tabellaRichieste').hide();
        Pagination.updatePaginationInfo(0, 0);
        return;
    }

    const tbody = $('#bodyTabellaRichieste');
    tbody.empty();

    richiesteData.forEach(function(richiesta) {
        const row = createRichiestaRowNonPositive(richiesta);
        tbody.append(row);
    });

    $('#tabellaRichieste').show();
    Pagination.updatePaginationInfo(richiesteData.length, Pagination.getTotalItems());
    Pagination.updatePaginationControls();
}

function createRichiestaRow(richiesta) {
    const statoBadge = `<span class="stato-badge stato-${richiesta.statoRichiesta}">${richiesta.statoRichiesta}</span>`;
    const dataFormatted = Utils.formatDate(richiesta.dataCreazione);
    const descrizioneShort = richiesta.descrizione && richiesta.descrizione.length > 50 ? 
        richiesta.descrizione.substring(0, 50) + '...' : (richiesta.descrizione || '');
    
    return `
        <tr data-id="${richiesta.id}">
            <td>${richiesta.id}</td>
            <td>${richiesta.richiedente || 'N/A'}</td>
            <td title="${richiesta.descrizione || ''}">${descrizioneShort}</td>
            <td>${richiesta.indirizzo || 'N/A'}</td>
            <td>${statoBadge}</td>
            <td>${dataFormatted}</td>
            <td>
                <button class="btn-action btn-view" onclick="Richieste.visualizzaDettagliRichiesta(${richiesta.id})">
                    👁️ Visualizza
                </button>
                ${richiesta.statoRichiesta === 'PENDING_VALIDATION' || richiesta.statoRichiesta === 'ATTIVA' ? 
                    `<button class="btn-action btn-edit" onclick="Richieste.modificaStatoRichiesta(${richiesta.id})">
                        ✏️ Modifica
                    </button>` : ''
                }
                ${richiesta.statoRichiesta === 'PENDING_VALIDATION' ? 
                    `<button class="btn-action btn-delete" onclick="Richieste.eliminaRichiesta(${richiesta.id})">
                        🗑️ Elimina
                    </button>` : ''
                }
            </td>
        </tr>
    `;
}

function createRichiestaRowNonPositive(richiesta) {
    const statoBadge = `<span class="stato-badge stato-${richiesta.statoRichiesta}">${richiesta.statoRichiesta}</span>`;
    const dataFormatted = Utils.formatDate(richiesta.dataCreazione);
    const descrizioneShort = richiesta.descrizione && richiesta.descrizione.length > 50 ? 
        richiesta.descrizione.substring(0, 50) + '...' : (richiesta.descrizione || '');
    
    const livelloSuccesso = richiesta.livelloSuccesso ? parseInt(richiesta.livelloSuccesso) : 0;
    const livelloDisplay = `<span style="color: #dc3545; font-weight: bold;">${livelloSuccesso}/10</span>`;
    
    return `
        <tr data-id="${richiesta.id}" style="background-color: #fff3cd;">
            <td>${richiesta.id}</td>
            <td>${richiesta.richiedente || 'N/A'}</td>
            <td title="${richiesta.descrizione || ''}">${descrizioneShort}</td>
            <td>${richiesta.indirizzo || 'N/A'}</td>
            <td>${statoBadge}</td>
            <td>${livelloDisplay}</td>
            <td>${dataFormatted}</td>
            <td>
                <button class="btn-action btn-view" onclick="Richieste.visualizzaDettagliRichiesta(${richiesta.id})">
                    👁️ Visualizza
                </button>
            </td>
        </tr>
    `;
}

function getLivelloSuccessoDescrizione(livello) {
    const descrizioni = {
        1: 'Fallimento completo',
        2: 'Fallimento grave', 
        3: 'Fallimento moderato',
        4: 'Risultato insufficiente',
        5: 'Risultato medio/parziale',
        6: 'Risultato discreto',
        7: 'Buon risultato',
        8: 'Ottimo risultato',
        9: 'Eccellente risultato',
        10: 'Successo totale'
    };
    
    return descrizioni[livello] || 'Livello non definito';
}

function visualizzaDettagliRichiesta(id) {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(richiesta) {
            let livelloSuccessoDisplay = '';
            if (richiesta.livelloSuccesso) {
                const livelloNum = parseInt(richiesta.livelloSuccesso);
                if (!isNaN(livelloNum) && livelloNum >= 1 && livelloNum <= 10) {
                    const descrizione = getLivelloSuccessoDescrizione(livelloNum);
                    livelloSuccessoDisplay = `✅ Livello Successo: ${livelloNum}/10 (${descrizione})`;
                } else {
                    livelloSuccessoDisplay = `✅ Livello Successo: ${richiesta.livelloSuccesso}`;
                }
            }
            
            let dettagli = `🆔 ID: ${richiesta.id}
👤 Richiedente: ${richiesta.richiedente || 'N/A'}
📋 Descrizione: ${richiesta.descrizione}
📍 Indirizzo: ${richiesta.indirizzo}
📞 Telefono: ${richiesta.telefonoContattoRichiesta || 'N/A'}
📧 Email: ${richiesta.emailContattoRichiesta || 'N/A'}
🔄 Stato: ${richiesta.statoRichiesta}
📅 Data Creazione: ${Utils.formatDate(richiesta.dataCreazione)}
${livelloSuccessoDisplay}`;
            
            alert(dettagli);
        },
        error: function(xhr) {
            alert('Errore nel caricamento dei dettagli: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function modificaStatoRichiesta(id) {
    const nuovoStato = prompt(`Seleziona nuovo stato per la richiesta ${id}:

1 - PENDING_VALIDATION
2 - ATTIVA  
3 - IN_CORSO
4 - CHIUSA
5 - IGNORATA

Inserisci il numero:`);

    if (!nuovoStato) return;

    const statiMap = {
        '1': 'PENDING_VALIDATION',
        '2': 'ATTIVA',
        '3': 'IN_CORSO', 
        '4': 'CHIUSA',
        '5': 'IGNORATA'
    };

    const statoEnum = statiMap[nuovoStato];
    if (!statoEnum) {
        alert('Selezione non valida');
        return;
    }

    if (statoEnum === 'CHIUSA') {
        const livelloSuccesso = prompt(`La richiesta sarà chiusa. Inserisci il livello di successo:

📊 SCALA DA 1 A 10:
1️⃣ = Fallimento completo
2️⃣ = Fallimento grave
3️⃣ = Fallimento moderato
4️⃣ = Risultato insufficiente
5️⃣ = Risultato medio/parziale
6️⃣ = Risultato discreto
7️⃣ = Buon risultato
8️⃣ = Ottimo risultato
9️⃣ = Eccellente risultato
🔟 = Successo totale

Inserisci un numero da 1 a 10:`);

        if (!livelloSuccesso) {
            alert('Il livello di successo è obbligatorio per chiudere una richiesta');
            return;
        }

        const livelloNumerico = parseInt(livelloSuccesso.trim());
        
        if (isNaN(livelloNumerico) || livelloNumerico < 1 || livelloNumerico > 10) {
            alert('❌ Il livello di successo deve essere un numero da 1 a 10\n\n1 = Fallimento completo\n10 = Successo totale');
            return;
        }

        aggiornaStatoConLivello(id, statoEnum, livelloNumerico.toString());
    } else {
        aggiornaStato(id, statoEnum);
    }
}

function aggiornaStato(id, stato) {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}/stato?stato=${stato}`,
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function() {
            alert('✅ Stato aggiornato con successo!');
            if (currentFilters.livelloSuccesso === 'sotto5') {
                caricaRichiesteNonPositive();
            } else {
                caricaRichieste();
            }
        },
        error: function(xhr) {
            alert('❌ Errore nell\'aggiornamento: ' + 
                  (xhr.responseJSON?.error || xhr.responseJSON?.message || 'Errore sconosciuto'));
        }
    });
}

function aggiornaStatoConLivello(id, stato, livelloSuccesso) {
    const token = Auth.getAuthToken();
    
    const livelloCodificato = encodeURIComponent(livelloSuccesso);
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}/stato?stato=${stato}&livelloSuccesso=${livelloCodificato}`,
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            const livelloNum = parseInt(livelloSuccesso);
            const descrizioneSuccesso = getLivelloSuccessoDescrizione(livelloNum);
            alert(`✅ Richiesta chiusa con successo!\n\nStato: ${stato}\nLivello di Successo: ${livelloSuccesso}/10\nDescrizione: ${descrizioneSuccesso}`);
            if (currentFilters.livelloSuccesso === 'sotto5') {
                caricaRichiesteNonPositive();
            } else {
                caricaRichieste();
            }
        },
        error: function(xhr) {
            let errorMsg = 'Errore sconosciuto';
            
            if (xhr.responseJSON?.error) {
                errorMsg = xhr.responseJSON.error;
            } else if (xhr.responseJSON?.message) {
                errorMsg = xhr.responseJSON.message;
            } else if (xhr.status === 400) {
                errorMsg = 'Dati non validi. Il livello di successo è obbligatorio per chiudere una richiesta.';
            }
            
            alert('❌ Errore nell\'aggiornamento: ' + errorMsg);
        }
    });
}

function eliminaRichiesta(id) {
    if (!confirm(`Sei sicuro di voler eliminare la richiesta ${id}?\nQuesta azione non può essere annullata.`)) {
        return;
    }

    const token = Auth.getAuthToken();
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}`,
        type: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function() {
            alert('✅ Richiesta eliminata con successo!');
            if (currentFilters.livelloSuccesso === 'sotto5') {
                caricaRichiesteNonPositive();
            } else {
                caricaRichieste();
            }
        },
        error: function(xhr) {
            alert('❌ Errore nell\'eliminazione: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function applicaFiltri() {
    currentFilters = {
        stato: $('#filtroStato').val(),
        periodo: $('#filtroData').val()
    };
    Pagination.setCurrentPage(1);
    caricaRichieste();
}

function resetFiltri() {
    $('#filtroStato').val('');
    $('#filtroData').val('');
    currentFilters = {};
    Pagination.setCurrentPage(1);
    if ($('#modalListaRichieste').is(':visible')) {
        caricaRichieste();
    }
}

// Esportiamo le funzioni come oggetto
const Richieste = {
    inviaRichiestaSoccorso,
    caricaRichieste,
    caricaRichiesteNonPositive,
    visualizzaDettagliRichiesta,
    modificaStatoRichiesta,
    eliminaRichiesta,
    applicaFiltri,
    resetFiltri,
    currentFilters
};