// Array para armazenar todas as disciplinas
let todasDisciplinas = [];
let periodoAtual = null;

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = 'login-admin.html';
        return;
    }
    
    loadUserInfo();
    carregarPeriodoAtual();
    carregarDisciplinas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'Administrador'}`;
    }
}

async function carregarPeriodoAtual() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/periodos/atual');
        
        if (!response.ok) {
            throw new Error('Erro ao buscar período atual');
        }
        
        periodoAtual = await response.json();
        exibirPeriodoAtual(periodoAtual);
    } catch (error) {
        console.error('Erro ao carregar período atual:', error);
        // Define um período padrão caso não haja no banco
        periodoAtual = {
            nome: '2025.2',
            status: 'ATIVO'
        };
        exibirPeriodoAtual(periodoAtual);
    }
}

function exibirPeriodoAtual(periodo) {
    const statusElement = document.getElementById('periodo-status');
    const nomeElement = document.getElementById('periodo-nome');
    
    if (statusElement && nomeElement) {
        const statusFormatado = periodo.status === 'ATIVO' ? 'Ativo' : 'Encerrado';
        statusElement.textContent = statusFormatado;
        statusElement.className = 'periodo-status ' + periodo.status.toLowerCase();
        nomeElement.textContent = periodo.nome;
    }
}

async function carregarDisciplinas() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/disciplinas');
        
        if (!response.ok) {
            // Se for 404 ou outro erro, mostra mensagem de nenhuma disciplina
            todasDisciplinas = [];
            exibirDisciplinas(todasDisciplinas);
            return;
        }
        
        todasDisciplinas = await response.json();
        exibirDisciplinas(todasDisciplinas);
    } catch (error) {
        console.error('Erro ao carregar disciplinas:', error);
        // Em caso de erro de conexão, mostra mensagem vazia
        todasDisciplinas = [];
        exibirDisciplinas(todasDisciplinas);
    }
}

function exibirDisciplinas(disciplinas) {
    const tbody = document.getElementById('tabela-disciplinas');
    
    if (!disciplinas || disciplinas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-message">Nenhuma disciplina cadastrada</td></tr>';
        return;
    }
    
    const html = disciplinas.map(disciplina => {
        const statusFormatado = disciplina.status.charAt(0) + disciplina.status.slice(1).toLowerCase();
        const statusClass = disciplina.status.toLowerCase();
        
        return `
        <tr>
            <td>${disciplina.codigo}</td>
            <td>${disciplina.periodo}</td>
            <td>${disciplina.nome}</td>
            <td>${disciplina.salasOfertadas}</td>
            <td>
                <span class="status-badge ${statusClass}">${statusFormatado}</span>
            </td>
            <td>
                <div class="action-buttons-grid">
                    <button class="btn-editar-small" onclick="editarDisciplina(${disciplina.id})" title="Editar disciplina">
                        Editar disciplina
                    </button>
                    <button class="btn-gerenciar" onclick="gerenciarSalas(${disciplina.id})" title="Gerenciar salas">
                        Gerenciar salas
                    </button>
                    <button class="btn-desativar-small ${statusClass === 'inativo' ? 'btn-reativar' : ''}" 
                            onclick="toggleStatusDisciplina(${disciplina.id}, '${disciplina.status}')" 
                            title="${statusClass === 'ativo' ? 'Desativar' : 'Reativar'}">
                        ${statusClass === 'ativo' ? 'Desativar' : 'Reativar'}
                    </button>
                    <button class="btn-excluir" onclick="excluirDisciplina(${disciplina.id})" title="Excluir">
                        Excluir
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
    
    tbody.innerHTML = html;
}

function criarPeriodoLetivo() {
    alert('Criar período letivo\n\nEm desenvolvimento...');
}

function criarDisciplina() {
    // Limpa o formulário
    document.getElementById('form-criar-disciplina').reset();
    document.getElementById('pre-requisitos-selecionados').innerHTML = '';
    preRequisitosSelecionados = [];
    
    // Carrega a lista de disciplinas para pré-requisitos
    carregarListaDisciplinas();
    
    // Abre o modal
    document.getElementById('modal-criar-disciplina').style.display = 'flex';
}

function fecharModalCriarDisciplina() {
    document.getElementById('modal-criar-disciplina').style.display = 'none';
}

let preRequisitosSelecionados = [];

function carregarListaDisciplinas() {
    const datalist = document.getElementById('lista-disciplinas');
    datalist.innerHTML = '';
    
    todasDisciplinas.forEach(disciplina => {
        const option = document.createElement('option');
        option.value = disciplina.nome;
        option.setAttribute('data-id', disciplina.id);
        datalist.appendChild(option);
    });
}

// Adiciona evento ao input de pré-requisitos
document.addEventListener('DOMContentLoaded', () => {
    const inputPreRequisitos = document.getElementById('input-pre-requisitos');
    if (inputPreRequisitos) {
        inputPreRequisitos.addEventListener('change', function() {
            const disciplinaNome = this.value;
            const disciplina = todasDisciplinas.find(d => d.nome === disciplinaNome);
            
            if (disciplina && !preRequisitosSelecionados.find(p => p.id === disciplina.id)) {
                preRequisitosSelecionados.push(disciplina);
                exibirPreRequisitosSelecionados();
                this.value = '';
            }
        });
    }
});

function exibirPreRequisitosSelecionados() {
    const container = document.getElementById('pre-requisitos-selecionados');
    container.innerHTML = '';
    
    preRequisitosSelecionados.forEach(disciplina => {
        const tag = document.createElement('div');
        tag.className = 'tag-item';
        tag.innerHTML = `
            <span>${disciplina.nome}</span>
            <button type="button" onclick="removerPreRequisito(${disciplina.id})">&times;</button>
        `;
        container.appendChild(tag);
    });
}

function removerPreRequisito(disciplinaId) {
    preRequisitosSelecionados = preRequisitosSelecionados.filter(d => d.id !== disciplinaId);
    exibirPreRequisitosSelecionados();
}

async function salvarDisciplina(event) {
    event.preventDefault();
    
    const nome = document.getElementById('input-nome-disciplina').value.trim();
    const periodo = document.getElementById('input-periodo').value;
    const preRequisitosIds = preRequisitosSelecionados.map(d => d.id);
    
    const disciplinaData = {
        nome: nome,
        periodo: periodo,
        preRequisitos: preRequisitosIds
    };
    
    try {
        const response = await fetch('http://localhost:8080/api/admin/disciplinas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(disciplinaData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao criar disciplina');
        }
        
        alert('Disciplina criada com sucesso!');
        fecharModalCriarDisciplina();
        
        // Recarrega a lista
        carregarDisciplinas();
    } catch (error) {
        console.error('Erro ao criar disciplina:', error);
        alert(`Erro ao criar disciplina: ${error.message}`);
    }
}

function editarDisciplina(disciplinaId) {
    alert(`Editar disciplina ID: ${disciplinaId}\n\nEm desenvolvimento...`);
}

function gerenciarSalas(disciplinaId) {
    alert(`Gerenciar salas da disciplina ID: ${disciplinaId}\n\nEm desenvolvimento...`);
}

async function toggleStatusDisciplina(disciplinaId, statusAtual) {
    const acao = statusAtual === 'ATIVO' ? 'desativar' : 'ativar';
    const acaoTexto = statusAtual === 'ATIVO' ? 'desativar' : 'reativar';
    
    if (!confirm(`Deseja realmente ${acaoTexto} esta disciplina?`)) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/disciplinas/${disciplinaId}/${acao}`, {
            method: 'PATCH'
        });
        
        if (!response.ok) {
            throw new Error(`Erro ao ${acaoTexto} disciplina`);
        }
        
        alert(`Disciplina ${acaoTexto === 'desativar' ? 'desativada' : 'reativada'} com sucesso!`);
        carregarDisciplinas();
    } catch (error) {
        console.error(`Erro ao ${acaoTexto} disciplina:`, error);
        alert(`Erro ao ${acaoTexto} disciplina. Tente novamente.`);
    }
}

async function excluirDisciplina(disciplinaId) {
    if (!confirm('Deseja realmente excluir esta disciplina?\n\nEsta ação não pode ser desfeita.')) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/disciplinas/${disciplinaId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Erro ao excluir disciplina');
        }
        
        alert('Disciplina excluída com sucesso!');
        carregarDisciplinas();
    } catch (error) {
        console.error('Erro ao excluir disciplina:', error);
        alert('Erro ao excluir disciplina. Tente novamente.');
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
