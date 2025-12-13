// perfil-aluno.js

const usuarioId = localStorage.getItem('usuarioId');
const usuarioPerfil = localStorage.getItem('usuarioPerfil');

// Verifica autenticação
if (!usuarioId || usuarioPerfil !== 'ALUNO') {
    window.location.href = '/login-aluno.html';
}

// Atualiza nome no header
const nome = localStorage.getItem('usuarioNome');
document.getElementById('user-name').textContent = `Aluno - ${nome || 'Usuário'}`;

// Carrega dados do perfil ao iniciar
document.addEventListener('DOMContentLoaded', () => {
    carregarPerfil();
    inicializarMenuToggle();
    inicializarValidacaoCPF();
});

// Inicializa validação do campo CPF
function inicializarValidacaoCPF() {
    const cpfInput = document.getElementById('cpf');
    
    // Permite apenas números
    cpfInput.addEventListener('input', (e) => {
        // Remove tudo que não é número
        let valor = e.target.value.replace(/\D/g, '');
        
        // Limita a 11 dígitos
        if (valor.length > 11) {
            valor = valor.substring(0, 11);
        }
        
        e.target.value = valor;
    });
    
    // Previne entrada de caracteres não numéricos
    cpfInput.addEventListener('keypress', (e) => {
        const char = String.fromCharCode(e.which);
        if (!/[0-9]/.test(char)) {
            e.preventDefault();
        }
    });
    
    // Valida ao colar texto
    cpfInput.addEventListener('paste', (e) => {
        e.preventDefault();
        const texto = (e.clipboardData || window.clipboardData).getData('text');
        const numeros = texto.replace(/\D/g, '').substring(0, 11);
        cpfInput.value = numeros;
    });
}

async function carregarPerfil() {
    const loading = document.getElementById('loading');
    const form = document.getElementById('perfil-form');
    
    loading.style.display = 'block';
    form.style.display = 'none';
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/perfil`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar perfil');
        }
        
        const perfil = await response.json();
        
        // Preenche os campos do formulário
        document.getElementById('nome').value = perfil.nome || '';
        document.getElementById('email').value = perfil.email || '';
        // Remove formatação do CPF (apenas números)
        const cpfLimpo = perfil.cpf ? perfil.cpf.replace(/\D/g, '') : '';
        document.getElementById('cpf').value = cpfLimpo;
        
        loading.style.display = 'none';
        form.style.display = 'block';
        
    } catch (error) {
        console.error('Erro ao carregar perfil:', error);
        mostrarAlerta('Erro ao carregar dados do perfil', 'error');
        loading.style.display = 'none';
    }
}

// Manipula o envio do formulário
document.getElementById('perfil-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const senha = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmar-senha').value;
    
    // Valida senha se fornecida
    if (senha && senha !== confirmarSenha) {
        mostrarAlerta('As senhas não coincidem', 'error');
        return;
    }
    
    // Valida CPF se preenchido
    const cpf = document.getElementById('cpf').value.trim();
    if (cpf && cpf.length !== 11) {
        mostrarAlerta('CPF deve conter exatamente 11 dígitos', 'error');
        return;
    }
    
    const dados = {
        nome: document.getElementById('nome').value.trim(),
        email: document.getElementById('email').value.trim(),
        cpf: cpf || null,
        senha: senha ? senha : null
    };
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/perfil`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dados)
        });
        
        const resultado = await response.json();
        
        if (!response.ok) {
            throw new Error(resultado.erro || 'Erro ao atualizar perfil');
        }
        
        // Atualiza localStorage com novos dados
        localStorage.setItem('usuarioNome', resultado.nome);
        localStorage.setItem('usuarioEmail', resultado.email);
        
        // Atualiza header
        document.getElementById('user-name').textContent = `Aluno - ${resultado.nome}`;
        
        mostrarAlerta('Perfil atualizado com sucesso!', 'success');
        
        // Limpa campos de senha
        document.getElementById('senha').value = '';
        document.getElementById('confirmar-senha').value = '';
        
    } catch (error) {
        console.error('Erro ao atualizar perfil:', error);
        mostrarAlerta(error.message || 'Erro ao atualizar perfil', 'error');
    }
});

function mostrarAlerta(mensagem, tipo) {
    const container = document.getElementById('alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${tipo}`;
    alert.textContent = mensagem;
    
    container.innerHTML = '';
    container.appendChild(alert);
    
    // Remove o alerta após 5 segundos
    setTimeout(() => {
        alert.remove();
    }, 5000);
}

function voltar() {
    window.history.back();
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/login-aluno.html';
    }
}

function inicializarMenuToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
}

