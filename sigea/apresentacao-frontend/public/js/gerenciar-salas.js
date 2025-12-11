// Variáveis globais
let disciplinaId = null;
let disciplinaNome = '';

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = 'login-admin.html';
        return;
    }
    
    // Pega o ID da disciplina da URL
    const urlParams = new URLSearchParams(window.location.search);
    disciplinaId = urlParams.get('disciplinaId');
    
    if (!disciplinaId) {
        alert('Disciplina não especificada');
        window.location.href = 'gerenciar-disciplinas.html';
        return;
    }
    
    loadUserInfo();
    carregarDisciplina();
    carregarSalas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'Usuário'}`;
    }
}

async function carregarDisciplina() {
    try {
        const response = await fetch(`http://localhost:8080/api/admin/disciplinas/${disciplinaId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar disciplina');
        }
        
        const disciplina = await response.json();
        disciplinaNome = disciplina.nome;
        
        document.getElementById('nome-disciplina').textContent = disciplina.nome;
    } catch (error) {
        console.error('Erro ao carregar disciplina:', error);
        document.getElementById('nome-disciplina').textContent = 'Disciplina';
    }
}

async function carregarSalas() {
    try {
        const response = await fetch(`http://localhost:8080/api/admin/disciplinas/${disciplinaId}/salas`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar salas');
        }
        
        const salas = await response.json();
        exibirSalas(salas);
    } catch (error) {
        console.error('Erro ao carregar salas:', error);
        exibirSalas([]);
    }
}

function exibirSalas(salas) {
    const tbody = document.getElementById('tabela-salas');
    
    if (!salas || salas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-message">Nenhuma sala cadastrada</td></tr>';
        return;
    }
    
    const html = salas.map(sala => {
        const statusClass = sala.status.toLowerCase();
        const statusFormatado = sala.status.charAt(0) + sala.status.slice(1).toLowerCase();
        
        // Formata dias da semana
        const diasMap = {
            'SEG': 'Seg',
            'TER': 'Ter',
            'QUA': 'Qua',
            'QUI': 'Qui',
            'SEX': 'Sex',
            'SAB': 'Sáb',
            'DOM': 'Dom'
        };
        const diasFormatados = sala.diasSemana.map(d => diasMap[d] || d).join(', ');
        
        // Formata horários
        const horario = `${sala.horarioInicio} - ${sala.horarioFim}`;
        
        return `
        <tr>
            <td>${sala.identificador}</td>
            <td>${sala.professorNome}</td>
            <td>${diasFormatados}</td>
            <td>${horario}</td>
            <td>${sala.vagasOcupadas} / ${sala.limiteVagas}</td>
            <td>
                <span class="status-badge ${statusClass}">${statusFormatado}</span>
            </td>
            <td>
                <div class="action-buttons-grid">
                    <button class="btn-editar-small" onclick="editarSala(${sala.id})" title="Editar sala">
                        Editar sala
                    </button>
                    <button class="btn-desativar-small ${statusClass === 'inativo' ? 'btn-reativar' : ''}" 
                            onclick="toggleStatusSala(${sala.id}, '${sala.status}')" 
                            title="${statusClass === 'ativo' ? 'Desativar' : 'Reativar'}">
                        ${statusClass === 'ativo' ? 'Desativar' : 'Reativar'}
                    </button>
                    <button class="btn-excluir" onclick="excluirSala(${sala.id})" title="Excluir">
                        Excluir
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
    
    tbody.innerHTML = html;
}

function voltarParaDisciplinas() {
    window.location.href = 'gerenciar-disciplinas.html';
}

async function adicionarNovaSala() {
    console.log('adicionarNovaSala() chamada');
    
    // Carrega lista de professores
    await carregarProfessores();
    
    // Preenche nome da disciplina no modal
    document.getElementById('disciplina-modal-nome').textContent = disciplinaNome;
    
    // Limpa o formulário
    document.getElementById('form-adicionar-sala').reset();
    
    // Abre o modal
    const modal = document.getElementById('modal-adicionar-sala');
    if (modal) {
        modal.classList.add('show');
        console.log('Modal adicionar sala aberto');
    }
}

function fecharModalAdicionarSala() {
    const modal = document.getElementById('modal-adicionar-sala');
    if (modal) {
        modal.classList.remove('show');
    }
}

async function carregarProfessores() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/professores');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar professores');
        }
        
        const professores = await response.json();
        const select = document.getElementById('input-professor');
        
        // Limpa opções anteriores (mantém apenas a primeira)
        select.innerHTML = '<option value="">Selecione o professor responsável</option>';
        
        // Adiciona apenas professores com status ATIVO
        professores.filter(p => p.status === 'ATIVO').forEach(professor => {
            const option = document.createElement('option');
            option.value = professor.id;
            option.textContent = professor.nome;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Erro ao carregar professores:', error);
        alert('Erro ao carregar lista de professores');
    }
}

async function salvarSala(event) {
    event.preventDefault();
    
    const identificador = document.getElementById('input-identificador-sala').value.trim();
    const professorId = document.getElementById('input-professor').value;
    const horarioInicio = document.getElementById('input-horario-inicio').value;
    const horarioFim = document.getElementById('input-horario-fim').value;
    const vagas = parseInt(document.getElementById('input-vagas').value);
    
    // Validações
    if (!identificador) {
        alert('Preencha o identificador da sala');
        return;
    }
    
    if (!professorId) {
        alert('Selecione um professor');
        return;
    }
    
    if (!horarioInicio || !horarioFim) {
        alert('Preencha os horários');
        return;
    }
    
    if (!vagas || vagas <= 0) {
        alert('Informe um número válido de vagas');
        return;
    }
    
    // Pega os dias selecionados
    const diasCheckboxes = document.querySelectorAll('input[name="dia-semana"]:checked');
    if (diasCheckboxes.length === 0) {
        alert('Selecione pelo menos um dia da semana');
        return;
    }
    
    const diasSemana = Array.from(diasCheckboxes).map(cb => cb.value);
    
    const salaData = {
        identificador: identificador,
        professorId: parseInt(professorId),
        diasSemana: diasSemana,
        horarioInicio: horarioInicio,
        horarioFim: horarioFim,
        vagas: vagas,
        disciplinaId: parseInt(disciplinaId)
    };
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/disciplinas/${disciplinaId}/salas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(salaData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao criar sala');
        }
        
        alert('Sala criada com sucesso!');
        fecharModalAdicionarSala();
        carregarSalas();
    } catch (error) {
        console.error('Erro ao criar sala:', error);
        alert(`Erro ao criar sala: ${error.message}`);
    }
}

async function editarSala(salaId) {
    try {
        // Busca os dados da sala
        const response = await fetch(`http://localhost:8080/api/admin/salas/${salaId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar dados da sala');
        }
        
        const sala = await response.json();
        
        // Carrega lista de professores
        await carregarProfessoresEdit();
        
        // Preenche o nome da disciplina
        document.getElementById('disciplina-modal-nome-edit').textContent = disciplinaNome;
        
        // Preenche o formulário
        document.getElementById('edit-sala-id').value = sala.id;
        document.getElementById('edit-identificador-sala').value = sala.identificador;
        document.getElementById('edit-professor').value = sala.professorId || '';
        document.getElementById('edit-horario-inicio').value = sala.horarioInicio || '';
        document.getElementById('edit-horario-fim').value = sala.horarioFim || '';
        document.getElementById('edit-vagas').value = sala.limiteVagas || '';
        
        // Marca os dias da semana selecionados
        const checkboxes = document.querySelectorAll('input[name="edit-dia-semana"]');
        checkboxes.forEach(cb => {
            cb.checked = sala.diasSemana && sala.diasSemana.includes(cb.value);
        });
        
        // Abre o modal
        const modal = document.getElementById('modal-editar-sala');
        if (modal) {
            modal.classList.add('show');
        }
    } catch (error) {
        console.error('Erro ao editar sala:', error);
        alert('Erro ao carregar dados da sala. Tente novamente.');
    }
}

function fecharModalEditarSala() {
    const modal = document.getElementById('modal-editar-sala');
    if (modal) {
        modal.classList.remove('show');
    }
}

async function carregarProfessoresEdit() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/professores');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar professores');
        }
        
        const professores = await response.json();
        const select = document.getElementById('edit-professor');
        
        // Limpa opções anteriores (mantém apenas a primeira)
        select.innerHTML = '<option value="">Selecione o professor responsável</option>';
        
        // Adiciona apenas professores com status ATIVO
        professores.filter(p => p.status === 'ATIVO').forEach(professor => {
            const option = document.createElement('option');
            option.value = professor.id;
            option.textContent = professor.nome;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Erro ao carregar professores:', error);
        alert('Erro ao carregar lista de professores');
    }
}

async function atualizarSala(event) {
    event.preventDefault();
    
    const salaId = document.getElementById('edit-sala-id').value;
    const identificador = document.getElementById('edit-identificador-sala').value.trim();
    const professorId = document.getElementById('edit-professor').value;
    const horarioInicio = document.getElementById('edit-horario-inicio').value;
    const horarioFim = document.getElementById('edit-horario-fim').value;
    const vagas = parseInt(document.getElementById('edit-vagas').value);
    
    // Validações
    if (!identificador) {
        alert('Preencha o identificador da sala');
        return;
    }
    
    if (!professorId) {
        alert('Selecione um professor');
        return;
    }
    
    if (!horarioInicio || !horarioFim) {
        alert('Preencha os horários');
        return;
    }
    
    if (!vagas || vagas <= 0) {
        alert('Informe um número válido de vagas');
        return;
    }
    
    // Pega os dias selecionados
    const diasCheckboxes = document.querySelectorAll('input[name="edit-dia-semana"]:checked');
    if (diasCheckboxes.length === 0) {
        alert('Selecione pelo menos um dia da semana');
        return;
    }
    
    const diasSemana = Array.from(diasCheckboxes).map(cb => cb.value);
    
    const salaData = {
        identificador: identificador,
        professorId: parseInt(professorId),
        diasSemana: diasSemana,
        horarioInicio: horarioInicio,
        horarioFim: horarioFim,
        vagas: vagas
    };
    
    try {
        const response = await fetch(`http://localhost:8080/api/admin/salas/${salaId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(salaData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao atualizar sala');
        }
        
        alert('Sala atualizada com sucesso!');
        fecharModalEditarSala();
        carregarSalas();
    } catch (error) {
        console.error('Erro ao atualizar sala:', error);
        alert(`Erro ao atualizar sala: ${error.message}`);
    }
}

function toggleStatusSala(salaId, statusAtual) {
    const acao = statusAtual === 'ATIVO' ? 'desativar' : 'ativar';
    const acaoTexto = statusAtual === 'ATIVO' ? 'desativar' : 'reativar';
    
    if (!confirm(`Deseja realmente ${acaoTexto} esta sala?`)) {
        return;
    }
    
    fetch(`http://localhost:8080/api/admin/salas/${salaId}/${acao}`, {
        method: 'PATCH'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao atualizar status da sala');
        }
        alert(`Sala ${acaoTexto === 'desativar' ? 'desativada' : 'reativada'} com sucesso!`);
        carregarSalas();
    })
    .catch(error => {
        console.error('Erro ao atualizar status:', error);
        alert(`Erro ao ${acaoTexto} sala`);
    });
}

function excluirSala(salaId) {
    if (!confirm('Deseja realmente excluir esta sala?\n\nEsta ação não pode ser desfeita.')) {
        return;
    }
    
    fetch(`http://localhost:8080/api/admin/salas/${salaId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao excluir sala');
        }
        alert('Sala excluída com sucesso!');
        carregarSalas();
    })
    .catch(error => {
        console.error('Erro ao excluir sala:', error);
        alert('Erro ao excluir sala');
    });
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

// Toggle do menu lateral
document.addEventListener('DOMContentLoaded', () => {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
});
