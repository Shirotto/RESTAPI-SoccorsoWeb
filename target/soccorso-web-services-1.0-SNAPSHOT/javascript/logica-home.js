    
        document.getElementById('btn-insert-request').addEventListener('click', function() {
            // Implementa inserimento richiesta di soccorso
            console.log('Inserimento richiesta di soccorso');
        });

        document.getElementById('btn-validate-request').addEventListener('click', function() {
            // Implementa convalida richiesta di soccorso
            console.log('Convalida richiesta di soccorso');
        });

        document.getElementById('btn-list-requests').addEventListener('click', function() {
            // Implementa lista richieste filtrate
            console.log('Lista richieste filtrate');
        });

        document.getElementById('btn-list-unsuccessful').addEventListener('click', function() {
            // Implementa lista richieste non totalmente positive
            console.log('Lista richieste non totalmente positive');
        });

        document.getElementById('btn-list-free-operators').addEventListener('click', function() {
            // Implementa lista operatori liberi
            console.log('Lista operatori liberi');
        });

        document.getElementById('btn-create-mission').addEventListener('click', function() {
            // Implementa creazione missione
            console.log('Creazione missione');
        });

        document.getElementById('btn-close-mission').addEventListener('click', function() {
            // Implementa chiusura missione
            console.log('Chiusura missione');
        });

        document.getElementById('btn-cancel-request').addEventListener('click', function() {
            // Implementa annullamento richiesta
            console.log('Annullamento richiesta');
        });

        document.getElementById('btn-mission-details').addEventListener('click', function() {
            // Implementa dettagli missione
            console.log('Dettagli missione');
        });

        document.getElementById('btn-request-details').addEventListener('click', function() {
            // Implementa dettagli richiesta
            console.log('Dettagli richiesta');
        });

        document.getElementById('btn-operator-details').addEventListener('click', function() {
            // Implementa dettagli operatore
            console.log('Dettagli operatore');
        });

        document.getElementById('btn-operator-missions').addEventListener('click', function() {
            // Implementa missioni operatore
            console.log('Missioni operatore');
        });
        
  $('#logoutBtn').on('click', function(e) {
    e.preventDefault();
    logout();
  });

// Funzione per il logout
function logout() {
  // Rimuovi i dati dal sessionStorage
  sessionStorage.removeItem('authToken');
  sessionStorage.removeItem('userInfo');
  
  // Chiama anche l'endpoint di logout del server
  $.ajax({
    url: 'http://localhost:8080/soccorso-web-services/api/auth/logout',
    type: 'POST',
    success: function() {
      // Reindirizza alla pagina di login
      window.location.href = 'index.html';
    },
    error: function() {
      // Anche se c'è un errore, reindirizza comunque
      window.location.href = 'index.html';
    }
  });
}
