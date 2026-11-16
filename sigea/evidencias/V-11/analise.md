# V-11 — Ausência de log de segurança

## Ativo afetado
Capacidade de detectar e investigar incidentes de segurança

## Ameaça
Qualquer comprometimento do sistema passaria despercebido, sem
rastro algum para investigação posterior

## Vulnerabilidade
Nenhum evento de segurança (login, falha de autenticação, criação de
conta privilegiada, reset de senha) era registrado. O único "log"
existente eram as queries brutas do Hibernate, sem significado de
auditoria.

## Impacto
Um ataque bem-sucedido através de qualquer uma das outras
vulnerabilidades (V-01 a V-13) seria indetectável e não
reconstituível - não haveria como saber quem fez o quê, quando, ou
provar que um registro não foi alterado depois

## Probabilidade
Alta - a ausência é total e afeta toda a aplicação igualmente

## Risco
Alto

## Categoria (PDF da disciplina)
Falhas de configuração / requisito obrigatório nº 12 (logs) e nº 10
(função hash para integridade)

## Justificativa da seleção
É a vulnerabilidade que sustenta a capacidade de resposta a incidente
de todas as outras - sem ela, nenhuma das correções anteriores pode
ser auditada ou comprovada depois do fato

## Antes da correção
- Código vulnerável: nenhuma tabela, entidade ou lógica de auditoria
  existia no projeto
- Evidência: antes.png - console do servidor mostrando apenas queries
  Hibernate, nenhum evento de segurança nomeado
- Impacto observado: três tentativas de login não deixam rastro
  identificável como evento de segurança

## Depois da correção
- Código corrigido: nova tabela LogSeguranca (migration V26), entidade
  e repositório JPA, LogSegurancaService com encadeamento SHA-256
  (cada registro guarda o hash do anterior), integrado nos pontos de
  login, registro de usuário, criação de professor e reset de senha
- Justificativa técnica: encadeamento por hash é o mesmo princípio de
  uma blockchain simplificada - qualquer alteração retroativa em um
  registro quebra a cadeia de hashes a partir daquele ponto, tornando
  a adulteração detectável mesmo com acesso direto ao banco
- Mecanismo de proteção utilizado: SHA-256 encadeado + endpoint de
  verificação de integridade (GET /api/admin/auditoria/verificar-integridade,
  restrito a ADMINISTRADOR)

## Reteste
- Cadeia de logs gerada corretamente, com hash_anterior de cada
  registro batendo com o hash_atual do registro anterior
  (depois-logs-gravados.png)
- Alteração manual do campo "acao" no registro id=6, direto no banco
  via console H2
- Endpoint de verificação detecta a adulteração e aponta precisamente
  o id 6 como o primeiro registro corrompido
  (depois-adulteracao-detectada.png)
- Resultado: Ausência de auditoria → Log implementado → Adulteração
  detectada com precisão (confirmado)