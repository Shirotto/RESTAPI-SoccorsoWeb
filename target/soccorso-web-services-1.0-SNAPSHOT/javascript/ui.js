/**
 * ui.js
 * Gestione dell'interfaccia utente e dei modali
 */

function openModalRichiesta() {
    $('#formRichiesta')[0].reset();
    
    const userInfo = Auth.getUserInfo();
    
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
    Pagination.setCurrentPage(1);
    Pagination.setPageSize(25);
    Richieste.resetFiltri();
    $('#modalListaRichieste').fadeIn(300);
    Richieste.caricaRichieste();
}

function openModalRichiesteNonPositive() {
    Pagination.setCurrentPage(1);
    Pagination.setPageSize(25);
    
    Richieste.currentFilters = {
        stato: 'CHIUSA',
        livelloSuccesso: 'sotto5'
    };
    
    $('#filtroStato').val('CHIUSA');
    $('#filtroData').val('');
    
    $('#modalListaRichieste').fadeIn(300);
    Richieste.caricaRichiesteNonPositive();
}

function closeModalListaRichieste() {
    $('#modalListaRichieste').fadeOut(300);
}

function openModalOperatoriLiberi() {
    const modal = $('#modalListaOperatoriLiberi');
    Operatori.caricaOperatoriLiberi();
    modal.show();
}

function closeModalOperatoriLiberi() {
    $('#modalListaOperatoriLiberi').hide();
}

function openModalDettagliOperatori() {
    const modal = $('#modalDettagliOperatori');
    Operatori.caricaTuttiOperatori();
    modal.show();
}

function closeModalDettagliOperatori() {
    $('#modalDettagliOperatori').hide();
}

function openModalMissioniOperatore() {
    const modal = $('#modalMissioniOperatore');
    Operatori.missioniDellOperatore();
    modal.show();
}

function closeModalMissioniOperatore() {
    $('#modalMissioniOperatore').hide();
}

function openModalCreazioneMissione() {
    const modal = $('#modalCreazioneMissione');
    Missioni.caricaRichiesteAttive();
    Operatori.caricaOperatoriLiberiSeparati();
    modal.show();
}

function closeModalCreazioneMissione() {
    $('#modalCreazioneMissione').hide();
}

function openModalChiudiMissione() {
    const modal = $('#modalChiudiMissione');
    Missioni.caricaMissioniAperte();
    modal.show();
}

function closeModalChiudiMissione() {
    $('#modalChiudiMissione').hide();
}

function openModalConvalidaRichiesta() {
    // Crea il modal HTML se non esiste
    if (!$('#modalConvalidaRichiesta').length) {
        const modalHtml = `
            <div id="modalConvalidaRichiesta" class="modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <span class="close" id="closeModalConvalida">&times;</span>
                        <h2>🔍 Convalida Richiesta di Soccorso</h2>
                    </div>
                    <div class="modal-body">
                        <form id="formConvalidaRichiesta">
                            <div class="form-group">
                                <label for="richiestaId">ID Richiesta da Convalidare:</label>
                                <input type="number" id="richiestaId" name="richiestaId" required 
                                       placeholder="Inserisci l'ID della richiesta" min="1">
                            </div>
                            <div class="form-group">
                                <label for="validationToken">Token di Validazione:</label>
                                <input type="text" id="validationToken" name="validationToken" required 
                                       placeholder="Inserisci il token di validazione">
                                <small class="form-help">Il token viene fornito insieme alla richiesta</small>
                            </div>
                        </form>
                        <div id="convalidaRichiestaResult" class="result-message" style="display: none;"></div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" id="confermaConvalida" class="btn btn-success">
                            ✅ Convalida Richiesta
                        </button>
                        <button type="button" id="annullaConvalida" class="btn btn-secondary">
                            ❌ Annulla
                        </button>
                    </div>
                </div>
            </div>
        `;
        $('body').append(modalHtml);
        
        // Aggiungi gli event listeners per il nuovo modal
        $('#closeModalConvalida, #annullaConvalida').on('click', function(e) {
            e.preventDefault();
            closeModalConvalidaRichiesta();
        });
        
        $('#confermaConvalida').on('click', function(e) {
            e.preventDefault();
            Validation.convalidaRichiesta();
        });
        
        $('#formConvalidaRichiesta').on('submit', function(e) {
            e.preventDefault();
            Validation.convalidaRichiesta();
        });
        
        $('#modalConvalidaRichiesta').on('click', function(e) {
            if (e.target === this) {
                closeModalConvalidaRichiesta();
            }
        });
    }
    
    // Reset del form e mostra il modal
    $('#formConvalidaRichiesta')[0].reset();
    $('#convalidaRichiestaResult').hide();
    $('#modalConvalidaRichiesta').fadeIn();
}

function closeModalConvalidaRichiesta() {
    $('#modalConvalidaRichiesta').fadeOut();
}

// Aggiungi il CSS per il modal di convalida
function addConvalidaStyles() {
    const convalidaCss = `
    <style id="convalidaRichiestaStyles">
    #modalConvalidaRichiesta .form-group {
        margin-bottom: 20px;
    }

    #modalConvalidaRichiesta .form-help {
        display: block;
        margin-top: 5px;
        font-size: 0.9em;
        color: #666;
        font-style: italic;
    }

    #modalConvalidaRichiesta .result-message {
        padding: 12px;
        border-radius: 4px;
        margin: 15px 0;
        font-weight: bold;
    }

    #modalConvalidaRichiesta .result-message.success {
        background-color: #d4edda;
        border: 1px solid #c3e6cb;
        color: #155724;
    }

    #modalConvalidaRichiesta .result-message.error {
        background-color: #f8d7da;
        border: 1px solid #f5c6cb;
        color: #721c24;
    }

    #modalConvalidaRichiesta .result-message.warning {
        background-color: #fff3cd;
        border: 1px solid #ffeaa7;
        color: #856404;
    }

    #modalConvalidaRichiesta .result-message.info {
        background-color: #d1ecf1;
        border: 1px solid #bee5eb;
        color: #0c5460;
    }

    #modalConvalidaRichiesta input[type="number"],
    #modalConvalidaRichiesta input[type="text"] {
        width: 100%;
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
    }

    #modalConvalidaRichiesta input[type="number"]:focus,
    #modalConvalidaRichiesta input[type="text"]:focus {
        outline: none;
        border-color: #007bff;
        box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
    }
    </style>
    `;

    // Aggiungi il CSS al documento se non esiste già
    if (!$('#convalidaRichiestaStyles').length) {
        $('head').append(convalidaCss);
    }
}

// Inizializza i componenti UI
function initUI() {
    addConvalidaStyles();
}

// Esportiamo le funzioni come oggetto
const UI = {
    openModalRichiesta,
    closeModalRichiesta,
    openModalListaRichieste,
    openModalRichiesteNonPositive,
    closeModalListaRichieste,
    openModalOperatoriLiberi,
    closeModalOperatoriLiberi,
    openModalDettagliOperatori,
    closeModalDettagliOperatori,
    openModalMissioniOperatore,
    closeModalMissioniOperatore,
    openModalCreazioneMissione,
    closeModalCreazioneMissione,
    openModalChiudiMissione,
    closeModalChiudiMissione,
    openModalConvalidaRichiesta,
    closeModalConvalidaRichiesta,
    initUI
};