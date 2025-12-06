const API_URL = 'http://localhost:8080/api/auth';

function showLogin() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('registro-form').style.display = 'none';
    clearMessages();
}

function showRegistro() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('registro-form').style.display = 'block';
    clearMessages();
}

function clearMessages() {
    document.getElementById('login-error').style.display = 'none';
    document.getElementById('registro-error').style.display = 'none';
    document.getElementById('registro-success').style.display = 'none';
}

async function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('login-email').value;
    const senha = document.getElementById('login-senha').value;
    
    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, senha })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Salva dados do usuário no localStorage
            localStorage.setItem('usuarioId', data.usuarioId);
            localStorage.setItem('usuarioNome', data.nome);
            localStorage.setItem('usuarioEmail', data.email);
            localStorage.setItem('usuarioPerfil', data.perfil);
            
            window.location.href = '/dashboard-aluno.html';
        } else {
            showError('login-error', data.erro || 'Erro ao fazer login');
        }
    } catch (error) {
        console.error('Erro:', error);
        showError('login-error', 'Erro ao conectar com o servidor. Verifique se o backend está rodando.');
    }
}

async function handleRegistro(event) {
    event.preventDefault();
    
    const nome = document.getElementById('registro-nome').value;
    const email = document.getElementById('registro-email').value;
    const senha = document.getElementById('registro-senha').value;
    const confirmaSenha = document.getElementById('registro-confirma-senha').value;
    
    if (senha !== confirmaSenha) {
        showError('registro-error', 'As senhas não coincidem');
        return;
    }
    
    if (senha.length < 6) {
        showError('registro-error', 'A senha deve ter pelo menos 6 caracteres');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/registro`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nome, email, senha })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showSuccess('registro-success', data.mensagem + ' Você será redirecionado para fazer login...');
            
            document.getElementById('registro-nome').value = '';
            document.getElementById('registro-email').value = '';
            document.getElementById('registro-senha').value = '';
            document.getElementById('registro-confirma-senha').value = '';
            
            setTimeout(() => {
                showLogin();
                document.getElementById('login-email').value = email;
            }, 2000);
        } else {
            showError('registro-error', data.erro || 'Erro ao criar conta');
        }
    } catch (error) {
        console.error('Erro:', error);
        showError('registro-error', 'Erro ao conectar com o servidor. Verifique se o backend está rodando.');
    }
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.style.display = 'block';
}

function showSuccess(elementId, message) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.style.display = 'block';
}

window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    if (usuarioId) {
        window.location.href = '/dashboard-aluno.html';
    }
});
