# V-09 — Dados pessoais expostos em log

## Ativo afetado
Dados pessoais de qualquer usuário recém-registrado (nome, e-mail, CPF)

## Ameaça
Qualquer pessoa com acesso ao console/log do servidor

## Vulnerabilidade
AutenticacaoController.registro() imprimia nome, e-mail e CPF em texto
claro via System.out.println a cada novo cadastro

## Impacto
Dado pessoal persistido sem controle de acesso, retenção ou expurgo -
viola o princípio de minimização da LGPD

## Probabilidade
Baixa - exige acesso ao console/log do servidor, que já não fica mais
exposto publicamente após a correção da V-04

## Risco
Médio

## Categoria (PDF da disciplina)
Exposição indevida de dados

## Justificativa da seleção
Complementa a análise de proteção de dados pessoais exigida pela LGPD -
mesmo não sendo a falha mais crítica isoladamente, mostra um padrão
recorrente de descuido com dado sensível em toda a aplicação

## Observação sobre o ciclo desta vulnerabilidade
Diferente das demais, esta não foi atacada isoladamente em runtime: ao
reescrever o AutenticacaoController.java durante a correção da V-02
(implementação de JWT), a linha de println foi removida como efeito
colateral da refatoração completa do arquivo. A evidência de "antes"
e "depois" foi reconstituida via `git diff` entre a tag v1-vulneravel
e a main atual, mostrando a remoção completa das cinco linhas do log
inseguro (arquivo diff-removido.txt).

## Antes da correção
- Codigo vulneravel: System.out.println com nome/email/cpf em texto
  claro, presente na v1-vulneravel
- Evidencia: diff-removido.txt

## Depois da correção
- Nenhuma chamada de log imprime dado pessoal na rota de registro
- A remoção ocorreu durante a reescrita do controller para a V-02, sem
  necessidade de intervenção adicional

## Achado complementar (fora do escopo original da V-09)
Durante a investigação desta vulnerabilidade, foram encontradas duas
novas ocorrências do padrão de senha insegura já corrigido na V-01,
em UsuariosAdminController.java:
- criarProfessor(): usava "HASH_senha123" como literal
- resetarSenhaProfessor(): gravava "senha123" sem nenhum hash, com
  comentário no próprio código reconhecendo o problema
Ambas corrigidas nesta mesma branch, usando Senha.criarNova() (Argon2id),
consistente com a correção já aplicada na V-01.

## Reteste do achado complementar
- Professor criado via POST /api/admin/professores
- senha_hash gravado: $argon2id$v=19$... (não mais HASH_senha123)
- Evidência: reteste-hash-argon2.png
- Resultado: Correção confirmada