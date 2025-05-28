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
    
    // Qui puoi chiamare la tua funzione API per il login
    console.log('Dati login:', loginData);
    // loginUser(loginData); // <- chiama qui la tua funzione API
  }

  // Funzione per gestire la registrazione
  function handleSignup() {
    const nome = $('#nome').val();
    const cognome = $('#cognome').val();
    const telefono = $('#telefono').val();
    const indirizzo = $('#indirizzo').val();
    const email = $('#signupEmail').val();
    const password = $('#signupPassword').val();
    
    // Validazione base
    if (!nome || !cognome || !telefono || !indirizzo || !email || !password) {
      alert('Compila tutti i campi');
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
    
    // Chiama la tua API
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
        alert('Errore registrazione: ' + xhr.responseText);
      }
    });
  }

  // Funzione per ottenere i dati del login
  function getLoginData() {
    const email = $('#email').val();
    const password = $('#password').val();
    
    return {
      username: email,
      password: password
    };
  }

  // Funzione per ottenere i dati della registrazione
  function getSignupData() {
    const nome = $('#nome').val();
    const cognome = $('#cognome').val();
    const telefono = $('#telefono').val();
    const indirizzo = $('#indirizzo').val();
    const email = $('#signupEmail').val();
    const password = $('#signupPassword').val();
    
    return {
      nome: nome,
      cognome: cognome,
      telefono: telefono,
      indirizzo: indirizzo,
      email: email,
      password: password
    };
  }

  // Collegamento diretto al bottone (opzionale)
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

document.getElementById('loginBtn').addEventListener('click', function() {
    window.location.href = 'home.html';
});