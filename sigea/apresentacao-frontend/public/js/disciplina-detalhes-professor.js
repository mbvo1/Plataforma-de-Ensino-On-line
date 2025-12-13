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
        // Busca alunos matriculados na sala
        const response = await fetch(`http://localhost:8080/api/salas/${salaId}/alunos?professorId=${usuarioId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar alunos');
        }

        const alunos = await response.json();
        console.log('Alunos carregados:', alunos);

        loadingElement.style.display = 'none';

        if (!alunos || alunos.length === 0) {
            emptyState.style.display = 'block';
            return;
        }

        // Ordena alunos por nome (ordem alfabética)
        alunos.sort((a, b) => a.nome.localeCompare(b.nome));

        // Preenche a tabela
        tbody.innerHTML = '';
        alunos.forEach(aluno => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${aluno.nome}</td>
                <td>${aluno.notaAv1 !== null ? aluno.notaAv1 : '—'}</td>
                <td>${aluno.notaAv2 !== null ? aluno.notaAv2 : '—'}</td>
                <td>${aluno.totalFaltas || 0}</td>
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
