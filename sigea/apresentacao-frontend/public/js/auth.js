const API_URL = '/api/auth';

// FunÃ§Ã£o para formatar CPF
function formatCPF(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length <= 11) {
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
        value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }
    
    input.value = value;
}

// Adicionar listener para formatar CPF
document.addEventListener('DOMContentLoaded', () => {
    const cpfInput = document.getElementById('registro-cpf');
    if (cpfInput) {
        cpfInput.addEventListener('input', (e) => formatCPF(e.target));
    }
});

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
        
        // Debug - ver o que o backend estÃ¡ retornando
        console.log('DEBUG Login - Resposta do backend:', data);
        
        if (response.ok) {
            // Salva dados do usuÃ¡rio no localStorage
            localStorage.setItem('usuarioId', data.usuarioId);
            localStorage.setItem('usuarioNome', data.nome);
            localStorage.setItem('usuarioEmail', data.email);
            localStorage.setItem('usuarioPerfil', data.perfil);
            localStorage.setItem('token', data.token);

            console.log('DEBUG Login - Dados salvos no localStorage:', {
                usuarioId: data.usuarioId,
                usuarioNome: data.nome,
                usuarioEmail: data.email,
                usuarioPerfil: data.perfil
            });
            
            window.location.href = '/dashboard-aluno.html';
        } else {
            showError('login-error', data.erro || 'Erro ao fazer login');
        }
    } catch (error) {
        console.error('Erro:', error);
        showError('login-error', 'Erro ao conectar com o servidor. Verifique se o backend estÃ¡ rodando.');
    }
}

async function handleRegistro(event) {
    event.preventDefault();
    
    const nome = document.getElementById('registro-nome').value;
    const email = document.getElementById('registro-email').value;
    const cpfRaw = document.getElementById('registro-cpf').value;
    const senha = document.getElementById('registro-senha').value;
    const confirmaSenha = document.getElementById('registro-confirma-senha').value;
    
    // Valida se CPF foi preenchido
    if (!cpfRaw || cpfRaw.trim() === '') {
        showError('registro-error', 'CPF Ã© obrigatÃ³rio');
        return;
    }
    
    const cpf = cpfRaw.replace(/\D/g, ''); // Remove formataÃ§Ã£o
    
    if (senha !== confirmaSenha) {
        showError('registro-error', 'As senhas nÃ£o coincidem');
        return;
    }
    
    if (senha.length < 6) {
        showError('registro-error', 'A senha deve ter pelo menos 6 caracteres');
        return;
    }
    
    if (cpf.length !== 11) {
        showError('registro-error', 'CPF invÃ¡lido. Digite 11 dÃ­gitos.');
        return;
    }
    
    console.log('Enviando dados:', { nome, email, cpf, senha: '***' });
    
    try {
        const response = await fetch(`${API_URL}/registro`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nome, email, cpf, senha })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showSuccess('registro-success', data.mensagem + ' VocÃª serÃ¡ redirecionado para fazer login...');
            
            document.getElementById('registro-nome').value = '';
            document.getElementById('registro-email').value = '';
            document.getElementById('registro-cpf').value = '';
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
        showError('registro-error', 'Erro ao conectar com o servidor. Verifique se o backend estÃ¡ rodando.');
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
    // Verifica se jÃ¡ estÃ¡ logado e redireciona para o dashboard correto
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    // Verifica se os dados sÃ£o vÃ¡lidos (nÃ£o nulos, nÃ£o undefined, nÃ£o vazios)
    const isValidLogin = usuarioId && 
                         usuarioPerfil && 
                         usuarioId !== 'null' && 
                         usuarioId !== 'undefined' && 
                         usuarioId.trim() !== '' &&
                         usuarioPerfil !== 'null' && 
                         usuarioPerfil !== 'undefined' &&
                         usuarioPerfil.trim() !== '';
    
    if (isValidLogin) {
        // Redireciona baseado no perfil do usuÃ¡rio
        if (usuarioPerfil === 'ALUNO') {
            window.location.href = '/dashboard-aluno.html';
        } else if (usuarioPerfil === 'PROFESSOR') {
            window.location.href = '/dashboard-professor.html';
        } else if (usuarioPerfil === 'ADMINISTRADOR') {
            window.location.href = '/dashboard-admin.html';
        } else {
            // Perfil invÃ¡lido - limpa tudo
            limparDadosUsuario();
        }
    } else {
        // Dados invÃ¡lidos - limpa tudo para garantir
        limparDadosUsuario();
    }
});

// FunÃ§Ã£o para limpar dados do usuÃ¡rio do localStorage
function limparDadosUsuario() {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioEmail');
    localStorage.removeItem('usuarioPerfil');
    localStorage.removeItem('token');
}

// FunÃ§Ã£o de logout que pode ser chamada de qualquer pÃ¡gina
function logout() {
    limparDadosUsuario();
    window.location.href = '/index.html';
}
