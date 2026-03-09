# Análise técnica do projeto Listado

## 1. Leitura da documentação fornecida

### 1.1 Escopo identificado

Com base em `docs/relatório-final.pdf`, `docs/listado.asta` e nos diagramas exportados, o app foi concebido para:

- gerenciar catálogo reutilizável de itens;
- montar listas de compras com orçamento opcional;
- persistir tudo localmente;
- manter listas concluídas como histórico imutável;
- permitir análise temporal de preços por item;
- normalizar preços por unidade de medida com fator de conversão.

### 1.2 Regras de negócio materializadas

- Itens inativados não aparecem em novas listas, mas seguem íntegros no histórico.
- A ocorrência de um item na lista é contextual e guarda “snapshot” próprio.
- Uma lista concluída não pode mais ser alterada.
- A finalização exige ao menos um item marcado como comprado.
- Itens pendentes podem ser mantidos ou removidos no fechamento.
- A análise usa apenas itens efetivamente comprados.

### 1.3 Requisitos funcionais traduzidos para a implementação

| Requisito | Implementação |
| --- | --- |
| RF1-RF2 | Catálogo com cadastro, edição e inativação lógica |
| RF3 | Unidades `g`, `kg`, `ml`, `l` e `un` com normalização |
| RF4-RF6 | Criação, edição e detalhamento de listas abertas |
| RF7-RF8 | Finalização com bloqueio de edição e histórico |
| RF9-RF10 | Tela de análises com série histórica e métricas |
| RF11 | Busca de itens no catálogo e na inclusão em listas |

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
- Normalização de preço pré-calculada na finalização para leitura simples em análise.
- Interface Material 3 com feedback visual por `Snackbar`, chips e estados explícitos.
- `minSdk = 29` para compatibilidade com Android 10+.

## 6. Ambiente de desenvolvimento configurado

### 6.1 Ferramentas-alvo

- JDK 17
- Gradle Kotlin DSL
- Android SDK Platform 34
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
