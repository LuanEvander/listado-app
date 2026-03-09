# Análise técnica do projeto Listado

## 1. Leitura da documentação fornecida

### 1.1 Escopo identificado

Com base em `docs/relatório-final.pdf`, `docs/listado.asta` e nos diagramas exportados, o app foi concebido para:

- gerenciar catálogo reutilizável de itens;
- registrar a dimensão de cada item por unidade/embalagem;
- montar listas de compras com orçamento opcional;
- persistir tudo localmente;
- manter listas concluídas como histórico imutável;
- permitir análise temporal de preços por item;
- normalizar preços por unidade de medida com fator de conversão.

### 1.1.1 Mudança registrada em 2026-03-08

Após a primeira entrega, a regra de negócio do cadastro de itens foi refinada para incluir o atributo `dimensão`.

Nova interpretação do produto:

- o item cadastrado representa uma unidade comercial do produto;
- essa unidade possui um conteúdo próprio, expresso por `dimensão + unidade de medida`;
- a quantidade informada durante a compra representa quantas unidades/embalagens foram levadas;
- a análise de preço deve comparar o valor pago por unidade comercial e também o preço normalizado pelo conteúdo base.

Exemplo rastreado:

- `Refri`, `2`, `litros` → cada unidade do item contém 2 litros;
- ao comprar `3` unidades, o usuário levou 3 garrafas de 2 litros, e não 3 litros totais.

### 1.1.2 Mudança registrada em 2026-03-09

O catálogo deixou de aceitar categorias livres informadas pelo usuário.

Nova interpretação do produto:

- as categorias são limitadas e definidas pelo sistema;
- o cadastro de item passa a selecionar uma categoria pré-definida;
- a padronização evita duplicidades semânticas como `Bebida`, `Bebidas` e `Refrigerantes` para o mesmo grupo de produtos;
- a linha do tempo do produto deve registrar essa restrição como regra permanente do catálogo.

### 1.2 Regras de negócio materializadas

- Itens inativados não aparecem em novas listas, mas seguem íntegros no histórico.
- Categorias de item são pré-definidas pelo sistema e não podem ser gerenciadas pelo usuário.
- Itens do catálogo possuem dimensão e unidade de medida fixas por unidade comercial.
- A ocorrência de um item na lista é contextual e guarda “snapshot” próprio.
- A quantidade lançada na lista representa o número de unidades compradas.
- O preço base é calculado dividindo o preço de uma unidade comercial pelo conteúdo total dessa unidade.
- Uma lista concluída não pode mais ser alterada.
- A finalização exige ao menos um item marcado como comprado.
- Itens pendentes podem ser mantidos ou removidos no fechamento.
- A análise usa apenas itens efetivamente comprados.

### 1.3 Requisitos funcionais traduzidos para a implementação

| Requisito | Implementação |
| --- | --- |
| RF1-RF2 | Catálogo com cadastro, edição, inativação lógica e dimensão por unidade |
| RF3 | Unidades `g`, `kg`, `ml`, `l` e `un` com normalização a partir da dimensão do item |
| RF4-RF6 | Criação, edição e detalhamento de listas abertas |
| RF7-RF8 | Finalização com bloqueio de edição e histórico |
| RF9-RF10 | Tela de análises com série histórica e métricas |
| RF11 | Busca de itens no catálogo e na inclusão em listas |

### 1.3.1 Impacto da mudança nos casos de uso

- o cadastro do item passou a exigir o preenchimento da dimensão;
- a inclusão do item na lista preserva um snapshot de `dimensão + unidade de medida`;
- a edição do item na lista passa a alterar apenas `quantidade em unidades` e `preço por unidade`;
- a escolha de unidade durante a compra deixou de ser um ajuste contextual, pois agora faz parte da definição do item do catálogo.
- a categoria do item deixou de ser texto livre e passou a ser escolhida a partir de uma lista fixa do sistema.

## 2. Pesquisa aplicada em melhores práticas Android

As decisões seguiram principalmente a documentação oficial do Android Developers:

- arquitetura em camadas com separação de responsabilidades;
- UI guiada por estado e dados persistentes;
- `ViewModel` como state holder;
- persistência local com Room como fonte única de verdade;
- Compose declarativo, idempotente e sem side-effects na renderização;
- Material 3 com suporte a tema dinâmico.

## 3. Pesquisa aplicada em código limpo

As convenções de implementação foram alinhadas com as convenções oficiais de Kotlin:

- preferência por `val` e tipos imutáveis;
- nomes explícitos e sem classes “utilitárias” genéricas;
- funções pequenas e com responsabilidade única;
- organização por feature;
- arquivos com nomes semânticos;
- ausência de efeitos colaterais em composables;
- regras de formatação consistentes com Kotlin official style.

## 4. Arquitetura escolhida

### 4.1 Macrovisão

- `core`: modelos e formatação;
- `data`: entidades, DAOs, banco e repositórios;
- `feature`: UI e estado por caso de uso/tela;
- `ui`: tema e navegação.

### 4.2 Justificativa

Essa organização respeita o espírito de “package by feature” da documentação e mantém:

- baixo acoplamento;
- coesão por funcionalidade;
- facilidade de manutenção;
- evolução incremental do app.

## 5. Estratégias aplicadas aos requisitos não funcionais

- Persistência local com Room para robustez offline.
- Consultas e fluxos reativos para atualização imediata da UI.
- Normalização de preço pré-calculada na finalização para leitura simples em análise, considerando a dimensão por unidade.
- Interface Material 3 com feedback visual por `Snackbar`, chips e estados explícitos.
- `minSdk = 29` para compatibilidade com Android 10+.

## 6. Ambiente de desenvolvimento configurado

### 6.1 Ferramentas-alvo

- JDK 17
- Gradle Kotlin DSL
- Android SDK Platform 36
- Android Studio atual com suporte a Compose

### 6.2 Dependências centrais

- Compose BOM
- Material 3
- Navigation Compose
- Room
- Hilt

## 7. Política de commits

O repositório deve seguir Conventional Commits em PT-BR. Exemplos válidos:

- `feat: cria estrutura base do app android`
- `feat: implementa fluxo de listas e finalização`
- `docs: documenta arquitetura e ambiente`
- `fix: corrige atualização de itens na lista`

## 8. Rastreabilidade da mudança do produto

| Data | Tipo | Descrição | Impacto |
| --- | --- | --- | --- |
| 2026-03-08 | Regra de negócio | Inclusão do atributo `dimensão` no item do catálogo | Cadastro de item, snapshot da lista, cálculo de preço base e documentação |
| 2026-03-09 | Regra de negócio | Categorias passaram a ser pré-definidas pelo sistema | Cadastro de item, padronização do catálogo, busca e rastreabilidade do produto |
