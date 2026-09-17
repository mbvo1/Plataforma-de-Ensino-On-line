// Estado global
let matriculas = [];

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

// FunÃ§Ã£o para limpar dados do usuÃ¡rio
function limparDadosUsuario() {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioEmail');
    localStorage.removeItem('usuarioPerfil');
}

// Verifica se o login Ã© vÃ¡lido
function isLoginValido() {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    return usuarioId && 
           usuarioPerfil && 
           usuarioId !== 'null' && 
           usuarioId !== 'undefined' && 
           usuarioId.trim() !== '' &&
           usuarioPerfil !== 'null' && 
           usuarioPerfil !== 'undefined' &&
           usuarioPerfil.trim() !== '' &&
           usuarioPerfil === 'ALUNO';
}

// Verifica autenticaÃ§Ã£o ao carregar a pÃ¡gina
window.addEventListener('DOMContentLoaded', () => {
    if (!isLoginValido()) {
        limparDadosUsuario();
        window.location.href = '/index.html';
        return;
    }
    
    loadUserInfo();
    initializeMenuToggle();
    verificarPeriodoInscricao();
    carregarMatriculas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'UsuÃ¡rio'}`;
    }
}

function initializeMenuToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
}

// Carregar matrÃ­culas do aluno
async function carregarMatriculas() {
    const usuarioId = localStorage.getItem('usuarioId');
    const subtitulo = document.getElementById('subtitulo');
    const container = document.getElementById('disciplinasContainer');
    
    try {
        const response = await fetch(`/api/aluno/${usuarioId}/matriculas`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        
        if (!response.ok) {
            throw new Error('Erro ao carregar matrÃ­culas');
        }
        
        matriculas = await response.json();
        renderizarMatriculas();
        
    } catch (error) {
        console.error('Erro ao carregar matrÃ­culas:', error);
        subtitulo.textContent = 'Erro ao carregar dados';
        container.innerHTML = '<p class="mensagem-vazia">Erro ao carregar matrÃ­culas.</p>';
    }
}

// Renderizar lista de matrÃ­culas
function renderizarMatriculas() {
    const container = document.getElementById('disciplinasContainer');
    const subtitulo = document.getElementById('subtitulo');
    
    if (matriculas.length === 0) {
        // SÃ³ atualiza o subtÃ­tulo se nÃ£o houver mensagem de perÃ­odo de inscriÃ§Ã£o
        if (!subtitulo.textContent.includes('perÃ­odo de inscriÃ§Ã£o')) {
            subtitulo.textContent = 'Nenhuma disciplina matriculada';
        }
        container.innerHTML = '<p class="mensagem-vazia">VocÃª ainda nÃ£o estÃ¡ matriculado em nenhuma disciplina. Clique em "Matricular disciplina" para se matricular.</p>';
        return;
    }
    
    // SÃ³ limpa o subtÃ­tulo se nÃ£o houver mensagem de perÃ­odo de inscriÃ§Ã£o
    if (!subtitulo.textContent.includes('perÃ­odo de inscriÃ§Ã£o')) {
        subtitulo.textContent = '';
    }
    
    // Atualizar botÃ£o para "Editar MatrÃ­cula"
    const btnMatricula = document.querySelector('.btn-matricular');
    if (btnMatricula) {
        btnMatricula.textContent = 'Editar MatrÃ­cula';
    }
    
    container.innerHTML = matriculas.map(matricula => {
        const horario = matricula.horario || '';
        const disciplinaNome = matricula.disciplinaNome || 'Disciplina';
        const salaIdentificador = matricula.salaIdentificador || 'Turma';
        const professorNome = matricula.professorNome || 'Nome';
        
        // Parsear dias e horÃ¡rio
        const diasHorario = parseDiasHorario(horario);
        
        return `
            <div class="disciplina-card">
                <div class="disciplina-titulo">${escapeHtml(disciplinaNome)} - ${salaIdentificador}</div>
                <div class="disciplina-info">
                    <div class="disciplina-detalhe">Dias: ${diasHorario.diasTexto}</div>
                    <div class="disciplina-detalhe">HorÃ¡rio: ${diasHorario.horario}</div>
                    <div class="disciplina-detalhe">Professor: "${escapeHtml(professorNome)}"</div>
                </div>
            </div>
        `;
    }).join('');
}

// Parsear dias e horÃ¡rio do formato "SEG,QUA 08:15-10:15"
function parseDiasHorario(horarioStr) {
    if (!horarioStr) return { diasTexto: 'NÃ£o definido', horario: 'NÃ£o definido' };
    
    const partes = horarioStr.split(' ');
    if (partes.length < 2) return { diasTexto: horarioStr, horario: '' };
    
    const dias = partes[0];
    const horario = partes[1];
    
    // Mapear abreviaÃ§Ãµes para nomes completos
    const diasMap = {
        'SEG': 'Segunda',
        'TER': 'TerÃ§a',
        'QUA': 'Quarta',
        'QUI': 'Quinta',
        'SEX': 'Sexta',
        'SAB': 'SÃ¡bado'
    };
    
    const diasArray = dias.split(',');
    const diasNomes = diasArray.map(d => diasMap[d.trim()] || d.trim());
    
    let diasTexto;
    if (diasNomes.length === 1) {
        diasTexto = diasNomes[0];
    } else if (diasNomes.length === 2) {
        diasTexto = diasNomes.join(' e ');
    } else {
        diasTexto = diasNomes.slice(0, -1).join(', ') + ' e ' + diasNomes[diasNomes.length - 1];
    }
    
    // Formatar horÃ¡rio de "08:15-10:15" para "8:15 - 10:15"
    const horarioFormatado = horario.replace('-', ' - ');
    
    return { diasTexto, horario: horarioFormatado };
}

// Cancelar matrÃ­cula
async function cancelarMatricula(matriculaId) {
    if (!confirm('Tem certeza que deseja cancelar esta matrÃ­cula?')) {
        return;
    }
    
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`/api/aluno/${usuarioId}/matriculas/${matriculaId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (response.ok) {
            alert('MatrÃ­cula cancelada com sucesso!');
            carregarMatriculas();
        } else {
            alert('Erro ao cancelar matrÃ­cula');
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao cancelar matrÃ­cula');
    }
}

// Verificar se estÃ¡ no perÃ­odo de inscriÃ§Ã£o
async function verificarPeriodoInscricao() {
    try {
        const response = await fetch('/api/admin/periodos/atual', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        if (!response.ok) {
            console.error('Erro ao verificar perÃ­odo de inscriÃ§Ã£o');
            return;
        }
        
        const periodo = await response.json();
        const hoje = new Date();
        hoje.setHours(0, 0, 0, 0);
        
        if (periodo.dataInicioInscricao && periodo.dataFimInscricao) {
            const inicioInscricao = new Date(periodo.dataInicioInscricao);
            const fimInscricao = new Date(periodo.dataFimInscricao);
            inicioInscricao.setHours(0, 0, 0, 0);
            fimInscricao.setHours(23, 59, 59, 999);
            
            if (hoje < inicioInscricao || hoje > fimInscricao) {
                const btnMatricular = document.querySelector('.btn-matricular');
                if (btnMatricular) {
                    btnMatricular.disabled = true;
                    btnMatricular.style.opacity = '0.5';
                    btnMatricular.style.cursor = 'not-allowed';
                    
                    const subtitulo = document.getElementById('subtitulo');
                    if (subtitulo) {
                        const dataInicio = inicioInscricao.toLocaleDateString('pt-BR');
                        const dataFim = fimInscricao.toLocaleDateString('pt-BR');
                        subtitulo.textContent = `NÃ£o estÃ¡ no perÃ­odo de inscriÃ§Ã£o. O perÃ­odo de inscriÃ§Ã£o Ã© de ${dataInicio} atÃ© ${dataFim}.`;
                        subtitulo.style.color = '#e74c3c';
                        subtitulo.style.fontWeight = 'bold';
                    }
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar perÃ­odo de inscriÃ§Ã£o:', error);
    }
}

// Abrir pÃ¡gina de matrÃ­cula
async function abrirMatricula() {
    // Verificar perÃ­odo de inscriÃ§Ã£o antes de redirecionar
    try {
        const response = await fetch('/api/admin/periodos/atual', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        if (response.ok) {
            const periodo = await response.json();
            const hoje = new Date();
            hoje.setHours(0, 0, 0, 0);
            
            if (periodo.dataInicioInscricao && periodo.dataFimInscricao) {
                const inicioInscricao = new Date(periodo.dataInicioInscricao);
                const fimInscricao = new Date(periodo.dataFimInscricao);
                inicioInscricao.setHours(0, 0, 0, 0);
                fimInscricao.setHours(23, 59, 59, 999);
                
                if (hoje < inicioInscricao || hoje > fimInscricao) {
                    const dataInicio = inicioInscricao.toLocaleDateString('pt-BR');
                    const dataFim = fimInscricao.toLocaleDateString('pt-BR');
                    alert(`NÃ£o estÃ¡ no perÃ­odo de inscriÃ§Ã£o. O perÃ­odo de inscriÃ§Ã£o Ã© de ${dataInicio} atÃ© ${dataFim}.`);
                    return;
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar perÃ­odo de inscriÃ§Ã£o:', error);
    }
    
    // Salvar matrÃ­culas atuais para prÃ©-preencher na pÃ¡gina de ediÃ§Ã£o
    localStorage.setItem('matriculasAtuais', JSON.stringify(matriculas));
    window.location.href = 'matricula-detalhes-aluno.html';
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
