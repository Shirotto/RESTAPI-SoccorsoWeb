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
    });

