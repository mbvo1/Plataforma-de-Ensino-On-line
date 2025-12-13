// Variáveis globais
let salasDisponiveis = [];
let disciplinasSelecionadas = [];
let matriculasExistentesGlobal = []; // Armazena matrículas existentes para comparação

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
    carregarSalasDisponiveis();
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

// Carregar salas disponíveis
async function carregarSalasDisponiveis() {
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        // Carregar salas disponíveis
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/salas-disponiveis`);
        if (!response.ok) {
            throw new Error('Erro ao carregar salas');
        }
        
        salasDisponiveis = await response.json();
        
        // Carregar também as matrículas existentes do aluno
        const matriculasResponse = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matriculas`);
        let matriculasExistentes = [];
        if (matriculasResponse.ok) {
            matriculasExistentes = await matriculasResponse.json();
            matriculasExistentesGlobal = matriculasExistentes; // Armazenar globalmente
        }
        
        preencherSelects();
        
        // Pré-preencher com matrículas existentes
        if (matriculasExistentes.length > 0) {
            preencherComMatriculasExistentes(matriculasExistentes);
        }
    } catch (error) {
        console.error('Erro ao carregar salas:', error);
        exibirMensagem('Erro ao carregar disciplinas disponíveis', 'erro');
    }
}

// Pré-preencher selects com matrículas existentes
async function preencherComMatriculasExistentes(matriculasExistentes) {
    const usuarioId = localStorage.getItem('usuarioId');
    
    // Buscar informações completas das salas matriculadas e outras salas da mesma disciplina
    for (let i = 0; i < matriculasExistentes.length && i < 5; i++) {
        const matricula = matriculasExistentes[i];
        const select = document.getElementById(`disciplina${i + 1}`);
        
        if (select) {
            // Buscar todas as salas da disciplina matriculada
            try {
                const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/disciplinas/${matricula.disciplinaId}/salas`);
                if (response.ok) {
                    const data = await response.json();
                    const salasDaDisciplina = data.salas || [];
                    
                    // Adicionar a sala matriculada primeiro (que pode não aparecer se tem 0 vagas restantes pra outros)
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
                    
                    // Adicionar todas as salas da disciplina à lista
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
                            // Marcar a sala atual como já matriculado
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
    
    // Atualizar a grade com as seleções
    atualizarGrade();
    
    // Limpar o localStorage
    localStorage.removeItem('matriculasAtuais');
}

// Preencher todos os selects com as salas disponíveis
function preencherSelects() {
    atualizarSelectsDisponiveis();
}

// Atualizar selects removendo disciplinas já selecionadas em outros selects
function atualizarSelectsDisponiveis() {
    // Coletar disciplinas selecionadas em cada select
    const selecoesPorSelect = {};
    const disciplinasSelecionadasIds = new Set(); // IDs das disciplinas (não salas) já selecionadas
    
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
            
            // Limpar opções existentes
            select.innerHTML = '<option value="">Disciplina</option>';
            
            // Adicionar salas filtradas
            salasDisponiveis.forEach(sala => {
                const disciplinaId = sala.disciplinaId;
                
                // Verificar se esta disciplina já foi selecionada em outro select
                // Permite mostrar a disciplina se:
                // 1. A disciplina não foi selecionada em nenhum outro select, OU
                // 2. A disciplina é a mesma que está selecionada neste select (para manter a opção atual)
                const disciplinaJaSelecionadaEmOutro = disciplinasSelecionadasIds.has(disciplinaId) && disciplinaId !== disciplinaAtualId;
                
                if (!disciplinaJaSelecionadaEmOutro) {
                    const option = document.createElement('option');
                    option.value = sala.id;
                    const nomeDisciplina = sala.disciplinaNome || (sala.disciplina ? sala.disciplina.nome : 'Disciplina');
                    const identificador = sala.identificador || '';
                    const horario = sala.horario || 'Horário não definido';
                    const periodo = sala.disciplinaPeriodo || '';
                    const vagasDisponiveis = sala.vagasDisponiveis || 0;
                    
                    // Formato: "Disciplina - Sala X (HORARIO) | Período: X | Vagas: X"
                    option.textContent = `${nomeDisciplina} - ${identificador} (${horario}) | Período: ${periodo} | Vagas: ${vagasDisponiveis}`;
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

// Atualizar grade horária
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
    
    // Atualizar os selects para remover disciplinas já selecionadas
    atualizarSelectsDisponiveis();
}

// Limpar toda a grade
function limparGrade() {
    const dias = ['seg', 'ter', 'qua', 'qui', 'sex'];
    const horarios = ['0815', '0915', '1045', '1145', '1330', '1430'];
    
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

// Preencher horário na grade
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
        'TERÇA': 'ter',
        'QUARTA': 'qua',
        'QUINTA': 'qui',
        'SEXTA': 'sex'
    };
    
    // Lista ordenada de horários da grade
    const horariosOrdenados = [
        { id: '0815', inicio: '08:15', fim: '09:15' },
        { id: '0915', inicio: '09:15', fim: '10:15' },
        { id: '1045', inicio: '10:45', fim: '11:45' },
        { id: '1145', inicio: '11:45', fim: '12:45' },
        { id: '1330', inicio: '13:30', fim: '14:30' },
        { id: '1430', inicio: '14:30', fim: '15:30' }
    ];
    
    // Função auxiliar para converter horário em minutos
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
        
        // Encontrar quais células preencher
        const celulasParaPreencher = horariosOrdenados.filter(h => {
            const celulaInicio = horaParaMinutos(h.inicio);
            const celulaFim = horaParaMinutos(h.fim);
            // A célula deve ser preenchida se estiver dentro do intervalo
            return celulaInicio >= inicioMinutos && celulaFim <= fimMinutos;
        });
        
        // Preencher cada célula para cada dia
        dias.forEach(dia => {
            const diaId = diasMap[dia.trim()];
            if (diaId) {
                celulasParaPreencher.forEach(h => {
                    const celula = document.getElementById(`${diaId}-${h.id}`);
                    if (celula) {
                        if (celula.textContent && celula.textContent !== nomeDisciplina && !celula.textContent.includes(nomeDisciplina)) {
                            // Conflito de horário
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

// Realizar matrícula
async function realizarMatricula() {
    const usuarioId = localStorage.getItem('usuarioId');
    const btnMatricular = document.getElementById('btnMatricular');
    
    btnMatricular.disabled = true;
    btnMatricular.textContent = 'Processando...';
    
    // Verificar conflitos
    const gradeBody = document.getElementById('gradeBody');
    const conflitos = gradeBody.querySelectorAll('.conflito');
    if (conflitos.length > 0) {
        exibirMensagem('Existem conflitos de horário. Ajuste suas seleções.', 'erro');
        btnMatricular.disabled = false;
        btnMatricular.textContent = 'Realizar Matrícula';
        return;
    }
    
    let sucessoAdd = 0;
    let sucessoRemove = 0;
    let erros = [];
    
    // Identificar salas selecionadas atualmente (IDs)
    const salasSelecionadasIds = new Set(disciplinasSelecionadas.map(s => s.id));
    
    // Identificar matrículas que precisam ser canceladas
    // (estavam matriculadas antes, mas não estão mais selecionadas)
    const matriculasParaCancelar = matriculasExistentesGlobal.filter(
        mat => !salasSelecionadasIds.has(mat.salaId)
    );
    
    // Cancelar matrículas removidas
    for (const matricula of matriculasParaCancelar) {
        try {
            const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matriculas/${matricula.matriculaId}`, {
                method: 'DELETE'
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
    
    // Filtrar apenas salas novas que não estão já matriculadas
    const novasSalas = disciplinasSelecionadas.filter(sala => !sala.jaMatriculado);
    
    // Adicionar novas matrículas
    for (const sala of novasSalas) {
        try {
            const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matricular`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
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
    btnMatricular.textContent = 'Realizar Matrícula';
    
    const totalAlteracoes = sucessoAdd + sucessoRemove;
    
    if (totalAlteracoes === 0 && erros.length === 0) {
        exibirMensagem('Nenhuma alteração realizada.', 'sucesso');
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
        exibirMensagem('Erro ao atualizar matrícula: ' + erros.join(', '), 'erro');
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

// Voltar para página anterior
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
