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

## Categoria (PDF da disciplina)
Exposição indevida de dados

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