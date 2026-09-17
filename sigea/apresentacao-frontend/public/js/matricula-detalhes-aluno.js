// VariÃ¡veis globais
let salasDisponiveis = [];
let disciplinasSelecionadas = [];
let matriculasExistentesGlobal = []; // Armazena matrÃ­culas existentes para comparaÃ§Ã£o

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
    carregarSalasDisponiveis();
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

// Carregar salas disponÃ­veis
async function carregarSalasDisponiveis() {
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        // Carregar salas disponÃ­veis
        const response = await fetch(`/api/aluno/${usuarioId}/salas-disponiveis`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        if (!response.ok) {
            throw new Error('Erro ao carregar salas');
        }
        
        salasDisponiveis = await response.json();
        
        // Carregar tambÃ©m as matrÃ­culas existentes do aluno
        const matriculasResponse = await fetch(`/api/aluno/${usuarioId}/matriculas`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        let matriculasExistentes = [];
        if (matriculasResponse.ok) {
            matriculasExistentes = await matriculasResponse.json();
            matriculasExistentesGlobal = matriculasExistentes; // Armazenar globalmente
        }
        
        preencherSelects();
        
        // PrÃ©-preencher com matrÃ­culas existentes
        if (matriculasExistentes.length > 0) {
            preencherComMatriculasExistentes(matriculasExistentes);
        }
    } catch (error) {
        console.error('Erro ao carregar salas:', error);
        exibirMensagem('Erro ao carregar disciplinas disponÃ­veis', 'erro');
    }
}

// PrÃ©-preencher selects com matrÃ­culas existentes
async function preencherComMatriculasExistentes(matriculasExistentes) {
    const usuarioId = localStorage.getItem('usuarioId');
    
    // Buscar informaÃ§Ãµes completas das salas matriculadas e outras salas da mesma disciplina
    for (let i = 0; i < matriculasExistentes.length && i < 5; i++) {
        const matricula = matriculasExistentes[i];
        const select = document.getElementById(`disciplina${i + 1}`);
        
        if (select) {
            // Buscar todas as salas da disciplina matriculada
            try {
                const response = await fetch(`/api/aluno/${usuarioId}/disciplinas/${matricula.disciplinaId}/salas`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
                if (response.ok) {
                    const data = await response.json();
                    const salasDaDisciplina = data.salas || [];
                    
                    // Adicionar a sala matriculada primeiro (que pode nÃ£o aparecer se tem 0 vagas restantes pra outros)
                    const salaMatriculadaExiste = salasDaDisciplina.find(s => s.salaId === matricula.salaId);
                    if (!salaMatriculadaExiste) {
                        // Adicionar a sala matriculada manualmente
                        const salaMatriculada = {
                            id: matricula.salaId,
                            identificador: matricula.salaIdentificador,
                            disciplinaId: matricula.disciplinaId,
                            disciplinaNome: matricula.disciplinaNome,
                            disciplinaPeriodo: '',
                            horario: matricula.horario,
                            vagasDisponiveis: 0,
                            professorNome: matricula.professorNome,
                            jaMatriculado: true
                        };
                        const jaExisteNaLista = salasDisponiveis.find(s => s.id === salaMatriculada.id);
                        if (!jaExisteNaLista) {
                            salasDisponiveis.push(salaMatriculada);
                        }
                    }
                    
                    // Adicionar todas as salas da disciplina Ã  lista
                    salasDaDisciplina.forEach(salaInfo => {
                        const salaId = salaInfo.salaId || salaInfo.id;
                        const jaExiste = salasDisponiveis.find(s => s.id === salaId);
                        if (!jaExiste) {
                            const novaSala = {
                                id: salaId,
                                identificador: salaInfo.identificador,
                                disciplinaId: matricula.disciplinaId,
                                disciplinaNome: matricula.disciplinaNome,
                                disciplinaPeriodo: '',
                                horario: salaInfo.horario,
                                vagasDisponiveis: salaInfo.vagasDisponiveis || 0,
                                professorNome: salaInfo.professorNome || matricula.professorNome,
                                jaMatriculado: salaId === matricula.salaId
                            };
                            salasDisponiveis.push(novaSala);
                        } else if (salaId === matricula.salaId) {
                            // Marcar a sala atual como jÃ¡ matriculado
                            jaExiste.jaMatriculado = true;
                        }
                    });
                }
            } catch (error) {
                console.error('Erro ao buscar salas da disciplina:', error);
                
                // Fallback: adicionar apenas a sala matriculada
                const salaExistente = {
                    id: matricula.salaId,
                    identificador: matricula.salaIdentificador,
                    disciplinaId: matricula.disciplinaId,
                    disciplinaNome: matricula.disciplinaNome,
                    disciplinaPeriodo: '',
                    horario: matricula.horario,
                    vagasDisponiveis: 0,
                    professorNome: matricula.professorNome,
                    jaMatriculado: true
                };
                
                const jaExiste = salasDisponiveis.find(s => s.id === salaExistente.id);
                if (!jaExiste) {
                    salasDisponiveis.push(salaExistente);
                }
            }
        }
    }
    
    // Atualizar selects com as novas salas
    atualizarSelectsDisponiveis();
    
    // Selecionar as salas matriculadas nos selects corretos
    for (let i = 0; i < matriculasExistentes.length && i < 5; i++) {
        const matricula = matriculasExistentes[i];
        const select = document.getElementById(`disciplina${i + 1}`);
        if (select) {
            select.value = matricula.salaId;
        }
    }
    
    // Atualizar a grade com as seleÃ§Ãµes
    atualizarGrade();
    
    // Limpar o localStorage
    localStorage.removeItem('matriculasAtuais');
}

// Preencher todos os selects com as salas disponÃ­veis
function preencherSelects() {
    atualizarSelectsDisponiveis();
}

// Atualizar selects removendo disciplinas jÃ¡ selecionadas em outros selects
function atualizarSelectsDisponiveis() {
    // Coletar disciplinas selecionadas em cada select
    const selecoesPorSelect = {};
    const disciplinasSelecionadasIds = new Set(); // IDs das disciplinas (nÃ£o salas) jÃ¡ selecionadas
    
    for (let i = 1; i <= 5; i++) {
        const select = document.getElementById(`disciplina${i}`);
        if (select && select.value) {
            selecoesPorSelect[i] = select.value;
            const sala = salasDisponiveis.find(s => s.id == select.value);
            if (sala && sala.disciplinaId) {
                disciplinasSelecionadasIds.add(sala.disciplinaId);
            }
        }
    }
    
    // Atualizar cada select
    for (let i = 1; i <= 5; i++) {
        const select = document.getElementById(`disciplina${i}`);
        if (select) {
            const valorAtual = selecoesPorSelect[i] || '';
            const salaAtual = valorAtual ? salasDisponiveis.find(s => s.id == valorAtual) : null;
            const disciplinaAtualId = salaAtual ? salaAtual.disciplinaId : null;
            
            // Limpar opÃ§Ãµes existentes
            select.innerHTML = '<option value="">Disciplina</option>';
            
            // Adicionar salas filtradas
            salasDisponiveis.forEach(sala => {
                const disciplinaId = sala.disciplinaId;
                
                // Verificar se esta disciplina jÃ¡ foi selecionada em outro select
                // Permite mostrar a disciplina se:
                // 1. A disciplina nÃ£o foi selecionada em nenhum outro select, OU
                // 2. A disciplina Ã© a mesma que estÃ¡ selecionada neste select (para manter a opÃ§Ã£o atual)
                const disciplinaJaSelecionadaEmOutro = disciplinasSelecionadasIds.has(disciplinaId) && disciplinaId !== disciplinaAtualId;
                
                if (!disciplinaJaSelecionadaEmOutro) {
                    const option = document.createElement('option');
                    option.value = sala.id;
                    const nomeDisciplina = sala.disciplinaNome || (sala.disciplina ? sala.disciplina.nome : 'Disciplina');
                    const identificador = sala.identificador || '';
                    const horario = sala.horario || 'HorÃ¡rio nÃ£o definido';
                    const periodo = sala.disciplinaPeriodo || '';
                    const vagasDisponiveis = sala.vagasDisponiveis || 0;
                    
                    // Formato: "Disciplina - Sala X (HORARIO) | PerÃ­odo: X | Vagas: X"
                    option.textContent = `${nomeDisciplina} - ${identificador} (${horario}) | PerÃ­odo: ${periodo} | Vagas: ${vagasDisponiveis}`;
                    option.dataset.horario = sala.horario || '';
                    option.dataset.disciplinaNome = nomeDisciplina;
                    select.appendChild(option);
                }
            });
            
            // Restaurar valor selecionado
            if (valorAtual) {
                select.value = valorAtual;
            }
        }
    }
}

// Atualizar grade horÃ¡ria
function atualizarGrade() {
    limparGrade();
    
    // Obter salas selecionadas
    disciplinasSelecionadas = [];
    for (let i = 1; i <= 5; i++) {
        const select = document.getElementById(`disciplina${i}`);
        if (select && select.value) {
            const sala = salasDisponiveis.find(s => s.id == select.value);
            if (sala) {
                disciplinasSelecionadas.push(sala);
                preencherHorarioNaGrade(sala);
            }
        }
    }
    
    // Atualizar os selects para remover disciplinas jÃ¡ selecionadas
    atualizarSelectsDisponiveis();
}

// Limpar toda a grade
function limparGrade() {
    const dias = ['seg', 'ter', 'qua', 'qui', 'sex'];
    const horarios = ['0815', '0915', '1030', '1130', '1330', '1430'];
    
    dias.forEach(dia => {
        horarios.forEach(horario => {
            const celula = document.getElementById(`${dia}-${horario}`);
            if (celula) {
                celula.textContent = '';
                celula.className = '';
            }
        });
    });
}

// Preencher horÃ¡rio na grade
function preencherHorarioNaGrade(sala) {
    if (!sala.horario) return;
    
    const nomeDisciplina = sala.disciplinaNome || (sala.disciplina ? sala.disciplina.nome : 'Disciplina');
    
    // Mapear dias da semana para IDs
    const diasMap = {
        'SEG': 'seg',
        'TER': 'ter',
        'QUA': 'qua',
        'QUI': 'qui',
        'SEX': 'sex',
        'SEGUNDA': 'seg',
        'TERCA': 'ter',
        'TERÃ‡A': 'ter',
        'QUARTA': 'qua',
        'QUINTA': 'qui',
        'SEXTA': 'sex'
    };
    
    // Lista ordenada de horÃ¡rios da grade
    const horariosOrdenados = [
        { id: '0815', inicio: '08:15', fim: '09:15' },
        { id: '0915', inicio: '09:15', fim: '10:15' },
        { id: '1030', inicio: '10:30', fim: '11:30' },
        { id: '1130', inicio: '11:30', fim: '12:30' },
        { id: '1330', inicio: '13:30', fim: '14:30' },
        { id: '1430', inicio: '14:30', fim: '15:30' }
    ];
    
    // FunÃ§Ã£o auxiliar para converter horÃ¡rio em minutos
    function horaParaMinutos(hora) {
        const partes = hora.replace(':', '.').split('.');
        const h = parseInt(partes[0]);
        const m = parseInt(partes[1] || '0');
        return h * 60 + m;
    }
    
    // Parse do formato: "SEG,QUA 08:15-10:15" ou similar
    const partes = sala.horario.split(' ');
    if (partes.length >= 2) {
        const diasStr = partes[0].toUpperCase();
        const horarioStr = partes[1];
        
        const dias = diasStr.split(',');
        const [horaInicio, horaFim] = horarioStr.split('-');
        
        const inicioMinutos = horaParaMinutos(horaInicio);
        const fimMinutos = horaParaMinutos(horaFim);
        
        // Encontrar quais cÃ©lulas preencher
        const celulasParaPreencher = horariosOrdenados.filter(h => {
            const celulaInicio = horaParaMinutos(h.inicio);
            const celulaFim = horaParaMinutos(h.fim);
            // A cÃ©lula deve ser preenchida se estiver dentro do intervalo
            return celulaInicio >= inicioMinutos && celulaFim <= fimMinutos;
        });
        
        // Preencher cada cÃ©lula para cada dia
        dias.forEach(dia => {
            const diaId = diasMap[dia.trim()];
            if (diaId) {
                celulasParaPreencher.forEach(h => {
                    const celula = document.getElementById(`${diaId}-${h.id}`);
                    if (celula) {
                        if (celula.textContent && celula.textContent !== nomeDisciplina && !celula.textContent.includes(nomeDisciplina)) {
                            // Conflito de horÃ¡rio
                            celula.className = 'conflito';
                            celula.textContent = 'CONFLITO';
                        } else {
                            celula.textContent = nomeDisciplina.length > 20 
                                ? nomeDisciplina.substring(0, 18) + '...' 
                                : nomeDisciplina;
                            celula.className = 'ocupado';
                        }
                    }
                });
            }
        });
    }
}

// Realizar matrÃ­cula
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
                const btnMatricular = document.getElementById('btnMatricular');
                if (btnMatricular) {
                    btnMatricular.disabled = true;
                    btnMatricular.style.opacity = '0.5';
                    btnMatricular.style.cursor = 'not-allowed';
                    
                    const mensagem = document.getElementById('mensagem');
                    if (mensagem) {
                        const dataInicio = inicioInscricao.toLocaleDateString('pt-BR');
                        const dataFim = fimInscricao.toLocaleDateString('pt-BR');
                        mensagem.textContent = `NÃ£o estÃ¡ no perÃ­odo de inscriÃ§Ã£o. O perÃ­odo de inscriÃ§Ã£o Ã© de ${dataInicio} atÃ© ${dataFim}.`;
                        mensagem.style.color = '#e74c3c';
                        mensagem.style.fontWeight = 'bold';
                    }
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar perÃ­odo de inscriÃ§Ã£o:', error);
    }
}

async function realizarMatricula() {
    const usuarioId = localStorage.getItem('usuarioId');
    const btnMatricular = document.getElementById('btnMatricular');
    
    // Verificar perÃ­odo de inscriÃ§Ã£o antes de processar
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
                    exibirMensagem(`NÃ£o estÃ¡ no perÃ­odo de inscriÃ§Ã£o. O perÃ­odo de inscriÃ§Ã£o Ã© de ${dataInicio} atÃ© ${dataFim}.`, 'erro');
                    return;
                }
            }
        }
    } catch (error) {
        console.error('Erro ao verificar perÃ­odo de inscriÃ§Ã£o:', error);
    }
    
    btnMatricular.disabled = true;
    btnMatricular.textContent = 'Processando...';
    
    // Verificar conflitos
    const gradeBody = document.getElementById('gradeBody');
    const conflitos = gradeBody.querySelectorAll('.conflito');
    if (conflitos.length > 0) {
        exibirMensagem('Existem conflitos de horÃ¡rio. Ajuste suas seleÃ§Ãµes.', 'erro');
        btnMatricular.disabled = false;
        btnMatricular.textContent = 'Realizar MatrÃ­cula';
        return;
    }
    
    let sucessoAdd = 0;
    let sucessoRemove = 0;
    let erros = [];
    
    // Identificar salas selecionadas atualmente (IDs)
    const salasSelecionadasIds = new Set(disciplinasSelecionadas.map(s => s.id));
    
    // Identificar matrÃ­culas que precisam ser canceladas
    // (estavam matriculadas antes, mas nÃ£o estÃ£o mais selecionadas)
    const matriculasParaCancelar = matriculasExistentesGlobal.filter(
        mat => !salasSelecionadasIds.has(mat.salaId)
    );
    
    // Cancelar matrÃ­culas removidas
    for (const matricula of matriculasParaCancelar) {
        try {
            const response = await fetch(`/api/aluno/${usuarioId}/matriculas/${matricula.matriculaId}`, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
            });
            
            if (response.ok) {
                sucessoRemove++;
            } else {
                const erro = await response.text();
                erros.push(`Erro ao cancelar ${matricula.disciplinaNome}: ${erro}`);
            }
        } catch (error) {
            erros.push(`Erro ao cancelar ${matricula.disciplinaNome}: ${error.message}`);
        }
    }
    
    // Filtrar apenas salas novas que nÃ£o estÃ£o jÃ¡ matriculadas
    const novasSalas = disciplinasSelecionadas.filter(sala => !sala.jaMatriculado);
    
    // Adicionar novas matrÃ­culas
    for (const sala of novasSalas) {
        try {
            const response = await fetch(`/api/aluno/${usuarioId}/matricular`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
                body: JSON.stringify({ salaId: sala.id })
            });
            
            if (response.ok) {
                sucessoAdd++;
            } else {
                const erro = await response.text();
                erros.push(erro);
            }
        } catch (error) {
            erros.push(error.message);
        }
    }
    
    btnMatricular.disabled = false;
    btnMatricular.textContent = 'Realizar MatrÃ­cula';
    
    const totalAlteracoes = sucessoAdd + sucessoRemove;
    
    if (totalAlteracoes === 0 && erros.length === 0) {
        exibirMensagem('Nenhuma alteraÃ§Ã£o realizada.', 'sucesso');
        setTimeout(() => {
            window.location.href = 'matricula-aluno.html';
        }, 1500);
    } else if (totalAlteracoes > 0) {
        let mensagem = '';
        if (sucessoAdd > 0) mensagem += `${sucessoAdd} disciplina(s) adicionada(s). `;
        if (sucessoRemove > 0) mensagem += `${sucessoRemove} disciplina(s) removida(s).`;
        exibirMensagem(mensagem, 'sucesso');
        setTimeout(() => {
            window.location.href = 'matricula-aluno.html';
        }, 2000);
    } else if (erros.length > 0) {
        exibirMensagem('Erro ao atualizar matrÃ­cula: ' + erros.join(', '), 'erro');
    }
}

// Exibir mensagem
function exibirMensagem(texto, tipo) {
    const mensagem = document.getElementById('mensagem');
    if (mensagem) {
        mensagem.textContent = texto;
        mensagem.className = tipo === 'erro' ? 'mensagem-erro' : 'mensagem-sucesso';
        
        setTimeout(() => {
            mensagem.textContent = '';
            mensagem.className = '';
        }, 5000);
    }
}

// Voltar para pÃ¡gina anterior
function voltarPagina() {
    window.location.href = 'matricula-aluno.html';
}

// Logout
function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
