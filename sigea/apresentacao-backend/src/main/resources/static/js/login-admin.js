const API_BASE_URL = 'http://localhost:8080/api';

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    initLogin();
});

// Login Admin
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
                    
                    // Verificar se é administrador
                    if (data.perfil === 'ADMINISTRADOR') {
                        // Salvar token e dados do usuário
                        localStorage.setItem('authToken', data.token);
                        localStorage.setItem('currentUser', JSON.stringify(data));
                        
                        // Redirecionar para dashboard admin
                        window.location.href = '/admin/dashboard';
                    } else {
                        showError('Acesso negado. Este é um login exclusivo para administradores.');
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

