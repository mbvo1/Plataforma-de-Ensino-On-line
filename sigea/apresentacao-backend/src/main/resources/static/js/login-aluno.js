const API_BASE_URL = 'http://localhost:8080/api';

let currentUser = null;
let authToken = null;

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    initLogin();
});

// Login
function initLogin() {
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            
            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, senha })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    authToken = data.token;
                    currentUser = data;
                    
                    // Redirecionar baseado no perfil
                    if (data.perfil === 'ADMINISTRADOR') {
                        window.location.href = '/admin/dashboard';
                    } else if (data.perfil === 'PROFESSOR') {
                        window.location.href = '/professor/dashboard';
                    } else {
                        window.location.href = '/aluno/dashboard';
                    }
                } else {
                    showError('Email ou senha inválidos');
                }
            } catch (error) {
                console.error('Erro:', error);
                showError('Erro ao conectar com o servidor');
            }
        });
    }
}

function showError(message) {
    const errorDiv = document.getElementById('login-error');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.classList.add('show');
        setTimeout(() => {
            errorDiv.classList.remove('show');
        }, 5000);
    }
}

