/**
 * main.js
 * File principale per l'inizializzazione dell'applicazione
 */

// Quando il documento è pronto, inizializza l'applicazione
$(document).ready(function() {
    // Verifica autenticazione all'avvio
    Auth.checkAuthentication();
    
    // Inizializza i componenti UI
    UI.initUI();

    // EVENT HANDLERS PER I PULSANTI PRINCIPALI
    
    // Autenticazione
    $('#logoutBtn').on('click', function(e) {
        e.preventDefault();
        Auth.logout();
    });

    // Gestione Richieste
    $('#btn-insert-request').on('click', function(e) {
        e.preventDefault();
        UI.openModalRichiesta();
    });

    $('#btn-list-requests').on('click', function(e) {
        e.preventDefault();
        UI.openModalListaRichieste();
    });

    $('#btn-negative-requests').on('click', function(e) {
        e.preventDefault();
        UI.openModalRichiesteNonPositive();
    });

    $('#btn-request-details').on('click', function(e) {
        e.preventDefault();
        const id = prompt('Inserisci l\'ID della richiesta da visualizzare:');
        if (id && !isNaN(id)) {
            Richieste.visualizzaDettagliRichiesta(parseInt(id));
        }
    });

    $('#btn-cancel-request').on('click', function(e) {
        e.preventDefault();
        const id = prompt('Inserisci l\'ID della richiesta da eliminare (solo per Admin):');
        if (id && !isNaN(id)) {
            Richieste.eliminaRichiesta(parseInt(id));
        }
    });
    
    $('#btn-validate-request').on('click', function(e) {
        e.preventDefault();
        UI.openModalConvalidaRichiesta();
    });

    // Gestione Operatori
    $('#btn-list-free-operators').on('click', function(e) {
        e.preventDefault();
        UI.openModalOperatoriLiberi();
    });

    $('#btn-operator-details').on('click', function(e) {
        e.preventDefault();
        UI.openModalDettagliOperatori();
    });

    $('#btn-operator-missions').on('click', function(e) {
        e.preventDefault();
        UI.openModalMissioniOperatore();
    });

    // Gestione Missioni
    $('#btn-create-mission').on('click', function(e) {
        e.preventDefault();
        UI.openModalCreazioneMissione();
    });
    
    $('#btn-close-mission').on('click', function(e) {
        e.preventDefault();
        UI.openModalChiudiMissione();
    });

    $('#btn-mission-details').on('click', function(e) {
        e.preventDefault();
        const id = prompt('Inserisci l\'ID della missione da visualizzare:');
        if (id && !isNaN(id)) {
            Missioni.visualizzaDettagliMissione(parseInt(id));
        }
    });

    // EVENT HANDLERS PER I MODALI
    
    // Modal Richieste
    $('#closeModal, #cancellaRichiesta').on('click', function(e) {
        e.preventDefault();
        UI.closeModalRichiesta();
    });
    
    $('#closeModalLista').on('click', function(e) {
        e.preventDefault();
        UI.closeModalListaRichieste();
    });
    
    $('#closeModalNonPositive').on('click', function(e) {
        e.preventDefault();
        UI.closeModalListaNonPositive();
    });

    $('#applicaFiltri').on('click', function(e) {
        e.preventDefault();
        Richieste.applicaFiltri();
    });

    $('#resetFiltri').on('click', function(e) {
        e.preventDefault();
        Richieste.resetFiltri();
    });

    $('#formRichiesta').on('submit', function(e) {
        e.preventDefault();
        Richieste.inviaRichiestaSoccorso();
    });

    // Modal Operatori
    $('#closeModalOperatoriLiberi').on('click', function(e) {
        e.preventDefault();
        UI.closeModalOperatoriLiberi();
    });

    $('#closeModalDettagliOperatori').on('click', function(e) {
        e.preventDefault();
        UI.closeModalDettagliOperatori();
    });

    $('#closeModalMissioniOperatore').on('click', function(e) {
        e.preventDefault();
        UI.closeModalMissioniOperatore();
    });

    // Modal Missioni
    $('#closeModalMissione, #annullaMissione').on('click', function(e) {
        e.preventDefault();
        UI.closeModalCreazioneMissione();
    });

    $('#closeModalChiudi').on('click', function(e) {
        e.preventDefault();
        UI.closeModalChiudiMissione();
    });

    $('#creaMissione').on('click', function(e) {
        e.preventDefault();
        Missioni.creaMissione();
    });

    $('#btn-conferma-chiusura').on('click', function(e) {
        e.preventDefault();
        Missioni.chiudiMissione();
    });

    // Handler per cambio operatore nelle missioni
    $('#selectOperatoreMissioni').on('change', function() {
        const operatoreId = $(this).val();
        if (operatoreId) {
            Missioni.caricaMissioniOperatore(operatoreId);
        } else {
            $('#tabellaMissioniOperatore').hide();
            $('#noMissioniMsg').hide();
        }
    });

    // Gestione Paginazione
    $('#pageSize').on('change', function() {
        Pagination.setPageSize(parseInt($(this).val()));
        Pagination.setCurrentPage(1);
        if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
            Richieste.caricaRichiesteNonPositive();
        } else {
            Richieste.caricaRichieste();
        }
    });

    $('#btnPrimaPagina').on('click', function() {
        if (Pagination.getCurrentPage() > 1) {
            Pagination.setCurrentPage(1);
            if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                Richieste.caricaRichiesteNonPositive();
            } else {
                Richieste.caricaRichieste();
            }
        }
    });

    $('#btnPaginaPrecedente').on('click', function() {
        if (Pagination.getCurrentPage() > 1) {
            Pagination.setCurrentPage(Pagination.getCurrentPage() - 1);
            if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                Richieste.caricaRichiesteNonPositive();
            } else {
                Richieste.caricaRichieste();
            }
        }
    });

    $('#btnPaginaSuccessiva').on('click', function() {
        if (Pagination.getCurrentPage() < Pagination.getTotalPages()) {
            Pagination.setCurrentPage(Pagination.getCurrentPage() + 1);
            if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                Richieste.caricaRichiesteNonPositive();
            } else {
                Richieste.caricaRichieste();
            }
        }
    });

    $('#btnUltimaPagina').on('click', function() {
        if (Pagination.getCurrentPage() < Pagination.getTotalPages()) {
            Pagination.setCurrentPage(Pagination.getTotalPages());
            if (Richieste.currentFilters.livelloSuccesso === 'sotto5') {
                Richieste.caricaRichiesteNonPositive();
            } else {
                Richieste.caricaRichieste();
            }
        }
    });

    // Gestione chiusura modale cliccando fuori dal contenuto
    $('#modalRichiesta, #modalListaRichieste, #modalConvalidaRichiesta, #modalListaNonPositive').on('click', function(e) {
        if (e.target === this) {
            if (this.id === 'modalRichiesta') {
                UI.closeModalRichiesta();
            } else if (this.id === 'modalListaRichieste') {
                UI.closeModalListaRichieste();
            } else if (this.id === 'modalConvalidaRichiesta') {
                UI.closeModalConvalidaRichiesta();
            } else if (this.id === 'modalListaNonPositive') {
                UI.closeModalListaNonPositive();
            }
        }
    });
});