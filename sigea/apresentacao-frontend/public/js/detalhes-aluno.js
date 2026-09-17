let alunoAtual = null;

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
    
    loadUserInfo();
    
    // Pega o ID do aluno da URL
    const urlParams = new URLSearchParams(window.location.search);
    const alunoId = urlParams.get('id');
    
    if (!alunoId) {
        alert('ID do aluno nÃ£o encontrado');
        voltarParaLista();
        return;
    }
    
    carregarDetalhesAluno(alunoId);
    carregarHistoricoDisciplinas(alunoId);
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'Administrador'}`;
    }
}

async function carregarDetalhesAluno(alunoId) {
    try {
        const response = await fetch(`/api/admin/alunos/${alunoId}`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar detalhes do aluno');
        }
        
        alunoAtual = await response.json();
        exibirDetalhesAluno(alunoAtual);
    } catch (error) {
        console.error('Erro ao carregar detalhes do aluno:', error);
        const tbody = document.getElementById('info-aluno');
        tbody.innerHTML = '<tr><td colspan="3" class="error-message">Erro ao carregar informaÃ§Ãµes do aluno</td></tr>';
    }
}

function exibirDetalhesAluno(aluno) {
    const tbody = document.getElementById('info-aluno');
    
    // Formata o status para exibiÃ§Ã£o
    const statusFormatado = aluno.status.charAt(0) + aluno.status.slice(1).toLowerCase();
    const statusClass = aluno.status.toLowerCase();
    
    tbody.innerHTML = `
        <tr>
            <td>${escapeHtml(aluno.nome)}</td>
            <td>${aluno.email}</td>
            <td>
                <span class="status-badge ${statusClass}">${statusFormatado}</span>
            </td>
        </tr>
    `;
    
    // Atualiza o botÃ£o de desativar/ativar
    atualizarBotaoStatus(aluno.status);
}

function atualizarBotaoStatus(status) {
    const btn = document.getElementById('btn-desativar');
    
    if (status === 'ATIVO') {
        btn.textContent = 'Desativar';
        btn.className = 'btn-desativar';
        btn.onclick = desativarAluno;
    } else {
        btn.textContent = 'Ativar';
        btn.className = 'btn-ativar';
        btn.onclick = ativarAluno;
    }
}

async function carregarHistoricoDisciplinas(alunoId) {
    try {
        // Busca dados reais do banco de dados
        const response = await fetch(`/api/admin/alunos/${alunoId}/historico`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar histÃ³rico de disciplinas');
        }
        
        const historico = await response.json();
        exibirHistoricoDisciplinas(historico);
    } catch (error) {
        console.error('Erro ao carregar histÃ³rico:', error);
        const tbody = document.getElementById('historico-disciplinas');
        tbody.innerHTML = '<tr><td colspan="4" class="error-message">Erro ao carregar histÃ³rico de disciplinas</td></tr>';
    }
}

function exibirHistoricoDisciplinas(historico) {
    const tbody = document.getElementById('historico-disciplinas');
    
    if (!historico || historico.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-message">Nenhuma disciplina encontrada</td></tr>';
        return;
    }
    
    const html = historico.map(item => {
        // Normaliza o nome da classe CSS (remove espaÃ§os e converte para minÃºsculas)
        let statusClass = item.status.toLowerCase().replace(/\s+/g, '-');
        
        // Garante que os status conhecidos tenham as classes corretas
        if (item.status === 'Aprovado') {
            statusClass = 'aprovado';
        } else if (item.status === 'Reprovado') {
            statusClass = 'reprovado';
        } else if (item.status === 'Cursando') {
            statusClass = 'cursando';
        } else if (item.status === 'Cancelada') {
            statusClass = 'cancelada';
        }
        
        return `
            <tr>
                <td>${escapeHtml(item.nomeDisciplina || item.disciplina)}</td>
                <td>${item.periodoLetivo}</td>
                <td>
                    <span class="status-badge ${statusClass}">${item.status}</span>
                </td>
                <td>
                    ${item.status === 'Cursando' ? `<button class="btn-cancelar-matricula" onclick="cancelarMatricula(${item.id})">Cancelar MatrÃ­cula</button>` : '-'}
                </td>
            </tr>
        `;
    }).join('');
    
    tbody.innerHTML = html;
}

async function cancelarMatricula(matriculaId) {
    if (!confirm('Deseja realmente cancelar a matrÃ­cula nesta disciplina?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/admin/matriculas/${matriculaId}/cancelar`, {
            method: 'PATCH',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao cancelar matrÃ­cula');
        }
        
        alert('MatrÃ­cula cancelada com sucesso!');
        
        // Recarrega o histÃ³rico
        const urlParams = new URLSearchParams(window.location.search);
        const alunoId = urlParams.get('id');
        carregarHistoricoDisciplinas(alunoId);
    } catch (error) {
        console.error('Erro ao cancelar matrÃ­cula:', error);
        alert('Erro ao cancelar matrÃ­cula. Tente novamente.');
    }
}

async function desativarAluno() {
    if (!alunoAtual) return;
    
    const confirmacao = confirm(`Deseja realmente desativar o aluno ${alunoAtual.nome}?`);
    
    if (!confirmacao) return;
    
    try {
        const response = await fetch(`/api/admin/alunos/${alunoAtual.id}/desativar`, {
            method: 'PATCH',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao desativar aluno');
        }
        
        alert('Aluno desativado com sucesso!');
        
        // Atualiza o status local
        alunoAtual.status = 'INATIVO';
        exibirDetalhesAluno(alunoAtual);
    } catch (error) {
        console.error('Erro ao desativar aluno:', error);
        alert('Erro ao desativar aluno. Tente novamente.');
    }
}

async function ativarAluno() {
    if (!alunoAtual) return;
    
    const confirmacao = confirm(`Deseja realmente ativar o aluno ${alunoAtual.nome}?`);
    
    if (!confirmacao) return;
    
    try {
        const response = await fetch(`/api/admin/alunos/${alunoAtual.id}/ativar`, {
            method: 'PATCH',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao ativar aluno');
        }
        
        alert('Aluno ativado com sucesso!');
        
        // Atualiza o status local
        alunoAtual.status = 'ATIVO';
        exibirDetalhesAluno(alunoAtual);
    } catch (error) {
        console.error('Erro ao ativar aluno:', error);
        alert('Erro ao ativar aluno. Tente novamente.');
    }
}

function voltarParaLista() {
    window.location.href = 'gerenciar-alunos.html';
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

