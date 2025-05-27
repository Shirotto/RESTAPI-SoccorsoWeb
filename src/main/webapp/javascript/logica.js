const showSignup = document.getElementById('showSignup');
const showLogin = document.getElementById('showLogin');
const loginFields = document.getElementById('loginFields');
const signupFields = document.getElementById('signupFields');
const formTitle = document.getElementById('formTitle');
const form = document.getElementById('authForm');

if (showSignup) {
  showSignup.addEventListener('click', function(e) {
    e.preventDefault();
    loginFields.classList.add('hidden');
    signupFields.classList.remove('hidden');
    formTitle.textContent = 'Registrati';
  });
}

if (showLogin) {
  showLogin.addEventListener('click', function(e) {
    e.preventDefault();
    signupFields.classList.add('hidden');
    loginFields.classList.remove('hidden');
    formTitle.textContent = 'Accedi';
  });
}

form.addEventListener('submit', function(e) {
  e.preventDefault();
  
  const isLogin = formTitle.textContent === 'Accedi';
  
  if (isLogin) {
    handleLogin();
  } else {
    handleSignup();
  }
});

// Funzione per gestire il login
function handleLogin() {

  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  
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

  const nome = document.getElementById('nome').value;
  const cognome = document.getElementById('cognome').value;
  const telefono = document.getElementById('telefono').value;
  const indirizzo = document.getElementById('indirizzo').value;
  const email = document.getElementById('signupEmail').value;
  const password = document.getElementById('signupPassword').value;
  
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
async function registerUser(userData) {
  try {
    const response = await fetch('http://localhost:8080/soccorso-web-services/api/users', {  
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)  
    });
    
    if (response.ok) {
      const result = await response.json();
      alert('Registrazione completata con successo!');
      clearForm();
      switchToLogin();
    } else {
      const errorText = await response.text();
      alert('Errore registrazione: ' + errorText);
    }
  } catch (error) {
    console.error('Errore nella chiamata API:', error);
    alert('Errore di connessione');
  }
}


// Funzione per ottenere i dati del login
function getLoginData() {
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  
  return {
    username: email,
    password: password
  };
}

// Funzione per ottenere i dati della registrazione
function getSignupData() {
  const nome = document.getElementById('nome').value;
  const cognome = document.getElementById('cognome').value;
  const telefono = document.getElementById('telefono').value;
  const indirizzo = document.getElementById('indirizzo').value;
  const email = document.getElementById('signupEmail').value;
  const password = document.getElementById('signupPassword').value;
  
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
const signupBtn = document.querySelector('.signup-btn');
if (signupBtn) {
  signupBtn.addEventListener('click', function(e) {
    e.preventDefault();
    handleSignup();
  });
}

function clearForm() {
  form.reset();
}


function switchToLogin() {
  if (showLogin) {
    showLogin.click();
  }
}