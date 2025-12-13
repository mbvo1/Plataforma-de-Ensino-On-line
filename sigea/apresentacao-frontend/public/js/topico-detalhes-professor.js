// Variáveis globais
let topicoId = null;
let disciplinaId = null;

// Verificação de autenticação e inicialização
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        alert('Você precisa fazer login como PROFESSOR para acessar esta página.');
        window.location.href = '/login-professor.html';
        return;
    }

    // Recupera informações do tópico da URL
    const urlParams = new URLSearchParams(window.location.search);
    topicoId = urlParams.get('topicoId');
    disciplinaId = urlParams.get('disciplinaId');

    if (!topicoId) {
        alert('Tópico não especificado.');
        window.location.href = '/forum-professor.html';
        return;
    }

    loadUserInfo();
    carregarTopico();
    carregarComentarios();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Professor - ${nome || 'Usuário'}`;
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        window.location.href = '/login-professor.html';
    }
}

function voltarParaForum() {
    if (disciplinaId) {
        window.location.href = `/forum-detalhes-professor.html?disciplinaId=${disciplinaId}`;
    } else {
        window.location.href = '/forum-professor.html';
    }
}

async function carregarTopico() {
    const loadingElement = document.getElementById('loading');
    const topicoPrincipalElement = document.getElementById('topico-principal');
    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`http://localhost:8080/api/foruns/topicos/${topicoId}?usuarioId=${usuarioId}`);

        if (!response.ok) {
            throw new Error('Erro ao carregar tópico');
        }

        const topico = await response.json();
        console.log('Tópico carregado:', topico);

        // Preenche os dados do tópico
        document.getElementById('topico-titulo').textContent = topico.titulo;
        document.getElementById('topico-autor').textContent = `Por: "${topico.nomeAutor || 'Usuário ' + topico.autorId}"`;
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
        console.error('Erro ao carregar tópico:', error);
        loadingElement.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar tópico</p>
        `;
    }
}

async function carregarComentarios() {
    const comentariosListElement = document.getElementById('comentarios-list');
    const noComentariosElement = document.getElementById('no-comentarios');
    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`http://localhost:8080/api/foruns/topicos/${topicoId}/respostas?usuarioId=${usuarioId}`);

        if (!response.ok) {
            if (response.status === 404) {
                noComentariosElement.style.display = 'block';
                return;
            }
            throw new Error('Erro ao carregar comentários');
        }

        const comentarios = await response.json();
        console.log('Comentários carregados:', comentarios);

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
        console.error('Erro ao carregar comentários:', error);
        noComentariosElement.style.display = 'block';
    }
}

function criarCardComentario(comentario) {
    const card = document.createElement('div');
    card.className = 'comentario-card';
    card.setAttribute('data-comentario-id', comentario.id);

    const nomeAutor = comentario.nomeAutor || `Usuário ${comentario.autorId}`;
    const dataComentario = comentario.dataCriacao || new Date().toLocaleDateString('pt-BR');

    // Verifica se o usuário atual é o autor do comentário
    const usuarioId = localStorage.getItem('usuarioId');
    const isAutor = comentario.autorId && comentario.autorId.toString() === usuarioId;

    card.innerHTML = `
        ${isAutor ? `<button class="btn-excluir-comentario" onclick="excluirComentario(${comentario.id})" title="Excluir comentário">
            <i class="fas fa-times"></i>
        </button>` : ''}
        <div class="comentario-autor">Por: "${nomeAutor}"</div>
        <div class="comentario-conteudo">${comentario.conteudo}</div>
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
    if (!confirm('Tem certeza que deseja excluir este comentário?')) {
        return;
    }

    const usuarioId = localStorage.getItem('usuarioId');

    try {
        const response = await fetch(`http://localhost:8080/api/foruns/respostas/${comentarioId}?usuarioId=${usuarioId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Erro ao excluir comentário');
        }

        // Remove o card do DOM
        const card = document.querySelector(`[data-comentario-id="${comentarioId}"]`);
        if (card) {
            card.remove();
        }

        // Verifica se há mais comentários
        const comentariosList = document.getElementById('comentarios-list');
        if (comentariosList.children.length === 0) {
            document.getElementById('no-comentarios').style.display = 'block';
        }

    } catch (error) {
        console.error('Erro ao excluir comentário:', error);
        alert('Erro ao excluir comentário. Tente novamente.');
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
        alert('Por favor, digite um comentário.');
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/api/foruns/topicos/${topicoId}/respostas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                autorId: usuarioId,
                conteudo: texto
            })
        });

        if (!response.ok) {
            throw new Error('Erro ao publicar comentário');
        }

        // Limpa o campo e esconde o formulário
        document.getElementById('comentario-texto').value = '';
        document.getElementById('novo-comentario-container').classList.remove('show');
        
        // Recarrega os comentários
        document.getElementById('comentarios-list').innerHTML = '';
        document.getElementById('no-comentarios').style.display = 'none';
        carregarComentarios();

        alert('Comentário publicado com sucesso!');

    } catch (error) {
        console.error('Erro ao publicar comentário:', error);
        alert('Erro ao publicar comentário. Tente novamente.');
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
