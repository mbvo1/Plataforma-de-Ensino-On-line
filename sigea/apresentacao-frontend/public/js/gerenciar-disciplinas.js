// Array para armazenar todas as disciplinas
let todasDisciplinas = [];
let periodoAtual = null;

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

// Verifica autenticaÃ§Ã£o ao carregar a pÃ¡gina
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = 'login-admin.html';
        return;
    }
    
    // Fecha todos os modais ao carregar
    const modalCriarDisciplina = document.getElementById('modal-criar-disciplina');
    const modalEditarDisciplina = document.getElementById('modal-editar-disciplina');
    const modalCriarPeriodo = document.getElementById('modal-criar-periodo');
    if (modalCriarDisciplina) modalCriarDisciplina.style.display = 'none';
    if (modalEditarDisciplina) modalEditarDisciplina.style.display = 'none';
    if (modalCriarPeriodo) modalCriarPeriodo.style.display = 'none';
    
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
        const response = await fetch('/api/admin/periodos/atual', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar perÃ­odo atual');
        }
        
        periodoAtual = await response.json();
        exibirPeriodoAtual(periodoAtual);
    } catch (error) {
        console.error('Erro ao carregar perÃ­odo atual:', error);
        // Define um perÃ­odo padrÃ£o caso nÃ£o haja no banco
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
        const response = await fetch('/api/admin/disciplinas', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

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
        // Em caso de erro de conexÃ£o, mostra mensagem vazia
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
            <td>${escapeHtml(disciplina.nome)}</td>
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
    console.log('criarPeriodoLetivo() chamada');
    // Abre o modal de criar perÃ­odo letivo usando classe
    const modal = document.getElementById('modal-criar-periodo');
    modal.classList.add('show');
    
    // Limpa o formulÃ¡rio
    document.getElementById('form-criar-periodo').reset();
    console.log('Modal perÃ­odo aberto');
}

function fecharModalPeriodo() {
    document.getElementById('modal-criar-periodo').classList.remove('show');
}

function confirmarCriarPeriodo(event) {
    event.preventDefault();
    
    const nome = document.getElementById('nome-periodo').value;
    const dataInicio = document.getElementById('data-inicio-periodo').value;
    const dataFim = document.getElementById('data-fim-periodo').value;
    const inscricaoInicio = document.getElementById('inscricao-inicio-periodo').value;
    const inscricaoFim = document.getElementById('inscricao-fim-periodo').value;
    
    // ValidaÃ§Ãµes
    if (!nome || !dataInicio || !dataFim || !inscricaoInicio || !inscricaoFim) {
        alert('Por favor, preencha todos os campos.');
        return;
    }
    
    // Mensagem de confirmaÃ§Ã£o
    const mensagem = `ATENÃ‡ÃƒO!\n\nAo criar o perÃ­odo letivo "${nome}", o perÃ­odo letivo atual serÃ¡ automaticamente ENCERRADO.\n\nTodas as disciplinas do perÃ­odo atual serÃ£o transferidas para o novo perÃ­odo com status INATIVO.\n\nDeseja continuar?`;
    
    if (!confirm(mensagem)) {
        return;
    }
    
    // Dados do novo perÃ­odo
    const novoPeriodo = {
        nome: nome,
        dataInicio: dataInicio,
        dataFim: dataFim,
        inscricaoInicio: inscricaoInicio,
        inscricaoFim: inscricaoFim
    };
    
    // Envia requisiÃ§Ã£o para criar perÃ­odo
    fetch('/api/admin/periodos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify(novoPeriodo)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.erro || 'Erro ao criar perÃ­odo letivo');
            });
        }
        return response.json();
    })
    .then(data => {
        const mensagemSucesso = data.mensagem || `PerÃ­odo letivo "${data.nome}" criado com sucesso!`;
        alert(mensagemSucesso);
        fecharModalPeriodo();
        carregarPeriodoAtual(); // Atualiza o perÃ­odo exibido
        carregarDisciplinas(); // Recarrega a lista (estarÃ¡ vazia)
    })
    .catch(error => {
        console.error('Erro:', error);
        alert(error.message);
    });
}

function criarDisciplina() {
    console.log('criarDisciplina() chamada');
    // Limpa o formulÃ¡rio
    document.getElementById('form-criar-disciplina').reset();
    document.getElementById('pre-requisitos-selecionados').innerHTML = '';
    preRequisitosSelecionados = [];
    
    // Carrega a lista de disciplinas para prÃ©-requisitos
    carregarListaDisciplinas();
    
    // Abre o modal usando classe
    document.getElementById('modal-criar-disciplina').classList.add('show');
    console.log('Modal disciplina aberto');
}

function fecharModalCriarDisciplina() {
    document.getElementById('modal-criar-disciplina').classList.remove('show');
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

// Adiciona evento ao input de prÃ©-requisitos
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
    
    // Evento para o input de prÃ©-requisitos na ediÃ§Ã£o
    const editPreRequisitos = document.getElementById('edit-pre-requisitos');
    if (editPreRequisitos) {
        editPreRequisitos.addEventListener('change', function() {
            const disciplinaNome = this.value;
            const disciplina = todasDisciplinas.find(d => d.nome === disciplinaNome);
            
            if (disciplina && !preRequisitosEditados.find(p => p.id === disciplina.id)) {
                preRequisitosEditados.push(disciplina);
                exibirPreRequisitosEditados();
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
            <span>${escapeHtml(disciplina.nome)}</span>
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
        const response = await fetch('/api/admin/disciplinas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
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

let preRequisitosEditados = [];

async function editarDisciplina(disciplinaId) {
    try {
        // Busca os dados da disciplina
        const response = await fetch(`/api/admin/disciplinas/${disciplinaId}`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar dados da disciplina');
        }
        
        const disciplina = await response.json();
        
        // Preenche o formulÃ¡rio
        document.getElementById('edit-disciplina-id').value = disciplina.id;
        document.getElementById('edit-nome-disciplina').value = disciplina.nome;
        document.getElementById('edit-periodo').value = disciplina.periodo;
        
        // Carrega prÃ©-requisitos
        preRequisitosEditados = disciplina.preRequisitosIds.map(id => {
            return todasDisciplinas.find(d => d.id === id);
        }).filter(d => d !== undefined);
        
        exibirPreRequisitosEditados();
        carregarListaDisciplinasEdit();
        
        // Abre o modal
        const modal = document.getElementById('modal-editar-disciplina');
        if (modal) {
            modal.classList.add('show');
        }
    } catch (error) {
        console.error('Erro ao editar disciplina:', error);
        alert('Erro ao carregar dados da disciplina. Tente novamente.');
    }
}

function fecharModalEditarDisciplina() {
    const modal = document.getElementById('modal-editar-disciplina');
    if (modal) {
        modal.classList.remove('show');
    }
    preRequisitosEditados = [];
}

function carregarListaDisciplinasEdit() {
    const datalist = document.getElementById('lista-disciplinas-edit');
    datalist.innerHTML = '';
    
    const disciplinaIdEditando = parseInt(document.getElementById('edit-disciplina-id').value);
    
    todasDisciplinas
        .filter(d => d.id !== disciplinaIdEditando)
        .forEach(disciplina => {
            const option = document.createElement('option');
            option.value = disciplina.nome;
            option.setAttribute('data-id', disciplina.id);
            datalist.appendChild(option);
        });
}

function exibirPreRequisitosEditados() {
    const container = document.getElementById('edit-pre-requisitos-selecionados');
    container.innerHTML = '';
    
    if (preRequisitosEditados.length === 0) {
        container.innerHTML = '<p class="empty-message">Nenhum</p>';
        return;
    }
    
    preRequisitosEditados.forEach(disciplina => {
        const tag = document.createElement('div');
        tag.className = 'tag-item';
        tag.innerHTML = `
            <span>${escapeHtml(disciplina.nome)}</span>
            <button type="button" onclick="removerPreRequisitoEdit(${disciplina.id})">&times;</button>
        `;
        container.appendChild(tag);
    });
}

function removerPreRequisitoEdit(disciplinaId) {
    preRequisitosEditados = preRequisitosEditados.filter(d => d.id !== disciplinaId);
    exibirPreRequisitosEditados();
}

async function atualizarDisciplina(event) {
    event.preventDefault();
    
    const disciplinaId = document.getElementById('edit-disciplina-id').value;
    const nome = document.getElementById('edit-nome-disciplina').value.trim();
    const periodo = document.getElementById('edit-periodo').value;
    const preRequisitosIds = preRequisitosEditados.map(d => d.id);
    
    const disciplinaData = {
        nome: nome,
        periodo: periodo,
        preRequisitos: preRequisitosIds
    };
    
    try {
        const response = await fetch(`/api/admin/disciplinas/${disciplinaId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify(disciplinaData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erro ao atualizar disciplina');
        }
        
        alert('Disciplina atualizada com sucesso!');
        fecharModalEditarDisciplina();
        carregarDisciplinas();
    } catch (error) {
        console.error('Erro ao atualizar disciplina:', error);
        alert(`Erro ao atualizar disciplina: ${error.message}`);
    }
}

function gerenciarSalas(disciplinaId) {
    window.location.href = `gerenciar-salas.html?disciplinaId=${disciplinaId}`;
}

async function toggleStatusDisciplina(disciplinaId, statusAtual) {
    const acao = statusAtual === 'ATIVO' ? 'desativar' : 'ativar';
    const acaoTexto = statusAtual === 'ATIVO' ? 'desativar' : 'reativar';
    
    if (!confirm(`Deseja realmente ${acaoTexto} esta disciplina?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/disciplinas/${disciplinaId}/${acao}`, {
            method: 'PATCH',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
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
    if (!confirm('Deseja realmente excluir esta disciplina?\n\nEsta aÃ§Ã£o nÃ£o pode ser desfeita.')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/disciplinas/${disciplinaId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao excluir disciplina');
        }
        
        alert('Disciplina excluÃ­da com sucesso!');
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

// ========== SELETOR DE PERÃODOS ==========

let periodoSelecionadoId = null;

async function toggleSelectorPeriodo() {
    const selector = document.getElementById('selector-periodos');
    
    if (selector.style.display === 'none' || selector.style.display === '') {
        await carregarTodosPeriodos();
        selector.style.display = 'block';
    } else {
        selector.style.display = 'none';
    }
}

function fecharSelectorPeriodo() {
    document.getElementById('selector-periodos').style.display = 'none';
}

async function carregarTodosPeriodos() {
    try {
        const response = await fetch('/api/admin/periodos', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar perÃ­odos');
        }
        
        const periodos = await response.json();
        exibirListaPeriodos(periodos);
    } catch (error) {
        console.error('Erro ao carregar perÃ­odos:', error);
        alert('Erro ao carregar lista de perÃ­odos.');
    }
}

function exibirListaPeriodos(periodos) {
    const lista = document.getElementById('lista-periodos');
    
    if (!periodos || periodos.length === 0) {
        lista.innerHTML = '<p class="empty-message">Nenhum perÃ­odo cadastrado</p>';
        return;
    }
    
    const html = periodos.map(periodo => {
        const statusClass = periodo.status.toLowerCase();
        const statusFormatado = periodo.status === 'ATIVO' ? 'Ativo' : 'Encerrado';
        
        return `
        <div class="periodo-item" onclick="selecionarPeriodo(${periodo.id})">
            <div class="periodo-item-header">
                <span class="periodo-item-nome">${escapeHtml(periodo.nome)}</span>
                <span class="periodo-status ${statusClass}">${statusFormatado}</span>
            </div>
            <div class="periodo-item-datas">
                ${periodo.dataInicio ? `<small>InÃ­cio: ${formatarData(periodo.dataInicio)} | Fim: ${formatarData(periodo.dataFim)}</small>` : ''}
            </div>
        </div>
        `;
    }).join('');
    
    lista.innerHTML = html;
}

async function selecionarPeriodo(periodoId) {
    periodoSelecionadoId = periodoId;
    
    try {
        const response = await fetch(`/api/admin/periodos/${periodoId}/disciplinas`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar disciplinas do perÃ­odo');
        }

        const disciplinas = await response.json();
        todasDisciplinas = disciplinas;
        exibirDisciplinas(disciplinas);

        // Atualiza indicador visual do perÃ­odo selecionado
        const responsePeriodo = await fetch('/api/admin/periodos', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        const periodos = await responsePeriodo.json();
        const periodoSelecionado = periodos.find(p => p.id === periodoId);
        
        if (periodoSelecionado) {
            exibirPeriodoAtual(periodoSelecionado);
        }
        
        fecharSelectorPeriodo();
    } catch (error) {
        console.error('Erro ao selecionar perÃ­odo:', error);
        alert('Erro ao carregar disciplinas do perÃ­odo.');
    }
}

function formatarData(dataStr) {
    if (!dataStr) return '';
    
    const [ano, mes, dia] = dataStr.split('-');
    return `${dia}/${mes}/${ano}`;
}
