// Verifica autenticação
const usuarioId = localStorage.getItem('usuarioId');
const usuarioPerfil = localStorage.getItem('usuarioPerfil');
const usuarioNome = localStorage.getItem('usuarioNome');

if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
    window.location.href = 'login-professor.html';
}

// Pega o ID da turma da URL
const urlParams = new URLSearchParams(window.location.search);
const turmaId = urlParams.get('id');

if (!turmaId) {
    alert('Turma não encontrada!');
    window.location.href = 'turmas-professor.html';
}

// Carrega informações do usuário
document.getElementById('userName').textContent = `Professor - ${usuarioNome}`;

// Toggle sidebar
document.getElementById('menuToggle').addEventListener('click', function() {
    document.getElementById('sidebar').classList.toggle('collapsed');
});

// Logout
function handleLogout() {
    localStorage.clear();
    window.location.href = 'login-professor.html';
}

// Carrega dados da turma
async function carregarTurma() {
    try {
        const response = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar turma');
        }
        
        const turma = await response.json();
        
        // Atualiza o nome da turma
        document.getElementById('turmaNome').textContent = turma.titulo;
        
        // Atualiza o código da turma
        document.getElementById('codigoTurma').textContent = turma.codigoAcesso;
        
        // Carrega os avisos da turma
        carregarAvisos();
        carregarAtividades();
        
    } catch (error) {
        console.error('Erro ao carregar turma:', error);
        alert('Erro ao carregar informações da turma');
    }
}

// Carrega avisos da turma
async function carregarAvisos() {
    try {
        const response = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/avisos`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar avisos');
        }
        
        const avisos = await response.json();
        const avisosList = document.getElementById('avisosList');
        avisosList.innerHTML = '';
        
        avisos.forEach(aviso => {
            // Ao carregar da API, a lista vem ordenada do mais recente para o mais antigo.
            // Para manter a ordem correta na tela (mais recente em cima), inserimos em ordem
            // usando append quando a lista já está na ordem desejada.
            adicionarAvisoNaLista(aviso, false);
        });
        
    } catch (error) {
        console.error('Erro ao carregar avisos:', error);
    }
}

// Eventos dos botões
let arquivoSelecionado = null;
// Mapa temporário de blob URLs para atividades (atividadeId -> blobUrl)
const atividadeArquivoBlobMap = new Map();
// blob URL para pré-visualização no formulário de criar atividade
let arquivoAtividadePreviewUrl = null;

// Helper: read File -> data URL (base64) promise
function readFileAsDataURL(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

document.getElementById('btnAdicionarAviso').addEventListener('click', function() {
    // Esconde o botão e mostra o formulário
    document.getElementById('btnAdicionarAviso').style.display = 'none';
    document.getElementById('formAviso').style.display = 'block';
});

// Quando selecionar arquivo
document.getElementById('arquivoAviso').addEventListener('change', function(e) {
    if (e.target.files.length > 0) {
        arquivoSelecionado = e.target.files[0];
        const url = URL.createObjectURL(arquivoSelecionado);
        document.getElementById('nomeArquivo').textContent = arquivoSelecionado.name;
        document.getElementById('linkArquivo').href = url;
        document.getElementById('arquivoSelecionado').style.display = 'flex';
    }
});

// Remover arquivo
document.getElementById('btnRemoverArquivo').addEventListener('click', function(e) {
    e.preventDefault();
    const linkArquivo = document.getElementById('linkArquivo');
    if (linkArquivo.href.startsWith('blob:')) {
        URL.revokeObjectURL(linkArquivo.href);
    }
    arquivoSelecionado = null;
    document.getElementById('arquivoAviso').value = '';
    document.getElementById('linkArquivo').href = '#';
    document.getElementById('arquivoSelecionado').style.display = 'none';
});

document.getElementById('btnCancelarAviso').addEventListener('click', function() {
    // Limpa o formulário
    document.getElementById('textoAviso').value = '';
    document.getElementById('arquivoAviso').value = '';
    document.getElementById('arquivoSelecionado').style.display = 'none';
    arquivoSelecionado = null;
    
    // Esconde o formulário e mostra o botão
    document.getElementById('formAviso').style.display = 'none';
    document.getElementById('btnAdicionarAviso').style.display = 'flex';
});

document.getElementById('btnPostarAviso').addEventListener('click', async function() {
    const textoAviso = document.getElementById('textoAviso').value.trim();
    const professorId = localStorage.getItem('usuarioId');
    
    if (!textoAviso) {
        alert('Por favor, digite uma mensagem para o aviso.');
        return;
    }
    
    // Prepara os dados do aviso
    const avisoRequest = {
        mensagem: textoAviso,
        arquivoPath: arquivoSelecionado ? arquivoSelecionado.name : null,
        arquivoConteudo: null
    };
    
    // Se tiver arquivo, converte para base64
    if (arquivoSelecionado) {
        try {
            const base64 = await fileToBase64(arquivoSelecionado);
            avisoRequest.arquivoConteudo = base64;
        } catch (e) {
            console.error('Erro ao converter arquivo para base64:', e);
        }
    }
    
    // Envia para o backend
    fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/avisos?professorId=${professorId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(avisoRequest)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao criar aviso');
        }
        return response.json();
    })
    .then(aviso => {
        console.log('Aviso criado:', aviso); // Debug
        
        // Adiciona URL do arquivo local se foi anexado
        if (arquivoSelecionado) {
            aviso.arquivoUrl = URL.createObjectURL(arquivoSelecionado);
        }
        
        // Adiciona o aviso na lista
        adicionarAvisoNaLista(aviso);
        
        // Limpa o formulário
        document.getElementById('textoAviso').value = '';
        document.getElementById('arquivoAviso').value = '';
        document.getElementById('linkArquivo').href = '#';
        document.getElementById('arquivoSelecionado').style.display = 'none';
        arquivoSelecionado = null;
        
        // Esconde o formulário e mostra o botão
        document.getElementById('formAviso').style.display = 'none';
        document.getElementById('btnAdicionarAviso').style.display = 'flex';
    })
    .catch(error => {
        console.error('Erro ao postar aviso:', error);
        alert('Erro ao postar aviso. Tente novamente.');
    });
});

// Função para converter arquivo em base64
function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

function adicionarAvisoNaLista(aviso, prepend = true) {
    const avisosList = document.getElementById('avisosList');
    
    const avisoCard = document.createElement('div');
    avisoCard.className = 'aviso-card';
    
    // Verifica se há arquivo válido
    const arquivoUrl = aviso.arquivoUrl || null;
    const temArquivoValido = arquivoUrl && (arquivoUrl.startsWith('data:') || arquivoUrl.startsWith('blob:') || arquivoUrl.startsWith('http') || arquivoUrl.startsWith('/'));
    
    // Constrói o HTML do arquivo
    let arquivoHtml = '';
    if (aviso.arquivoPath) {
        if (temArquivoValido) {
            arquivoHtml = `
                <a href="${arquivoUrl}" class="aviso-arquivo arquivo-download-link" target="_blank" download="${aviso.arquivoPath}">
                    <i class="fas fa-paperclip"></i>
                    <span>${aviso.arquivoPath}</span>
                </a>
            `;
        } else {
            arquivoHtml = `
                <div class="aviso-arquivo arquivo-sem-download">
                    <i class="fas fa-paperclip"></i>
                    <span>${aviso.arquivoPath}</span>
                </div>
            `;
        }
    }
    
    avisoCard.innerHTML = `
        <div class="aviso-header">
            <div class="aviso-autor">
                <i class="fas fa-user-circle"></i>
                <div class="aviso-autor-info">
                    <strong>${aviso.professorNome || 'Professor'}</strong>
                </div>
            </div>
            <div style="position: relative;">
                <button class="btn-menu-aviso" data-aviso-id="${aviso.avisoId}">
                    <i class="fas fa-ellipsis-v"></i>
                </button>
                <div class="menu-dropdown-aviso">
                    <button class="edit-option">
                        <i class="fas fa-edit"></i>
                        <span>Editar</span>
                    </button>
                    <button class="delete-option">
                        <i class="fas fa-trash"></i>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        </div>
        <div class="aviso-body">
            <p>${aviso.mensagem || ''}</p>
            ${arquivoHtml}
        </div>
        <div class="aviso-footer">
            <span class="aviso-data">${aviso.dataPostagem || ''}</span>
        </div>
    `;
    
    // Se prepend=true adiciona no início, caso contrário adiciona ao fim.
    if (prepend) {
        avisosList.insertBefore(avisoCard, avisosList.firstChild);
    } else {
        avisosList.appendChild(avisoCard);
    }
    
    // Adiciona evento ao botão de menu
    const btnMenu = avisoCard.querySelector('.btn-menu-aviso');
    const dropdown = avisoCard.querySelector('.menu-dropdown-aviso');
    
    btnMenu.addEventListener('click', function(e) {
        e.stopPropagation();
        
        // Fecha outros dropdowns abertos
        document.querySelectorAll('.menu-dropdown-aviso.show').forEach(menu => {
            if (menu !== dropdown) {
                menu.classList.remove('show');
            }
        });
        
        // Toggle do dropdown atual
        dropdown.classList.toggle('show');
    });
    
    // Botão Editar
    const btnEditar = avisoCard.querySelector('.edit-option');
    btnEditar.addEventListener('click', function(e) {
        e.stopPropagation();
        dropdown.classList.remove('show');
        abrirModalEditar(aviso);
    });
    
    // Botão Excluir
    const btnExcluir = avisoCard.querySelector('.delete-option');
    btnExcluir.addEventListener('click', async function(e) {
        e.stopPropagation();
        dropdown.classList.remove('show');

        const confirmar = confirm('Tem certeza que deseja excluir este aviso? Esta ação não pode ser desfeita.');
        if (!confirmar) return;

        try {
            const resp = await fetch(
                `http://localhost:8080/api/professor/turmas/${turmaId}/avisos/${aviso.avisoId}?professorId=${usuarioId}`,
                { method: 'DELETE' }
            );

            if (!resp.ok) {
                const err = await resp.json().catch(() => ({}));
                throw new Error(err.message || 'Erro ao excluir aviso');
            }

            alert('Aviso excluído com sucesso!');
            // Recarrega a lista de avisos
            carregarAvisos();
        } catch (error) {
            console.error('Erro ao excluir aviso:', error);
            alert('Erro ao excluir aviso: ' + (error.message || error));
        }
    });
}

// Variáveis para modal de edição
let avisoEmEdicao = null;
let arquivoEditarUrl = null;
let arquivoFoiRemovido = false;
let atividadeEmEdicao = null;
let arquivoAtividadeEditarUrl = null;
let arquivoAtividadeFoiRemovido = false;

// Abre modal de edição
function abrirModalEditar(aviso) {
    avisoEmEdicao = aviso;
    arquivoFoiRemovido = false;
    
    // Preenche a mensagem
    document.getElementById('textoAvisoEditar').value = aviso.mensagem;
    
    // Reseta o input de arquivo
    document.getElementById('arquivoAvisoEditar').value = '';
    
    // Se tem arquivo anexado
    const arquivoAnexado = document.getElementById('arquivoAnexadoEditar');
    const nomeArquivoEditar = document.getElementById('nomeArquivoEditar');
    
    if (aviso.arquivoPath) {
        arquivoAnexado.style.display = 'flex';
        nomeArquivoEditar.textContent = aviso.arquivoPath;
        arquivoEditarUrl = aviso.arquivoUrl;
    } else {
        arquivoAnexado.style.display = 'none';
        arquivoEditarUrl = null;
    }
    
    // Mostra o modal centralizado
    const modal = document.getElementById('modalEditarAviso');
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
}

// Fecha modal de edição
function fecharModalEditar() {
    document.getElementById('modalEditarAviso').style.display = 'none';
    document.getElementById('textoAvisoEditar').value = '';
    document.getElementById('arquivoAnexadoEditar').style.display = 'none';
    document.getElementById('arquivoAvisoEditar').value = '';
    avisoEmEdicao = null;
    arquivoEditarUrl = null;
    arquivoFoiRemovido = false;
}

// Eventos do modal de edição
document.getElementById('btnFecharModalEditar').addEventListener('click', fecharModalEditar);
document.getElementById('btnCancelarEditar').addEventListener('click', fecharModalEditar);

// Remover arquivo do modal de edição
document.getElementById('btnRemoverArquivoEditar').addEventListener('click', function() {
    document.getElementById('arquivoAnexadoEditar').style.display = 'none';
    document.getElementById('arquivoAvisoEditar').value = '';
    if (arquivoEditarUrl) {
        URL.revokeObjectURL(arquivoEditarUrl);
        arquivoEditarUrl = null;
    }
    arquivoFoiRemovido = true;
});

// Adicionar novo arquivo no modal de edição
document.getElementById('arquivoAvisoEditar').addEventListener('change', function(e) {
    const arquivo = e.target.files[0];
    if (arquivo) {
        // Remove arquivo anterior se existir
        if (arquivoEditarUrl) {
            URL.revokeObjectURL(arquivoEditarUrl);
        }
        
        // Cria blob URL para o novo arquivo
        arquivoEditarUrl = URL.createObjectURL(arquivo);
        
        // Mostra o arquivo anexado
        document.getElementById('nomeArquivoEditar').textContent = arquivo.name;
        document.getElementById('arquivoAnexadoEditar').style.display = 'flex';
    }
});

// Salvar edição
document.getElementById('btnSalvarEditar').addEventListener('click', async function() {
    const mensagem = document.getElementById('textoAvisoEditar').value.trim();
    
    if (!mensagem) {
        alert('Digite uma mensagem para o aviso!');
        return;
    }
    
    try {
        // Pega o arquivo se foi alterado/adicionado
        const inputArquivo = document.getElementById('arquivoAvisoEditar');
        let arquivoPath = null;
        
        if (inputArquivo.files.length > 0) {
            // Novo arquivo selecionado
            arquivoPath = inputArquivo.files[0].name;
        } else if (!arquivoFoiRemovido && avisoEmEdicao.arquivoPath) {
            // Mantém o arquivo existente (só se não foi removido)
            arquivoPath = avisoEmEdicao.arquivoPath;
        }
        // Se arquivoFoiRemovido = true e não tem novo arquivo, arquivoPath = null
        
        // Requisição PUT para atualizar o aviso
        const response = await fetch(
            `http://localhost:8080/api/professor/turmas/${turmaId}/avisos/${avisoEmEdicao.avisoId}?professorId=${usuarioId}`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    mensagem: mensagem,
                    arquivoPath: arquivoPath
                })
            }
        );
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Erro ao atualizar aviso');
        }
        
        const avisoAtualizado = await response.json();
        
        // Se foi adicionado novo arquivo, cria blob URL
        if (inputArquivo.files.length > 0) {
            avisoAtualizado.arquivoUrl = URL.createObjectURL(inputArquivo.files[0]);
        }
        
        // Fecha o modal
        fecharModalEditar();
        
        // Mostra mensagem de sucesso
        alert('Aviso atualizado com sucesso!');
        
        // Recarrega os avisos para exibir a atualização
        carregarAvisos();
        
    } catch (error) {
        console.error('Erro ao salvar aviso:', error);
        alert('Erro ao salvar aviso: ' + error.message);
    }
});

// Fecha modal ao clicar fora
document.getElementById('modalEditarAviso').addEventListener('click', function(e) {
    if (e.target === this) {
        fecharModalEditar();
    }
});

document.getElementById('btnAdicionarAtividade').addEventListener('click', function() {
    // Toggle do formulário de atividade
    const form = document.getElementById('formAtividade');
    const willShow = form.style.display === 'none' || form.style.display === '';
    form.style.display = willShow ? 'block' : 'none';

    // If showing, reset fields to avoid old data persisting
    if (willShow) {
        document.getElementById('tituloAtividade').value = '';
        document.getElementById('descricaoAtividade').value = '';
        document.getElementById('arquivoAtividade').value = '';
        document.getElementById('arquivoNomeAtividade').textContent = 'Nenhum arquivo';
        document.getElementById('btnRemoverArquivoAtividade').style.display = 'none';
        document.getElementById('temPrazo').checked = false;
        document.getElementById('prazoAtividade').value = '';
        document.getElementById('prazoAtividade').style.display = 'none';
    }
});

// Toggle campo prazo
document.getElementById('temPrazo').addEventListener('change', function(e) {
    document.getElementById('prazoAtividade').style.display = e.target.checked ? 'inline-block' : 'none';
});

// Cancelar atividade
document.getElementById('btnCancelarAtividade').addEventListener('click', function() {
    document.getElementById('tituloAtividade').value = '';
    document.getElementById('descricaoAtividade').value = '';
    document.getElementById('arquivoAtividade').value = '';
    document.getElementById('prazoAtividade').value = '';
    document.getElementById('temPrazo').checked = false;
    document.getElementById('prazoAtividade').style.display = 'none';
    document.getElementById('formAtividade').style.display = 'none';
    document.getElementById('arquivoNomeAtividade').textContent = 'Nenhum arquivo';
    document.getElementById('btnRemoverArquivoAtividade').style.display = 'none';
});

// Atualiza nome do arquivo exibido quando selecionado
document.getElementById('arquivoAtividade').addEventListener('change', function(e) {
    const f = e.target.files[0];
    const label = document.getElementById('arquivoNomeAtividade');
    // clear previous preview data URL if exists
    arquivoAtividadePreviewUrl = null;
    if (f) {
        // create data URL for preview (survives navigation)
        readFileAsDataURL(f).then(dataUrl => {
            arquivoAtividadePreviewUrl = dataUrl;
            label.innerHTML = `<a href="${dataUrl}" target="_blank">${f.name}</a>`;
        }).catch(err => {
            console.error('Erro ao ler arquivo para preview', err);
            label.textContent = f.name;
        });
    } else {
        label.textContent = 'Nenhum arquivo';
    }
    // Show remove button when a file is selected
    document.getElementById('btnRemoverArquivoAtividade').style.display = f ? 'inline-block' : 'none';
});

// Remover arquivo selecionado (antes de postar)
document.getElementById('btnRemoverArquivoAtividade').addEventListener('click', function(e) {
    e.preventDefault();
    const input = document.getElementById('arquivoAtividade');
    if (input.files && input.files.length > 0) {
        // If there's an object URL used elsewhere, revoke it (not used here)
    }
    input.value = '';
    // revoke preview blob url if exists
    arquivoAtividadePreviewUrl = null;
    document.getElementById('arquivoNomeAtividade').textContent = 'Nenhum arquivo';
    this.style.display = 'none';
});

// Postar atividade
document.getElementById('btnPostarAtividade').addEventListener('click', async function() {
    const titulo = document.getElementById('tituloAtividade').value.trim();
    const descricao = document.getElementById('descricaoAtividade').value.trim();
    const arquivoInput = document.getElementById('arquivoAtividade');
    const temPrazo = document.getElementById('temPrazo').checked;
    const prazoVal = temPrazo ? document.getElementById('prazoAtividade').value : null;

    if (!titulo) { alert('Título obrigatório'); return; }

    try {
        // Send prazo as local datetime string (from input datetime-local) or null
        const prazoToSend = (temPrazo && prazoVal) ? prazoVal : null;

        const body = {
            titulo: titulo,
            descricao: descricao || null,
            arquivoPath: arquivoInput.files.length > 0 ? arquivoInput.files[0].name : null,
            arquivoConteudo: null,
            prazo: prazoToSend
        };
        
        // Se tiver arquivo, converte para base64
        if (arquivoInput.files.length > 0) {
            try {
                const base64 = await fileToBase64(arquivoInput.files[0]);
                body.arquivoConteudo = base64;
            } catch (e) {
                console.error('Erro ao converter arquivo para base64:', e);
            }
        }

        let resp;
        let criado;
        
        // Sempre usa o endpoint JSON (com base64 no corpo)
        resp = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/atividades?professorId=${usuarioId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!resp.ok) { const err = await resp.json().catch(()=>({})); throw new Error(err.message || 'Erro ao criar atividade'); }

        criado = await resp.json();
        
        // Salva a URL base64 do arquivo no sessionStorage para uso imediato
        if (body.arquivoConteudo) {
            try { sessionStorage.setItem(`atividadeArquivo_${criado.atividadeId}`, body.arquivoConteudo); } catch(_) {}
            atividadeArquivoBlobMap.set(criado.atividadeId, body.arquivoConteudo);
            criado.arquivoUrl = body.arquivoConteudo;
        }
        
        alert('Atividade criada com sucesso!');
        // adicionar diretamente na lista (como feito para avisos) para tornar o link clicável
        adicionarAtividadeNaLista(criado, true);
        // fechar e limpar form
        document.getElementById('btnCancelarAtividade').click();
    } catch (error) {
        console.error('Erro ao criar atividade:', error);
        alert('Erro ao criar atividade: ' + (error.message || error));
    }
});

// Carrega atividades da turma e renderiza
async function carregarAtividades() {
    try {
        const resp = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/atividades`);
        if (!resp.ok) throw new Error('Erro ao carregar atividades');
        const atividades = await resp.json();

        // Procura ou cria container para atividades (abaixo dos avisos)
        let container = document.getElementById('atividadesList');
        if (!container) {
            container = document.createElement('div');
            container.id = 'atividadesList';
            container.className = 'atividades-list';
            const avisosList = document.getElementById('avisosList');
            avisosList.parentNode.insertBefore(container, avisosList);
        }

            // When loading from server, preserve server ordering: append in sequence
            container.innerHTML = '';
            atividades.forEach(atv => adicionarAtividadeNaLista(atv, false));
    } catch (error) {
        console.error('Erro ao carregar atividades:', error);
    }
}

function adicionarAtividadeNaLista(atv, prepend = true) {
    const container = document.getElementById('atividadesList');
    if (!container) return;

    const card = document.createElement('div');
    card.className = 'aviso-card atividade-card';
    card.style.cursor = 'pointer';
    
    // determine attachment href: prefer sessionStorage data URL, then in-memory map, then server URL
    let arquivoHref = null;
    try {
        const stored = sessionStorage.getItem('atividadeArquivo_' + atv.atividadeId);
        if (stored) arquivoHref = stored;
        else if (atividadeArquivoBlobMap.get(atv.atividadeId)) arquivoHref = atividadeArquivoBlobMap.get(atv.atividadeId);
        else if (atv.arquivoUrl) arquivoHref = atv.arquivoUrl;
    } catch (e) { arquivoHref = atv.arquivoUrl || null; }

    const isValidArquivoHref = arquivoHref && (arquivoHref.startsWith('data:') || arquivoHref.startsWith('http') || arquivoHref.startsWith('/'));
    
    // Constrói o HTML do arquivo
    let arquivoHtml = '';
    if (atv.arquivoPath) {
        if (isValidArquivoHref) {
            arquivoHtml = `
                <a href="${arquivoHref}" class="aviso-arquivo arquivo-download-link" target="_blank" download="${atv.arquivoPath}" onclick="event.stopPropagation();">
                    <i class="fas fa-paperclip"></i>
                    <span>${atv.arquivoPath}</span>
                </a>
            `;
        } else {
            arquivoHtml = `
                <div class="aviso-arquivo arquivo-sem-download" onclick="event.stopPropagation();">
                    <i class="fas fa-paperclip"></i>
                    <span>${atv.arquivoPath}</span>
                </div>
            `;
        }
    }

    card.innerHTML = `
        <div class="aviso-header">
            <div class="aviso-autor">
                <i class="fas fa-folder" style="color: var(--primary-color);"></i>
                <div class="aviso-autor-info">
                    <div class="aviso-titulo"><strong>${atv.titulo}</strong></div>
                    <small style="color: var(--text-secondary);">${atv.professorNome || 'Professor'}</small>
                </div>
            </div>
            <div style="position: relative;">
                <button class="btn-menu-aviso" data-atividade-id="${atv.atividadeId}" onclick="event.stopPropagation();">
                    <i class="fas fa-ellipsis-v"></i>
                </button>
                <div class="menu-dropdown-aviso">
                    <button class="edit-option">
                        <i class="fas fa-edit"></i>
                        <span>Editar</span>
                    </button>
                    <button class="delete-option">
                        <i class="fas fa-trash"></i>
                        <span>Excluir</span>
                    </button>
                </div>
            </div>
        </div>
        <div class="aviso-body">
            <p>${atv.descricao || ''}</p>
            ${arquivoHtml}
        </div>
        <div class="aviso-footer">
            <div class="aviso-destinatarios">
                ${atv.prazo ? `<span class="atividade-prazo"><i class="fas fa-clock"></i> Prazo: ${atv.prazo}</span>` : ''}
            </div>
            <div class="aviso-data">${atv.dataCriacao || ''}</div>
        </div>
    `;

    if (prepend) {
        container.insertBefore(card, container.firstChild);
    } else {
        container.appendChild(card);
    }

    // Menu behavior (same pattern as avisos)
    const btnMenu = card.querySelector('.btn-menu-aviso');
    const dropdown = card.querySelector('.menu-dropdown-aviso');

    btnMenu.addEventListener('click', function(e) {
        e.stopPropagation();
        document.querySelectorAll('.menu-dropdown-aviso.show').forEach(menu => {
            if (menu !== dropdown) menu.classList.remove('show');
        });
        dropdown.classList.toggle('show');
    });

    // Edit action (placeholder for now)
    const btnEditar = card.querySelector('.edit-option');
    btnEditar.addEventListener('click', function(e) {
        e.stopPropagation();
        dropdown.classList.remove('show');
        abrirModalEditarAtividade(atv);
    });

    // Make entire card clickable (except menu and links) to open details page
    card.addEventListener('click', function(e) {
        // ignore clicks on menu button, dropdown, or attachment link
        if (e.target.closest('.btn-menu-aviso') || e.target.closest('.menu-dropdown-aviso') || e.target.closest('.aviso-arquivo')) return;
        // attach arquivoUrl from sessionStorage (persisted dataURL) or blob map so new page can open the file
        const atvCopy = Object.assign({}, atv);
        try {
            const sessKey = `atividadeArquivo_${atv.atividadeId}`;
            const sessVal = sessionStorage.getItem(sessKey);
            if (sessVal) {
                atvCopy.arquivoUrl = sessVal;
            } else {
                const mapped = atividadeArquivoBlobMap.get(atv.atividadeId);
                if (mapped) atvCopy.arquivoUrl = mapped;
            }
        } catch (err) {
            const mapped = atividadeArquivoBlobMap.get(atv.atividadeId);
            if (mapped) atvCopy.arquivoUrl = mapped;
        }
        // store in sessionStorage and navigate to new page
        try {
            sessionStorage.setItem('atividadeDetalhes', JSON.stringify(atvCopy));
        } catch (err) {
            console.warn('Não foi possível salvar atividade em sessionStorage', err);
        }
        window.location.href = 'atividade-detalhes.html';
    });

    // Delete action
    const btnExcluir = card.querySelector('.delete-option');
    btnExcluir.addEventListener('click', async function(e) {
        e.stopPropagation();
        dropdown.classList.remove('show');
        const confirmar = confirm('Tem certeza que deseja excluir esta atividade?');
        if (!confirmar) return;
        try {
            const resp = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/atividades/${atv.atividadeId}?professorId=${usuarioId}`, { method: 'DELETE' });
            if (!resp.ok) {
                const err = await resp.json().catch(()=>({}));
                throw new Error(err.message || 'Erro ao excluir atividade');
            }
            alert('Atividade excluída com sucesso!');
            carregarAtividades();
        } catch (err) {
            console.error('Erro excluir atividade', err);
            alert('Erro ao excluir atividade: ' + (err.message || err));
        }
    });
}

// Fecha dropdowns quando clicar fora
document.addEventListener('click', function() {
    document.querySelectorAll('.menu-dropdown-aviso.show').forEach(menu => {
        menu.classList.remove('show');
    });
});

// ---------- Modal Editar Atividade handlers ----------
function abrirModalEditarAtividade(atv) {
    atividadeEmEdicao = atv;
    arquivoAtividadeFoiRemovido = false;

    document.getElementById('tituloAtividadeEditar').value = atv.titulo || '';
    document.getElementById('descricaoAtividadeEditar').value = atv.descricao || '';

    // arquivo
    const arquivoAnexado = document.getElementById('arquivoAnexadoAtividadeEditar');
    const nomeArquivo = document.getElementById('nomeArquivoAtividadeEditar');
    if (atv.arquivoPath) {
        arquivoAnexado.style.display = 'flex';
        // Prefer persisted dataURL in sessionStorage, then in-memory map
        let href = null;
        try { href = sessionStorage.getItem('atividadeArquivo_' + atv.atividadeId) || atividadeArquivoBlobMap.get(atv.atividadeId); } catch(_) { href = atividadeArquivoBlobMap.get(atv.atividadeId); }
        if (href) {
            nomeArquivo.innerHTML = `<a href="${href}" target="_blank">${atv.arquivoPath}</a>`;
        } else {
            nomeArquivo.textContent = atv.arquivoPath;
        }
        arquivoAtividadeEditarUrl = null; // no new blob/data URL selected yet
    } else {
        arquivoAnexado.style.display = 'none';
    }

    // prazo
    const temPrazo = !!atv.prazoIso;
    document.getElementById('temPrazoEditar').checked = temPrazo;
    const prazoInput = document.getElementById('prazoAtividadeEditar');
    if (temPrazo && atv.prazoIso) {
        let iso = atv.prazoIso;
        if (iso.length > 16) iso = iso.substring(0,16);
        prazoInput.value = iso;
        prazoInput.style.display = 'inline-block';
    } else {
        prazoInput.value = '';
        prazoInput.style.display = 'none';
    }

    // reset file input
    document.getElementById('arquivoAtividadeEditar').value = '';

    // Mostra o modal centralizado
    const modal = document.getElementById('modalEditarAtividade');
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
}

function fecharModalEditarAtividade() {
    document.getElementById('modalEditarAtividade').style.display = 'none';
    document.getElementById('tituloAtividadeEditar').value = '';
    document.getElementById('descricaoAtividadeEditar').value = '';
    document.getElementById('arquivoAtividadeEditar').value = '';
    document.getElementById('arquivoAnexadoAtividadeEditar').style.display = 'none';
    document.getElementById('temPrazoEditar').checked = false;
    document.getElementById('prazoAtividadeEditar').value = '';
    document.getElementById('prazoAtividadeEditar').style.display = 'none';
    atividadeEmEdicao = null;
    arquivoAtividadeEditarUrl = null;
    arquivoAtividadeFoiRemovido = false;
}

document.getElementById('btnFecharModalAtividadeEditar').addEventListener('click', fecharModalEditarAtividade);
document.getElementById('btnCancelarAtividadeEditar').addEventListener('click', fecharModalEditarAtividade);

// Remover arquivo do modal
document.getElementById('btnRemoverArquivoAtividadeEditar').addEventListener('click', function() {
    document.getElementById('arquivoAnexadoAtividadeEditar').style.display = 'none';
    document.getElementById('arquivoAtividadeEditar').value = '';
    arquivoAtividadeEditarUrl = null;
    arquivoAtividadeFoiRemovido = true;
});

// Novo arquivo no modal
document.getElementById('arquivoAtividadeEditar').addEventListener('change', function(e) {
    const f = e.target.files[0];
    if (f) {
        document.getElementById('nomeArquivoAtividadeEditar').textContent = f.name;
        document.getElementById('arquivoAnexadoAtividadeEditar').style.display = 'flex';
        // create persisted data URL for the selected file
        readFileAsDataURL(f).then(dataUrl => {
            arquivoAtividadeEditarUrl = dataUrl;
            arquivoAtividadeFoiRemovido = false;
        }).catch(err => {
            console.error('Erro ao ler arquivo no modal editar', err);
            arquivoAtividadeEditarUrl = null;
            arquivoAtividadeFoiRemovido = false;
        });
    }
});

// Toggle prazo input in modal
document.getElementById('temPrazoEditar').addEventListener('change', function(e) {
    document.getElementById('prazoAtividadeEditar').style.display = e.target.checked ? 'inline-block' : 'none';
});

// Salvar edição da atividade
document.getElementById('btnSalvarAtividadeEditar').addEventListener('click', async function() {
    if (!atividadeEmEdicao) return;
    const titulo = document.getElementById('tituloAtividadeEditar').value.trim();
    if (!titulo) { alert('Título obrigatório'); return; }

    const descricao = document.getElementById('descricaoAtividadeEditar').value.trim() || null;
    const inputArquivo = document.getElementById('arquivoAtividadeEditar');
    let arquivoPath = null;
    if (inputArquivo.files.length > 0) {
        arquivoPath = inputArquivo.files[0].name;
    } else if (!arquivoAtividadeFoiRemovido && atividadeEmEdicao.arquivoPath) {
        arquivoPath = atividadeEmEdicao.arquivoPath;
    }

    const temPrazo = document.getElementById('temPrazoEditar').checked;
    const prazoVal = temPrazo ? document.getElementById('prazoAtividadeEditar').value : null;

    try {
        const body = {
            titulo: titulo,
            descricao: descricao,
            arquivoPath: arquivoPath,
            prazo: prazoVal ? prazoVal : null
        };

        const resp = await fetch(`http://localhost:8080/api/professor/turmas/${turmaId}/atividades/${atividadeEmEdicao.atividadeId}?professorId=${usuarioId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!resp.ok) {
            const err = await resp.json().catch(()=>({}));
            throw new Error(err.message || 'Erro ao atualizar atividade');
        }

        // If a new file was selected in the modal and we have a data URL, persist it for this atividadeId
        if (arquivoAtividadeEditarUrl) {
            try { sessionStorage.setItem('atividadeArquivo_' + atividadeEmEdicao.atividadeId, arquivoAtividadeEditarUrl); } catch(_) {}
            atividadeArquivoBlobMap.set(atividadeEmEdicao.atividadeId, arquivoAtividadeEditarUrl);
            arquivoAtividadeEditarUrl = null;
        }
        alert('Atividade atualizada com sucesso!');
        fecharModalEditarAtividade();
        carregarAtividades();
    } catch (err) {
        console.error('Erro ao atualizar atividade', err);
        alert('Erro ao atualizar atividade: ' + (err.message || err));
    }
});

// ---------- Modal Detalhes Atividade handlers ----------
function abrirModalDetalhesAtividade(atv) {
    // populate fields
    document.getElementById('detalhesTitulo').textContent = atv.titulo || 'Atividade';
    document.getElementById('detalhesTituloCentro').textContent = atv.titulo || '';
    const turmaNome = document.getElementById('turmaNome') ? document.getElementById('turmaNome').textContent : '';
    document.getElementById('detalhesProfessorTurma').textContent = `${atv.professorNome || 'Professor'} • ${turmaNome}`;
    document.getElementById('detalhesDataPostagem').textContent = atv.dataCriacao || '';
    document.getElementById('detalhesDescricao').textContent = atv.descricao || '';
    const arquivoHref = atividadeArquivoBlobMap.get(atv.atividadeId) || atv.arquivoUrl || '#';
    const arquivoLink = document.getElementById('detalhesArquivoLink');
    const arquivoNomeEl = document.getElementById('detalhesArquivoNome');
    if (atv.arquivoPath) {
        arquivoLink.href = arquivoHref;
        arquivoNomeEl.textContent = atv.arquivoPath;
        arquivoLink.style.display = 'inline-flex';
    } else {
        arquivoLink.style.display = 'none';
    }
    // prazo
    document.getElementById('detalhesPrazo').textContent = atv.prazo || '--';
    // entregues/pendentes placeholders
    document.getElementById('detalhesEntregues').textContent = '0';
    document.getElementById('detalhesPendentes').textContent = '0';

    document.getElementById('modalDetalhesAtividade').style.display = 'flex';
}

function fecharModalDetalhesAtividade() {
    document.getElementById('modalDetalhesAtividade').style.display = 'none';
}

document.getElementById('btnFecharModalDetalhes').addEventListener('click', fecharModalDetalhesAtividade);
document.getElementById('btnFecharModalDetalhesFooter').addEventListener('click', fecharModalDetalhesAtividade);
document.getElementById('modalDetalhesAtividade').addEventListener('click', function(e) {
    if (e.target === this) fecharModalDetalhesAtividade();
});

// Carrega a turma ao iniciar
carregarTurma();
