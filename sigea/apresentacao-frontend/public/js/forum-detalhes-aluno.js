// VariÃ¡veis globais
let disciplinaId = null;
let disciplinaNome = null;

// VerificaÃ§Ã£o de autenticaÃ§Ã£o e inicializaÃ§Ã£o
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ALUNO') {
        alert('VocÃª precisa fazer login como ALUNO para acessar esta pÃ¡gina.');
        window.location.href = '/index.html';
        return;
    }

    // Recupera informaÃ§Ãµes da disciplina da URL
    const urlParams = new URLSearchParams(window.location.search);
    disciplinaId = urlParams.get('disciplinaId');
    disciplinaNome = localStorage.getItem('forumDisciplinaNome') || 'Disciplina';

    if (!disciplinaId) {
        alert('Disciplina nÃ£o especificada.');
        window.location.href = '/forum-aluno.html';
        return;
    }

    loadUserInfo();
    atualizarTitulo();
    carregarTopicos();

    // Event listener para o formulÃ¡rio de novo tÃ³pico
    document.getElementById('formNovoTopico').addEventListener('submit', handleNovoTopico);
    
    // Event listener para mostrar nome do arquivo selecionado com botÃ£o X para remover
    document.getElementById('arquivoTopico').addEventListener('change', function(e) {
        const nomeArquivo = e.target.files[0]?.name || '';
        const arquivoNomeElement = document.getElementById('arquivo-nome');
        if (nomeArquivo) {
            arquivoNomeElement.innerHTML = `<span class="arquivo-selecionado"><i class="fas fa-paperclip"></i> ${nomeArquivo} <button type="button" class="btn-remover-arquivo" onclick="removerArquivoSelecionado()" title="Remover arquivo"><i class="fas fa-times"></i></button></span>`;
        } else {
            arquivoNomeElement.innerHTML = '';
        }
    });
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

function atualizarTitulo() {
    const tituloElement = document.getElementById('forum-titulo');
    tituloElement.textContent = `FÃ³rum: ${disciplinaNome}`;
}

function voltarParaForuns() {
    window.location.href = '/forum-aluno.html';
}

async function carregarTopicos() {
    const loadingElement = document.getElementById('loading');
    const topicosListElement = document.getElementById('topicos-list');
    const noTopicosElement = document.getElementById('no-topicos');
    const usuarioId = localStorage.getItem('usuarioId');

    console.log('=== DEBUG CARREGAR TÃ“PICOS ===');
    console.log('Disciplina ID:', disciplinaId);
    console.log('Usuario ID:', usuarioId);

    try {
        loadingElement.style.display = 'block';
        topicosListElement.style.display = 'none';
        noTopicosElement.style.display = 'none';

        const url = `/api/foruns/topicos?disciplinaId=${disciplinaId}&usuarioId=${usuarioId}`;
        console.log('URL da requisiÃ§Ã£o:', url);

        const response = await fetch(url, {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        console.log('Status da resposta:', response.status);
        console.log('Response OK?', response.ok);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro HTTP:', response.status);
            console.error('Mensagem de erro:', errorText);
            
            // Se for 404 ou 500, trata como lista vazia (sem tÃ³picos)
            if (response.status === 404 || response.status === 500) {
                loadingElement.style.display = 'none';
                noTopicosElement.style.display = 'block';
                return;
            }
            
            throw new Error('Erro ao carregar tÃ³picos: ' + response.status);
        }

        const topicos = await response.json();
        console.log('TÃ³picos recebidos:', topicos);
        console.log('Quantidade de tÃ³picos:', topicos.length);

        loadingElement.style.display = 'none';

        if (!topicos || topicos.length === 0) {
            noTopicosElement.style.display = 'block';
        } else {
            topicosListElement.style.display = 'block';
            renderizarTopicos(topicos);
        }

    } catch (error) {
        console.error('=== ERRO AO CARREGAR TÃ“PICOS ===');
        console.error('Tipo do erro:', error.name);
        console.error('Mensagem:', error.message);
        console.error('Stack:', error.stack);
        
        loadingElement.style.display = 'none';
        noTopicosElement.style.display = 'block';
        noTopicosElement.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <h3>Erro ao carregar tÃ³picos</h3>
            <p>Verifique o console (F12) para mais detalhes.</p>
            <p style="font-size: 0.875rem; color: #999;">Erro: ${error.message}</p>
        `;
    }
}

function renderizarTopicos(topicos) {
    const topicosListElement = document.getElementById('topicos-list');
    topicosListElement.innerHTML = '';

    topicos.forEach(topico => {
        const card = criarCardTopico(topico);
        topicosListElement.appendChild(card);
    });
}

function criarCardTopico(topico) {
    const card = document.createElement('div');
    card.className = 'topico-card';
    card.dataset.topicoId = topico.id;

    // Por enquanto, data fictÃ­cia (pode ser adicionada ao backend depois)
    const dataAtual = new Date().toLocaleDateString('pt-BR');

    // Ãcone de anexo se houver arquivo
    const anexoIcon = topico.arquivoPath ? '<i class="fas fa-paperclip" style="margin-left: 0.5rem; color: var(--primary-color);"></i>' : '';

    // Verifica se o usuÃ¡rio logado Ã© o autor do tÃ³pico
    const usuarioId = localStorage.getItem('usuarioId');
    const isAutor = topico.autorId === usuarioId || topico.autorId === parseInt(usuarioId) || String(topico.autorId) === usuarioId;
    const btnExcluir = isAutor ? `<button class="btn-excluir-topico" onclick="excluirTopico(event, '${topico.id}')" title="Excluir tÃ³pico"><i class="fas fa-times"></i></button>` : '';

    // Nome do autor - usa nomeAutor se disponÃ­vel, senÃ£o mostra o ID
    const nomeAutor = topico.nomeAutor || `UsuÃ¡rio ${topico.autorId}`;

    card.innerHTML = `
        <div class="topico-header">
            <div style="flex: 1;">
                <div class="topico-titulo">${escapeHtml(topico.titulo)}${anexoIcon}</div>
                <div class="topico-autor">
                    <i class="fas fa-user"></i>
                    Por: ${escapeHtml(nomeAutor)}
                </div>
            </div>
            ${btnExcluir}
        </div>
        <div class="topico-conteudo" onclick="abrirTopicoDetalhes('${topico.id}')">
            ${escapeHtml(topico.conteudo.substring(0, 200))}${topico.conteudo.length > 200 ? '...' : ''}
        </div>
        <div class="topico-footer" onclick="abrirTopicoDetalhes('${topico.id}')">
            <div class="topico-data">
                <i class="fas fa-calendar"></i>
                ${dataAtual}
            </div>
            <div class="topico-respostas">
                <i class="fas fa-comments"></i>
                ${topico.totalRespostas || 0} ${topico.totalRespostas === 1 ? 'resposta' : 'respostas'}
            </div>
        </div>
    `;

    return card;
}

function abrirTopicoDetalhes(topicoId) {
    // Navega para a pÃ¡gina de detalhes do tÃ³pico
    window.location.href = `topico-detalhes-aluno.html?topicoId=${topicoId}&disciplinaId=${disciplinaId}`;
}

// Modal de novo tÃ³pico
function abrirModalNovoTopico() {
    const modal = document.getElementById('modalNovoTopico');
    modal.classList.add('show');
    document.getElementById('formNovoTopico').reset();
    
    // Limpa o campo de arquivo e a exibiÃ§Ã£o do nome
    document.getElementById('arquivoTopico').value = '';
    document.getElementById('arquivo-nome').innerHTML = '';
}

function fecharModalNovoTopico() {
    const modal = document.getElementById('modalNovoTopico');
    modal.classList.remove('show');
    
    // Limpa o campo de arquivo ao fechar
    document.getElementById('arquivoTopico').value = '';
    document.getElementById('arquivo-nome').innerHTML = '';
}

// FunÃ§Ã£o para remover arquivo selecionado
function removerArquivoSelecionado() {
    document.getElementById('arquivoTopico').value = '';
    document.getElementById('arquivo-nome').innerHTML = '';
}

async function handleNovoTopico(event) {
    event.preventDefault();

    const titulo = document.getElementById('tituloTopico').value.trim();
    const conteudo = document.getElementById('conteudoTopico').value.trim();
    const arquivo = document.getElementById('arquivoTopico').files[0];
    const usuarioId = localStorage.getItem('usuarioId');

    if (!titulo || !conteudo) {
        alert('Por favor, preencha todos os campos obrigatÃ³rios.');
        return;
    }

    try {
        // Primeiro, faz upload do arquivo se houver
        let arquivoPath = null;
        if (arquivo) {
            const formData = new FormData();
            formData.append('file', arquivo);

            const uploadResponse = await fetch('/api/upload', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
                body: formData
            });

            if (uploadResponse.ok) {
                const uploadResult = await uploadResponse.json();
                arquivoPath = uploadResult.filePath || uploadResult.path;
                console.log('Arquivo enviado:', arquivoPath);
            } else {
                console.warn('Erro ao fazer upload do arquivo, continuando sem anexo');
            }
        }

        // Depois cria o tÃ³pico
        const response = await fetch('/api/foruns/topicos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: JSON.stringify({
                disciplinaId: disciplinaId,
                autorId: usuarioId,
                titulo: titulo,
                conteudo: conteudo,
                arquivoPath: arquivoPath
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro ao criar tÃ³pico:', errorText);
            throw new Error('Erro ao criar tÃ³pico');
        }

        const novoTopico = await response.json();
        console.log('TÃ³pico criado:', novoTopico);

        alert('TÃ³pico criado com sucesso!');
        fecharModalNovoTopico();
        carregarTopicos();

    } catch (error) {
        console.error('Erro ao criar tÃ³pico:', error);
        alert('Erro ao criar tÃ³pico. Tente novamente.');
    }
}

// FunÃ§Ã£o para excluir tÃ³pico
async function excluirTopico(event, topicoId) {
    event.stopPropagation(); // Evita abrir detalhes do tÃ³pico ao clicar no X
    
    if (!confirm('Tem certeza que deseja excluir este tÃ³pico?')) {
        return;
    }
    
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`/api/foruns/topicos/${topicoId}?usuarioId=${usuarioId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro ao excluir tÃ³pico:', errorText);
            throw new Error('Erro ao excluir tÃ³pico');
        }
        
        // Remove o card do DOM
        const card = document.querySelector(`[data-topico-id="${topicoId}"]`);
        if (card) {
            card.remove();
        }
        
        // Verifica se ainda hÃ¡ tÃ³picos
        const topicosListElement = document.getElementById('topicos-list');
        if (topicosListElement.children.length === 0) {
            topicosListElement.style.display = 'none';
            document.getElementById('no-topicos').style.display = 'block';
        }
        
        alert('TÃ³pico excluÃ­do com sucesso!');
        
    } catch (error) {
        console.error('Erro ao excluir tÃ³pico:', error);
        alert('Erro ao excluir tÃ³pico. Tente novamente.');
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

// Fechar modal ao clicar fora
window.onclick = function(event) {
    const modal = document.getElementById('modalNovoTopico');
    if (event.target === modal) {
        fecharModalNovoTopico();
    }
}

