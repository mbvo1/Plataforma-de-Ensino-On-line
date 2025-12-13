// disciplina-detalhes-professor.js

const usuarioId = localStorage.getItem('usuarioId');
document.getElementById('userName').textContent = `Professor - ${localStorage.getItem('usuarioNome') || 'Usuário'}`;

// Obtém parâmetros da URL
const urlParams = new URLSearchParams(window.location.search);
const salaId = urlParams.get('salaId');
const disciplinaNome = urlParams.get('disciplina');
const salaIdentificador = urlParams.get('sala');

// Define o título da página
if (disciplinaNome && salaIdentificador) {
    document.getElementById('titulo-disciplina').textContent = `${disciplinaNome} - ${salaIdentificador}`;
} else {
    // Tenta recuperar do sessionStorage
    try {
        const disciplinaSelecionada = JSON.parse(sessionStorage.getItem('disciplinaSelecionada'));
        if (disciplinaSelecionada) {
            document.getElementById('titulo-disciplina').textContent = 
                `${disciplinaSelecionada.nome} - ${disciplinaSelecionada.identificador}`;
        }
    } catch (e) {
        document.getElementById('titulo-disciplina').textContent = 'Disciplina';
    }
}

async function carregarAlunos() {
    const loadingElement = document.getElementById('loading');
    const tabelaContainer = document.getElementById('tabela-container');
    const emptyState = document.getElementById('empty-state');
    const tbody = document.getElementById('alunos-tbody');

    try {
        // Busca notas completas dos alunos da sala
        const response = await fetch(`http://localhost:8080/api/notas/sala/${salaId}?professorId=${usuarioId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar notas');
        }

        const dados = await response.json();
        const alunos = dados.alunos || [];
        console.log('Alunos carregados:', alunos);

        loadingElement.style.display = 'none';

        if (!alunos || alunos.length === 0) {
            emptyState.style.display = 'block';
            return;
        }

        // Ordena alunos por nome (ordem alfabética)
        alunos.sort((a, b) => a.nome.localeCompare(b.nome));

        // Busca faltas de todos os alunos de uma vez
        const faltasResponse = await fetch(`http://localhost:8080/api/salas/${salaId}/alunos?professorId=${usuarioId}`);
        const alunosComFaltas = faltasResponse.ok ? await faltasResponse.json() : [];
        const faltasMap = new Map();
        alunosComFaltas.forEach(a => {
            faltasMap.set(a.alunoId, a.totalFaltas || 0);
        });

        // Preenche a tabela
        tbody.innerHTML = '';
        alunos.forEach(aluno => {
            // Calcula média parcial
            const mediaParcial = calcularMediaParcial(aluno.av1, aluno.av2, aluno.segundaChamada);
            
            // Calcula média final
            let mediaFinal = null;
            if (mediaParcial !== null && mediaParcial !== undefined && aluno.final !== null && aluno.final !== undefined) {
                mediaFinal = (mediaParcial + aluno.final) / 2.0;
            }
            
            // Formata valores
            const av1 = aluno.av1 !== null && aluno.av1 !== undefined ? aluno.av1.toFixed(2) : '—';
            const av2 = aluno.av2 !== null && aluno.av2 !== undefined ? aluno.av2.toFixed(2) : '—';
            const segundaChamada = aluno.segundaChamada !== null && aluno.segundaChamada !== undefined ? aluno.segundaChamada.toFixed(2) : '—';
            const mediaParcialFormat = mediaParcial !== null && mediaParcial !== undefined ? mediaParcial.toFixed(2) : '—';
            const provaFinal = aluno.final !== null && aluno.final !== undefined ? aluno.final.toFixed(2) : '—';
            const mediaFinalFormat = mediaFinal !== null && mediaFinal !== undefined ? mediaFinal.toFixed(2) : '—';
            const faltas = faltasMap.get(aluno.alunoId) || 0;
            
            const tr = document.createElement('tr');
            
            // Determina classe CSS para média final
            let mediaFinalClass = '';
            if (mediaFinal !== null && mediaFinal !== undefined) {
                if (mediaFinal >= 6) {
                    mediaFinalClass = 'media-aprovado';
                } else {
                    mediaFinalClass = 'media-reprovado';
                }
            }
            
            tr.innerHTML = `
                <td>${aluno.nome}</td>
                <td>${av1}</td>
                <td>${av2}</td>
                <td>${segundaChamada}</td>
                <td>${mediaParcialFormat}</td>
                <td>${provaFinal}</td>
                <td class="${mediaFinalClass}">${mediaFinalFormat}</td>
                <td>${faltas}</td>
            `;
            tbody.appendChild(tr);
        });

        tabelaContainer.style.display = 'block';

    } catch (error) {
        console.error('Erro ao carregar alunos:', error);
        loadingElement.style.display = 'none';
        emptyState.style.display = 'block';
    }
}

/**
 * Calcula a média parcial seguindo as regras:
 * - Média = (Av1 + Av2) / 2
 * - Se Av1 preenchida, Av2 não preenchida, e Segunda Chamada preenchida,
 *   então Segunda Chamada substitui Av2
 */
function calcularMediaParcial(av1, av2, segundaChamada) {
    let nota1 = av1;
    let nota2 = av2;
    
    // Se Av1 está preenchida, Av2 não está, e Segunda Chamada está preenchida,
    // então Segunda Chamada substitui Av2
    if (av1 !== null && av1 !== undefined && (av2 === null || av2 === undefined) && segundaChamada !== null && segundaChamada !== undefined) {
        nota2 = segundaChamada;
    }
    
    // Calcula média
    if (nota1 !== null && nota1 !== undefined && nota2 !== null && nota2 !== undefined) {
        return (nota1 + nota2) / 2.0;
    } else if (nota1 !== null && nota1 !== undefined) {
        return nota1;
    } else if (nota2 !== null && nota2 !== undefined) {
        return nota2;
    }
    
    return null;
}

function voltarParaDisciplinas() {
    window.location.href = 'disciplinas-professor.html';
}

function realizarChamada() {
    // Salva os parâmetros para a próxima página
    const params = new URLSearchParams({
        salaId: salaId,
        disciplina: disciplinaNome || '',
        sala: salaIdentificador || ''
    });
    window.location.href = `chamada-professor.html?${params.toString()}`;
}

function atribuirNotas() {
    // Salva os parâmetros para a próxima página
    const params = new URLSearchParams({
        salaId: salaId,
        disciplina: disciplinaNome || '',
        sala: salaIdentificador || ''
    });
    window.location.href = `notas-professor.html?${params.toString()}`;
}

function handleLogout() {
    localStorage.clear();
    window.location.href = 'login-professor.html';
}

function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('collapsed');
}

// Carrega os alunos ao iniciar
if (salaId) {
    carregarAlunos();
} else {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('empty-state').style.display = 'block';
}
