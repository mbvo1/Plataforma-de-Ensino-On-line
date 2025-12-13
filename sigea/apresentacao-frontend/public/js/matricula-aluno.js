// Estado global
let matriculas = [];

// Função para limpar dados do usuário
function limparDadosUsuario() {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioEmail');
    localStorage.removeItem('usuarioPerfil');
}

// Verifica se o login é válido
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

// Verifica autenticação ao carregar a página
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
        userNameElement.textContent = `Aluno - ${nome || 'Usuário'}`;
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

// Carregar matrículas do aluno
async function carregarMatriculas() {
    const usuarioId = localStorage.getItem('usuarioId');
    const subtitulo = document.getElementById('subtitulo');
    const container = document.getElementById('disciplinasContainer');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matriculas`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar matrículas');
        }
        
        matriculas = await response.json();
        renderizarMatriculas();
        
    } catch (error) {
        console.error('Erro ao carregar matrículas:', error);
        subtitulo.textContent = 'Erro ao carregar dados';
        container.innerHTML = '<p class="mensagem-vazia">Erro ao carregar matrículas.</p>';
    }
}

// Renderizar lista de matrículas
function renderizarMatriculas() {
    const container = document.getElementById('disciplinasContainer');
    const subtitulo = document.getElementById('subtitulo');
    
    if (matriculas.length === 0) {
        // Só atualiza o subtítulo se não houver mensagem de período de inscrição
        if (!subtitulo.textContent.includes('período de inscrição')) {
            subtitulo.textContent = 'Nenhuma disciplina matriculada';
        }
        container.innerHTML = '<p class="mensagem-vazia">Você ainda não está matriculado em nenhuma disciplina. Clique em "Matricular disciplina" para se matricular.</p>';
        return;
    }
    
    // Só limpa o subtítulo se não houver mensagem de período de inscrição
    if (!subtitulo.textContent.includes('período de inscrição')) {
        subtitulo.textContent = '';
    }
    
    // Atualizar botão para "Editar Matrícula"
    const btnMatricula = document.querySelector('.btn-matricular');
    if (btnMatricula) {
        btnMatricula.textContent = 'Editar Matrícula';
    }
    
    container.innerHTML = matriculas.map(matricula => {
        const horario = matricula.horario || '';
        const disciplinaNome = matricula.disciplinaNome || 'Disciplina';
        const salaIdentificador = matricula.salaIdentificador || 'Turma';
        const professorNome = matricula.professorNome || 'Nome';
        
        // Parsear dias e horário
        const diasHorario = parseDiasHorario(horario);
        
        return `
            <div class="disciplina-card">
                <div class="disciplina-titulo">${disciplinaNome} - ${salaIdentificador}</div>
                <div class="disciplina-info">
                    <div class="disciplina-detalhe">Dias: ${diasHorario.diasTexto}</div>
                    <div class="disciplina-detalhe">Horário: ${diasHorario.horario}</div>
                    <div class="disciplina-detalhe">Professor: "${professorNome}"</div>
                </div>
            </div>
        `;
    }).join('');
}

// Parsear dias e horário do formato "SEG,QUA 08:15-10:15"
function parseDiasHorario(horarioStr) {
    if (!horarioStr) return { diasTexto: 'Não definido', horario: 'Não definido' };
    
    const partes = horarioStr.split(' ');
    if (partes.length < 2) return { diasTexto: horarioStr, horario: '' };
    
    const dias = partes[0];
    const horario = partes[1];
    
    // Mapear abreviações para nomes completos
    const diasMap = {
        'SEG': 'Segunda',
        'TER': 'Terça',
        'QUA': 'Quarta',
        'QUI': 'Quinta',
        'SEX': 'Sexta',
        'SAB': 'Sábado'
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
    
    // Formatar horário de "08:15-10:15" para "8:15 - 10:15"
    const horarioFormatado = horario.replace('-', ' - ');
    
    return { diasTexto, horario: horarioFormatado };
}

// Cancelar matrícula
async function cancelarMatricula(matriculaId) {
    if (!confirm('Tem certeza que deseja cancelar esta matrícula?')) {
        return;
    }
    
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matriculas/${matriculaId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Matrícula cancelada com sucesso!');
            carregarMatriculas();
        } else {
            alert('Erro ao cancelar matrícula');
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao cancelar matrícula');
    }
}

// Verificar se está no período de inscrição
async function verificarPeriodoInscricao() {
    try {
        const response = await fetch('http://localhost:8080/api/admin/periodos/atual');
        if (!response.ok) {
            console.error('Erro ao verificar período de inscrição');
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
                        subtitulo.textContent = `Não está no período de inscrição. O período de inscrição é de ${dataInicio} até ${dataFim}.`;
                        subtitulo.style.color = '#e74c3c';
                        subtitulo.style.fontWeight = 'bold';
                    }
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar período de inscrição:', error);
    }
}

// Abrir página de matrícula
async function abrirMatricula() {
    // Verificar período de inscrição antes de redirecionar
    try {
        const response = await fetch('http://localhost:8080/api/admin/periodos/atual');
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
                    alert(`Não está no período de inscrição. O período de inscrição é de ${dataInicio} até ${dataFim}.`);
                    return;
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar período de inscrição:', error);
    }
    
    // Salvar matrículas atuais para pré-preencher na página de edição
    localStorage.setItem('matriculasAtuais', JSON.stringify(matriculas));
    window.location.href = 'matricula-detalhes-aluno.html';
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
