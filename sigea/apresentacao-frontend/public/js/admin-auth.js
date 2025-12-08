// Função para formatar CPF
function formatCPF(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length <= 11) {
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
        value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }
    
    input.value = value;
}

// Adicionar listener para formatar CPF enquanto digita
document.addEventListener('DOMContentLoaded', () => {
    const cpfInput = document.getElementById('login-cpf');
    if (cpfInput) {
        cpfInput.addEventListener('input', (e) => formatCPF(e.target));
    }
});

async function handleAdminLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('login-email').value;
    const cpf = document.getElementById('login-cpf').value.replace(/\D/g, ''); // Remove formatação
    const senha = document.getElementById('login-senha').value;
    
    const errorDiv = document.getElementById('login-error');
    errorDiv.style.display = 'none';
    
    try {
        const response = await fetch('/api/auth/login-admin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, cpf, senha })
        });
        
        if (response.ok) {
            const data = await response.json();
            
            // Salvar dados do usuário no localStorage
            localStorage.setItem('usuarioId', data.id);
            localStorage.setItem('usuarioNome', data.nome);
            localStorage.setItem('usuarioEmail', data.email);
            localStorage.setItem('usuarioPerfil', data.perfil);
            
            // Redirecionar para dashboard do admin
            window.location.href = '/dashboard-admin.html';
        } else {
            const error = await response.json();
            showError(error.mensagem || 'Credenciais inválidas. Verifique email, CPF e senha.');
        }
    } catch (error) {
        console.error('Erro ao fazer login:', error);
        showError('Erro ao conectar com o servidor. Tente novamente.');
    }
}

function showError(message) {
    const errorDiv = document.getElementById('login-error');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}
