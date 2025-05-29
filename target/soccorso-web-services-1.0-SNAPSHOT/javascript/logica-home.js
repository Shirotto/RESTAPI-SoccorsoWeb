let currentPage = 1;
let pageSize = 25;
let totalPages = 1;
let totalItems = 0;
let currentFilters = {};

function checkAuthentication() {
    const token = sessionStorage.getItem('authToken');
    
    if (!token) {
        alert('Sessione scaduta. Effettua nuovamente il login.');
        window.location.href = 'index.html';
        return;
    }

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

function openModalRichiesta() {
    $('#formRichiesta')[0].reset();
    
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}');
    
    if (userInfo.nome && userInfo.cognome) {
        $('#richiedente').val(userInfo.nome + ' ' + userInfo.cognome);
    }
    if (userInfo.email) {
        $('#email').val(userInfo.email);
    }
    if (userInfo.telefono) {
        $('#telefono').val(userInfo.telefono);
    }
    
    $('#modalRichiesta').fadeIn(300);
}

function closeModalRichiesta() {
    $('#modalRichiesta').fadeOut(300);
}

function openModalListaRichieste() {
    currentPage = 1;
    pageSize = 25;
    resetFiltri();
    $('#modalListaRichieste').fadeIn(300);
    caricaRichieste();
}

function closeModalListaRichieste() {
    $('#modalListaRichieste').fadeOut(300);
}

function applicaFiltri() {
    currentFilters = {
        stato: $('#filtroStato').val(),
        periodo: $('#filtroData').val()
    };
    currentPage = 1;
    caricaRichieste();
}

function resetFiltri() {
    $('#filtroStato').val('');
    $('#filtroData').val('');
    currentFilters = {};
    currentPage = 1;
    if ($('#modalListaRichieste').is(':visible')) {
        caricaRichieste();
    }
}

function caricaRichieste() {
    const token = sessionStorage.getItem('authToken');
    
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
    
    params.append('page', currentPage.toString());
    params.append('size', pageSize.toString());
    
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

function displayRichieste(richieste) {
    $('#loadingRichieste').hide();
    
    if (!richieste || richieste.length === 0) {
        $('#noRichieste').show();
        $('#tabellaRichieste').hide();
        updatePaginationInfo(0, 0);
        return;
    }

    let richiesteData = richieste;
    if (richieste.content) {
        richiesteData = richieste.content;
        totalItems = richieste.totalElements || richieste.length;
        totalPages = richieste.totalPages || 1;
        currentPage = (richieste.number || 0) + 1;
    } else {
        totalItems = richieste.length;
        totalPages = Math.ceil(totalItems / pageSize);
    }

    const tbody = $('#bodyTabellaRichieste');
    tbody.empty();

    richiesteData.forEach(function(richiesta) {
        const row = createRichiestaRow(richiesta);
        tbody.append(row);
    });

    $('#tabellaRichieste').show();
    updatePaginationInfo(richiesteData.length, totalItems);
    updatePaginationControls();
}

function createRichiestaRow(richiesta) {
    const statoBadge = `<span class="stato-badge stato-${richiesta.statoRichiesta}">${richiesta.statoRichiesta}</span>`;
    const dataFormatted = formatDate(richiesta.dataCreazione);
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
                <button class="btn-action btn-view" onclick="visualizzaDettagliRichiesta(${richiesta.id})">
                    👁️ Visualizza
                </button>
                ${richiesta.statoRichiesta === 'PENDING_VALIDATION' || richiesta.statoRichiesta === 'ATTIVA' ? 
                    `<button class="btn-action btn-edit" onclick="modificaStatoRichiesta(${richiesta.id})">
                        ✏️ Modifica
                    </button>` : ''
                }
                ${richiesta.statoRichiesta === 'PENDING_VALIDATION' ? 
                    `<button class="btn-action btn-delete" onclick="eliminaRichiesta(${richiesta.id})">
                        🗑️ Elimina
                    </button>` : ''
                }
            </td>
        </tr>
    `;
}

function updatePaginationInfo(currentItems, totalItems) {
    const start = totalItems > 0 ? ((currentPage - 1) * pageSize) + 1 : 0;
    const end = Math.min(start + currentItems - 1, totalItems);
    $('#infoPaginazione').text(`Showing ${start}-${end} of ${totalItems} results`);
}

function updatePaginationControls() {
    $('#numeroPagina').text(`Pagina ${currentPage} di ${totalPages}`);
    
    $('#btnPrimaPagina, #btnPaginaPrecedente').prop('disabled', currentPage <= 1);
    $('#btnPaginaSuccessiva, #btnUltimaPagina').prop('disabled', currentPage >= totalPages);
}

function visualizzaDettagliRichiesta(id) {
    const token = sessionStorage.getItem('authToken');
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function(richiesta) {
            let dettagli = `🆔 ID: ${richiesta.id}
👤 Richiedente: ${richiesta.richiedente || 'N/A'}
📋 Descrizione: ${richiesta.descrizione}
📍 Indirizzo: ${richiesta.indirizzo}
📞 Telefono: ${richiesta.telefonoContattoRichiesta || 'N/A'}
📧 Email: ${richiesta.emailContattoRichiesta || 'N/A'}
🔄 Stato: ${richiesta.statoRichiesta}
📅 Data Creazione: ${formatDate(richiesta.dataCreazione)}
${richiesta.livelloSuccesso ? `✅ Livello Successo: ${richiesta.livelloSuccesso}` : ''}`;
            
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

    const token = sessionStorage.getItem('authToken');
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}/stato?stato=${statoEnum}`,
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function() {
            alert('✅ Stato aggiornato con successo!');
            caricaRichieste();
        },
        error: function(xhr) {
            alert('❌ Errore nell\'aggiornamento: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function eliminaRichiesta(id) {
    if (!confirm(`Sei sicuro di voler eliminare la richiesta ${id}?\nQuesta azione non può essere annullata.`)) {
        return;
    }

    const token = sessionStorage.getItem('authToken');
    
    $.ajax({
        url: `http://localhost:8080/soccorso-web-services/api/richieste/${id}`,
        type: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function() {
            alert('✅ Richiesta eliminata con successo!');
            caricaRichieste();
        },
        error: function(xhr) {
            alert('❌ Errore nell\'eliminazione: ' + 
                  (xhr.responseJSON?.error || 'Errore sconosciuto'));
        }
    });
}

function inviaRichiestaSoccorso() {
    const token = sessionStorage.getItem('authToken');
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}');

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
            
            closeModalRichiesta();
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

function formatDate(dateInput) {
    if (!dateInput) return 'N/A';
    
    try {
        console.log('Input data:', dateInput);
        
        let date;
        
        // Se è un array (formato dal server Java)
        if (Array.isArray(dateInput) && dateInput.length >= 6) {
            const [year, month, day, hour, minute, second] = dateInput;
            // JavaScript mesi sono 0-based, quindi month-1
            date = new Date(year, month - 1, day, hour, minute, second || 0);
            console.log('Parsed from array:', date);
        }
        // Se è una stringa
        else if (typeof dateInput === 'string') {
            const cleanDateString = dateInput.trim();
            
            // Formato MySQL: "2025-05-29 11:42:34"
            const mysqlMatch = cleanDateString.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/);
            if (mysqlMatch) {
                const [, year, month, day, hour, minute, second] = mysqlMatch;
                date = new Date(
                    parseInt(year), 
                    parseInt(month) - 1, 
                    parseInt(day), 
                    parseInt(hour), 
                    parseInt(minute), 
                    parseInt(second)
                );
            } else {
                date = new Date(cleanDateString);
            }
        }
        // Se è un numero (timestamp)
        else if (typeof dateInput === 'number') {
            date = new Date(dateInput);
        }
        else {
            console.error('Formato data non riconosciuto:', dateInput);
            return 'N/A';
        }
        
        if (isNaN(date.getTime())) {
            console.error('Data non valida:', dateInput);
            return 'N/A';
        }
        
        const formatted = date.toLocaleString('it-IT', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
        
        console.log('Data formattata:', formatted);
        return formatted;
        
    } catch (error) {
        console.error('Errore nel parsing della data:', error);
        return 'N/A';
    }
}

$(document).ready(function() {
    checkAuthentication();

    $('#logoutBtn').on('click', function(e) {
        e.preventDefault();
        logout();
    });

    $('#btn-insert-request').on('click', function(e) {
        e.preventDefault();
        openModalRichiesta();
    });

    $('#btn-list-requests').on('click', function(e) {
        e.preventDefault();
        openModalListaRichieste();
    });

    $('#closeModal, #cancellaRichiesta').on('click', function(e) {
        e.preventDefault();
        closeModalRichiesta();
    });

    $('#closeModalLista').on('click', function(e) {
        e.preventDefault();
        closeModalListaRichieste();
    });

    $('#applicaFiltri').on('click', function(e) {
        e.preventDefault();
        applicaFiltri();
    });

    $('#resetFiltri').on('click', function(e) {
        e.preventDefault();
        resetFiltri();
    });

    $('#pageSize').on('change', function() {
        pageSize = parseInt($(this).val());
        currentPage = 1;
        caricaRichieste();
    });

    $('#btnPrimaPagina').on('click', function() {
        if (currentPage > 1) {
            currentPage = 1;
            caricaRichieste();
        }
    });

    $('#btnPaginaPrecedente').on('click', function() {
        if (currentPage > 1) {
            currentPage--;
            caricaRichieste();
        }
    });

    $('#btnPaginaSuccessiva').on('click', function() {
        if (currentPage < totalPages) {
            currentPage++;
            caricaRichieste();
        }
    });

    $('#btnUltimaPagina').on('click', function() {
        if (currentPage < totalPages) {
            currentPage = totalPages;
            caricaRichieste();
        }
    });

    $('#modalRichiesta, #modalListaRichieste').on('click', function(e) {
        if (e.target === this) {
            if (this.id === 'modalRichiesta') {
                closeModalRichiesta();
            } else if (this.id === 'modalListaRichieste') {
                closeModalListaRichieste();
            }
        }
    });

    $('#formRichiesta').on('submit', function(e) {
        e.preventDefault();
        inviaRichiestaSoccorso();
    });
});