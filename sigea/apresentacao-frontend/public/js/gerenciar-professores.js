// Array para armazenar todos os professores
let todosProfessores = [];

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = 'login-admin.html';
        return;
    }
    
    // Fecha todos os modais ao carregar
    document.getElementById('modal-cadastrar').classList.remove('show');
    document.getElementById('modal-editar').classList.remove('show');
    
    loadUserInfo();
    carregarProfessores();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'Administrador'}`;
    }
}

async function carregarProfessores() {
    try {
        // Busca dados reais do banco de dados
        const response = await fetch('http://localhost:8080/api/admin/professores');
        
        if (!response.ok) {
            throw new Error('Erro ao buscar professores');
        }
        
        todosProfessores = await response.json();
        exibirProfessores(todosProfessores);
    } catch (error) {
        console.error('Erro ao carregar professores:', error);
        const tbody = document.getElementById('tabela-professores');
        tbody.innerHTML = '<tr><td colspan="4" class="error-message">Erro ao carregar professores</td></tr>';
    }
}

function exibirProfessores(professores) {
    const tbody = document.getElementById('tabela-professores');
    
    if (!professores || professores.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-message">Nenhum professor encontrado</td></tr>';
        return;
    }
    
    const html = professores.map(professor => {
        // Formata o status para exibição (ATIVO → Ativo, INATIVO → Inativo)
        const statusFormatado = professor.status.charAt(0) + professor.status.slice(1).toLowerCase();
        const statusClass = professor.status.toLowerCase();
        
        return `
        <tr>
            <td>${professor.nome}</td>
            <td>${professor.email}</td>
            <td>
                <span class="status-badge ${statusClass}">${statusFormatado}</span>
            </td>
            <td>
                <div class="action-buttons-inline">
                    <button class="btn-editar" onclick="editarProfessor(${professor.id})">
                        Editar
                    </button>
                    <button class="btn-desativar-inline ${statusClass === 'inativo' ? 'btn-reativar' : ''}" 
                            onclick="toggleStatusProfessor(${professor.id}, '${professor.status}')">
                        ${statusClass === 'ativo' ? 'Desativar' : 'Reativar'}
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
    
    tbody.innerHTML = html;
}

function filtrarProfessores() {
    const searchTerm = document.getElementById('search-input').value.toLowerCase().trim();
    
    if (searchTerm === '') {
        exibirProfessores(todosProfessores);
        return;
    }
    
    const professoresFiltrados = todosProfessores.filter(professor => {
        const nomeMatch = professor.nome.toLowerCase().includes(searchTerm);
        const emailMatch = professor.email.toLowerCase().includes(searchTerm);
        return nomeMatch || emailMatch;
    });
    
    exibirProfessores(professoresFiltrados);
}

async function editarProfessor(professorId) {
    try {
        // Busca os dados do professor
        const response = await fetch(`http://localhost:8080/api/admin/professores/${professorId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar dados do professor');
        }
        
        const professor = await response.json();
        
        // Preenche o formulário de edição
        document.getElementById('edit-professor-id').value = professor.id;
        document.getElementById('edit-nome').value = professor.nome;
        document.getElementById('edit-email').value = professor.email;
        document.getElementById('titulo-editar').textContent = professor.nome;
        
        // Abre o modal usando classe
        document.getElementById('modal-editar').classList.add('show');
    } catch (error) {
        console.error('Erro ao carregar professor:', error);
        alert('Erro ao carregar dados do professor');
    }
}

function fecharModalEdicao() {
    document.getElementById('modal-editar').classList.remove('show');
}

async function salvarEdicao(event) {
    event.preventDefault();
    
    const professorId = document.getElementById('edit-professor-id').value;
    const nome = document.getElementById('edit-nome').value.trim();
    const email = document.getElementById('edit-email').value.trim();
    
    const professorData = {
        nome: nome,
        email: email
    };
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/professores/${professorId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(professorData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao atualizar professor');
        }
        
        alert('Professor atualizado com sucesso!');
        fecharModalEdicao();
        
        // Recarrega a lista
        carregarProfessores();
    } catch (error) {
        console.error('Erro ao atualizar professor:', error);
        alert(`Erro ao atualizar professor: ${error.message}`);
    }
}

async function resetarSenha() {
    const professorId = document.getElementById('edit-professor-id').value;
    
    if (!confirm('Deseja realmente resetar a senha deste professor?\n\nA senha será redefinida para "senha123".')) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/professores/${professorId}/resetar-senha`, {
            method: 'PATCH'
        });
        
        if (!response.ok) {
            throw new Error('Erro ao resetar senha');
        }
        
        alert('Senha resetada com sucesso!\n\nNova senha: senha123');
    } catch (error) {
        console.error('Erro ao resetar senha:', error);
        alert('Erro ao resetar senha. Tente novamente.');
    }
}

async function toggleStatusProfessor(professorId, statusAtual) {
    const acao = statusAtual === 'ATIVO' ? 'desativar' : 'ativar';
    const acaoTexto = statusAtual === 'ATIVO' ? 'desativar' : 'reativar';
    
    if (!confirm(`Deseja realmente ${acaoTexto} este professor?`)) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/professores/${professorId}/${acao}`, {
            method: 'PATCH'
        });
        
        if (!response.ok) {
            throw new Error(`Erro ao ${acaoTexto} professor`);
        }
        
        alert(`Professor ${acaoTexto === 'desativar' ? 'desativado' : 'reativado'} com sucesso!`);
        
        // Recarrega a lista
        carregarProfessores();
    } catch (error) {
        console.error(`Erro ao ${acaoTexto} professor:`, error);
        alert(`Erro ao ${acaoTexto} professor. Tente novamente.`);
    }
}

function cadastrarProfessor() {
    console.log('cadastrarProfessor() chamada');
    // Limpa os campos do formulário
    document.getElementById('form-cadastrar-professor').reset();
    // Abre o modal usando classe
    const modal = document.getElementById('modal-cadastrar');
    modal.classList.add('show');
    console.log('Modal aberto');
}

function fecharModalCadastro() {
    document.getElementById('modal-cadastrar').classList.remove('show');
}

async function salvarProfessor(event) {
    event.preventDefault();
    
    const nome = document.getElementById('input-nome').value.trim();
    const email = document.getElementById('input-email').value.trim();
    const cpf = document.getElementById('input-cpf').value.trim();
    
    // Validação básica de CPF (apenas números e 11 dígitos)
    if (!/^[0-9]{11}$/.test(cpf)) {
        alert('CPF inválido! Digite 11 números.');
        return;
    }
    
    const professorData = {
        nome: nome,
        email: email,
        cpf: cpf
    };
    
    try {
        const response = await fetch('http://localhost:8080/api/admin/professores', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(professorData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao cadastrar professor');
        }
        
        alert('Professor cadastrado com sucesso!');
        fecharModalCadastro();
        
        // Recarrega a lista
        carregarProfessores();
    } catch (error) {
        console.error('Erro ao cadastrar professor:', error);
        alert(`Erro ao cadastrar professor: ${error.message}`);
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = 'login-admin.html';
    }
}
