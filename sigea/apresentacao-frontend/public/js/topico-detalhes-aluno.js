// VariÃ¡veis globais
let topicoId = null;
let disciplinaId = null;

// VerificaÃ§Ã£o de autenticaÃ§Ã£o e inicializaÃ§Ã£o
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ALUNO') {
        alert('VocÃª precisa fazer login como ALUNO para acessar esta pÃ¡gina.');
        window.location.href = '/index.html';
        return;
    }

    // Recupera informaÃ§Ãµes do tÃ³pico da URL
    const urlParams = new URLSearchParams(window.location.search);
    topicoId = urlParams.get('topicoId');
    disciplinaId = urlParams.get('disciplinaId');

    if (!topicoId) {
        alert('TÃ³pico nÃ£o especificado.');
        window.location.href = '/forum-aluno.html';
        return;
    }

    loadUserInfo();
    carregarTopico();
    carregarComentarios();
});

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'UsuÃ¡rio'}`;
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        localStorage.removeItem('forumDisciplinaId');
        localStorage.removeItem('forumDisciplinaNome');
        window.location.href = '/index.html';
    }
}

function voltarParaForum() {
    if (disciplinaId) {
        window.location.href = `/forum-detalhes-aluno.html?disciplinaId=${disciplinaId}`;
    } else {
        window.location.href = '/forum-aluno.html';
    }
}

async function carregarTopico() {
    const loadingElement = document.getElementById('loading');
    const topicoPrincipalElement = document.getElementById('topico-principal');
    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`/api/foruns/topicos/${topicoId}?usuarioId=${usuarioId}`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });

        if (!response.ok) {
            throw new Error('Erro ao carregar tÃ³pico');
        }

        const topico = await response.json();
        console.log('TÃ³pico carregado:', topico);

        // Preenche os dados do tÃ³pico
        document.getElementById('topico-titulo').textContent = topico.titulo;
        document.getElementById('topico-autor').textContent = `Por: "${topico.nomeAutor || 'UsuÃ¡rio ' + topico.autorId}"`;
        document.getElementById('topico-conteudo').textContent = topico.conteudo;
        
        // Data
        const dataAtual = new Date().toLocaleDateString('pt-BR') + ', ' + new Date().toLocaleTimeString('pt-BR', {hour: '2-digit', minute: '2-digit'});
        document.getElementById('topico-data').textContent = dataAtual;

        // Arquivo anexado
        if (topico.arquivoPath) {
            const arquivoDiv = document.getElementById('topico-arquivo');
            const arquivoLink = document.getElementById('topico-arquivo-link');
            arquivoDiv.style.display = 'flex';
            arquivoLink.href = `http://localhost:8080/${topico.arquivoPath}`;
            
            // Extrai nome do arquivo do path
            const nomeArquivo = topico.arquivoPath.split('/').pop();
            arquivoLink.textContent = nomeArquivo;
        }

        loadingElement.style.display = 'none';
        topicoPrincipalElement.style.display = 'block';

    } catch (error) {
        console.error('Erro ao carregar tÃ³pico:', error);
        loadingElement.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar tÃ³pico</p>
        `;
    }
}

async function carregarComentarios() {
    const comentariosListElement = document.getElementById('comentarios-list');
    const noComentariosElement = document.getElementById('no-comentarios');
    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`/api/foruns/topicos/${topicoId}/respostas`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });

        if (!response.ok) {
            if (response.status === 404) {
                noComentariosElement.style.display = 'block';
                return;
            }
            throw new Error('Erro ao carregar comentÃ¡rios');
        }

        const comentarios = await response.json();
        console.log('ComentÃ¡rios carregados:', comentarios);

        if (!comentarios || comentarios.length === 0) {
            noComentariosElement.style.display = 'block';
        } else {
            comentariosListElement.innerHTML = '';
            comentarios.forEach(comentario => {
                const card = criarCardComentario(comentario);
                comentariosListElement.appendChild(card);
            });
        }

    } catch (error) {
        console.error('Erro ao carregar comentÃ¡rios:', error);
        noComentariosElement.style.display = 'block';
    }
}

function criarCardComentario(comentario) {
    const card = document.createElement('div');
    card.className = 'comentario-card';
    card.setAttribute('data-comentario-id', comentario.id);

    const nomeAutor = comentario.nomeAutor || `UsuÃ¡rio ${comentario.autorId}`;
    const dataComentario = comentario.dataCriacao || new Date().toLocaleDateString('pt-BR');

    // Verifica se o usuÃ¡rio atual Ã© o autor do comentÃ¡rio
    const usuarioId = localStorage.getItem('usuarioId');
    const isAutor = comentario.autorId && comentario.autorId.toString() === usuarioId;

    card.innerHTML = `
        ${isAutor ? `<button class="btn-excluir-comentario" onclick="excluirComentario(${comentario.id})" title="Excluir comentÃ¡rio">
            <i class="fas fa-times"></i>
        </button>` : ''}
        <div class="comentario-autor">Por: "${escapeHtml(nomeAutor)}"</div>
        <div class="comentario-conteudo">${escapeHtml(comentario.conteudo)}</div>
        <div class="comentario-footer">
            <span>${dataComentario}</span>
            <button class="btn-comentar" onclick="toggleNovoComentario()">
                <i class="fas fa-comment"></i>
                Comente
            </button>
        </div>
    `;

    return card;
}

async function excluirComentario(comentarioId) {
    if (!confirm('Tem certeza que deseja excluir este comentÃ¡rio?')) {
        return;
    }

    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`/api/foruns/respostas/${comentarioId}?usuarioId=${usuarioId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao excluir comentÃ¡rio');
        }

        // Remove o card do DOM
        const card = document.querySelector(`[data-comentario-id="${comentarioId}"]`);
        if (card) {
            card.remove();
        }

        // Verifica se hÃ¡ mais comentÃ¡rios
        const comentariosList = document.getElementById('comentarios-list');
        if (comentariosList.children.length === 0) {
            document.getElementById('no-comentarios').style.display = 'block';
        }

    } catch (error) {
        console.error('Erro ao excluir comentÃ¡rio:', error);
        alert('Erro ao excluir comentÃ¡rio. Tente novamente.');
    }
}

function toggleNovoComentario() {
    const container = document.getElementById('novo-comentario-container');
    container.classList.toggle('show');
    
    if (container.classList.contains('show')) {
        document.getElementById('comentario-texto').focus();
    }
}

async function publicarComentario() {
    const texto = document.getElementById('comentario-texto').value.trim();
    const usuarioId = localStorage.getItem('usuarioId');

    if (!texto) {
        alert('Por favor, digite um comentÃ¡rio.');
        return;
    }

    try {
        const response = await fetch(`/api/foruns/topicos/${topicoId}/respostas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify({
                autorId: usuarioId,
                conteudo: texto
            })
        });

        if (!response.ok) {
            throw new Error('Erro ao publicar comentÃ¡rio');
        }

        // Limpa o campo e esconde o formulÃ¡rio
        document.getElementById('comentario-texto').value = '';
        document.getElementById('novo-comentario-container').classList.remove('show');
        
        // Recarrega os comentÃ¡rios
        document.getElementById('comentarios-list').innerHTML = '';
        document.getElementById('no-comentarios').style.display = 'none';
        carregarComentarios();

        alert('ComentÃ¡rio publicado com sucesso!');

    } catch (error) {
        console.error('Erro ao publicar comentÃ¡rio:', error);
        alert('Erro ao publicar comentÃ¡rio. Tente novamente.');
    }
}

// Toggle da sidebar
document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('collapsed');
        });
    }
});

