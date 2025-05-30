$(document).ready(function() {
  const $showSignup = $('#showSignup');
  const $showLogin = $('#showLogin');
  const $loginFields = $('#loginFields');
  const $signupFields = $('#signupFields');
  const $formTitle = $('#formTitle');
  const $form = $('#authForm');

  $showSignup.on('click', function(e) {
    e.preventDefault();
    $loginFields.addClass('hidden');
    $signupFields.removeClass('hidden');
    $formTitle.text('Registrati');
  });

  $showLogin.on('click', function(e) {
    e.preventDefault();
    $signupFields.addClass('hidden');
    $loginFields.removeClass('hidden');
    $formTitle.text('Accedi');
  });

  $form.on('submit', function(e) {
    e.preventDefault();
    
    const isLogin = $formTitle.text() === 'Accedi';
    
    if (isLogin) {
      handleLogin();
    } else {
      handleSignup();
    }
  });

  // Gestione click sul bottone Login
  $('#loginBtn').on('click', function(e) {
    e.preventDefault();
    handleLogin();
  });

  // Funzione per gestire il login
  function handleLogin() {
    const email = $('#email').val();
    const password = $('#password').val();
    
    // Validazione base
    if (!email || !password) {
      alert('Inserisci email e password');
      return;
    }
    
    // Prepara i dati per l'API
    const loginData = {
      email: email, 
      password: password
    };
    
    // Chiama l'API di login
    loginUser(loginData);
  }

 // Funzione per chiamare l'API di login
function loginUser(loginData) {
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/auth/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(loginData),
        success: function(response) {
            if (response.success && response.token) {
                // Salva il token nel sessionStorage
                sessionStorage.setItem('authToken', response.token);
                sessionStorage.setItem('userInfo', JSON.stringify(response.user));
                
                alert(response.message || 'Login effettuato con successo!');              
                window.location.href = 'home.html';
            } else {
                alert(response.message || 'Errore nel login');
            }
        },
        error: function(xhr, status, error) {
            let errorMessage = 'Errore nel login';
            
            if (xhr.responseJSON) {
                if (xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                } else if (xhr.responseJSON.error) {
                    errorMessage = xhr.responseJSON.error;
                }
            } else if (xhr.responseText) {
                try {
                    const errorObj = JSON.parse(xhr.responseText);
                    errorMessage = errorObj.message || errorObj.error || errorMessage;
                } catch (e) {
                    errorMessage = xhr.responseText;
                }
            }
            
            alert('Errore login: ' + errorMessage);
            console.error('Dettagli errore:', xhr);
        }
    });
}

  // Funzione per gestire la registrazione
function handleSignup() {
    const nome = $('#nome').val().trim();
    const cognome = $('#cognome').val().trim();
    const telefonoStr = $('#telefono').val().trim();
    const indirizzo = $('#indirizzo').val().trim();
    const email = $('#signupEmail').val().trim();
    const password = $('#signupPassword').val();
    
    // Validazione base
    if (!nome || !cognome || !telefonoStr || !indirizzo || !email || !password) {
        alert('Compila tutti i campi');
        return;
    }
    
    // Validazione email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        alert('Inserisci un indirizzo email valido');
        return;
    }
    
    // Validazione e conversione telefono
    const phoneRegex = /^[0-9]+$/;
    if (!phoneRegex.test(telefonoStr)) {
        alert('Il numero di telefono deve contenere solo cifre');
        return;
    }
    
    const telefono = parseInt(telefonoStr, 10);
    if (isNaN(telefono) || telefono <= 0) {
        alert('Numero di telefono non valido');
        return;
    }
    
    // Verifica che il numero non sia troppo grande per un int
    if (telefono > 2147483647) {
        alert('Numero di telefono troppo lungo');
        return;
    }
    
    const signupData = {
        nome: nome,
        cognome: cognome,
        telefono: telefono, 
        indirizzo: indirizzo,
        email: email,
        password: password
    };
    
    registerUser(signupData);
}

// Funzione per chiamare la tua API di registrazione
function registerUser(userData) {
    $.ajax({
        url: 'http://localhost:8080/soccorso-web-services/api/users',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userData),
        success: function(result) {
            alert('Registrazione completata con successo!');
            clearForm();
            switchToLogin();
        },
        error: function(xhr, status, error) {
            let errorMessage = 'Errore nella registrazione';
            
            if (xhr.responseJSON && xhr.responseJSON.error) {
                errorMessage = xhr.responseJSON.error;
            } else if (xhr.responseText) {
                try {
                    const errorObj = JSON.parse(xhr.responseText);
                    errorMessage = errorObj.error || errorMessage;
                } catch (e) {
                    errorMessage = xhr.responseText;
                }
            }
            
            alert(errorMessage);
            console.error('Errore registrazione:', xhr.responseText);
        }
    });
}

  // Collegamento al bottone signup
  $('.signup-btn').on('click', function(e) {
    e.preventDefault();
    handleSignup();
  });

  function clearForm() {
    $form[0].reset();
  }

  function switchToLogin() {
    $showLogin.trigger('click');
  }
});

// Funzione per verificare se l'utente è autenticato
function isAuthenticated() {
  const token = sessionStorage.getItem('authToken');
  return token !== null;
}

// Funzione per ottenere il token di autenticazione
function getAuthToken() {
  return sessionStorage.getItem('authToken');
}

// Funzione per ottenere le info utente
function getUserInfo() {
  const userInfo = sessionStorage.getItem('userInfo');
  return userInfo ? JSON.parse(userInfo) : null;
}

// Funzione per fare richieste autenticate
function makeAuthenticatedRequest(url, options = {}) {
  const token = getAuthToken();
  
  if (!token) {
    alert('Sessione scaduta. Effettua nuovamente il login.');
    window.location.href = 'index.html';
    return;
  }
  
  // Aggiungi l'header Authorization
  options.headers = options.headers || {};
  options.headers['Authorization'] = 'Bearer ' + token;
  
  return $.ajax(url, options);
}


