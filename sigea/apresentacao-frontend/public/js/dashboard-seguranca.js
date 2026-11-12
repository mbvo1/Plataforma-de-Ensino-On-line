// Projeto 2, Fase 6: painel de seguranca do administrador.
//
// Regra desta tela: nenhum dado vindo do servidor passa por innerHTML.
// Texto entra por textContent e linhas de tabela por createElement. O
// frontend tem 167 usos de innerHTML com escape em so parte dos arquivos;
// esta tela nao aumenta a divida. Os graficos do Chart.js desenham em
// canvas, onde nao ha HTML para injetar.
//
// Os paineis "medidos" leem os CSVs de evidencia como estao (servidos pelo
// backend so para admin) e nao recalculam nada; os "ao vivo" leem o log.

(function () {
    'use strict';

    const FUSO = 'America/Recife';
    const CORES = {
        sigea: '#667eea',
        aes: '#e67e22',
        antiga1: '#9e9e9e',
        antiga2: '#bdbdbd',
        claro: '#27ae60',
        total: '#667eea',
        falhas: '#e74c3c',
        alertas: '#ff9800',
        precisao: '#667eea',
        recall: '#27ae60',
        f1: '#e67e22',
    };
    let anomalias = [];

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
        document.getElementById('verificar').addEventListener('click', verificarIntegridade);
        document.getElementById('filtro-modelo').addEventListener('change', desenharAnomalias);

        if (typeof Chart === 'undefined') {
            // CDN fora do ar ou SRI recusado: os paineis de tabela continuam funcionando.
            document.querySelectorAll('.seg-grafico').forEach(g => {
                g.textContent = 'Biblioteca de gráficos indisponível (Chart.js não carregou).';
            });
        } else {
            Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
            Chart.defaults.maintainAspectRatio = false;
        }

        executar(eventosPorHora, 'grafico-eventos');
        executar(verificarIntegridade, 'integridade');
        executar(carregarAnomalias, 'tabela-anomalias');
        executar(metricasDetector, 'grafico-metricas');
        executar(avalanche, 'grafico-avalanche');
        executar(distribuicaoBytes, 'grafico-bytes');
    });

    // ------------------------------------------------------------------ apoio

    async function executar(painel, idDoElemento) {
        try {
            await painel();
        } catch (erro) {
            console.error(erro);
            const alvo = document.getElementById(idDoElemento);
            const aviso = document.createElement('p');
            aviso.className = 'seg-nota';
            aviso.textContent = 'Falha ao carregar: ' + erro.message;
            alvo.closest('.seg-painel').appendChild(aviso);
        }
    }

    async function api(caminho) {
        const resposta = await fetch(caminho, {
            headers: { Authorization: 'Bearer ' + localStorage.getItem('token') },
        });
        if (resposta.status === 401 || resposta.status === 403) {
            window.location.href = '/login-admin.html';
            throw new Error('sessão expirada ou sem permissão');
        }
        if (!resposta.ok) {
            throw new Error('HTTP ' + resposta.status + ' em ' + caminho);
        }
        return resposta;
    }

    /** Os CSVs de evidencia sao gerados pelos testes: sem aspas, sem virgula dentro de campo. */
    function lerCsv(texto) {
        const linhas = texto.split('\n').map(l => l.replace(/\r$/, '')).filter(l => l.length > 0);
        const cabecalho = linhas[0].split(',');
        return linhas.slice(1).map(linha => {
            const campos = linha.split(',');
            const objeto = {};
            cabecalho.forEach((nome, i) => { objeto[nome] = campos[i]; });
            return objeto;
        });
    }

    async function evidencia(nome) {
        return lerCsv(await (await api('/api/admin/dashboard-seguranca/evidencias/' + nome)).text());
    }

    function horaLocal(isoUtc, opcoes) {
        const texto = isoUtc.endsWith('Z') ? isoUtc : isoUtc + 'Z';
        return new Date(texto).toLocaleString('pt-BR', Object.assign({ timeZone: FUSO }, opcoes));
    }

    function celula(linha, texto, numerica) {
        const td = document.createElement('td');
        td.textContent = texto;
        if (numerica) {
            td.className = 'seg-num';
        }
        linha.appendChild(td);
        return td;
    }

    function pct(valor) {
        return valor === undefined || valor === '' ? '—' : (Number(valor) * 100).toFixed(1) + '%';
    }

    function grafico(id, configuracao) {
        if (typeof Chart === 'undefined') {
            return null;
        }
        return new Chart(document.getElementById(id), configuracao);
    }

    function sair(evento) {
        evento.preventDefault();
        if (confirm('Deseja realmente sair?')) {
            ['usuarioId', 'usuarioNome', 'usuarioEmail', 'usuarioPerfil', 'token'].forEach(k => localStorage.removeItem(k));
            window.location.href = '/login-admin.html';
        }
    }

    // ------------------------------------------------------------------ 1. eventos por hora (ao vivo)

    async function eventosPorHora() {
        const pontos = await (await api('/api/admin/dashboard-seguranca/eventos-por-hora')).json();
        const nota = document.getElementById('nota-eventos');
        if (pontos.length === 0) {
            nota.textContent = 'Nenhum evento no log.';
            return;
        }
        const rotulos = pontos.map(p => horaLocal(p.hora, { day: '2-digit', month: '2-digit', hour: '2-digit' }));
        grafico('grafico-eventos', {
            type: 'line',
            data: {
                labels: rotulos,
                datasets: [
                    { label: 'Total', data: pontos.map(p => p.total), borderColor: CORES.total, pointRadius: 0, borderWidth: 1.5 },
                    { label: 'Falhas', data: pontos.map(p => p.falhas), borderColor: CORES.falhas, pointRadius: 0, borderWidth: 1.5 },
                    { label: 'Alertas', data: pontos.map(p => p.alertas), borderColor: CORES.alertas, pointRadius: 0, borderWidth: 1.5 },
                ],
            },
            options: {
                interaction: { mode: 'index', intersect: false },
                scales: { x: { ticks: { maxTicksLimit: 14 } }, y: { beginAtZero: true, title: { display: true, text: 'eventos' } } },
            },
        });
        const total = pontos.reduce((s, p) => s + p.total, 0);
        nota.textContent = total.toLocaleString('pt-BR') + ' eventos em ' + pontos.length +
            ' horas com atividade, de ' + horaLocal(pontos[0].hora) + ' a ' + horaLocal(pontos[pontos.length - 1].hora) +
            ' (horário de Recife). Horas sem nenhum evento não aparecem no eixo. ANOMALIA_DETECTADA fica fora: ' +
            'é gravada na hora em que o detector envia o lote, não na hora do comportamento (ver painel de anomalias).';
    }

    // ------------------------------------------------------------------ 5. cadeia de integridade (ao vivo)

    async function verificarIntegridade() {
        const botao = document.getElementById('verificar');
        const caixa = document.getElementById('integridade');
        const detalhes = document.getElementById('integridade-detalhes');
        botao.disabled = true;
        caixa.className = 'seg-integridade seg-carregando';
        caixa.textContent = 'Verificando...';
        try {
            const r = await (await api('/api/admin/auditoria/verificar-integridade')).json();
            detalhes.replaceChildren();
            const item = (rotulo, valor) => {
                const dt = document.createElement('dt');
                dt.textContent = rotulo;
                const dd = document.createElement('dd');
                dd.textContent = valor === null || valor === undefined ? '—' : String(valor);
                detalhes.append(dt, dd);
            };
            if (r.status === 'OK') {
                caixa.className = 'seg-integridade seg-ok';
                caixa.textContent = 'OK — cadeia íntegra';
                item('Registros verificados', Number(r.registrosVerificados).toLocaleString('pt-BR'));
                item('Último log_id', r.ultimoLogIdIntegro);
                item('Hash final (âncora)', r.hashFinal);
            } else {
                caixa.className = 'seg-integridade seg-corrompido';
                caixa.textContent = 'Corrompida no log_id ' + r.logIdCorrompido;
                item('Motivo', r.motivo);
                item('Último log_id íntegro', r.ultimoLogIdIntegro);
                item('Hash esperado', r.hashEsperado);
                item('Hash encontrado', r.hashEncontrado);
            }
        } finally {
            botao.disabled = false;
        }
    }

    // ------------------------------------------------------------------ 2. anomalias (ao vivo)

    async function carregarAnomalias() {
        const r = await (await api('/api/admin/dashboard-seguranca/anomalias?porModelo=50')).json();
        anomalias = r.maioresScores;
        const filtro = document.getElementById('filtro-modelo');
        filtro.replaceChildren();
        const todos = document.createElement('option');
        todos.value = '';
        todos.textContent = 'Todos';
        filtro.appendChild(todos);
        Object.keys(r.porModelo).forEach(modelo => {
            const opcao = document.createElement('option');
            opcao.value = modelo;
            opcao.textContent = modelo;
            filtro.appendChild(opcao);
        });
        const partes = Object.entries(r.porModelo).map(([m, n]) => m + ': ' + n.toLocaleString('pt-BR'));
        document.getElementById('anomalias-resumo').textContent = r.total === 0
            ? 'Nenhuma anomalia registrada. Rode analise-anomalias/detectar.py --enviar.'
            : r.total.toLocaleString('pt-BR') + ' registradas (' + partes.join(', ') +
              '). A tabela mostra as 50 de maior score de cada modelo; scores de modelos diferentes não são comparáveis.';
        desenharAnomalias();
    }

    function desenharAnomalias() {
        const modelo = document.getElementById('filtro-modelo').value;
        const corpo = document.querySelector('#tabela-anomalias tbody');
        corpo.replaceChildren();
        anomalias.filter(a => !modelo || a.modelo === modelo).forEach(a => {
            const tr = document.createElement('tr');
            celula(tr, a.score === null ? '—' : Number(a.score).toFixed(4), true);
            celula(tr, a.usuarioId === null ? '(sem usuário)' : String(a.usuarioId), true);
            celula(tr, a.janelaInicio && a.janelaFim
                ? horaLocal(a.janelaInicio, { dateStyle: 'short', timeStyle: 'short' }) + ' – ' +
                  horaLocal(a.janelaFim, { timeStyle: 'short' })
                : '—');
            celula(tr, a.modelo || '—');
            celula(tr, String(a.logId), true);
            corpo.appendChild(tr);
        });
    }

    // ------------------------------------------------------------------ 6. metricas do detector (medido)

    async function metricasDetector() {
        const linhas = await evidencia('deteccao.csv');
        const gerais = linhas.filter(l => l.escopo === 'todas' && l.unidade === 'janela');
        const incidentes = Object.fromEntries(linhas
            .filter(l => l.escopo === 'todos' && l.unidade === 'incidente')
            .map(l => [l.modelo, l.vp + ' de ' + l.total]));
        grafico('grafico-metricas', {
            type: 'bar',
            data: {
                labels: gerais.map(l => l.modelo),
                datasets: [
                    { label: 'Precisão', data: gerais.map(l => Number(l.precisao)), backgroundColor: CORES.precisao },
                    { label: 'Recall', data: gerais.map(l => Number(l.recall)), backgroundColor: CORES.recall },
                    { label: 'F1', data: gerais.map(l => Number(l.f1)), backgroundColor: CORES.f1 },
                ],
            },
            options: { scales: { y: { min: 0, max: 1 } } },
        });
        const corpo = document.querySelector('#tabela-metricas tbody');
        corpo.replaceChildren();
        gerais.forEach(l => {
            const tr = document.createElement('tr');
            celula(tr, l.modelo);
            celula(tr, pct(l.precisao), true);
            celula(tr, pct(l.recall), true);
            celula(tr, Number(l.f1).toFixed(3), true);
            celula(tr, Number(l.roc_auc).toFixed(3), true);
            celula(tr, incidentes[l.modelo] || '—', true);
            corpo.appendChild(tr);
        });
    }

    // ------------------------------------------------------------------ 3. avalanche por rodada (medido)

    async function avalanche() {
        const linhas = (await evidencia('avalanche.csv')).filter(l => l.tipo === 'texto');
        const rodadas = [...new Set(linhas.map(l => Number(l.rodadas)))].sort((a, b) => a - b);
        const serie = (cifra, rotulo, cor, oculta, tracejada) => ({
            label: rotulo,
            data: rodadas.map(r => {
                const l = linhas.find(x => x.cifra === cifra && Number(x.rodadas) === r);
                return l ? Number(l.media_pct) : null;
            }),
            borderColor: cor,
            backgroundColor: cor,
            hidden: oculta,
            borderDash: tracejada ? [6, 4] : [],
            spanGaps: true,
        });
        grafico('grafico-avalanche', {
            type: 'line',
            data: {
                labels: rodadas,
                datasets: [
                    serie('CIFRA_SIGEA_MDS', 'CifraSigea (atual)', CORES.sigea, false, false),
                    serie('AES_256', 'AES-256', CORES.aes, false, false),
                    serie('CIFRA_SIGEA_MDS_P_LIVRE', 'CifraSigea, P livre (Adendo 01)', CORES.antiga1, true, true),
                    serie('CIFRA_SIGEA_CADEIA_DUPLA_P_LIVRE', 'CifraSigea, cadeia dupla (Fase 1)', CORES.antiga2, true, true),
                    { label: 'Ideal (50%)', data: rodadas.map(() => 50), borderColor: '#999', borderDash: [2, 3], pointRadius: 0, borderWidth: 1 },
                ],
            },
            options: {
                scales: {
                    x: { title: { display: true, text: 'rodadas' } },
                    y: { min: 0, max: 60, title: { display: true, text: '% de bits alterados (média)' } },
                },
            },
        });
    }

    // ------------------------------------------------------------------ 4. distribuicao de bytes (medido)

    async function distribuicaoBytes() {
        const linhas = await evidencia('frequencia-bytes.csv');
        // Os tres textos tem tamanhos diferentes (nonce, padding, tag): compara em % do total de cada um.
        const colunas = [
            ['texto_claro', 'Texto claro', CORES.claro],
            ['cifra_sigea_mds_cbc', 'CifraSigea (CBC)', CORES.sigea],
            ['aes_256_gcm', 'AES-256-GCM', CORES.aes],
        ];
        const datasets = colunas.map(([coluna, rotulo, cor]) => {
            const soma = linhas.reduce((s, l) => s + Number(l[coluna]), 0);
            return { label: rotulo, data: linhas.map(l => 100 * Number(l[coluna]) / soma), backgroundColor: cor, borderWidth: 0 };
        });
        grafico('grafico-bytes', {
            type: 'bar',
            data: { labels: linhas.map(l => l.byte), datasets },
            options: {
                interaction: { mode: 'index', intersect: false },
                scales: {
                    x: { title: { display: true, text: 'valor do byte' }, ticks: { maxTicksLimit: 16 } },
                    y: { title: { display: true, text: '% dos bytes' } },
                },
            },
        });
    }
})();
