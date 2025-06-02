/**
 * missioni.js
 * Gestione delle missioni di soccorso
 */

function visualizzaDettagliMissione(id) {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/missions/${id}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(missione) {
            console.log(missione); // 👈 stampa l'oggetto missione
            let dettagli = `🆔 ID Missione: ${missione.id}
🆔 ID Richiesta: ${missione.requestId}
👤 Autista: ${missione.autistaId || 'N/A'}
👥 Caposquadra: ${missione.caposquadraId || 'N/A'}
🔄 Stato: ${missione.status || 'N/A'}
📅 Data Creazione: ${Utils.formatDate(missione.createdAt)}`;
           
            
            if (missione.closedAt) {
                dettagli += `\n🏁 Data Fine: ${Utils.formatDate(missione.closedAt)}`;
            }

            if (missione.note) {
                dettagli += `\n📝 Note: ${missione.note}`;
            }
            
            alert(dettagli);
        },
        error: function(xhr) {
            alert('Errore nel caricamento dei dettagli della missione: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function creaMissione() {
    const richiestaId = $('#selectRichiesta').val();
    const autistaId = $('#selectAutista').val();
    const caposquadraId = $('#selectCaposquadra').val();
    
    if (!richiestaId || !autistaId || !caposquadraId) {
        alert('Tutti i campi sono obbligatori');
        return;
    }
    
    const token = Auth.getAuthToken();
    const missione = {
        requestId: parseInt(richiestaId),
        autistaId: parseInt(autistaId),
        caposquadraId: parseInt(caposquadraId),
        status: 'ATTIVA'
    };
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/missions',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(missione),
        success: function(response) {
            alert('Missione creata con successo!');
            UI.closeModalCreazioneMissione();
            // Ricarica la lista delle richieste se è aperta
            if ($('#modalListaRichieste').is(':visible')) {
                if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                    Richieste.caricaRichiesteNonPositive();
                } else {
                    Richieste.caricaRichieste();
                }
            }
        },
        error: function(xhr) {
            alert('Errore nella creazione missione: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function caricaMissioniOperatore(operatoreId) {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/missions?operatoreId=${operatoreId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(missioni) {
            console.log(missioni);
            const tbody = $('#tabellaMissioniOperatore tbody');
            const noMissioniMsg = $('#noMissioniMsg');
            const tabella = $('#tabellaMissioniOperatore');
            
            tbody.empty();
            
            if (missioni.length === 0) {
                noMissioniMsg.show();
                tabella.hide();
                return;
            }
            
            noMissioniMsg.hide();
            tabella.show();
            
            missioni.forEach(function(missione) {
                tbody.append(`
                    <tr>
                        <td>${missione.id}</td>
                        <td>${missione.requestId}</td>
                        <td>${missione.status || 'N/A'}</td>
                        <td>${Utils.formatDate(Utils.arrayToDateString(missione.createdAt))}</td>
                        <td>${Utils.formatDate(Utils.arrayToDateString(missione.closedAt))}</td>
                    </tr>
                `);
            });
        },
        error: function(xhr) {
            alert('Errore nel caricamento missioni: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function caricaRichiesteAttive() {
    const token = Auth.getAuthToken();
    console.log('Token usato:', token);
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/richieste?stato=ATTIVA',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(richieste) {
            const select = $('#selectRichiesta');
            select.empty().append('<option value="">Seleziona una richiesta</option>');
            
            richieste.content.forEach(function(richiesta) {
                select.append(`<option value="${richiesta.id}">${richiesta.id} - ${richiesta.descrizione.substring(0, 50)}...</option>`);
            });
        },
        error: function(xhr) {
            console.error('Errore nel caricamento richieste attive:', xhr);
            $('#selectRichiesta').html('<option value="">Errore nel caricamento</option>');
        }
    });
}

function caricaMissioniAperte() {
    const token = Auth.getAuthToken();
    
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/missions?status=open',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(missioni) {
            const select = $('#selectMissioneOpen');
            select.empty().append('<option value="">Seleziona una missione</option>');
            
            missioni.forEach(function(missione) {
                select.append(`<option value="${missione.id}">Missione ${missione.id} - Richiesta ${missione.requestId}</option>`);
            });
        },
        error: function(xhr) {
            console.error('Errore nel caricamento missioni aperte:', xhr);
            $('#selectMissioneOpen').html('<option value="">Errore nel caricamento</option>');
        }
    });
}

function chiudiMissione() {
    const missioneId = $('#selectMissioneOpen').val();
    
    if (!missioneId) {
        alert('Seleziona una missione da chiudere');
        return;
    }
    
    // Richiedi il livello di successo
    const livelloSuccesso = prompt(`La missione sarà chiusa. Inserisci il livello di successo:

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
        alert('Il livello di successo è obbligatorio per chiudere una missione');
        return;
    }

    const livelloNumerico = parseInt(livelloSuccesso.trim());
    
    if (isNaN(livelloNumerico) || livelloNumerico < 1 || livelloNumerico > 10) {
        alert('❌ Il livello di successo deve essere un numero da 1 a 10\n\n1 = Fallimento completo\n10 = Successo totale');
        return;
    }
    
    const token = Auth.getAuthToken();
    
    // Aggiunto il parametro livelloSuccesso all'URL della richiesta
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/missions/${missioneId}/close?livelloSuccesso=${livelloNumerico}`,
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(response) {
            const descrizioneSuccesso = getLivelloSuccessoDescrizione(livelloNumerico);
            alert(`✅ Missione chiusa con successo!\n\nLivello di Successo: ${livelloNumerico}/10\nDescrizione: ${descrizioneSuccesso}`);
            UI.closeModalChiudiMissione();
            // Ricarica la lista delle richieste se è aperta
            if ($('#modalListaRichieste').is(':visible')) {
                if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                    Richieste.caricaRichiesteNonPositive();
                } else {
                    Richieste.caricaRichieste();
                }
            }
        },
        error: function(xhr) {
            let errorMsg = 'Errore sconosciuto';
            
            if (xhr.responseJSON?.error) {
                errorMsg = xhr.responseJSON.error;
            } else if (xhr.responseJSON?.message) {
                errorMsg = xhr.responseJSON.message;
            } else if (xhr.status === 400) {
                errorMsg = 'Dati non validi. Il livello di successo è obbligatorio per chiudere una missione.';
            }
            
            alert('❌ Errore nella chiusura missione: ' + errorMsg);
        }
    });
}

// Esportiamo le funzioni come oggetto
const Missioni = {
    visualizzaDettagliMissione,
    creaMissione,
    caricaMissioniOperatore,
    caricaRichiesteAttive,
    caricaMissioniAperte,
    chiudiMissione
};