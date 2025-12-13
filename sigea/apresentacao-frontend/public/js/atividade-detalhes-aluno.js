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

// Variáveis globais
let atividadeAtual = null;
let arquivoSelecionado = null;

// Inicialização
window.addEventListener('DOMContentLoaded', () => {
    if (!isLoginValido()) {
        limparDadosUsuario();
        window.location.href = '/index.html';
        return;
    }
    
    loadUserInfo();
    initializeMenuToggle();
    carregarAtividade();
    setupEventListeners();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const nomeValido = nome && nome !== 'null' && nome !== 'undefined' && nome.trim() !== '';
    const nomeExibir = nomeValido ? nome : 'Usuário';
    
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nomeExibir}`;
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

function carregarAtividade() {
    // Tenta carregar do sessionStorage
    try {
        atividadeAtual = JSON.parse(sessionStorage.getItem('atividadeDetalhesAluno'));
    } catch (e) {
        atividadeAtual = null;
    }
    
    if (!atividadeAtual) {
        document.getElementById('detTituloTopo').textContent = 'Atividade não encontrada';
        return;
    }
    
    // Preenche os dados da atividade
    document.getElementById('pageTitulo').textContent = atividadeAtual.titulo || 'Atividade';
    document.getElementById('detTituloTopo').textContent = atividadeAtual.titulo || '';
    document.getElementById('detTituloCentro').textContent = atividadeAtual.titulo || '';
    document.getElementById('detSubInfo').textContent = `${atividadeAtual.professorNome || 'Professor'}`;
    document.getElementById('detDescricao').textContent = atividadeAtual.descricao || '';
    document.getElementById('detDataPostagem').textContent = atividadeAtual.dataCriacao || '';
    document.getElementById('detPrazo').textContent = atividadeAtual.prazo || '--';
    
    // Arquivo da atividade
    const arquivoContainer = document.getElementById('detArquivoContainer');
    const arquivoLink = document.getElementById('detArquivoLink');
    const arquivoNome = document.getElementById('detArquivoNome');
    
    if (atividadeAtual.arquivoPath) {
        let href = atividadeAtual.arquivoUrl || null;
        const isValidHref = href && (href.startsWith('data:') || href.startsWith('http') || href.startsWith('/'));
        
        if (isValidHref) {
            arquivoLink.href = href;
            arquivoLink.setAttribute('download', atividadeAtual.arquivoPath);
            arquivoNome.textContent = atividadeAtual.arquivoPath;
            arquivoContainer.style.display = 'block';
        } else {
            // Mostra o nome do arquivo mas sem link
            arquivoContainer.innerHTML = `
                <div class="arquivo-sem-download">
                    <i class="fas fa-paperclip"></i>
                    <span>${atividadeAtual.arquivoPath}</span>
                    <small>(arquivo não disponível para download)</small>
                </div>
            `;
        }
    } else {
        arquivoContainer.style.display = 'none';
    }
    
    // Verifica status de envio
    verificarStatusEnvio();
    
    // Verifica se o prazo expirou
    verificarPrazoExpirado();
}

// Verifica se o prazo da atividade já passou
function verificarPrazoExpirado() {
    if (!atividadeAtual || !atividadeAtual.prazo) return false;
    
    // Parse do prazo (formato: dd/MM/yyyy HH:mm)
    const partes = atividadeAtual.prazo.split(' ');
    if (partes.length < 2) return false;
    
    const dataPartes = partes[0].split('/');
    const horaPartes = partes[1].split(':');
    
    if (dataPartes.length < 3 || horaPartes.length < 2) return false;
    
    const prazoDate = new Date(
        parseInt(dataPartes[2]), // ano
        parseInt(dataPartes[1]) - 1, // mês (0-indexed)
        parseInt(dataPartes[0]), // dia
        parseInt(horaPartes[0]), // hora
        parseInt(horaPartes[1]) // minuto
    );
    
    const agora = new Date();
    const prazoExpirado = agora > prazoDate;
    
    if (prazoExpirado) {
        // Marca o prazo como expirado visualmente
        document.getElementById('detPrazo').classList.add('prazo-expirado');
        document.getElementById('prazoExpiradoMsg').style.display = 'block';
        document.getElementById('envioBox').classList.add('envio-bloqueado');
        
        // Desabilita os controles de envio
        document.getElementById('btnSelecionarArquivo').style.display = 'none';
        document.getElementById('btnEnviar').style.display = 'none';
        document.getElementById('btnCancelar').style.display = 'none';
    }
    
    return prazoExpirado;
}

async function verificarStatusEnvio() {
    const alunoId = localStorage.getItem('usuarioId');
    const atividadeId = atividadeAtual.atividadeId;
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/atividades/${atividadeId}/envio`);
        
        if (response.ok) {
            const data = await response.json();
            atualizarStatusEnvio(data);
        }
    } catch (error) {
        console.error('Erro ao verificar status de envio:', error);
    }
}

function atualizarStatusEnvio(data) {
    const statusElement = document.getElementById('envioStatus');
    const dataEnvioInfo = document.getElementById('dataEnvioInfo');
    const arquivoInfo = document.getElementById('arquivoSelecionadoInfo');
    const btnCancelar = document.getElementById('btnCancelar');
    const prazoExpirado = verificarPrazoExpirado();
    
    // Elementos de nota
    const notaContainer = document.getElementById('notaContainer');
    const notaValor = document.getElementById('notaValor');
    const feedbackContainer = document.getElementById('feedbackContainer');
    const feedbackTexto = document.getElementById('feedbackTexto');
    
    if (data.enviado) {
        // Remove classes anteriores
        statusElement.classList.remove('nao-enviado', 'enviado', 'atrasado');
        
        if (data.status === 'ATRASADO') {
            statusElement.textContent = 'Enviado (Atrasado)';
            statusElement.classList.add('atrasado');
        } else if (data.status === 'CORRIGIDO') {
            statusElement.textContent = 'Corrigido';
            statusElement.classList.add('enviado');
            
            // Mostra a nota
            if (notaContainer) {
                notaContainer.style.display = 'block';
                notaContainer.classList.add('nota-status-corrigido');
                notaValor.textContent = data.nota !== null && data.nota !== undefined ? data.nota : 'N/A';
            }
            
            // Mostra feedback se existir
            if (data.feedback && feedbackContainer && feedbackTexto) {
                feedbackContainer.style.display = 'block';
                feedbackTexto.textContent = data.feedback;
            }
            
            // Esconde o botão de cancelar se já foi corrigido
            btnCancelar.style.display = 'none';
        } else {
            statusElement.textContent = 'Enviado';
            statusElement.classList.add('enviado');
            
            // Mostra botão de cancelar se não expirou e não foi corrigido
            if (!prazoExpirado) {
                btnCancelar.style.display = 'block';
            }
        }
        
        // Mostra informações do envio
        if (data.arquivoPath) {
            arquivoInfo.textContent = `Arquivo: ${data.arquivoPath}`;
            arquivoInfo.style.display = 'block';
        }
        
        if (data.dataEnvio) {
            dataEnvioInfo.textContent = `Enviado em: ${data.dataEnvio}`;
            dataEnvioInfo.style.display = 'block';
        }
        
        // Muda o texto do botão de seleção para reenviar (se não expirou)
        if (!prazoExpirado) {
            document.getElementById('btnSelecionarArquivo').innerHTML = '<i class="fas fa-redo"></i> Selecionar novo arquivo';
            document.getElementById('btnEnviar').textContent = 'Reenviar';
        }
    } else {
        statusElement.classList.remove('enviado', 'atrasado');
        statusElement.classList.add('nao-enviado');
        statusElement.textContent = 'Não enviado';
        btnCancelar.style.display = 'none';
    }
}

function setupEventListeners() {
    const inputArquivo = document.getElementById('inputArquivo');
    const btnSelecionar = document.getElementById('btnSelecionarArquivo');
    const btnEnviar = document.getElementById('btnEnviar');
    const btnCancelar = document.getElementById('btnCancelar');
    
    // Quando selecionar arquivo
    inputArquivo.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            arquivoSelecionado = e.target.files[0];
            
            // Atualiza o visual do botão
            btnSelecionar.classList.add('arquivo-selecionado');
            btnSelecionar.innerHTML = `<i class="fas fa-file"></i> ${arquivoSelecionado.name}`;
            
            // Habilita o botão de enviar
            btnEnviar.disabled = false;
        } else {
            arquivoSelecionado = null;
            btnSelecionar.classList.remove('arquivo-selecionado');
            btnSelecionar.innerHTML = '<i class="fas fa-upload"></i> Selecione seu arquivo';
            btnEnviar.disabled = true;
        }
    });
    
    // Quando clicar em enviar
    btnEnviar.addEventListener('click', enviarAtividade);
    
    // Quando clicar em cancelar
    btnCancelar.addEventListener('click', cancelarEnvio);
}

async function cancelarEnvio() {
    if (!confirm('Tem certeza que deseja cancelar o envio desta atividade?')) {
        return;
    }
    
    const alunoId = localStorage.getItem('usuarioId');
    const atividadeId = atividadeAtual.atividadeId;
    
    const btnCancelar = document.getElementById('btnCancelar');
    btnCancelar.disabled = true;
    btnCancelar.textContent = 'Cancelando...';
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/atividades/${atividadeId}/envio`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            const data = await response.json();
            alert(data.mensagem || 'Envio cancelado com sucesso!');
            
            // Reseta o status
            const statusElement = document.getElementById('envioStatus');
            statusElement.classList.remove('enviado', 'atrasado');
            statusElement.classList.add('nao-enviado');
            statusElement.textContent = 'Não enviado';
            
            // Esconde informações do envio
            document.getElementById('arquivoSelecionadoInfo').style.display = 'none';
            document.getElementById('dataEnvioInfo').style.display = 'none';
            
            // Esconde botão de cancelar
            btnCancelar.style.display = 'none';
            
            // Reseta o formulário
            document.getElementById('btnSelecionarArquivo').innerHTML = '<i class="fas fa-upload"></i> Selecione seu arquivo';
            document.getElementById('btnSelecionarArquivo').classList.remove('arquivo-selecionado');
            document.getElementById('btnEnviar').textContent = 'Enviar';
            document.getElementById('btnEnviar').disabled = true;
            document.getElementById('inputArquivo').value = '';
            arquivoSelecionado = null;
        } else {
            const error = await response.json();
            alert(error.erro || 'Erro ao cancelar envio.');
        }
    } catch (error) {
        console.error('Erro ao cancelar envio:', error);
        alert('Erro ao conectar com o servidor.');
    } finally {
        btnCancelar.disabled = false;
        btnCancelar.innerHTML = '<i class="fas fa-times"></i> Cancelar Envio';
    }
}

async function enviarAtividade() {
    if (!arquivoSelecionado) {
        alert('Por favor, selecione um arquivo para enviar.');
        return;
    }
    
    // Verifica prazo antes de enviar
    if (verificarPrazoExpirado()) {
        alert('O prazo para envio desta atividade já expirou.');
        return;
    }
    
    const alunoId = localStorage.getItem('usuarioId');
    const atividadeId = atividadeAtual.atividadeId;
    
    const formData = new FormData();
    formData.append('arquivo', arquivoSelecionado);
    
    const btnEnviar = document.getElementById('btnEnviar');
    btnEnviar.disabled = true;
    btnEnviar.textContent = 'Enviando...';
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/atividades/${atividadeId}/enviar`, {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            const data = await response.json();
            alert(data.mensagem || 'Atividade enviada com sucesso!');
            
            // Atualiza o status na tela
            atualizarStatusEnvio({
                enviado: true,
                status: data.status,
                arquivoPath: data.arquivoPath,
                dataEnvio: data.dataEnvio
            });
            
            // Reseta o formulário
            arquivoSelecionado = null;
            const inputArquivo = document.getElementById('inputArquivo');
            if (inputArquivo) inputArquivo.value = '';
            const btnSelecionar = document.getElementById('btnSelecionarArquivo');
            if (btnSelecionar) {
                btnSelecionar.classList.remove('arquivo-selecionado');
                btnSelecionar.innerHTML = '<i class="fas fa-redo"></i> Selecionar novo arquivo';
            }
            btnEnviar.textContent = 'Reenviar';
            btnEnviar.disabled = true;
        } else {
            const error = await response.json();
            alert(error.erro || 'Erro ao enviar atividade.');
            btnEnviar.textContent = 'Enviar';
            btnEnviar.disabled = false;
        }
    } catch (error) {
        console.error('Erro ao enviar atividade:', error);
        alert('Erro ao conectar com o servidor.');
        btnEnviar.textContent = 'Enviar';
        btnEnviar.disabled = false;
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        limparDadosUsuario();
        window.location.href = '/index.html';
    }
}
