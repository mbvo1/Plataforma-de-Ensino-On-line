// Projeto 2: tela de esteganografia do administrador.
//
// Mesma regra do painel de seguranca: nada vindo do servidor passa por
// innerHTML. Titulos de aviso e texto decifrado sao dados de usuario e
// entram por textContent; as imagens entram por src de Blob, nunca por HTML.

(function () {
    'use strict';

    let urlOriginal = null;
    let urlEstego = null;

    document.addEventListener('DOMContentLoaded', () => {
        if (!localStorage.getItem('token') || localStorage.getItem('usuarioPerfil') !== 'ADMINISTRADOR') {
            window.location.href = '/login-admin.html';
            return;
        }
        const nome = localStorage.getItem('usuarioNome');
        document.getElementById('user-name').textContent = 'Admin - ' + (nome || 'Usuário');
        document.getElementById('menuToggle').addEventListener('click', () => {
            document.getElementById('sidebar').classList.toggle('collapsed');
        });
        document.getElementById('sair').addEventListener('click', sair);
        document.querySelectorAll('input[name="origem"]').forEach(r => r.addEventListener('change', alternarOrigem));
        document.getElementById('form-embutir').addEventListener('submit', embutir);
        document.getElementById('form-extrair').addEventListener('submit', extrair);
        carregarAvisos();
    });

    // ------------------------------------------------------------------ apoio

    async function api(caminho, opcoes) {
        const resposta = await fetch(caminho, Object.assign({
            headers: { Authorization: 'Bearer ' + localStorage.getItem('token') },
        }, opcoes));
        if (resposta.status === 401 || resposta.status === 403) {
            window.location.href = '/login-admin.html';
            throw new Error('sessão expirada ou sem permissão');
        }
        return resposta;
    }

    // O backend devolve {erro: "..."} em 400, 404 e 422; o resto vira mensagem generica.
    async function lerJson(resposta) {
        let corpo = null;
        try {
            corpo = await resposta.json();
        } catch (e) {
            corpo = null;
        }
        if (!resposta.ok) {
            let mensagem = 'HTTP ' + resposta.status;
            if (corpo && corpo.erro) {
                mensagem = corpo.erro;
            } else if (resposta.status === 413) {
                mensagem = 'Imagem acima do limite de 5 MB.';
            }
            const erro = new Error(mensagem);
            erro.corpo = corpo;
            throw erro;
        }
        return corpo;
    }

    function mostrarErro(id, mensagem) {
        const p = document.getElementById(id);
        p.textContent = mensagem || '';
        p.hidden = !mensagem;
    }

    function texto(id, valor) {
        document.getElementById(id).textContent = valor;
    }

    function sair(evento) {
        evento.preventDefault();
        if (confirm('Deseja realmente sair?')) {
            ['usuarioId', 'usuarioNome', 'usuarioEmail', 'usuarioPerfil', 'token'].forEach(k => localStorage.removeItem(k));
            window.location.href = '/login-admin.html';
        }
    }

    function alternarOrigem() {
        const porAviso = document.querySelector('input[name="origem"]:checked').value === 'aviso';
        document.getElementById('aviso').disabled = !porAviso;
        document.getElementById('texto').disabled = porAviso;
    }

    // ------------------------------------------------------------------ avisos

    async function carregarAvisos() {
        const select = document.getElementById('aviso');
        try {
            const avisos = await lerJson(await api('/api/admin/esteganografia/avisos'));
            select.replaceChildren();
            if (avisos.length === 0) {
                const opcao = document.createElement('option');
                opcao.value = '';
                opcao.textContent = 'Nenhum aviso cadastrado: use um texto digitado';
                select.appendChild(opcao);
                document.querySelector('input[name="origem"][value="texto"]').checked = true;
                alternarOrigem();
                return;
            }
            avisos.forEach(a => {
                const opcao = document.createElement('option');
                opcao.value = a.id;
                opcao.textContent = '#' + a.id + ' · ' + a.titulo + (a.cifrado ? '' : ' (ainda em claro)');
                select.appendChild(opcao);
            });
        } catch (erro) {
            mostrarErro('erro-embutir', 'Falha ao listar avisos: ' + erro.message);
        }
    }

    // ------------------------------------------------------------------ esconder

    async function embutir(evento) {
        evento.preventDefault();
        mostrarErro('erro-embutir', null);
        const arquivo = document.getElementById('imagem-capa').files[0];
        if (!arquivo) {
            mostrarErro('erro-embutir', 'Escolha uma imagem PNG.');
            return;
        }
        const dados = new FormData();
        dados.append('imagem', arquivo);
        if (document.querySelector('input[name="origem"]:checked').value === 'aviso') {
            const id = document.getElementById('aviso').value;
            if (!id) {
                mostrarErro('erro-embutir', 'Escolha um aviso.');
                return;
            }
            dados.append('avisoId', id);
        } else {
            const mensagem = document.getElementById('texto').value;
            if (!mensagem.trim()) {
                mostrarErro('erro-embutir', 'Digite o texto a esconder.');
                return;
            }
            dados.append('texto', mensagem);
        }

        const botao = document.getElementById('botao-embutir');
        botao.disabled = true;
        try {
            const r = await lerJson(await api('/api/admin/esteganografia/embutir', { method: 'POST', body: dados }));
            exibirResultado(arquivo, r);
        } catch (erro) {
            mostrarErro('erro-embutir', erro.message);
        } finally {
            botao.disabled = false;
        }
    }

    function exibirResultado(arquivo, r) {
        const binario = atob(r.imagemPng);
        const bytes = new Uint8Array(binario.length);
        for (let i = 0; i < binario.length; i++) {
            bytes[i] = binario.charCodeAt(i);
        }
        if (urlOriginal) URL.revokeObjectURL(urlOriginal);
        if (urlEstego) URL.revokeObjectURL(urlEstego);
        urlOriginal = URL.createObjectURL(arquivo);
        urlEstego = URL.createObjectURL(new Blob([bytes], { type: 'image/png' }));

        document.getElementById('previa-original').src = urlOriginal;
        document.getElementById('previa-estego').src = urlEstego;
        document.getElementById('baixar').href = urlEstego;

        texto('m-psnr', r.psnrDb === null ? 'imagens idênticas' : r.psnrDb.toFixed(2) + ' dB');
        texto('m-capacidade', r.payloadBytes + ' de ' + r.capacidadeBytes + ' bytes ('
            + r.capacidadeUsadaPct.toFixed(2) + '%)');
        texto('m-chi-original', valorP(r.pValorOriginal));
        texto('m-chi-estego', valorP(r.pValorEstego));
        texto('m-cifrado', r.textoCifradoInicio);

        const painel = document.getElementById('resultado-embutir');
        painel.hidden = false;
        painel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function valorP(p) {
        if (p === null || p === undefined) {
            return 'não calculável nesse trecho';
        }
        return 'p = ' + (p < 0.0001 ? p.toExponential(2) : p.toFixed(4));
    }

    // ------------------------------------------------------------------ extrair

    async function extrair(evento) {
        evento.preventDefault();
        mostrarErro('erro-extrair', null);
        document.getElementById('resultado-extrair').hidden = true;
        document.getElementById('ext-claro').hidden = true;
        const arquivo = document.getElementById('imagem-estego').files[0];
        if (!arquivo) {
            mostrarErro('erro-extrair', 'Escolha uma imagem PNG.');
            return;
        }
        const dados = new FormData();
        dados.append('imagem', arquivo);

        const botao = document.getElementById('botao-extrair');
        botao.disabled = true;
        try {
            const r = await lerJson(await api('/api/admin/esteganografia/extrair', { method: 'POST', body: dados }));
            exibirExtraido(r);
            const claro = document.getElementById('ext-claro');
            claro.textContent = r.textoClaro;
            claro.hidden = false;
        } catch (erro) {
            // 422: os bits sairam da imagem, mas nao decifram. Mostra o que saiu.
            if (erro.corpo && erro.corpo.payloadBytes !== undefined) {
                exibirExtraido(erro.corpo);
            }
            mostrarErro('erro-extrair', erro.message);
        } finally {
            botao.disabled = false;
        }
    }

    function exibirExtraido(r) {
        texto('ext-bytes', String(r.payloadBytes));
        texto('ext-cifrado', r.textoCifradoInicio);
        document.getElementById('resultado-extrair').hidden = false;
    }
})();
