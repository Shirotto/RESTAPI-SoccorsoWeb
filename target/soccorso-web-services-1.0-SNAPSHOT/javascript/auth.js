/**
 * auth.js
 * Gestione dell'autenticazione e della sessione utente
 */

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

// Esponiamo le funzioni come oggetto
const Auth = {
    checkAuthentication,
    logout,
    getAuthToken: () => sessionStorage.getItem('authToken'),
    getUserInfo: () => JSON.parse(sessionStorage.getItem('userInfo') || '{}')
};